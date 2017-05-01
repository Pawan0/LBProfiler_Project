package trainedge.lbprofiler;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AreWeThereIntentService extends IntentService implements TextToSpeech.OnInitListener {

    // region Properties

    private final String TAG = AreWeThereIntentService.class.getName();

    private SharedPreferences prefs;
    private Gson gson;
    private TextToSpeech engine;

    // endregion

    // region Constructors

    public AreWeThereIntentService() {
        super("AreWeThereIntentService");
    }

    // endregion

    // region Overrides

    @Override
    protected void onHandleIntent(Intent intent) {
        prefs = getApplicationContext().getSharedPreferences(Constants.SharedPrefs.Geofences, Context.MODE_PRIVATE);
        gson = new Gson();

        GeofencingEvent event = GeofencingEvent.fromIntent(intent);
        if (event != null) {
            if (event.hasError()) {
                onError(event.getErrorCode());
            } else {
                int transition = event.getGeofenceTransition();
                if (transition == Geofence.GEOFENCE_TRANSITION_ENTER || transition == Geofence.GEOFENCE_TRANSITION_DWELL || transition == Geofence.GEOFENCE_TRANSITION_EXIT) {
                    List<String> geofenceIds = new ArrayList<>();
                    for (Geofence geofence : event.getTriggeringGeofences()) {
                        geofenceIds.add(geofence.getRequestId());
                    }
                    if (transition == Geofence.GEOFENCE_TRANSITION_ENTER || transition == Geofence.GEOFENCE_TRANSITION_DWELL) {
                        onEnteredGeofences(geofenceIds);
                    }
                }
            }
        }
    }

    // endregion

    // region Private

    private void onEnteredGeofences(List<String> geofenceIds) {
        for (String geofenceId : geofenceIds) {
            String geofenceName = "";

            // Loop over all geofence keys in prefs and retrieve NamedGeofence from SharedPreference
            Map<String, ?> keys = prefs.getAll();
            for (Map.Entry<String, ?> entry : keys.entrySet()) {
                String jsonString = prefs.getString(entry.getKey(), null);
                NamedGeofence namedGeofence = gson.fromJson(jsonString, NamedGeofence.class);
                if (namedGeofence.id.equals(geofenceId)) {
                    geofenceName = namedGeofence.name;
                    break;
                }
            }

      // Set the notification text and send the notification
      String contextText = String.format(this.getResources().getString(R.string.Notification_Text), geofenceName);

      NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
      Intent intent = new Intent(this, AllGeofencesActivity.class);
      intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
      PendingIntent pendingNotificationIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

      Notification notification = new NotificationCompat.Builder(this)
              .setSmallIcon(R.mipmap.ic_launcher)
              .setContentTitle(this.getResources().getString(R.string.Notification_Title))
              .setContentText(contextText)
              .setContentIntent(pendingNotificationIntent)
              .setStyle(new NotificationCompat.BigTextStyle().bigText(contextText))
              .setPriority(NotificationCompat.PRIORITY_HIGH)
              .setAutoCancel(true)
              .build();
      notificationManager.notify(0, notification);




            Context context = null;
            engine = new TextToSpeech(context, this);
            TaskGeofenceNotification.notify(getApplicationContext(), "You have reached your destination, ", 0);
            initTTS();
        }
    }

    private void initTTS() {

    }

    private void onError(int i) {
        Log.e(TAG, "Geofencing Error: " + i);
    }

    // endregion

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            engine.setLanguage(Locale.ENGLISH);
            CharSequence msg = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                engine.speak("you have reached your destination", TextToSpeech.QUEUE_FLUSH, null, null);
            } else {
                engine.speak(String.valueOf("you have reached your destination"), TextToSpeech.QUEUE_FLUSH, null);
            }
        }
    }
}

