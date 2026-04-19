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
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setBackgroundColor(Color.WHITE);
        root.setPadding(80, 40, 80, 80);

        Space topSpace = new Space(this);
        root.addView(topSpace, new LinearLayout.LayoutParams(0, 0, 1.2f));

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.CENTER);

        TextView emoji = new TextView(this);
        emoji.setText("💊");
        emoji.setTextSize(100);
        content.addView(emoji);

        TextView title = new TextView(this);
        title.setText("¡HORA DE TU MEDICACIÓN!");
        title.setTextSize(16);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setTextColor(Color.parseColor("#afafaf"));
        title.setPadding(0, 40, 0, 0);
        content.addView(title);

        TextView medName = new TextView(this);
        medName.setText(medicationName != null ? medicationName.toUpperCase() : "MEDICINA");
        medName.setTextSize(42);
        medName.setTypeface(Typeface.create("sans-serif-black", Typeface.BOLD));
        medName.setTextColor(Color.parseColor("#4b4b4b"));
        medName.setPadding(0, 0, 0, 80);
        content.addView(medName);

        // Contenedor dinámico de botones
        buttonsContainer = new LinearLayout(this);
        buttonsContainer.setOrientation(LinearLayout.VERTICAL);
        buttonsContainer.setGravity(Gravity.CENTER);
        content.addView(buttonsContainer);

        showMainButtons();

        root.addView(content);

        Space bottomSpace = new Space(this);
        root.addView(bottomSpace, new LinearLayout.LayoutParams(0, 0, 1.0f));

        return root;
    }

    private void showMainButtons() {
        buttonsContainer.removeAllViews();

        Button takeBtn = create3DButton("TOMAR AHORA", "#58cc02", "#46a302");
        takeBtn.setOnClickListener(v -> handleTake());
        buttonsContainer.addView(takeBtn);

        View spacer = new View(this);
        spacer.setMinimumHeight(40);
        buttonsContainer.addView(spacer);

        Button snoozeBtn = create3DButton("POSPONER...", "#1cb0f6", "#1899d6");
        snoozeBtn.setOnClickListener(v -> showSnoozeOptions());
        buttonsContainer.addView(snoozeBtn);
    }

    private void showSnoozeOptions() {
        buttonsContainer.removeAllViews();

        TextView snoozeTitle = new TextView(this);
        snoozeTitle.setText("¿CUÁNTOS MINUTOS?");
        snoozeTitle.setTypeface(Typeface.DEFAULT_BOLD);
        snoozeTitle.setPadding(0, 0, 0, 30);
        buttonsContainer.addView(snoozeTitle);

        // Opciones 5, 10, 15
        int[] minutes = {5, 10, 15};
        for (int m : minutes) {
            Button b = create3DButton(m + " MINUTOS", "#1cb0f6", "#1899d6");
            b.setOnClickListener(v -> handleSnooze(m));
            buttonsContainer.addView(b);
            View s = new View(this);
            s.setMinimumHeight(20);
            buttonsContainer.addView(s);
        }

        Button backBtn = create3DButton("ATRÁS", "#e5e5e5", "#afafaf");
        backBtn.setOnClickListener(v -> showMainButtons());
        buttonsContainer.addView(backBtn);
    }

    private Button create3DButton(String text, String bgColor, String shadowColor) {
        Button btn = new Button(this);
        btn.setText(text);
        btn.setTextColor(bgColor.equals("#e5e5e5") ? Color.parseColor("#afafaf") : Color.WHITE);
        btn.setTypeface(Typeface.DEFAULT_BOLD);
        btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        
        GradientDrawable top = new GradientDrawable();
        top.setColor(Color.parseColor(bgColor));
        top.setCornerRadius(40);
        
        GradientDrawable bottom = new GradientDrawable();
        bottom.setColor(Color.parseColor(shadowColor));
        bottom.setCornerRadius(40);

        LayerDrawable normal = new LayerDrawable(new GradientDrawable[]{bottom, top});
        normal.setLayerInset(1, 0, 0, 0, 15);

        StateListDrawable states = new StateListDrawable();
        states.addState(new int[]{android.R.attr.state_pressed}, top);
        states.addState(new int[]{}, normal);
        
        btn.setBackground(states);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 160);
        btn.setLayoutParams(params);
        
        return btn;
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

    private void handleTake() {
        // Marcamos como tomada para mañana
        scheduleRescheduledAlarm(24 * 60); 
        stopAndExit();
    }

    private void handleSnooze(int minutes) {
        scheduleRescheduledAlarm(minutes);
        stopAndExit();
    }

    private void scheduleRescheduledAlarm(int minutes) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("scheduleId", scheduleId);
        intent.putExtra("medicationName", medicationName);
        intent.putExtra("ringtoneUri", ringtoneUri);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, scheduleId.hashCode(), intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        long triggerTime = System.currentTimeMillis() + (minutes * 60 * 1000);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
        }
    }

    private void stopAndExit() {
        if (mediaPlayer != null) { mediaPlayer.stop(); mediaPlayer.release(); mediaPlayer = null; }
        if (vibrator != null) { vibrator.cancel(); }
        handler.removeCallbacks(stopAlarmRunnable);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAndExit();
    }
}
