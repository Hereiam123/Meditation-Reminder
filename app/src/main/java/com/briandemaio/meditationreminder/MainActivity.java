package com.briandemaio.meditationreminder;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

public class MainActivity extends AppCompatActivity {

    private AdView mAdView;

    // Notification channel ID.
    public static final String PRIMARY_CHANNEL_ID =
            "primary_notification_channel";

    private final String MEDITATION_SET = "Set Meditation Timer";
    private final String ALARM_CHANNEL = "Meditate";

    private SharedPreferences mPreferences;
    private boolean isMeditating;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final Button meditate = findViewById(R.id.button1);

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
                    setTimeAlarm(ALARM_CHANNEL);
                    meditate.setText(R.string.meditation_timer_set);
                    mPreferences.edit().putBoolean(MEDITATION_SET, true).apply();
                    isMeditating=true;
                }
                else{
                    cancelTimeAlarm(ALARM_CHANNEL);
                    meditate.setText(R.string.meditation_timer_not_set);
                    mPreferences.edit().putBoolean(MEDITATION_SET, false).apply();
                    isMeditating=false;
                }
            }
        });

        //Music in background Service
        startService(new Intent(MainActivity.this, BackgroundSound.class));

        //Google ads
        MobileAds.initialize(this, "ca-app-pub-2580444339985264~4603320181");
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    /*@Override
    public void onResume() {
        super.onResume();
        mBackgroundSound.execute();
    }*/

    /*@Override
    public void onPause(){
        super.onPause();
        stopService(new Intent(MainActivity.this, BackgroundSound.class));
    }

    @Override
    public void onStop(){
        super.onStop();
        stopService(new Intent(MainActivity.this, BackgroundSound.class));
    }*/

    private void showEditDialog() {
        FragmentManager fm = getSupportFragmentManager();
        FirstTimeFragment editNameDialogFragment = FirstTimeFragment.newInstance("Some Title");
        editNameDialogFragment.show(fm, "fragment_edit_name");
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

    public void setTimeAlarm(String leftOrRight) {
        int prefTime= mPreferences.getInt("num_1", 3);
        long timeInterval  = (prefTime * 3600000);
        int broadcastId = 1;

        if(leftOrRight.equals("Left")){
            broadcastId = 2;
            prefTime= mPreferences.getInt("num_2", 3);
        }

        Toast.makeText(getApplicationContext(),"Set timer for "+leftOrRight+" Breast, for "+
                       prefTime + " hours",
                Toast.LENGTH_SHORT).show();
        Intent notifyIntent = new Intent(this, AlarmReceiver.class);
        notifyIntent.putExtra(AlarmReceiver.NOTIFICATION_ID, broadcastId);
        notifyIntent.putExtra(AlarmReceiver.NOTIFICATION, "A reminder that you set a timer to pump "+leftOrRight);
        PendingIntent notifyPendingIntent = PendingIntent.getBroadcast
                (this, broadcastId, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.setInexactRepeating
                (AlarmManager.RTC_WAKEUP,
                        System.currentTimeMillis() + timeInterval, timeInterval, notifyPendingIntent);
    }

    public void cancelTimeAlarm(String leftOrRight) {
        int broadcastId = 1;
        if(leftOrRight.equals("Left")){
            broadcastId = 2;
        }
        Toast.makeText(getApplicationContext(),"Canceled timer for "+leftOrRight+" Breast",
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
                            "Pump Breast Milk Notification",
                            NotificationManager.IMPORTANCE_HIGH);

            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationChannel.setDescription
                    ("Notifies user to pump breat milk");
            mNotificationManager.createNotificationChannel(notificationChannel);
        }
    }
}
