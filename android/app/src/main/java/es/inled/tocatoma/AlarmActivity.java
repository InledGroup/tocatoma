package es.inled.tocatoma;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.StateListDrawable;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.util.TypedValue;
import android.widget.Space;

public class AlarmActivity extends Activity {
    private MediaPlayer mediaPlayer;
    private Vibrator vibrator;
    private String scheduleId;
    private String medicationName;
    private String ringtoneUri;
    private Handler handler = new Handler();
    private Runnable stopAlarmRunnable;
    private LinearLayout buttonsContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Despertar pantalla y mantener encendida
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        }

        medicationName = getIntent().getStringExtra("medicationName");
        ringtoneUri = getIntent().getStringExtra("ringtoneUri");
        scheduleId = getIntent().getStringExtra("scheduleId");

        setContentView(createStyledLayout());
        startAlarm();
    }

    private View createStyledLayout() {
        // Usamos RelativeLayout para fijar elementos arriba/centro/abajo
        RelativeLayout root = new RelativeLayout(this);
        root.setBackgroundColor(Color.WHITE);
        root.setPadding(dpToPx(40), dpToPx(40), dpToPx(40), dpToPx(60));

        // 1. CONTENIDO CENTRAL (Pill, Título, Nombre, Botón Tomar)
        LinearLayout centerContent = new LinearLayout(this);
        centerContent.setOrientation(LinearLayout.VERTICAL);
        centerContent.setGravity(Gravity.CENTER);
        
        RelativeLayout.LayoutParams centerParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        centerParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        centerContent.setLayoutParams(centerParams);

        // Emoji Medicina XXL
        TextView emoji = new TextView(this);
        emoji.setText("💊");
        emoji.setTextSize(TypedValue.COMPLEX_UNIT_SP, 100);
        emoji.setGravity(Gravity.CENTER);
        centerContent.addView(emoji);

        // Título Duolingo
        TextView title = new TextView(this);
        title.setText("¡HORA DE TU MEDICACIÓN!");
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        title.setTypeface(Typeface.create("sans-serif-black", Typeface.BOLD));
        title.setTextColor(Color.parseColor("#afafaf"));
        title.setPadding(0, dpToPx(20), 0, dpToPx(10));
        title.setGravity(Gravity.CENTER);
        centerContent.addView(title);

        // Nombre Medicina XXL
        TextView medName = new TextView(this);
        medName.setText(medicationName != null ? medicationName.toUpperCase() : "MEDICINA");
        medName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 42);
        medName.setTypeface(Typeface.create("sans-serif-black", Typeface.BOLD));
        medName.setTextColor(Color.parseColor("#4b4b4b"));
        medName.setGravity(Gravity.CENTER);
        medName.setPadding(0, 0, 0, dpToPx(60));
        centerContent.addView(medName);

        // Contenedor para botones principales (dentro del centro)
        buttonsContainer = new LinearLayout(this);
        buttonsContainer.setOrientation(LinearLayout.VERTICAL);
        buttonsContainer.setGravity(Gravity.CENTER);
        centerContent.addView(buttonsContainer);

        root.addView(centerContent);

        // 2. BOTÓN POSPONER FIJO ABAJO
        // En modo "Principal" mostramos Snooze abajo, al pulsar cambiaremos el centerContent
        showMainButtons(root);

        return root;
    }

    private void showMainButtons(RelativeLayout root) {
        buttonsContainer.removeAllViews();

        // Botón TOMAR en el centro para máxima visibilidad
        Button takeBtn = create3DButton("TOMAR AHORA", "#58cc02", "#46a302");
        takeBtn.setOnClickListener(v -> handleTake());
        buttonsContainer.addView(takeBtn);

        // Botón POSPONER anclado al fondo del root
        Button snoozeBtn = create3DButton("POSPONER...", "#1cb0f6", "#1899d6");
        RelativeLayout.LayoutParams snoozeParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, dpToPx(80));
        snoozeParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        snoozeParams.bottomMargin = dpToPx(20);
        snoozeBtn.setLayoutParams(snoozeParams);
        
        snoozeBtn.setOnClickListener(v -> showSnoozeOptions());
        
        // Lo añadimos al root, no al centerContent
        root.addView(snoozeBtn);
    }

    private void showSnoozeOptions() {
        // Al pulsar posponer, cambiamos el contenido central por las opciones
        buttonsContainer.removeAllViews();

        TextView snoozeTitle = new TextView(this);
        snoozeTitle.setText("¿CUÁNTOS MINUTOS?");
        snoozeTitle.setTypeface(Typeface.create("sans-serif-black", Typeface.BOLD));
        snoozeTitle.setTextColor(Color.GRAY);
        snoozeTitle.setPadding(0, 0, 0, dpToPx(30));
        snoozeTitle.setGravity(Gravity.CENTER);
        buttonsContainer.addView(snoozeTitle);

        int[] minutes = {5, 10, 15};
        for (int m : minutes) {
            Button b = create3DButton(m + " MINUTOS", "#1cb0f6", "#1899d6");
            b.setOnClickListener(v -> handleSnooze(m));
            buttonsContainer.addView(b);
            Space s = new Space(this);
            buttonsContainer.addView(s, new LinearLayout.LayoutParams(0, dpToPx(15)));
        }

        Button backBtn = create3DButton("ATRÁS", "#e5e5e5", "#afafaf");
        backBtn.setOnClickListener(v -> {
            // Recargar layout para volver a main (simple para este caso)
            setContentView(createStyledLayout());
        });
        buttonsContainer.addView(backBtn);
        
        // Ocultamos el botón de posponer del fondo para no confundir
        View snoozeAtBottom = findViewById(snoozeBtnId); // Necesitaríamos un ID
        // Simplificado: Re-renderizamos todo el layout
    }

    private int snoozeBtnId = View.generateViewId();

    private Button create3DButton(String text, String bgColor, String shadowColor) {
        Button btn = new Button(this);
        btn.setText(text);
        btn.setTextColor(bgColor.equals("#e5e5e5") ? Color.parseColor("#afafaf") : Color.WHITE);
        btn.setTypeface(Typeface.create("sans-serif-black", Typeface.BOLD));
        btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        btn.setAllCaps(true);
        
        GradientDrawable top = new GradientDrawable();
        top.setColor(Color.parseColor(bgColor));
        top.setCornerRadius(dpToPx(20));
        
        GradientDrawable bottom = new GradientDrawable();
        bottom.setColor(Color.parseColor(shadowColor));
        bottom.setCornerRadius(dpToPx(20));

        LayerDrawable normal = new LayerDrawable(new GradientDrawable[]{bottom, top});
        normal.setLayerInset(1, 0, 0, 0, dpToPx(8)); // Sombra 3D proporcional

        StateListDrawable states = new StateListDrawable();
        states.addState(new int[]{android.R.attr.state_pressed}, top);
        states.addState(new int[]{}, normal);
        
        btn.setBackground(states);
        btn.setPadding(0, 0, 0, 0);
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(75)); 
        btn.setLayoutParams(params);
        
        return btn;
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    private void startAlarm() {
        try {
            AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, 
                    audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM), 0);

            Uri uri = (ringtoneUri != null && !ringtoneUri.isEmpty()) 
                    ? Uri.parse(ringtoneUri) 
                    : android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI;

            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(this, uri);
            mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build());
            mediaPlayer.setLooping(true);
            mediaPlayer.prepare();
            mediaPlayer.start();

            vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createWaveform(new long[]{0, 500, 500}, 0));
                } else {
                    vibrator.vibrate(new long[]{0, 500, 500}, 0);
                }
            }
            stopAlarmRunnable = () -> stopAndExit();
            handler.postDelayed(stopAlarmRunnable, 60000);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void handleTake() { scheduleRescheduledAlarm(24 * 60); stopAndExit(); }
    private void handleSnooze(int minutes) { scheduleRescheduledAlarm(minutes); stopAndExit(); }

    private void scheduleRescheduledAlarm(int minutes) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("scheduleId", scheduleId);
        intent.putExtra("medicationName", medicationName);
        intent.putExtra("ringtoneUri", ringtoneUri);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, scheduleId.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        long triggerTime = System.currentTimeMillis() + (minutes * 60 * 1000);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
        else alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
    }

    private void stopAndExit() {
        if (mediaPlayer != null) { mediaPlayer.stop(); mediaPlayer.release(); mediaPlayer = null; }
        if (vibrator != null) vibrator.cancel();
        handler.removeCallbacks(stopAlarmRunnable);
        finish();
    }
}
