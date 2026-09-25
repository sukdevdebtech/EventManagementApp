const { onSchedule } = require("firebase-functions/v2/scheduler");
const admin = require("firebase-admin");

admin.initializeApp();
const db = admin.firestore();

const REMINDER_WINDOW_MIN = 30;

// Runs every 5 minutes, finds events starting within the next 30 minutes
// and sends an FCM notification to the owner's saved device token.
exports.sendEventReminders = onSchedule("every 5 minutes", async () => {
  const now = admin.firestore.Timestamp.now();
  const until = admin.firestore.Timestamp.fromMillis(
    now.toMillis() + REMINDER_WINDOW_MIN * 60 * 1000
  );

  const snapshot = await db
    .collectionGroup("events")
    .where("dateTime", ">=", now)
    .where("dateTime", "<=", until)
    .get();

  for (const doc of snapshot.docs) {
    const event = doc.data();
    if (event.reminderSent) continue;

    const userDoc = await doc.ref.parent.parent.get(); // users/{uid}
    const token = userDoc.get("fcmToken");
    if (!token) continue;

    await admin.messaging().send({
      token,
      notification: {
        title: `Upcoming: ${event.title}`,
        body: event.location ? `Starts soon at ${event.location}` : "Starts soon",
      },
      android: { notification: { channelId: "event_reminders" } },
    });
    await doc.ref.update({ reminderSent: true });
  }
});
