package com.briandemaio.meditationreminder;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class BackgroundSound extends Service implements MediaPlayer.OnPreparedListener{
    MediaPlayer mediaPlayer;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String audioChoice = intent.getStringExtra("Music Choice");

        //Don't plan on adding more audio tracks right now, so...
        switch (audioChoice) {
            case "Ambient Universe":
                mediaPlayer = MediaPlayer.create(this, R.raw.ambient_universe);
                break;
            case "Deep Meditation":
                mediaPlayer = MediaPlayer.create(this, R.raw.deep_meditation_om);
                break;
            case "Lucid Tones":
                mediaPlayer = MediaPlayer.create(this, R.raw.lucid_toads);
                break;
            case "Piano Lullaby":
                mediaPlayer = MediaPlayer.create(this, R.raw.piano_lullabynowm);
                break;
            default:
                mediaPlayer = MediaPlayer.create(this, R.raw.lucid_toads);
                break;
        }

        mediaPlayer.setOnPreparedListener(this);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public boolean stopService(Intent name) {
        return super.stopService(name);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = null;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mediaPlayer.setLooping(true);
        mediaPlayer.start();
    }
}
