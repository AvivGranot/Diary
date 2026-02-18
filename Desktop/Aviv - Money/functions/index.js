const { onCall, HttpsError } = require("firebase-functions/v2/https");
const { onSchedule } = require("firebase-functions/v2/scheduler");
const { onDocumentCreated } = require("firebase-functions/v2/firestore");
const { beforeUserCreated } = require("firebase-functions/v2/identity");
const admin = require("firebase-admin");
const sgMail = require("@sendgrid/mail");
const { GoogleGenerativeAI } = require("@google/generative-ai");

admin.initializeApp();
const db = admin.firestore();

// Configuration
const SUPPORT_EMAIL = "support@proactivediary.com";
const SENDGRID_API_KEY = process.env.SENDGRID_API_KEY || "";
const GEMINI_API_KEY = process.env.GEMINI_API_KEY || "";

// ──────────────────────────────────────────────
// ANALYTICS EVENT LOGGING
// ──────────────────────────────────────────────

async function logEvent(event, uid, props = {}) {
  try {
    await db.collection("events").add({
      event,
      uid: uid || "anonymous",
      ts: admin.firestore.FieldValue.serverTimestamp(),
      props,
    });
  } catch (e) {
    console.error("Event log failed:", e.message);
  }
}

// ──────────────────────────────────────────────
// SUPPORT FUNCTIONS (existing)
// ──────────────────────────────────────────────

exports.submitSupportRequest = onCall(async (request) => {
  const { category, email, subject, description, deviceInfo } = request.data;

  if (!category || !email || !subject || !description) {
    throw new HttpsError("invalid-argument", "Missing required fields");
  }

  if (!["bug_report", "customer_support"].includes(category)) {
    throw new HttpsError("invalid-argument", "Invalid category");
  }

  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!emailRegex.test(email)) {
    throw new HttpsError("invalid-argument", "Invalid email format");
  }

  const docRef = await db.collection("support_requests").add({
    category,
    email,
    subject,
    description,
    deviceInfo: deviceInfo || {},
    status: "new",
    createdAt: admin.firestore.FieldValue.serverTimestamp(),
    resolution: null,
    resolvedAt: null,
  });

  const subjectPrefix =
    category === "bug_report" ? "[BUG REPORT]" : "[CUSTOMER SUPPORT]";
  const emailSubject = `${subjectPrefix} ${subject}`;

  const deviceInfoText = deviceInfo
    ? Object.entries(deviceInfo)
        .map(([k, v]) => `${k}: ${v}`)
        .join("\n")
    : "Not provided";

  const emailBody = `
New ${category === "bug_report" ? "Bug Report" : "Customer Support"} Request
${"=".repeat(50)}

From: ${email}
Subject: ${subject}
Request ID: ${docRef.id}

Description:
${description}

Device Info:
${deviceInfoText}

---
Reply to this user at: ${email}
Firestore doc: support_requests/${docRef.id}
  `.trim();

  if (SENDGRID_API_KEY) {
    sgMail.setApiKey(SENDGRID_API_KEY);
    try {
      await sgMail.send({
        to: SUPPORT_EMAIL,
        from: "noreply@proactivediary.com",
        subject: emailSubject,
        text: emailBody,
        replyTo: email,
      });
    } catch (emailError) {
      console.error("Email send failed:", emailError);
    }
  } else {
    console.log("SendGrid not configured. Subject:", emailSubject);
  }

  return { success: true, requestId: docRef.id };
});

exports.sendSupportReply = onCall(async (request) => {
  const { requestId, replyMessage, newStatus } = request.data;

  if (!requestId || !replyMessage || !newStatus) {
    throw new HttpsError("invalid-argument", "Missing required fields");
  }

  if (!["resolved", "needs_info", "needs_screenshot"].includes(newStatus)) {
    throw new HttpsError("invalid-argument", "Invalid status");
  }

  const docRef = db.collection("support_requests").doc(requestId);
  const doc = await docRef.get();

  if (!doc.exists) {
    throw new HttpsError("not-found", "Request not found");
  }

  const requestData = doc.data();

  await docRef.update({
    status: newStatus,
    resolution: replyMessage,
    resolvedAt: admin.firestore.FieldValue.serverTimestamp(),
  });

  const statusLabels = {
    resolved: "Issue Resolved",
    needs_info: "More Information Needed",
    needs_screenshot: "Screenshot Requested",
  };

  const emailSubject = `Re: ${requestData.subject} — ${statusLabels[newStatus]}`;

  const emailBody = `
Hi there,

Thank you for reaching out about "${requestData.subject}".

${replyMessage}

${
  newStatus === "resolved"
    ? "This fix will be included in the next app update. Thank you for helping us improve Proactive Diary!"
    : "Please reply to this email with the requested information."
}

Best,
Proactive Diary Team
  `.trim();

  if (SENDGRID_API_KEY) {
    sgMail.setApiKey(SENDGRID_API_KEY);
    try {
      await sgMail.send({
        to: requestData.email,
        from: SUPPORT_EMAIL,
        subject: emailSubject,
        text: emailBody,
      });
    } catch (emailError) {
      console.error("Reply email failed:", emailError);
      throw new HttpsError("internal", "Failed to send reply email");
    }
  } else {
    console.log("SendGrid not configured. Reply to:", requestData.email);
  }

  return { success: true };
});

// ──────────────────────────────────────────────
// CONTENT MODERATION
// ──────────────────────────────────────────────

// Client-side blocklist also applied; this is the server-side check
const BLOCKED_WORDS = [
  // Slurs and hate speech patterns (kept minimal, Gemini handles nuance)
  "fuck", "shit", "bitch", "asshole", "bastard", "damn", "crap",
  "hate you", "kill you", "die", "kys", "ugly", "stupid", "loser",
  "worthless", "pathetic", "disgusting", "trash",
];

function containsBlockedWords(text) {
  const lower = text.toLowerCase();
  return BLOCKED_WORDS.some((word) => lower.includes(word));
}

async function isContentPositive(text) {
  // Fast path: keyword blocklist
  if (containsBlockedWords(text)) {
    return { positive: false, reason: "Contains inappropriate language" };
  }

  // Gemini check for nuanced negativity
  if (GEMINI_API_KEY) {
    try {
      const genAI = new GoogleGenerativeAI(GEMINI_API_KEY);
      const model = genAI.getGenerativeModel({ model: "gemini-2.0-flash" });

      const prompt = `You are a content moderator for a positivity app. Analyze this message and determine if it is positive, kind, supportive, or neutral. Messages that are negative, hurtful, passive-aggressive, sarcastic in a mean way, or inappropriate should be rejected.

Message: "${text}"

Respond with exactly one word: POSITIVE or NEGATIVE`;

      const result = await model.generateContent(prompt);
      const response = result.response.text().trim().toUpperCase();

      if (response.includes("NEGATIVE")) {
        return { positive: false, reason: "Message doesn't meet our positivity guidelines" };
      }
      return { positive: true, reason: null };
    } catch (err) {
      console.error("Gemini moderation failed, falling back to keyword check:", err.message);
      // Fallback: if Gemini is unavailable and keyword check passed, allow it
      return { positive: true, reason: null };
    }
  }

  // No Gemini configured: rely on keyword check only
  return { positive: true, reason: null };
}

// ──────────────────────────────────────────────
// USER PROFILE — created on first sign-in
// ──────────────────────────────────────────────

exports.createUserProfile = onCall(async (request) => {
  if (!request.auth) {
    throw new HttpsError("unauthenticated", "Must be signed in");
  }

  const uid = request.auth.uid;
  const { displayName, phoneHash, emailHash, photoUrl } = request.data;

  const userRef = db.collection("users").doc(uid);
  const existing = await userRef.get();

  if (existing.exists) {
    // Update FCM token + photo if profile already exists
    const updates = {};
    if (request.data.fcmToken) updates.fcmToken = request.data.fcmToken;
    if (displayName) updates.displayName = displayName;
    if (photoUrl) updates.photoUrl = photoUrl;
    if (Object.keys(updates).length > 0) await userRef.update(updates);
    await logEvent("user_returned", uid, { hadToken: !!existing.data().fcmToken });
    return { success: true, created: false };
  }

  await userRef.set({
    displayName: displayName || "Anonymous",
    phoneHash: phoneHash || null,
    emailHash: emailHash || null,
    photoUrl: photoUrl || null,
    fcmToken: request.data.fcmToken || null,
    noteCount: 0,
    quoteCount: 0,
    createdAt: admin.firestore.FieldValue.serverTimestamp(),
  });

  // Increment global user counter for social proof
  const counterRef = db.collection("counters").doc("global");
  await counterRef.set(
    { userCount: admin.firestore.FieldValue.increment(1) },
    { merge: true }
  );

  await logEvent("user_created", uid, {
    method: phoneHash ? "phone" : emailHash ? "email" : "anonymous",
    hasContacts: !!(phoneHash || emailHash),
  });

  return { success: true, created: true };
});

// ──────────────────────────────────────────────
// CONTACT MATCHING
// ──────────────────────────────────────────────

exports.resolveContacts = onCall(async (request) => {
  if (!request.auth) {
    throw new HttpsError("unauthenticated", "Must be signed in");
  }

  const { phoneHashes, emailHashes } = request.data;

  if (!phoneHashes && !emailHashes) {
    throw new HttpsError("invalid-argument", "Provide phoneHashes or emailHashes");
  }

  const matches = [];

  // Match by phone hash
  if (phoneHashes && phoneHashes.length > 0) {
    // Firestore 'in' queries support up to 30 values
    const chunks = chunkArray(phoneHashes, 30);
    for (const chunk of chunks) {
      const snap = await db.collection("users")
        .where("phoneHash", "in", chunk)
        .get();
      snap.forEach((doc) => {
        if (doc.id !== request.auth.uid) {
          matches.push({ userId: doc.id, displayName: doc.data().displayName });
        }
      });
    }
  }

  // Match by email hash
  if (emailHashes && emailHashes.length > 0) {
    const chunks = chunkArray(emailHashes, 30);
    for (const chunk of chunks) {
      const snap = await db.collection("users")
        .where("emailHash", "in", chunk)
        .get();
      snap.forEach((doc) => {
        if (doc.id !== request.auth.uid) {
          const already = matches.find((m) => m.userId === doc.id);
          if (!already) {
            matches.push({ userId: doc.id, displayName: doc.data().displayName });
          }
        }
      });
    }
  }

  await logEvent("contacts_resolved", request.auth.uid, {
    phoneCount: phoneHashes ? phoneHashes.length : 0,
    emailCount: emailHashes ? emailHashes.length : 0,
    matchCount: matches.length,
  });

  return { matches };
});

function chunkArray(arr, size) {
  const chunks = [];
  for (let i = 0; i < arr.length; i += size) {
    chunks.push(arr.slice(i, i + size));
  }
  return chunks;
}

// ──────────────────────────────────────────────
// RATE LIMITING
// ──────────────────────────────────────────────

async function checkRateLimit(uid, collection, field, maxPerHour) {
  const hourAgo = new Date(Date.now() - 60 * 60 * 1000);
  const snap = await db.collection(collection)
    .where(field, "==", uid)
    .where("createdAt", ">=", admin.firestore.Timestamp.fromDate(hourAgo))
    .count()
    .get();
  return snap.data().count < maxPerHour;
}

// ──────────────────────────────────────────────
// ANONYMOUS NOTES
// ──────────────────────────────────────────────

exports.sendNote = onCall(async (request) => {
  if (!request.auth) {
    throw new HttpsError("unauthenticated", "Must be signed in");
  }

  // Rate limit: 10 notes per hour per sender
  const withinLimit = await checkRateLimit(request.auth.uid, "notes", "senderId", 10);
  if (!withinLimit) {
    throw new HttpsError("resource-exhausted", "Slow down! Max 10 notes per hour.");
  }

  const { recipientId, content } = request.data;

  if (!recipientId || !content) {
    throw new HttpsError("invalid-argument", "Missing recipientId or content");
  }

  // Validate word count (max 150)
  const wordCount = content.trim().split(/\s+/).length;
  if (wordCount > 150) {
    throw new HttpsError("invalid-argument", "Note must be 150 words or less");
  }

  if (content.trim().length === 0) {
    throw new HttpsError("invalid-argument", "Note cannot be empty");
  }

  // Check recipient exists
  const recipientDoc = await db.collection("users").doc(recipientId).get();
  if (!recipientDoc.exists) {
    throw new HttpsError("not-found", "Recipient not found");
  }

  // Content moderation
  const modResult = await isContentPositive(content);
  if (!modResult.positive) {
    await logEvent("content_rejected", request.auth.uid, { type: "note", reason: modResult.reason });
    throw new HttpsError("permission-denied", modResult.reason);
  }

  // Create note
  const noteRef = await db.collection("notes").add({
    senderId: request.auth.uid,
    recipientId,
    content: content.trim(),
    status: "delivered",
    createdAt: admin.firestore.FieldValue.serverTimestamp(),
    readAt: null,
  });

  // Increment sender's note count
  await db.collection("users").doc(request.auth.uid).update({
    noteCount: admin.firestore.FieldValue.increment(1),
  });

  await logEvent("note_sent", request.auth.uid, { recipientId, wordCount });

  // Send FCM push notification to recipient
  const recipientData = recipientDoc.data();
  if (recipientData.fcmToken) {
    try {
      await admin.messaging().send({
        token: recipientData.fcmToken,
        notification: {
          title: "\uD83D\uDC8C Someone thinks you\u2019re amazing",
          body: "Tap to open your anonymous note",
        },
        data: {
          type: "note",
          noteId: noteRef.id,
          destination: "note_inbox",
        },
        android: {
          priority: "high",
          notification: {
            channelId: "anonymous_notes",
            icon: "ic_notification",
          },
        },
      });
      await logEvent("note_push_sent", request.auth.uid, { recipientId, noteId: noteRef.id });
    } catch (fcmErr) {
      console.error("FCM send failed:", fcmErr.message);
      // Note was saved successfully, FCM failure is non-fatal
    }
  }

  return { success: true, noteId: noteRef.id };
});

// ──────────────────────────────────────────────
// QUOTES LEADERBOARD
// ──────────────────────────────────────────────

exports.submitQuote = onCall(async (request) => {
  if (!request.auth) {
    throw new HttpsError("unauthenticated", "Must be signed in");
  }

  // Rate limit: 5 quotes per hour per author
  const withinLimit = await checkRateLimit(request.auth.uid, "quotes", "authorId", 5);
  if (!withinLimit) {
    throw new HttpsError("resource-exhausted", "Slow down! Max 5 quotes per hour.");
  }

  const { content } = request.data;

  if (!content || content.trim().length === 0) {
    throw new HttpsError("invalid-argument", "Quote cannot be empty");
  }

  // Validate word count (max 25)
  const wordCount = content.trim().split(/\s+/).length;
  if (wordCount > 25) {
    throw new HttpsError("invalid-argument", "Quote must be 25 words or less");
  }

  // Content moderation
  const modResult = await isContentPositive(content);
  if (!modResult.positive) {
    await logEvent("content_rejected", request.auth.uid, { type: "quote", reason: modResult.reason });
    throw new HttpsError("permission-denied", modResult.reason);
  }

  // Get author display name and photo
  const userDoc = await db.collection("users").doc(request.auth.uid).get();
  const userData = userDoc.exists ? userDoc.data() : {};
  const authorName = userData.displayName || "Anonymous";
  const authorPhotoUrl = userData.photoUrl || null;

  const quoteRef = await db.collection("quotes").add({
    authorId: request.auth.uid,
    authorName,
    authorPhotoUrl,
    content: content.trim(),
    likeCount: 0,
    commentCount: 0,
    createdAt: admin.firestore.FieldValue.serverTimestamp(),
    reported: false,
  });

  // Increment user's quote count
  await db.collection("users").doc(request.auth.uid).update({
    quoteCount: admin.firestore.FieldValue.increment(1),
  });

  // Increment global quote counter
  await db.collection("counters").doc("global").set(
    { quoteCount: admin.firestore.FieldValue.increment(1) },
    { merge: true }
  );

  await logEvent("quote_submitted", request.auth.uid, { wordCount, quoteId: quoteRef.id });

  return { success: true, quoteId: quoteRef.id };
});

exports.toggleLike = onCall(async (request) => {
  if (!request.auth) {
    throw new HttpsError("unauthenticated", "Must be signed in");
  }

  const { quoteId } = request.data;

  if (!quoteId) {
    throw new HttpsError("invalid-argument", "Missing quoteId");
  }

  const quoteRef = db.collection("quotes").doc(quoteId);
  const quoteDoc = await quoteRef.get();

  if (!quoteDoc.exists) {
    throw new HttpsError("not-found", "Quote not found");
  }

  const likeRef = quoteRef.collection("likes").doc(request.auth.uid);
  const likeDoc = await likeRef.get();

  if (likeDoc.exists) {
    // Unlike
    await likeRef.delete();
    await quoteRef.update({
      likeCount: admin.firestore.FieldValue.increment(-1),
    });
    await logEvent("quote_unliked", request.auth.uid, { quoteId });
    return { success: true, liked: false };
  } else {
    // Like
    await likeRef.set({
      createdAt: admin.firestore.FieldValue.serverTimestamp(),
    });
    await quoteRef.update({
      likeCount: admin.firestore.FieldValue.increment(1),
    });
    await logEvent("quote_liked", request.auth.uid, { quoteId, authorId: quoteDoc.data().authorId });
    return { success: true, liked: true };
  }
});

exports.addComment = onCall(async (request) => {
  if (!request.auth) {
    throw new HttpsError("unauthenticated", "Must be signed in");
  }

  const { quoteId, content } = request.data;

  if (!quoteId || !content || content.trim().length === 0) {
    throw new HttpsError("invalid-argument", "Missing quoteId or content");
  }

  // Max 100 characters for comments
  if (content.trim().length > 100) {
    throw new HttpsError("invalid-argument", "Comment must be 100 characters or less");
  }

  const quoteRef = db.collection("quotes").doc(quoteId);
  const quoteDoc = await quoteRef.get();

  if (!quoteDoc.exists) {
    throw new HttpsError("not-found", "Quote not found");
  }

  // Content moderation for comments too
  const modResult = await isContentPositive(content);
  if (!modResult.positive) {
    await logEvent("content_rejected", request.auth.uid, { type: "comment", reason: modResult.reason });
    throw new HttpsError("permission-denied", modResult.reason);
  }

  // Get author name
  const userDoc = await db.collection("users").doc(request.auth.uid).get();
  const authorName = userDoc.exists ? userDoc.data().displayName : "Anonymous";

  await quoteRef.collection("comments").add({
    authorId: request.auth.uid,
    authorName,
    content: content.trim(),
    createdAt: admin.firestore.FieldValue.serverTimestamp(),
  });

  // Increment comment count
  await quoteRef.update({
    commentCount: admin.firestore.FieldValue.increment(1),
  });

  await logEvent("comment_added", request.auth.uid, { quoteId, charCount: content.trim().length });

  return { success: true };
});

exports.getLeaderboard = onCall(async (request) => {
  if (!request.auth) {
    throw new HttpsError("unauthenticated", "Must be signed in");
  }

  const { period, limit: queryLimit } = request.data;
  const resultLimit = Math.min(queryLimit || 20, 50);

  let query = db.collection("quotes").where("reported", "==", false);

  if (period === "weekly") {
    const weekAgo = new Date();
    weekAgo.setDate(weekAgo.getDate() - 7);
    query = query.where("createdAt", ">=", admin.firestore.Timestamp.fromDate(weekAgo));
  } else if (period === "daily") {
    const dayAgo = new Date();
    dayAgo.setDate(dayAgo.getDate() - 1);
    query = query.where("createdAt", ">=", admin.firestore.Timestamp.fromDate(dayAgo));
  }
  // "all_time" = no time filter

  const snapshot = await query
    .orderBy("likeCount", "desc")
    .orderBy("createdAt", "desc")
    .limit(resultLimit)
    .get();

  const quotes = [];
  snapshot.forEach((doc) => {
    const data = doc.data();
    quotes.push({
      id: doc.id,
      authorId: data.authorId,
      authorName: data.authorName,
      authorPhotoUrl: data.authorPhotoUrl || null,
      content: data.content,
      likeCount: data.likeCount,
      commentCount: data.commentCount,
      createdAt: data.createdAt ? data.createdAt.toMillis() : 0,
    });
  });

  await logEvent("leaderboard_viewed", request.auth.uid, { period: period || "all_time", resultCount: quotes.length });

  return { quotes };
});

// ──────────────────────────────────────────────
// SOCIAL PROOF COUNTERS
// ──────────────────────────────────────────────

exports.getCounters = onCall(async (request) => {
  const counterDoc = await db.collection("counters").doc("global").get();
  if (!counterDoc.exists) {
    return { userCount: 0, quoteCount: 0 };
  }
  const data = counterDoc.data();
  return {
    userCount: data.userCount || 0,
    quoteCount: data.quoteCount || 0,
  };
});

// ──────────────────────────────────────────────
// CLIENT EVENT TRACKING
// ──────────────────────────────────────────────

exports.trackClientEvent = onCall(async (request) => {
  if (!request.auth) {
    throw new HttpsError("unauthenticated", "Must be signed in");
  }
  const { event, props } = request.data;
  if (!event || typeof event !== "string") {
    throw new HttpsError("invalid-argument", "Missing event name");
  }
  await logEvent(event, request.auth.uid, props || {});
  return { success: true };
});

exports.trackBatchEvents = onCall(async (request) => {
  if (!request.auth) {
    throw new HttpsError("unauthenticated", "Must be signed in");
  }
  const { events } = request.data;
  if (!events || !Array.isArray(events)) {
    throw new HttpsError("invalid-argument", "Missing events array");
  }
  const batch = db.batch();
  // Cap at 25 events per batch to stay within Firestore limits
  events.slice(0, 25).forEach((e) => {
    if (e.event && typeof e.event === "string") {
      const ref = db.collection("events").doc();
      batch.set(ref, {
        event: e.event,
        uid: request.auth.uid,
        ts: admin.firestore.FieldValue.serverTimestamp(),
        props: e.props || {},
      });
    }
  });
  await batch.commit();
  return { success: true, count: Math.min(events.length, 25) };
});

// ──────────────────────────────────────────────
// GROWTH METRICS (admin dashboard)
// ──────────────────────────────────────────────

exports.getGrowthMetrics = onCall(async (request) => {
  if (!request.auth) {
    throw new HttpsError("unauthenticated", "Must be signed in");
  }

  const now = new Date();
  const dayAgo = new Date(now - 24 * 60 * 60 * 1000);
  const weekAgo = new Date(now - 7 * 24 * 60 * 60 * 1000);
  const monthAgo = new Date(now - 30 * 24 * 60 * 60 * 1000);

  // Helper: count distinct UIDs in time range
  async function countActiveUsers(since) {
    const snap = await db.collection("events")
      .where("ts", ">=", admin.firestore.Timestamp.fromDate(since))
      .select("uid")
      .get();
    const uids = new Set();
    snap.forEach((doc) => uids.add(doc.data().uid));
    return uids.size;
  }

  // Helper: count events by name in time range
  async function countEvents(eventName, since) {
    const snap = await db.collection("events")
      .where("event", "==", eventName)
      .where("ts", ">=", admin.firestore.Timestamp.fromDate(since))
      .count()
      .get();
    return snap.data().count;
  }

  const [dau, wau, mau] = await Promise.all([
    countActiveUsers(dayAgo),
    countActiveUsers(weekAgo),
    countActiveUsers(monthAgo),
  ]);

  // Onboarding funnel (last 30 days)
  const [
    onboardingStarts,
    authCompletes,
    firstNoteCompletes,
    onboardingCompletes,
    notifGranted,
    notifDenied,
  ] = await Promise.all([
    countEvents("onboarding_start", monthAgo),
    countEvents("onboarding_auth_complete", monthAgo),
    countEvents("onboarding_first_note_complete", monthAgo),
    countEvents("onboarding_complete", monthAgo),
    countEvents("onboarding_notif_granted", monthAgo),
    countEvents("onboarding_notif_denied", monthAgo),
  ]);

  // Viral metrics (last 30 days)
  const [notesSent, usersCreated, quotesSub, quoteLikes, comments] = await Promise.all([
    countEvents("note_sent", monthAgo),
    countEvents("user_created", monthAgo),
    countEvents("quote_submitted", monthAgo),
    countEvents("quote_liked", monthAgo),
    countEvents("comment_added", monthAgo),
  ]);

  // Paywall (last 30 days)
  const [paywallShown, subStarted] = await Promise.all([
    countEvents("paywall_shown", monthAgo),
    countEvents("subscription_started", monthAgo),
  ]);

  return {
    engagement: {
      dau,
      wau,
      mau,
      stickiness: mau > 0 ? Math.round((dau / mau) * 100) : 0,
    },
    onboardingFunnel: {
      starts: onboardingStarts,
      authCompletes,
      firstNoteCompletes,
      completes: onboardingCompletes,
      completionRate: onboardingStarts > 0
        ? Math.round((onboardingCompletes / onboardingStarts) * 100)
        : 0,
      notifOptInRate: (notifGranted + notifDenied) > 0
        ? Math.round((notifGranted / (notifGranted + notifDenied)) * 100)
        : 0,
    },
    social: {
      notesSent,
      usersCreated,
      quotesSubmitted: quotesSub,
      quoteLikes,
      comments,
      viralRatio: usersCreated > 0
        ? Math.round((notesSent / usersCreated) * 100) / 100
        : 0,
    },
    monetization: {
      paywallShown,
      subscriptionsStarted: subStarted,
      conversionRate: paywallShown > 0
        ? Math.round((subStarted / paywallShown) * 100)
        : 0,
    },
  };
});

// ──────────────────────────────────────────────
// CLOUD SYNC
// ──────────────────────────────────────────────

/**
 * Returns the count of cloud entries/goals for the authenticated user.
 * Used by the client to decide whether to restore or push.
 */
exports.getSyncStatus = onCall(async (request) => {
  if (!request.auth) {
    throw new HttpsError("unauthenticated", "Must be signed in");
  }

  const uid = request.auth.uid;
  const userRef = db.collection("users").doc(uid);

  const [entrySnap, goalSnap] = await Promise.all([
    userRef.collection("entries").where("_deleted", "!=", true).count().get(),
    userRef.collection("goals").where("_deleted", "!=", true).count().get(),
  ]);

  return {
    hasCloudData: entrySnap.data().count > 0 || goalSnap.data().count > 0,
    entryCount: entrySnap.data().count,
    goalCount: goalSnap.data().count,
  };
});

/**
 * Scheduled cleanup: hard-deletes tombstoned sync documents older than 30 days.
 * Runs once daily to keep Firestore clean.
 */
exports.cleanupDeletedSyncData = onSchedule("every 24 hours", async () => {
  const thirtyDaysAgo = new Date(Date.now() - 30 * 24 * 60 * 60 * 1000);
  const thirtyDaysAgoMs = thirtyDaysAgo.getTime();
  const collections = ["entries", "goals", "goal_checkins", "reminders"];

  const usersSnap = await db.collection("users").listDocuments();

  for (const userDoc of usersSnap) {
    for (const collName of collections) {
      try {
        const snap = await userDoc.collection(collName)
          .where("_deleted", "==", true)
          .where("updatedAt", "<", thirtyDaysAgoMs)
          .limit(100)
          .get();

        if (snap.size > 0) {
          const batch = db.batch();
          snap.forEach((doc) => batch.delete(doc.ref));
          await batch.commit();
          console.log(`Cleaned ${snap.size} deleted docs from ${userDoc.id}/${collName}`);
        }
      } catch (e) {
        console.error(`Cleanup failed for ${userDoc.id}/${collName}: ${e.message}`);
      }
    }
  }
});
