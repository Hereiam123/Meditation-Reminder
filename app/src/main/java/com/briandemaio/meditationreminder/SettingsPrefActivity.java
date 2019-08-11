package com.briandemaio.meditationreminder;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;

import java.sql.Time;

public class SettingsPrefActivity extends AppCompatPreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Load Settings Fragment
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MainPreferenceFragment()).commit();
    }

    public static class MainPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_main);

            //Meditation settings
            bindPreferenceSummaryToValue(findPreference(getString(R.string.meditation_length)));

            bindPreferenceSummaryToValue(findPreference(getString(R.string.meditation_timer)));

            bindPreferenceSummaryToValue(findPreference(getString(R.string.time_reminder)));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private static void bindPreferenceSummaryToValue(Preference preference) {
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
        if (preference instanceof TimePreference) {
            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                    PreferenceManager
                            .getDefaultSharedPreferences(preference.getContext())
                            .getString(preference.getKey(), "8:00"));
        }
        else {
            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                    PreferenceManager
                            .getDefaultSharedPreferences(preference.getContext())
                            .getInt(preference.getKey(), 0));
        }
    }

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String stringValue = newValue.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            }
            else if (preference instanceof TimePreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                String[] time = stringValue.split(":");
                String hour = time[0];
                String minute = time[1];
                String timeOfDay;

                //Need to parse out hour and minute values, as what is returned from the TimePreference
                //is military time, or minute values are only single digits if 1-9
                if(Integer.parseInt(hour)>12){
                    hour = String.valueOf(Integer.parseInt(hour) - 12);
                    timeOfDay = "PM";
                }
                else if(Integer.parseInt(hour)==12 || Integer.parseInt(hour)==0){
                    hour = "12";
                    timeOfDay = "PM";
                    if(Integer.parseInt(hour)==0){
                        timeOfDay="AM";
                    }
                }
                else{
                    timeOfDay = "AM";
                }

                if(Integer.parseInt(minute)<10){
                    minute = "0"+minute;
                }

                preference.setSummary(hour+":"+minute+timeOfDay);
            }
            else if (preference instanceof EditTextPreference) {
                if (preference.getKey().equals("key_gallery_name")) {

                    // update the changed gallery name to summary filed
                    preference.setSummary(stringValue);
                }
            } else {
                preference.setSummary(stringValue);
            }
            return true;
        }
    };
}

