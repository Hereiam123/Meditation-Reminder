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
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
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

    // Notification channel ID.
    public static final String PRIMARY_CHANNEL_ID =
            "primary_notification_channel";

    private final String MEDITATION_SET = "Set Meditation Timer";
    private final String ALARM_CHANNEL = "Meditate";
    private final String MEDITATION_LENGTH = "Meditation Length";

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

        meditate = findViewById(R.id.meditate);
        final Button musicStartStop = findViewById(R.id.music);
        progressBar = findViewById(R.id.progressBar);
        progressTimer = findViewById(R.id.progressTime);

        //Get settings values
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        //Check if meditation session is currently set
        if(mPreferences.getBoolean( MEDITATION_SET, false)){
            meditate.setText(R.string.meditation_timer_set);
            isMeditating=true;
        }

        //Start notification channel service
        createNotificationChannel();

        //Show dialog box to remind user of app options
        showEditDialog();

        //Toggle meditation session status
        meditate.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(!isMeditating) {
                    progressBarCountdown = ProgressBarCountdown();
                    setTimeAlarm(ALARM_CHANNEL);
                    meditate.setText(R.string.meditation_timer_set);
                    mPreferences.edit().putBoolean(MEDITATION_SET, true).apply();
                    progressBarCountdown.start();
                }
                else{
                    cancelTimeAlarm(ALARM_CHANNEL);
                    meditate.setText(R.string.meditation_timer_not_set);
                    mPreferences.edit().putBoolean(MEDITATION_SET, false).apply();
                    progressBarCountdown.cancel();
                    progressStatus=0;
                    progressBar.setProgress(0);
                    progressTimer.setText("Start Meditation Session");

                }
                isMeditating = !isMeditating;
            }
        });

        //Music in background Service
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

        //Google ads
        MobileAds.initialize(this, "ca-app-pub-2580444339985264~4603320181");
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    private void showEditDialog() {
        FragmentManager fm = getSupportFragmentManager();
        FirstTimeFragment editNameDialogFragment = FirstTimeFragment.newInstance("Some Title");
        editNameDialogFragment.show(fm, "fragment_edit_name");
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
                Log.w("Log_tag:","The progress " + progressStatus);
                progressBar.setProgress((int)progressStatus*100/(meditationLength/1000));
                progressTimer.setText((millisUntilFinished/1000)+" seconds left!");
            }

            @Override
            public void onFinish() {
                Log.w("Log_tag:","The progress is finished");
                meditate.setText(R.string.meditation_timer_not_set);
                progressTimer.setText("You are finished for this session!");
                isMeditating = !isMeditating;
            }
        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    public void setTimeAlarm(String setting) {
        //Setting the time alarm for how long a notification should come up for a reminder (Set by hours)
        int prefTime= mPreferences.getInt("Meditation Timer", 24);
        long timeInterval  = (prefTime * 3600000);

        //Check which broadcast notification ID you are setting
        int broadcastId = 1;

        /*if(leftOrRight.equals("Left")){
            broadcastId = 2;
            prefTime= mPreferences.getInt("num_2", 3);
        }*/

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

    public void cancelTimeAlarm(String setting) {

        //Check which broadcast notification ID you are cancelling
        int broadcastId = 1;
        /*if(leftOrRight.equals("Left")){
            broadcastId = 2;
        }*/
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
