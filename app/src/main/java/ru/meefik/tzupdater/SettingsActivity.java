package ru.meefik.tzupdater;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by anton on 19.09.15.
 */
public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        PrefStore.updateTheme(this);
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new MyPreferenceFragment()).commit();
    }

    public static class MyPreferenceFragment extends PreferenceFragment implements
            SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceClickListener {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            getPreferenceManager().setSharedPreferencesName(PrefStore.APP_PREF_NAME);
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
            }

            if (pref instanceof ListPreference) {
                ListPreference listPref = (ListPreference) pref;
                pref.setSummary(listPref.getEntry());
            }
        }
    }

}