const admin = require("firebase-admin");
const Anthropic = require("@anthropic-ai/sdk").default;
const fs = require("fs");
const path = require("path");

// ──────────────────────────────────────────────
// Initialize Firebase Admin
// ──────────────────────────────────────────────
const serviceAccount = JSON.parse(process.env.FIREBASE_SERVICE_ACCOUNT);
admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  projectId: process.env.FIREBASE_PROJECT_ID,
});
const db = admin.firestore();

// ──────────────────────────────────────────────
// Initialize Anthropic client
// ──────────────────────────────────────────────
const anthropic = new Anthropic({
  apiKey: process.env.ANTHROPIC_API_KEY,
});

// ──────────────────────────────────────────────
// Codebase context configuration
// ──────────────────────────────────────────────

// Root directory of the Android source tree relative to the repo root.
// Adjust this if the repo layout changes.
const ANDROID_ROOT = process.env.ANDROID_ROOT || "Desktop/Proactive Diary";

// Key files to include as codebase context for bug analysis.
// Paths are relative to ANDROID_ROOT.
const CONTEXT_FILES = [
  "app/src/main/AndroidManifest.xml",
  "app/build.gradle.kts",
  "app/src/main/java/com/proactivediary/MainActivity.kt",
  "app/src/main/java/com/proactivediary/navigation/Routes.kt",
  "app/src/main/java/com/proactivediary/navigation/NavGraph.kt",
  "app/src/main/java/com/proactivediary/navigation/MainScreen.kt",
  "app/src/main/java/com/proactivediary/ui/write/WriteScreen.kt",
  "app/src/main/java/com/proactivediary/ui/write/WriteViewModel.kt",
  "app/src/main/java/com/proactivediary/ui/journal/JournalScreen.kt",
  "app/src/main/java/com/proactivediary/ui/journal/JournalViewModel.kt",
  "app/src/main/java/com/proactivediary/ui/journal/EntryDetailScreen.kt",
  "app/src/main/java/com/proactivediary/ui/journal/EntryDetailViewModel.kt",
  "app/src/main/java/com/proactivediary/ui/goals/GoalsScreen.kt",
  "app/src/main/java/com/proactivediary/ui/goals/GoalsViewModel.kt",
  "app/src/main/java/com/proactivediary/ui/settings/SettingsScreen.kt",
  "app/src/main/java/com/proactivediary/ui/paywall/BillingService.kt",
  "app/src/main/java/com/proactivediary/ui/paywall/BillingViewModel.kt",
  "app/src/main/java/com/proactivediary/notifications/NotificationService.kt",
  "app/src/main/java/com/proactivediary/notifications/AlarmReceiver.kt",
  "app/src/main/java/com/proactivediary/data/db/AppDatabase.kt",
  "app/src/main/java/com/proactivediary/data/repository/EntryRepositoryImpl.kt",
  "app/src/main/java/com/proactivediary/data/db/dao/EntryDao.kt",
];

/**
 * Load the content of key codebase files to give Claude context
 * when analyzing bug reports.
 */
function loadCodebaseContext() {
  const repoRoot = path.resolve(__dirname, "..");
  const androidRoot = path.join(repoRoot, ANDROID_ROOT);
  let context = "";
  let found = 0;

  for (const filePath of CONTEXT_FILES) {
    const fullPath = path.join(androidRoot, filePath);
    try {
      const content = fs.readFileSync(fullPath, "utf8");
      context += `\n--- ${filePath} ---\n${content}\n`;
      found++;
    } catch {
      context += `\n--- ${filePath} ---\n[File not found]\n`;
    }
  }

  console.log(
    `  Loaded ${found}/${CONTEXT_FILES.length} codebase context files.`
  );
  return context;
}

/**
 * Send the bug report and codebase context to Claude for analysis.
 * Returns a structured JSON response with classification and user message.
 */
async function analyzeBug(bugReport, codebaseContext) {
  const deviceInfoLines = bugReport.deviceInfo
    ? Object.entries(bugReport.deviceInfo)
        .map(([k, v]) => `  - ${k}: ${v}`)
        .join("\n")
    : "  Not provided";

  const prompt = `You are a senior Android developer analyzing a bug report for the Proactive Diary app — a privacy-first journaling app built with Kotlin, Jetpack Compose, Material 3, Hilt, and Room.

## Bug Report
- **Subject:** ${bugReport.subject}
- **Description:** ${bugReport.description}
- **Device Info:**
${deviceInfoLines}

## Codebase Context
${codebaseContext}

## Your Task
Analyze this bug report and respond with a JSON object (no markdown fences, just raw JSON):

{
  "status": "resolved" | "needs_info" | "needs_screenshot",
  "confidence": "high" | "medium" | "low",
  "analysis": "Your internal analysis of the bug (for developer logs, not sent to user)",
  "user_message": "A friendly, professional message to the user explaining what you found and what happens next. If resolved, explain the fix. If needs_info, ask specific questions. If needs_screenshot, explain why you need it.",
  "likely_file": "The file most likely containing the bug (or null)",
  "suggested_fix": "Brief description of the code fix needed (or null)"
}

Rules:
- If the bug description is vague or could have multiple causes, respond with "needs_info" and ask specific questions.
- If the bug is visual/UI-related and you cannot determine the cause from code alone, respond with "needs_screenshot".
- If you can identify the likely cause from the code, respond with "resolved" and explain the fix.
- Be empathetic and professional in user_message — these are real users.
- Never expose internal code details, file paths, or technical jargon in user_message.
- Keep user_message under 200 words.`;

  const response = await anthropic.messages.create({
    model: "claude-sonnet-4-20250514",
    max_tokens: 1500,
    messages: [{ role: "user", content: prompt }],
  });

  const text = response.content[0].text;

  // Parse JSON response (handle potential markdown wrapping)
  const jsonMatch = text.match(/\{[\s\S]*\}/);
  if (!jsonMatch) {
    throw new Error("Failed to parse Claude response as JSON");
  }

  return JSON.parse(jsonMatch[0]);
}

/**
 * Query Firestore for new bug reports, analyze each one with Claude,
 * and update the documents with the resolution.
 */
async function processNewBugReports() {
  console.log("Querying for new bug reports...");

  const snapshot = await db
    .collection("support_requests")
    .where("status", "==", "new")
    .where("category", "==", "bug_report")
    .orderBy("createdAt", "asc")
    .limit(5) // Process max 5 per run to stay within the 15-min Actions timeout
    .get();

  if (snapshot.empty) {
    console.log("No new bug reports to process.");
    return;
  }

  console.log(
    `Found ${snapshot.size} new bug report(s). Loading codebase context...`
  );
  const codebaseContext = loadCodebaseContext();

  for (const doc of snapshot.docs) {
    const bugReport = doc.data();
    console.log(`\nProcessing: ${doc.id} - "${bugReport.subject}"`);

    try {
      const analysis = await analyzeBug(bugReport, codebaseContext);
      console.log(
        `  Status: ${analysis.status} (confidence: ${analysis.confidence})`
      );
      console.log(`  Analysis: ${analysis.analysis}`);

      // Update Firestore with the resolution
      await db
        .collection("support_requests")
        .doc(doc.id)
        .update({
          status: analysis.status,
          resolution: analysis.user_message,
          resolvedAt: admin.firestore.FieldValue.serverTimestamp(),
          _internal: {
            analysis: analysis.analysis,
            confidence: analysis.confidence,
            likely_file: analysis.likely_file,
            suggested_fix: analysis.suggested_fix,
            processed_by: "auto-bug-resolver",
            processed_at: new Date().toISOString(),
          },
        });

      console.log(
        `  Firestore updated. User message: "${analysis.user_message.substring(0, 80)}..."`
      );

      // The sendSupportReply Cloud Function handles emailing the user.
      // It can be triggered either via a Firestore onUpdate trigger or
      // by calling the callable function directly. For now we log; the
      // Cloud Function is already deployed and will pick up the status change.
      console.log(`  Reply will be sent via sendSupportReply for ${doc.id}`);
    } catch (err) {
      console.error(`  Error processing ${doc.id}:`, err.message);

      // On failure, mark as needs_info with a safe fallback message
      await db
        .collection("support_requests")
        .doc(doc.id)
        .update({
          status: "needs_info",
          resolution:
            "Thank you for your bug report. Our team is looking into this and may need additional information. We'll follow up with you shortly.",
          resolvedAt: admin.firestore.FieldValue.serverTimestamp(),
          _internal: {
            error: err.message,
            processed_by: "auto-bug-resolver",
            processed_at: new Date().toISOString(),
          },
        });
    }
  }

  console.log("\nDone processing bug reports.");
}

// ──────────────────────────────────────────────
// Entry point
// ──────────────────────────────────────────────
processNewBugReports()
  .then(() => process.exit(0))
  .catch((err) => {
    console.error("Fatal error:", err);
    process.exit(1);
  });
