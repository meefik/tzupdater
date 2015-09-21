package ru.meefik.timezoneupdater;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;

import java.io.File;

public class SettingsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new MyPreferenceFragment()).commit();
    }

    public static class MyPreferenceFragment extends PreferenceFragment implements
            SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceClickListener {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings);
            initSummaries(getPreferenceScreen());
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            return false;
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Preference pref = findPreference(key);
            setSummary(pref, true);
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        }

        @Override
        public void onPause() {
            getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
            super.onPause();
        }

        private void initSummaries(PreferenceGroup pg) {
            for (int i = 0; i < pg.getPreferenceCount(); ++i) {
                Preference p = pg.getPreference(i);
                if (p instanceof PreferenceGroup)
                    this.initSummaries((PreferenceGroup) p);
                else
                    this.setSummary(p, false);
                if (p instanceof PreferenceScreen)
                    p.setOnPreferenceClickListener(this);
            }
        }

        private void setSummary(Preference pref, boolean init) {
            if (pref instanceof EditTextPreference) {
                EditTextPreference editPref = (EditTextPreference) pref;
                pref.setSummary(editPref.getText());

                if (editPref.getKey().equals("logfile") && editPref.getText().isEmpty()) {
                    File extStore = Environment.getExternalStorageDirectory();
                    String logFile = extStore.getAbsolutePath() + "/tzupdater.log";
                    ((EditTextPreference) pref).setText(logFile);
                    ((EditTextPreference) pref).setSummary(logFile);
                }
            }

            if (pref instanceof ListPreference) {
                ListPreference listPref = (ListPreference) pref;
                pref.setSummary(listPref.getEntry());
            }
        }
    }

}