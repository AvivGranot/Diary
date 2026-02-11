const { onCall, HttpsError } = require("firebase-functions/v2/https");
const admin = require("firebase-admin");
const sgMail = require("@sendgrid/mail");

admin.initializeApp();
const db = admin.firestore();

// ─── Configuration ───────────────────────────────────────────────
// Update SUPPORT_EMAIL before deploying.
// Set SENDGRID_API_KEY via: firebase functions:secrets:set SENDGRID_API_KEY
const SUPPORT_EMAIL = process.env.SUPPORT_EMAIL || "support@proactivediary.com";
const SENDGRID_FROM = process.env.SENDGRID_FROM || "noreply@proactivediary.com";

function getSendGridKey() {
  return process.env.SENDGRID_API_KEY || "";
}

// ─── submitSupportRequest ────────────────────────────────────────
// Called from the Android app via Firebase Cloud Functions callable.
// Writes the request to Firestore and sends a notification email
// to the developer with [BUG REPORT] or [CUSTOMER SUPPORT] prefix
// so Gmail filters can auto-label.
exports.submitSupportRequest = onCall(async (request) => {
  const { category, email, subject, description, deviceInfo } = request.data;

  // Validate required fields
  if (!category || !email || !subject || !description) {
    throw new HttpsError("invalid-argument", "Missing required fields: category, email, subject, description");
  }

  if (!["bug_report", "customer_support"].includes(category)) {
    throw new HttpsError("invalid-argument", "category must be 'bug_report' or 'customer_support'");
  }

  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!emailRegex.test(email)) {
    throw new HttpsError("invalid-argument", "Invalid email format");
  }

  // Write to Firestore
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

  // Build notification email for developer
  const subjectPrefix =
    category === "bug_report" ? "[BUG REPORT]" : "[CUSTOMER SUPPORT]";
  const emailSubject = `${subjectPrefix} ${subject}`;

  const deviceInfoText = deviceInfo
    ? Object.entries(deviceInfo)
        .map(([k, v]) => `  ${k}: ${v}`)
        .join("\n")
    : "  Not provided";

  const emailBody = [
    `New ${category === "bug_report" ? "Bug Report" : "Customer Support Request"}`,
    "=".repeat(50),
    "",
    `From: ${email}`,
    `Subject: ${subject}`,
    `Request ID: ${docRef.id}`,
    "",
    "Description:",
    description,
    "",
    "Device Info:",
    deviceInfoText,
    "",
    "---",
    `Reply to this user at: ${email}`,
    `Firestore doc: support_requests/${docRef.id}`,
  ].join("\n");

  // Send via SendGrid if configured
  const apiKey = getSendGridKey();
  if (apiKey) {
    sgMail.setApiKey(apiKey);
    try {
      await sgMail.send({
        to: SUPPORT_EMAIL,
        from: SENDGRID_FROM, // Must be verified in SendGrid
        subject: emailSubject,
        text: emailBody,
        replyTo: email,
      });
    } catch (emailError) {
      // Don't throw — the Firestore write succeeded, which is the important part
      console.error("Email notification failed:", emailError.message);
    }
  } else {
    console.log("SendGrid not configured. Firestore write succeeded.");
    console.log("Subject:", emailSubject);
  }

  return { success: true, requestId: docRef.id };
});

// ─── sendSupportReply ────────────────────────────────────────────
// Called by the auto-bug-resolution pipeline (GitHub Action) or
// manually by the developer to reply to a support request.
// Updates the Firestore doc and emails the user.
exports.sendSupportReply = onCall(async (request) => {
  const { requestId, replyMessage, newStatus } = request.data;

  if (!requestId || !replyMessage || !newStatus) {
    throw new HttpsError(
      "invalid-argument",
      "Missing required fields: requestId, replyMessage, newStatus"
    );
  }

  const validStatuses = ["resolved", "needs_info", "needs_screenshot"];
  if (!validStatuses.includes(newStatus)) {
    throw new HttpsError(
      "invalid-argument",
      `newStatus must be one of: ${validStatuses.join(", ")}`
    );
  }

  // Get the original request
  const docRef = db.collection("support_requests").doc(requestId);
  const doc = await docRef.get();

  if (!doc.exists) {
    throw new HttpsError("not-found", `Request ${requestId} not found`);
  }

  const requestData = doc.data();

  // Update Firestore
  await docRef.update({
    status: newStatus,
    resolution: replyMessage,
    resolvedAt: admin.firestore.FieldValue.serverTimestamp(),
  });

  // Send reply email to user
  const statusLabels = {
    resolved: "Issue Resolved",
    needs_info: "More Information Needed",
    needs_screenshot: "Screenshot Requested",
  };

  const emailSubject = `Re: ${requestData.subject} — ${statusLabels[newStatus]}`;

  const closingLine =
    newStatus === "resolved"
      ? "This fix will be included in the next app update. Thank you for helping us improve Proactive Diary!"
      : "Please reply to this email with the requested information.";

  const emailBody = [
    "Hi there,",
    "",
    `Thank you for reaching out about "${requestData.subject}".`,
    "",
    replyMessage,
    "",
    closingLine,
    "",
    "Best,",
    "Proactive Diary Team",
  ].join("\n");

  const apiKey = getSendGridKey();
  if (apiKey) {
    sgMail.setApiKey(apiKey);
    try {
      await sgMail.send({
        to: requestData.email,
        from: SUPPORT_EMAIL,
        subject: emailSubject,
        text: emailBody,
      });
    } catch (emailError) {
      console.error("Reply email failed:", emailError.message);
      throw new HttpsError("internal", "Firestore updated but email failed to send");
    }
  } else {
    console.log("SendGrid not configured. Firestore updated successfully.");
    console.log("Would email:", requestData.email);
    console.log("Subject:", emailSubject);
  }

  return { success: true };
});
