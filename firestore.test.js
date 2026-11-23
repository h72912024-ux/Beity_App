const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_uid_123";
const BOB_UID = "bob_uid_456";
const ADMIN_UID = "admin_uid_789";
const ADMIN_EMAIL = "mstfymhmwdydabwkhdr@gmail.com";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function pastTime(msAgo = 5000) {
  return new Date(Date.now() - msAgo);
}

function validPropertyPayload(ownerId, overrides = {}) {
  const now = pastTime(5000);
  return {
    ownerId,
    ownerName: "أحمد محمود",
    contactPhone: "+201001234567",
    title: "فيلا فاخرة بالتجمع الخامس",
    description: "فيلا مستقلة بتشطيب سوبر لوكس وحديقة خاصة وحمام سباحة",
    propertyType: "villa",
    listingType: "sale",
    price: 12500000,
    areaSqm: 420,
    governorate: "القاهرة",
    city: "القاهرة الجديدة",
    district: "التجمع الخامس",
    address: "كمبوند ميفيدا، قطعة 14",
    bedrooms: 5,
    bathrooms: 4,
    floor: 0,
    finishingType: "super_lux",
    furnished: false,
    hasElevator: false,
    hasGarage: true,
    hasBalcony: true,
    features: ["حديقة خاصة", "حمام سباحة"],
    amenities: ["أمن 24 ساعة", "نادي صحي"],
    imageUrls: ["preset:villa"],
    latitude: 30.015,
    longitude: 31.491,
    status: "pending",
    featured: false,
    viewsCount: 0,
    createdAt: now,
    updatedAt: now,
    ...overrides,
  };
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated users cannot read or write any collection", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("properties").where("status", "==", "approved").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    unauthDb.collection("properties").doc("prop_1").set(validPropertyPayload(ALICE_UID))
  );
});

test("User profile PII isolation and role/status escalation guards", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const adminDb = testEnv
    .authenticatedContext(ADMIN_UID, { email: ADMIN_EMAIL, email_verified: true })
    .firestore();
  const now = pastTime();

  // Alice creates her active profile
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "أليس",
      email: "alice@example.com",
      phone: "+201011111111",
      city: "القاهرة",
      accountType: "owner",
      status: "active",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Alice cannot self-suspend or inject ghost field
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      status: "suspended",
      updatedAt: pastTime(1000),
    })
  );
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      isAdmin: true,
      updatedAt: pastTime(1000),
    })
  );

  // Bob cannot read Alice's PII profile or list users
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(bobDb.collection("users").get());

  // Admin can list users and suspend Alice
  await assertSucceeds(adminDb.collection("users").get());
  await assertSucceeds(
    adminDb.collection("users").doc(ALICE_UID).update({
      status: "suspended",
      updatedAt: pastTime(500),
    })
  );
});

test("Property lifecycle: owner submits pending, cannot self-approve, admin approves, view count increments", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const adminDb = testEnv
    .authenticatedContext(ADMIN_UID, { email: ADMIN_EMAIL, email_verified: true })
    .firestore();

  // Owner cannot create directly as approved or featured
  await assertFails(
    aliceDb
      .collection("properties")
      .doc("prop_bad")
      .set(validPropertyPayload(ALICE_UID, { status: "approved" }))
  );
  await assertFails(
    aliceDb
      .collection("properties")
      .doc("prop_bad2")
      .set(validPropertyPayload(ALICE_UID, { featured: true }))
  );
  // Owner cannot include @DocumentId `id` field
  await assertFails(
    aliceDb
      .collection("properties")
      .doc("prop_bad3")
      .set({ id: "prop_bad3", ...validPropertyPayload(ALICE_UID) })
  );

  // Valid pending property creation succeeds
  await assertSucceeds(
    aliceDb.collection("properties").doc("prop_1").set(validPropertyPayload(ALICE_UID))
  );

  // Owner cannot self-approve pending listing
  await assertFails(
    aliceDb.collection("properties").doc("prop_1").update({
      status: "approved",
      updatedAt: pastTime(1000),
    })
  );

  // Bob cannot read Alice's pending listing, and unfiltered query fails
  await assertFails(bobDb.collection("properties").doc("prop_1").get());
  await assertFails(bobDb.collection("properties").get());

  // Admin approves & features prop_1
  await assertSucceeds(
    adminDb.collection("properties").doc("prop_1").update({
      status: "approved",
      featured: true,
      updatedAt: pastTime(800),
    })
  );

  // Bob can now query approved properties and read prop_1
  await assertSucceeds(bobDb.collection("properties").where("status", "==", "approved").get());
  await assertSucceeds(bobDb.collection("properties").doc("prop_1").get());

  // Bob can increment viewsCount by exactly 1
  await assertSucceeds(
    bobDb.collection("properties").doc("prop_1").update({ viewsCount: 1 })
  );
  // Bob cannot jump viewsCount by 10 or edit title
  await assertFails(
    bobDb.collection("properties").doc("prop_1").update({ viewsCount: 11 })
  );
  await assertFails(
    bobDb.collection("properties").doc("prop_1").update({ title: "Hacked", updatedAt: pastTime(500) })
  );

  // Owner can pause & republish approved listing
  await assertSucceeds(
    aliceDb.collection("properties").doc("prop_1").update({
      status: "paused",
      updatedAt: pastTime(400),
    })
  );
  await assertSucceeds(
    aliceDb.collection("properties").doc("prop_1").update({
      status: "approved",
      updatedAt: pastTime(300),
    })
  );
});

test("Favorites & Notifications are strictly owner-scoped", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = pastTime();

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("favorites").doc("prop_1").set({
      userId: ALICE_UID,
      propertyId: "prop_1",
      title: "شقة بالزمالك",
      price: 5500000,
      city: "القاهرة",
      createdAt: now,
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("favorites").get()
  );
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("favorites").get()
  );
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("favorites").doc("prop_1").delete()
  );

  // Notification sent from Bob to Alice
  await assertSucceeds(
    bobDb.collection("users").doc(ALICE_UID).collection("notifications").doc("notif_1").set({
      userId: ALICE_UID,
      senderId: BOB_UID,
      title: "رسالة جديدة",
      body: "استفسار حول العقار",
      type: "message",
      targetId: "conv_1",
      read: false,
      createdAt: now,
    })
  );
  // Alice marks read
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("notifications").doc("notif_1").update({
      read: true,
    })
  );
});

test("Conversations and Messages restrict access strictly to participants", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const carolDb = testEnv.authenticatedContext("carol_789").firestore();
  const now = pastTime();

  const convId = "prop_1_alice_uid_123_bob_uid_456";
  await assertSucceeds(
    bobDb.collection("conversations").doc(convId).set({
      participantIds: [ALICE_UID, BOB_UID],
      propertyId: "prop_1",
      propertyTitle: "فيلا بالتجمع",
      participantNames: { [ALICE_UID]: "أليس", [BOB_UID]: "بوب" },
      lastMessage: "مرحبا هل العقار متاح؟",
      lastSenderId: BOB_UID,
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(
    bobDb.collection("conversations").doc(convId).collection("messages").doc("msg_1").set({
      conversationId: convId,
      senderId: BOB_UID,
      senderName: "بوب",
      participantIds: [ALICE_UID, BOB_UID],
      text: "مرحبا هل العقار متاح؟",
      createdAt: now,
    })
  );

  // Alice can query her conversations & messages
  await assertSucceeds(
    aliceDb.collection("conversations").where("participantIds", "array-contains", ALICE_UID).get()
  );
  await assertSucceeds(
    aliceDb
      .collection("conversations")
      .doc(convId)
      .collection("messages")
      .where("participantIds", "array-contains", ALICE_UID)
      .get()
  );

  // Carol cannot read or message
  await assertFails(carolDb.collection("conversations").doc(convId).get());
  await assertFails(
    carolDb.collection("conversations").doc(convId).collection("messages").doc("msg_2").set({
      conversationId: convId,
      senderId: "carol_789",
      senderName: "كارول",
      participantIds: [ALICE_UID, BOB_UID],
      text: "تطفل",
      createdAt: now,
    })
  );
});

test("Reports & Spoofed Admin Email Protection", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const spoofedAdminDb = testEnv
    .authenticatedContext("spoof_uid", { email: ADMIN_EMAIL, email_verified: false })
    .firestore();
  const realAdminDb = testEnv
    .authenticatedContext(ADMIN_UID, { email: ADMIN_EMAIL, email_verified: true })
    .firestore();
  const now = pastTime();

  await assertSucceeds(
    aliceDb.collection("reports").doc("rep_1").set({
      reporterId: ALICE_UID,
      reporterName: "أليس",
      propertyId: "prop_1",
      propertyTitle: "فيلا بالتجمع",
      reportType: "property_report",
      reason: "سعر غير صحيح",
      details: "السعر المذكور لا يطابق الواقع",
      status: "open",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Spoofed unverified email admin is rejected
  await assertFails(spoofedAdminDb.collection("reports").get());
  await assertFails(
    spoofedAdminDb.collection("reports").doc("rep_1").update({
      status: "resolved",
      updatedAt: pastTime(500),
    })
  );

  // Verified admin succeeds
  await assertSucceeds(realAdminDb.collection("reports").get());
  await assertSucceeds(
    realAdminDb.collection("reports").doc("rep_1").update({
      status: "resolved",
      adminNotes: "تمت المراجعة",
      updatedAt: pastTime(400),
    })
  );
});
