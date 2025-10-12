const { onValueWritten, onValueCreated } = require("firebase-functions/v2/database");
const admin = require("firebase-admin");

admin.initializeApp();

/**
 * Sends a notification to the Mess Admin when an NGO books their food.
 * This uses the modern V2 syntax.
 */
exports.notifyMessAdminOnBooking = onValueWritten("/food_listings/{foodId}/bookedBy", async (event) => {
    // The foodId is available in the event parameters
    const foodId = event.params.foodId;
    // The new value (the NGO's UID) is in event.data.after
    const ngoId = event.data.after.val();

    // If there's no NGO ID, it means the booking was removed. Stop.
    if (!ngoId) {
        console.log("Booking removed. No notification sent.");
        return null;
    }

    // 1. Get the food item to find out who posted it.
    const foodSnapshot = await admin.database().ref(`/food_listings/${foodId}`).get();
    const foodItem = foodSnapshot.val();

    // Make sure the food item and its poster exist
    if (!foodItem || !foodItem.postedBy) {
        console.log("Food item or original poster not found.");
        return null;
    }
    const messAdminId = foodItem.postedBy;

    // 2. Get the Mess Admin's FCM token.
    const userSnapshot = await admin.database().ref(`/users/${messAdminId}`).get();
    const messAdminUser = userSnapshot.val();
    const token = messAdminUser ? messAdminUser.fcmToken : null;

    if (!token) {
        console.log("Mess Admin has no FCM token. No notification sent.");
        return null;
    }

    // 3. Get the NGO's name to use in the message.
    const ngoUserSnapshot = await admin.database().ref(`/users/${ngoId}`).get();
    const ngoUser = ngoUserSnapshot.val();
    const ngoName = ngoUser ? ngoUser.name : "An NGO"; // Use a default if name is not set

    // 4. Create the notification message.
    const payload = {
        notification: {
            title: "Your Food Has Been Booked!",
            body: `${ngoName} has booked your listing: "${foodItem.name}".`,
            icon: "ic_stat_ic_notification",
        },
    };

    // 5. Send the notification.
    console.log(`Sending booking notification to token: ${token}`);
    return admin.messaging().sendToDevice(token, payload);
});

/**
 * Sends a notification to ALL NGOs when a new food item is posted.
 * This uses the modern V2 syntax.
 */
exports.notifyNgosOnNewFood = onValueCreated("/food_listings/{foodId}", async (event) => {
    // The newly created data is in event.data
    const foodItem = event.data.val();

    if (!foodItem) {
        console.log("Food item data is missing.");
        return null;
    }

    // 1. Get the list of all users.
    const usersSnapshot = await admin.database().ref("/users").get();
    const users = usersSnapshot.val();
    const tokens = [];

    if (!users) {
        console.log("No users found in the database.");
        return null;
    }

    // 2. Collect the tokens of all users who are NGOs.
    for (const userId in users) {
        if (users[userId].role === "NGO" && users[userId].fcmToken) {
            tokens.push(users[userId].fcmToken);
        }
    }

    if (tokens.length === 0) {
        console.log("No NGOs with tokens found. No notifications sent.");
        return null;
    }

    // 3. Create the notification message.
    const payload = {
        notification: {
            title: "New Food Available!",
            body: `A new food listing is available: "${foodItem.name}".`,
            icon: "ic_stat_ic_notification",
        },
    };

    // 4. Send the notification to all collected tokens.
    console.log(`Sending new food notification to ${tokens.length} NGOs.`);
    return admin.messaging().sendToDevice(tokens, payload);
});