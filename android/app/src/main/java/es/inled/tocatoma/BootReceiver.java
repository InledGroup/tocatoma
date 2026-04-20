package es.inled.tocatoma;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d("Tocatoma", "Boot completed. Waking up app to reschedule alarms.");
            
            // Lanzamos la actividad principal para que Capacitor se inicialice 
            // y el código de TypeScript pueda reprogramar las alarmas desde la DB.
            Intent i = new Intent(context, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }
    }
}
