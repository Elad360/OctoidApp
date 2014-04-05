package il.ac.shenkar.octoid;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;

import java.util.List;

/**
 * This class receives geofence transition events from Location Services, in the
 * form of an Intent containing the transition type and geofence id(s) that triggered
 * the event.
 */
public class ReceiveTransitionsIntentService extends IntentService {

    /**
     * Sets an identifier for this class' background thread
     */
    public ReceiveTransitionsIntentService() {
        super("ReceiveTransitionsIntentService");
    }

    /**
     * Handles incoming intents
     * @param intent The Intent sent by Location Services. This Intent is provided
     * to Location Services (inside a PendingIntent) when you call addGeofences()
     */
    @Override
    protected void onHandleIntent(Intent intent) {

        // Create a local broadcast Intent
        Intent broadcastIntent = new Intent();

        // Get the type of transition (entry or exit)
        int transition = LocationClient.getGeofenceTransition(intent);


        if ((transition == Geofence.GEOFENCE_TRANSITION_ENTER) ||
            (transition == Geofence.GEOFENCE_TRANSITION_EXIT))
        {
            List<Geofence> triggerList = LocationClient.getTriggeringGeofences(intent);
            String[] triggerIds = new String[triggerList.size()];
            for (int i = 0; i < triggerIds.length; i++)
            {
                // Store the Id of each geofence
                triggerIds[i] = triggerList.get(i).getRequestId();
            }
            if (transition == Geofence.GEOFENCE_TRANSITION_ENTER)
            { sendNotification("Entered Geofence",triggerIds); }
            else if (transition == Geofence.GEOFENCE_TRANSITION_EXIT)
            { sendNotification("Exited Geofence",triggerIds); }
        }
        else
        {
            sendNotification("Unknown Geofence transition",null);
        }

    }


    private void sendNotification(String message, String[] triggerIds)
    {

        // Create an explicit content Intent that starts the main Activity
        Intent notificationIntent =
                new Intent(getApplicationContext(),ListViewTasksActivity.class);

        // Construct a task stack
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

        // Adds the main Activity to the task stack as the parent
        stackBuilder.addParentStack(ListViewTasksActivity.class);

        // Push the content Intent onto the stack
        stackBuilder.addNextIntent(notificationIntent);

        // Get a PendingIntent containing the entire back stack
        PendingIntent notificationPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        // Get a notification builder that's compatible with platform versions >= 4
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        // Set the notification contents
        builder.setSmallIcon(R.drawable.appicon)
                .setContentTitle(message)
                .setContentText("Tap here to enter the app")
                .setContentIntent(notificationPendingIntent);

        // Get an instance of the Notification manager
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Issue the notification
        mNotificationManager.notify(0, builder.build());
    }

}

