package com.briandemaio.meditationreminder;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;

import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

public class MainActivity extends AppCompatActivity {

    private AdView mAdView;

    private ProgressBar progressBar;
    private CountDownTimer progressBarCountdown;
    private Button meditate;
    private TextView progressTimer;
    private SwitchCompat reminders;
    private ImageButton alarm;

    // Notification channel ID.
    public static final String PRIMARY_CHANNEL_ID =
            "primary_notification_channel";

    private final String MEDITATION_SET = "Set Meditation Timer";
    private final String MEDITATION_LENGTH = "Meditation Length";
    private final String REMINDER_SET = "Reminder Set";
    private final int MEDITATION_INTERVAL = 1;
    private final int MEDITATION_DAY_TIME = 2;

    private SharedPreferences mPreferences;
    private boolean isMeditating;
    private boolean isMusicPlaying;

    int progressStatus = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Get Shared preference settings
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        //Get buttons from layout view
        meditate = findViewById(R.id.meditate);
        final Button musicStartStop = findViewById(R.id.music);
        progressBar = findViewById(R.id.progressBar);
        progressTimer = findViewById(R.id.progressTime);
        reminders = findViewById(R.id.notificationToggle);
        alarm = findViewById(R.id.alarm);

        //Check if reminders are enabled, and set reminders button checked status
        if(mPreferences.getBoolean(REMINDER_SET, false)){
            reminders.setChecked(true);
        }
        else{
            reminders.setChecked(false);
        }

        //Check if meditation session is currently set, from savedInstanceState
        if(savedInstanceState != null) {
            if (savedInstanceState.getBoolean(MEDITATION_SET, false)) {
                savedInstanceState.putBoolean(MEDITATION_SET, true);
                meditate.setText(R.string.meditation_timer_set);
                isMeditating = true;
            } else {
                savedInstanceState.putBoolean(MEDITATION_SET, false);
                meditate.setText(R.string.meditation_timer_not_set);
                isMeditating = false;
            }
        }

        //Start notification channel service
        createNotificationChannel();

        //Show dialog box to remind user of app options
        showStartupDialog();

        //Toggle meditation session status
        meditate.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                progressStatus=0;
                progressBar.setProgress(0);
                if(!isMeditating) {
                    progressBarCountdown = ProgressBarCountdown();
                    meditate.setText(R.string.meditation_timer_set);
                    mPreferences.edit().putBoolean(MEDITATION_SET, true).apply();
                    progressBarCountdown.start();
                }
                else{
                    meditate.setText(R.string.meditation_timer_not_set);
                    mPreferences.edit().putBoolean(MEDITATION_SET, false).apply();
                    if(progressBarCountdown!=null) {
                        progressBarCountdown.cancel();
                    }
                    progressTimer.setText("Start Meditation Session");

                }
                isMeditating = !isMeditating;
            }
        });

        //Music in background Service Switch
        musicStartStop.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(!isMusicPlaying) {
                    startService(new Intent(MainActivity.this, BackgroundSound.class));
                    musicStartStop.setText(R.string.music_unset);
                }
                else{
                    stopService(new Intent(MainActivity.this, BackgroundSound.class));
                    musicStartStop.setText(R.string.music);

                }
                isMusicPlaying = !isMusicPlaying;
            }
        });

        alarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alarm.setVisibility(View.INVISIBLE);
                stopService(new Intent(MainActivity.this, BackgroundSound.class));
            }
        });

        //Notification Switch
        reminders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(reminders.isChecked()){
                    setTimeAlarm(MEDITATION_DAY_TIME);
                    setTimeAlarm(MEDITATION_INTERVAL);
                    mPreferences.edit().putBoolean(REMINDER_SET, true).apply();
                }
                else{
                    cancelTimeAlarm(MEDITATION_DAY_TIME);
                    cancelTimeAlarm(MEDITATION_INTERVAL);
                    mPreferences.edit().putBoolean(REMINDER_SET, false).apply();
                }
            }
        });

        //Google ads
        MobileAds.initialize(this, "ca-app-pub-2580444339985264~4603320181");
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    @Override
    protected void onDestroy(){
        mPreferences.edit().putBoolean(MEDITATION_SET, false).commit();
        super.onDestroy();
    }

    private void showStartupDialog() {
        FragmentManager fm = getSupportFragmentManager();
        StartupFragment startupFragment = StartupFragment.newInstance("Some Title");
        startupFragment.show(fm, "fragment_edit_name");
    }

    //Countdown timer that updates progress bar on page
    private CountDownTimer ProgressBarCountdown() {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int meditationLengthStored = mPreferences.getInt(MEDITATION_LENGTH, 15);
        final int meditationLength = (meditationLengthStored * (60 * 1000));
        //Convert meditation length to seconds
        return new CountDownTimer( meditationLength, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                progressStatus+=1;
                progressBar.setProgress((int)progressStatus*100/(meditationLength/1000));
                progressTimer.setText((millisUntilFinished/1000)+" seconds left!");
            }

            @Override
            public void onFinish() {
                meditate.setText(R.string.meditation_timer_not_set);
                progressTimer.setText("You are finished for this session!");
                alarm.setVisibility(View.VISIBLE);
                startService(new Intent(MainActivity.this, BackgroundSound.class));
                isMeditating = !isMeditating;
            }
        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        //MenuItem item = menu.findItem(R.id.switchForActionBar);
        //item.setActionView(R.layout.switch_layout);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(MainActivity.this, SettingsPrefActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void setTimeAlarm(int broadcastId) {
        //Setting the time alarm for how long a notification should come up for a reminder (Set by hours)
        int prefTime= mPreferences.getInt("Meditation Timer", 24);
        long timeInterval  = (prefTime * 3600000);
        Toast.makeText(getApplicationContext(),"Set timer for meditation session",Toast.LENGTH_SHORT).show();
        Intent notifyIntent = new Intent(this, AlarmReceiver.class);
        notifyIntent.putExtra(AlarmReceiver.NOTIFICATION_ID, broadcastId);
        notifyIntent.putExtra(AlarmReceiver.NOTIFICATION, "A reminder that you set a timer to meditate.");
        PendingIntent notifyPendingIntent = PendingIntent.getBroadcast
                (this, broadcastId, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.setInexactRepeating
                (AlarmManager.RTC_WAKEUP,
                        System.currentTimeMillis() + timeInterval, timeInterval, notifyPendingIntent);
    }

    public void cancelTimeAlarm(int broadcastId) {
        //Check which broadcast notification ID you are cancelling
        Toast.makeText(getApplicationContext(),"Canceled timer for meditation reminder",
                Toast.LENGTH_SHORT).show();
        Intent notifyIntent = new Intent(this, AlarmReceiver.class);
        notifyIntent.putExtra(AlarmReceiver.NOTIFICATION_ID, broadcastId);
        PendingIntent cancelPendingIntent= PendingIntent.getBroadcast
                (this, broadcastId, notifyIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        cancelPendingIntent.cancel();
        alarmManager.cancel(cancelPendingIntent);
    }

    /**
     * Creates a Notification channel, for OREO and higher.
     */
    public void createNotificationChannel() {

        // Create a notification manager object.
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Notification channels are only available in OREO and higher.
        // So, add a check on SDK version.
        if (android.os.Build.VERSION.SDK_INT >=
                android.os.Build.VERSION_CODES.O) {

            // Create the NotificationChannel with all the parameters.
            NotificationChannel notificationChannel = new NotificationChannel
                    (PRIMARY_CHANNEL_ID,
                            "Meditation Notification",
                            NotificationManager.IMPORTANCE_HIGH);

            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationChannel.setDescription
                    ("Notifies user to meditate.");
            mNotificationManager.createNotificationChannel(notificationChannel);
        }
    }
}
