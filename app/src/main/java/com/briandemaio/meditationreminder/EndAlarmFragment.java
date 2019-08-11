package com.briandemaio.meditationreminder;

import android.media.MediaPlayer;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class EndAlarmFragment extends DialogFragment {

    MediaPlayer alarm;

    public EndAlarmFragment() {
        // Empty constructor is required for DialogFragment
        // Make sure not to add arguments to the constructor
        // Use `newInstance` instead as shown below
    }

    public static EndAlarmFragment newInstance(String title) {
        EndAlarmFragment frag = new EndAlarmFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.main_activity_reminder, container);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        alarm = MediaPlayer.create(getContext(), R.raw.bell);
        alarm.setLooping(true);
        alarm.start();
        Button dismissButton = view.findViewById(R.id.first_time_dialog_ok_button);
        dismissButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alarm.stop();
                alarm.release();
                alarm = null;
                getDialog().dismiss();
            }
        });
    }
}

