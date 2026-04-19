package es.inled.tocatoma;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String medicationName = intent.getStringExtra("medicationName");
        String ringtoneUri = intent.getStringExtra("ringtoneUri");
        String scheduleId = intent.getStringExtra("scheduleId");

        Intent alarmIntent = new Intent(context, AlarmActivity.class);
        alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        alarmIntent.putExtra("medicationName", medicationName);
        alarmIntent.putExtra("ringtoneUri", ringtoneUri);
        alarmIntent.putExtra("scheduleId", scheduleId);
        
        context.startActivity(alarmIntent);
    }
}
