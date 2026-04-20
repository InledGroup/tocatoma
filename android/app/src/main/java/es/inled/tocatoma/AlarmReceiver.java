package es.inled.tocatoma;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;

public class AlarmReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "ALARM_CHANNEL_ID";
    private static final String CHANNEL_NAME = "Alarmas de Medicación";

    @Override
    public void onReceive(Context context, Intent intent) {
        String scheduleId = intent.getStringExtra("scheduleId");
        
        // Si no hay scheduleId, no es una alarma válida de nuestra app o es un boot vacío
        if (scheduleId == null) return;

        String medicationName = intent.getStringExtra("medicationName");
        String ringtoneUri = intent.getStringExtra("ringtoneUri");

        // Crear canal de notificación (Android 8+)
        createNotificationChannel(context);

        // Intent para abrir la AlarmActivity
        Intent alarmIntent = new Intent(context, AlarmActivity.class);
        alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        alarmIntent.putExtra("medicationName", medicationName);
        alarmIntent.putExtra("ringtoneUri", ringtoneUri);
        alarmIntent.putExtra("scheduleId", scheduleId);

        // PendingIntent para FullScreenIntent
        PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(
            context, 
            scheduleId.hashCode(), 
            alarmIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Construir la notificación de alta prioridad
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle("¡Hora de tu medicación!")
                .setContentText("Toma tu " + (medicationName != null ? medicationName : "medicina"))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(true)
                .setOngoing(true)
                .setFullScreenIntent(fullScreenPendingIntent, true) // ESTO ES LA CLAVE
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(scheduleId.hashCode(), builder.build());
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID, 
                CHANNEL_NAME, 
                NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Canal para las alarmas de medicación");
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            channel.enableVibration(true);
            channel.setSound(null, null); // El sonido lo manejamos en la Activity
            
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}
