package com.briandemaio.bpump;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    // Notification channel ID.
    private static final String PRIMARY_CHANNEL_ID =
            "primary_notification_channel";

    private SharedPreferences mPreferences;

    private boolean isLeftSet;
    private boolean isRightSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final Button left = findViewById(R.id.button2);
        Button right = findViewById(R.id.button1);

        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        createNotificationChannel();
        showEditDialog();


        left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isLeftSet) {
                    setTimeAlarm("Left");
                    left.setText('Left Breast Timer Set');
                }
                else{
                    cancelTimeAlarm("Left");
                }
            }
        });

        right.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                setItemTimeAlarm("Right");
            }
        });
    }

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
        long timeInterval = (mPreferences.getInt("keys_num_1", 3) * 3600000)+ System.currentTimeMillis();
        int broadcastId = 1;
        if(leftOrRight == "Left"){
            broadcastId = 2;
        }
        Toast.makeText(getApplicationContext(),"Set timer for "+leftOrRight+" on id "+broadcastId,
                Toast.LENGTH_LONG).show();
        Intent notifyIntent = new Intent(this, AlarmReceiver.class);
        notifyIntent.putExtra(AlarmReceiver.NOTIFICATION_ID, broadcastId);
        notifyIntent.putExtra(AlarmReceiver.NOTIFICATION, "A reminder that you set a timer to pump "+leftOrRight);
        PendingIntent notifyPendingIntent = PendingIntent.getBroadcast
                (this, broadcastId, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, timeInterval
                , notifyPendingIntent);
    }

    public void cancelTimeAlarm(String leftOrRight) {
        int broadcastId = 1;
        if(leftOrRight == "Left"){
            broadcastId = 2;
        }
        Toast.makeText(getApplicationContext(),"Set timer for "+leftOrRight+" on id "+broadcastId,
                Toast.LENGTH_LONG).show();
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
