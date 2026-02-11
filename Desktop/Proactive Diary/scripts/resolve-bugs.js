/**
 * Auto Bug Resolution Pipeline
 *
 * Polls Firestore for new bug reports, sends them to Claude for analysis,
 * updates Firestore with the resolution, and logs results.
 *
 * The sendSupportReply Cloud Function handles emailing the user
 * when the Firestore doc status changes.
 *
 * Required env vars:
 *   FIREBASE_SERVICE_ACCOUNT  — JSON string of the service account key
 *   ANTHROPIC_API_KEY         — Anthropic API key for Claude
 *   FIREBASE_PROJECT_ID       — Firebase project ID (proactive-diary)
 */

const admin = require("firebase-admin");
const Anthropic = require("@anthropic-ai/sdk");
const fs = require("fs");
const path = require("path");

// ─── Initialize Firebase Admin ───────────────────────────────────
const serviceAccount = JSON.parse(process.env.FIREBASE_SERVICE_ACCOUNT);
admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  projectId: process.env.FIREBASE_PROJECT_ID,
});
const db = admin.firestore();

// ─── Initialize Anthropic ────────────────────────────────────────
const anthropic = new Anthropic({ apiKey: process.env.ANTHROPIC_API_KEY });

// ─── Codebase context files ──────────────────────────────────────
// Key files that give Claude enough context to analyze most bugs.
// These are read from the checked-out repo in the GitHub Action.
const CONTEXT_FILES = [
  "app/src/main/AndroidManifest.xml",
  "app/build.gradle.kts",
  "app/src/main/java/com/proactivediary/MainActivity.kt",
  "app/src/main/java/com/proactivediary/ProactiveDiaryApp.kt",
  "app/src/main/java/com/proactivediary/navigation/Routes.kt",
  "app/src/main/java/com/proactivediary/navigation/NavGraph.kt",
  "app/src/main/java/com/proactivediary/navigation/MainScreen.kt",
  "app/src/main/java/com/proactivediary/ui/write/WriteScreen.kt",
  "app/src/main/java/com/proactivediary/ui/write/WriteViewModel.kt",
  "app/src/main/java/com/proactivediary/ui/journal/JournalScreen.kt",
  "app/src/main/java/com/proactivediary/ui/journal/JournalViewModel.kt",
  "app/src/main/java/com/proactivediary/ui/goals/GoalsScreen.kt",
  "app/src/main/java/com/proactivediary/ui/goals/GoalsViewModel.kt",
  "app/src/main/java/com/proactivediary/ui/settings/SettingsScreen.kt",
  "app/src/main/java/com/proactivediary/ui/paywall/BillingService.kt",
  "app/src/main/java/com/proactivediary/ui/paywall/BillingViewModel.kt",
  "app/src/main/java/com/proactivediary/notifications/NotificationService.kt",
  "app/src/main/java/com/proactivediary/notifications/AlarmReceiver.kt",
  "app/src/main/java/com/proactivediary/data/db/AppDatabase.kt",
  "app/src/main/java/com/proactivediary/data/repository/EntryRepositoryImpl.kt",
  "app/src/main/java/com/proactivediary/data/repository/GoalRepositoryImpl.kt",
];

function loadCodebaseContext() {
  const rootDir = path.resolve(__dirname, "..");
  let context = "";

  for (const filePath of CONTEXT_FILES) {
    const fullPath = path.join(rootDir, filePath);
    try {
      const content = fs.readFileSync(fullPath, "utf8");
      // Truncate very long files to stay within token limits
      const truncated =
        content.length > 8000
          ? content.substring(0, 8000) + "\n... [truncated]"
          : content;
      context += `\n--- ${filePath} ---\n${truncated}\n`;
    } catch {
      context += `\n--- ${filePath} ---\n[File not found]\n`;
    }
  }

  return context;
}

// ─── Bug analysis via Claude ─────────────────────────────────────
async function analyzeBug(bugReport, codebaseContext) {
  const deviceInfoLines = bugReport.deviceInfo
    ? Object.entries(bugReport.deviceInfo)
        .map(([k, v]) => `  - ${k}: ${v}`)
        .join("\n")
    : "  Not provided";

  const prompt = `You are a senior Android developer analyzing a bug report for Proactive Diary — a privacy-first journaling app built with Kotlin, Jetpack Compose, Material 3, Hilt, and Room.

## Bug Report
- **Subject:** ${bugReport.subject}
- **Description:** ${bugReport.description}
- **Device Info:**
${deviceInfoLines}

## Codebase Context
${codebaseContext}

## Your Task
Analyze this bug report. Respond with ONLY a JSON object (no markdown fences):

{
  "status": "resolved" | "needs_info" | "needs_screenshot",
  "confidence": "high" | "medium" | "low",
  "analysis": "Internal analysis for developer logs (not sent to user)",
  "user_message": "Friendly, professional message to the user (under 200 words). If resolved: explain the fix coming in the next update. If needs_info: ask specific questions. If needs_screenshot: explain why.",
  "likely_file": "Path to the file most likely containing the bug, or null",
  "suggested_fix": "Brief description of the code fix, or null"
}

Rules:
- Vague reports with multiple possible causes → "needs_info" with specific questions
- Visual/UI bugs you can't determine from code → "needs_screenshot"
- If you can identify the cause from code → "resolved"
- Be empathetic in user_message — these are real users
- Never expose file paths, class names, or technical jargon in user_message
- Keep user_message under 200 words`;

  const response = await anthropic.messages.create({
    model: "claude-sonnet-4-20250514",
    max_tokens: 1500,
    messages: [{ role: "user", content: prompt }],
  });

  const text = response.content[0].text;

  // Parse JSON (handle potential markdown wrapping)
  const jsonMatch = text.match(/\{[\s\S]*\}/);
  if (!jsonMatch) {
    throw new Error("Failed to parse Claude response as JSON");
  }

  return JSON.parse(jsonMatch[0]);
}

// ─── Main pipeline ───────────────────────────────────────────────
async function processNewBugReports() {
  console.log(
    `[${new Date().toISOString()}] Querying for new bug reports...`
  );

  const snapshot = await db
    .collection("support_requests")
    .where("status", "==", "new")
    .where("category", "==", "bug_report")
    .orderBy("createdAt", "asc")
    .limit(5) // Max 5 per run to stay within GitHub Actions timeout
    .get();

  if (snapshot.empty) {
    console.log("No new bug reports to process.");
    return;
  }

  console.log(
    `Found ${snapshot.size} new bug report(s). Loading codebase context...`
  );
  const codebaseContext = loadCodebaseContext();

  let processed = 0;
  let errors = 0;

  for (const doc of snapshot.docs) {
    const bugReport = doc.data();
    console.log(`\nProcessing: ${doc.id} — "${bugReport.subject}"`);

    try {
      const analysis = await analyzeBug(bugReport, codebaseContext);
      console.log(
        `  Status: ${analysis.status} (confidence: ${analysis.confidence})`
      );
      console.log(`  Analysis: ${analysis.analysis.substring(0, 120)}...`);

      // Update Firestore
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

      console.log(`  ✓ Firestore updated`);
      processed++;
    } catch (err) {
      console.error(`  ✗ Error processing ${doc.id}:`, err.message);
      errors++;

      // Graceful fallback: mark as needs_info with a generic message
      try {
        await db
          .collection("support_requests")
          .doc(doc.id)
          .update({
            status: "needs_info",
            resolution:
              "Thank you for your bug report. Our team is looking into this and may need additional details. We'll follow up shortly.",
            resolvedAt: admin.firestore.FieldValue.serverTimestamp(),
            _internal: {
              error: err.message,
              processed_by: "auto-bug-resolver",
              processed_at: new Date().toISOString(),
            },
          });
      } catch (updateErr) {
        console.error(`  ✗ Fallback update also failed:`, updateErr.message);
      }
    }
  }

  console.log(
    `\nDone. Processed: ${processed}, Errors: ${errors}, Total: ${snapshot.size}`
  );
}

// ─── Run ─────────────────────────────────────────────────────────
processNewBugReports()
  .then(() => process.exit(0))
  .catch((err) => {
    console.error("Fatal error:", err);
    process.exit(1);
  });
