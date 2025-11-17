import {onDocumentUpdated} from "firebase-functions/v2/firestore";
import * as admin from "firebase-admin";
import {distanceBetween} from "geofire-common";

admin.initializeApp();
const firestore = admin.firestore();
const messaging = admin.messaging();

// Ispravna V2 sintaksa za definisanje funkcije
export const checkNearbyPlaylistsOnLocationUpdate = onDocumentUpdated(
  "users/{userId}",
  async (event) => {
    // U V2, podaci se nalaze unutar event.data objekta
    if (!event.data) {
      console.log("Nema podataka u event objektu.");
      return;
    }

    const beforeData = event.data.before.data();
    const afterData = event.data.after.data();
    // U V2, parametri (kao userId) se nalaze u event.params
    const userId = event.params.userId;

    const locationUnchanged = beforeData.lastLocation &&
      afterData.lastLocation &&
      beforeData.lastLocation.isEqual(afterData.lastLocation);

    if (locationUnchanged) {
      return; // U V2 funkcijama, samo koristimo return
    }

    if (!afterData.fcmToken || !afterData.lastLocation) {
      return;
    }

    const userLocation: [number, number] = [
      afterData.lastLocation.latitude,
      afterData.lastLocation.longitude,
    ];
    const radiusInM = 200; // 200 metara

    console.log(`Korisnik ${userId} se pomerio. Proveravam plejliste.`);

    const playlistsSnapshot = await firestore.collection("playlists").get();
    if (playlistsSnapshot.empty) {
      return;
    }

    for (const playlistDoc of playlistsSnapshot.docs) {
      const playlist = playlistDoc.data();
      const playlistId = playlistDoc.id;

      if (!playlist.location) {
        continue;
      }

      const playlistLocation: [number, number] = [
        playlist.location.latitude,
        playlist.location.longitude,
      ];

      const notificationRecordRef = firestore
        .collection("users").doc(userId)
        .collection("notifiedPlaylists").doc(playlistId);

      const distanceInM =
        distanceBetween(userLocation, playlistLocation) * 1000;

      if (distanceInM <= radiusInM) {
        const notificationRecord = await notificationRecordRef.get();
        if (!notificationRecord.exists) {
          console.log(
            `Korisnik ${userId} ušao u radijus plejliste ${playlistId}.`,
            "Šaljem notifikaciju.",
          );

          const message: admin.messaging.Message = {
            token: afterData.fcmToken,
            data: {
              title: playlist.name,
              body: "You are near this playlist. You can contribute!",
              ...(playlist.coverImageUrl && {imageUrl: playlist.coverImageUrl}),
            },
            notification: {
              title: playlist.name,
              body: "You are near this playlist. You can contribute!",
              ...(playlist.coverImageUrl && {imageUrl: playlist.coverImageUrl}),
            },
          };
          await messaging.send(message);

          await notificationRecordRef.set({
            notifiedAt: admin.firestore.FieldValue.serverTimestamp(),
          });
        }
      } else {
        const notificationRecord = await notificationRecordRef.get();
        if (notificationRecord.exists) {
          console.log(
            `Korisnik ${userId} izašao iz radijusa plejliste ${playlistId}.`,
            "Brišem zapis.",
          );
          await notificationRecordRef.delete();
        }
      }
    }
    return;
  });
