package ru.meefik.tzupdater;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Environment;

import java.util.Locale;

/**
 * Created by anton on 18.09.15.
 */
public class PrefStore {
    public static final String APP_PREF_NAME = "app_settings";
    public static String ENV_DIR;
    public static String THEME;
    public static String LANGUAGE;
    public static Integer FONT_SIZE;
    public static Integer MAX_LINES;
    public static Boolean LOGGER;
    public static String LOG_FILE;
    public static Boolean DEBUG_MODE;
    public static Boolean TRACE_MODE;
    public static String VERSION;
    public static String EXTERNAL_STORAGE;

    // application version
    public static String getVersion(Context c) {
        String version = "";
        try {
            PackageInfo pi = c.getPackageManager().getPackageInfo(c.getPackageName(), 0);
            version = pi.versionName + "-" + pi.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return version;
    }

    // multilanguage support
    public static void updateLocale(Context c) {
        Locale locale = new Locale(LANGUAGE);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        c.getResources().updateConfiguration(config, c.getResources().getDisplayMetrics());
    }

    // themes support
    public static void updateTheme(Context c) {
        switch (THEME) {
            case "dark":
                c.setTheme(R.style.DarkTheme);
                break;
            case "light":
                c.setTheme(R.style.LightTheme);
                break;
        }
    }

    // refresh configuration
    public static void refresh(Context c) {
        SharedPreferences pref = c.getSharedPreferences(APP_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        EXTERNAL_STORAGE = Environment.getExternalStorageDirectory().getAbsolutePath();
        ENV_DIR = c.getFilesDir().getAbsolutePath();
        VERSION = getVersion(c);
        LANGUAGE = pref.getString("language", c.getString(R.string.language));
        if (LANGUAGE.isEmpty()) {
            String countryCode = Locale.getDefault().getLanguage();
            switch (countryCode) {
                case "ru":
                    LANGUAGE = countryCode;
                    break;
                default:
                    LANGUAGE = "en";
            }
            editor.putString("language", LANGUAGE);
        }
        updateLocale(c);
        THEME = pref.getString("theme", c.getString(R.string.theme));
        updateTheme(c);
        FONT_SIZE = Integer.parseInt(pref.getString("fontsize", c.getString(R.string.fontsize)));
        MAX_LINES = Integer.parseInt(pref.getString("maxlines", c.getString(R.string.maxlines)));
        DEBUG_MODE = pref.getBoolean("debug", c.getString(R.string.debug).equals("true"));
        TRACE_MODE = pref.getBoolean("trace", c.getString(R.string.trace).equals("true"));
        LOGGER = pref.getBoolean("logger", c.getString(R.string.logger).equals("true"));
        LOG_FILE = pref.getString("logfile", c.getString(R.string.logfile));
        if (LOG_FILE.isEmpty()) {
            LOG_FILE = EXTERNAL_STORAGE + "/tzupdater.log";
            editor.putString("logfile", LOG_FILE);
            editor.commit();
        }
    }
}
