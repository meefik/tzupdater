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

    /**
     * Get application version
     *
     * @param c context
     * @return version, format versionName-versionCode
     */
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

    /**
     * Get external storage path
     *
     * @return path
     */
    public static String getStorage() {
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    /**
     * Get environment directory
     *
     * @param c context
     * @return path, e.g. /data/data/com.example.app/files
     */
    public static String getEnvDir(Context c) {
        return c.getFilesDir().getAbsolutePath();
    }

    /**
     * Get language code
     *
     * @param c context
     * @return language code, e.g. "en"
     */
    public static String getLanguage(Context c) {
        SharedPreferences pref = c.getSharedPreferences(APP_PREF_NAME, Context.MODE_PRIVATE);
        String language = pref.getString("language", c.getString(R.string.language));
        if (language.length() == 0) {
            String countryCode = Locale.getDefault().getLanguage();
            switch (countryCode) {
                case "ru":
                    language = countryCode;
                    break;
                default:
                    language = "en";
            }
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("language", language);
            editor.apply();
        }
        return language;
    }

    /**
     * Get application theme resource id
     *
     * @param c context
     * @return resource id
     */
    public static int getTheme(Context c) {
        SharedPreferences pref = c.getSharedPreferences(APP_PREF_NAME, Context.MODE_PRIVATE);
        String theme = pref.getString("theme", c.getString(R.string.theme));
        int themeId = R.style.DarkTheme;
        switch (theme) {
            case "dark":
                themeId = R.style.DarkTheme;
                break;
            case "light":
                themeId = R.style.LightTheme;
                break;
        }
        return themeId;
    }

    /**
     * Get font size
     *
     * @param c context
     * @return font size
     */
    public static int getFontSize(Context c) {
        SharedPreferences pref = c.getSharedPreferences(APP_PREF_NAME, Context.MODE_PRIVATE);
        return Integer.parseInt(pref.getString("fontsize", c.getString(R.string.fontsize)));
    }

    /**
     * Get maximum limit to scroll
     *
     * @param c context
     * @return number of lines
     */
    public static int getMaxLines(Context c) {
        SharedPreferences pref = c.getSharedPreferences(APP_PREF_NAME, Context.MODE_PRIVATE);
        return Integer.parseInt(pref.getString("maxlines", c.getString(R.string.maxlines)));
    }

    /**
     * Debug mode is enabled
     *
     * @param c context
     * @return true if enabled
     */
    public static boolean isDebugMode(Context c) {
        SharedPreferences pref = c.getSharedPreferences(APP_PREF_NAME, Context.MODE_PRIVATE);
        return pref.getBoolean("debug", c.getString(R.string.debug).equals("true"));
    }

    /**
     * Trace mode is enabled
     *
     * @param c context
     * @return true if enabled
     */
    public static boolean isTraceMode(Context c) {
        SharedPreferences pref = c.getSharedPreferences(APP_PREF_NAME, Context.MODE_PRIVATE);
        return pref.getBoolean("trace", c.getString(R.string.trace).equals("true"));
    }

    /**
     * Logging is enabled
     *
     * @param c context
     * @return true if enabled
     */
    public static boolean isLogger(Context c) {
        SharedPreferences pref = c.getSharedPreferences(APP_PREF_NAME, Context.MODE_PRIVATE);
        return pref.getBoolean("logger", c.getString(R.string.logger).equals("true"));
    }

    /**
     * Get path of log file
     *
     * @param c context
     * @return path
     */
    public static String getLogFile(Context c) {
        SharedPreferences pref = c.getSharedPreferences(APP_PREF_NAME, Context.MODE_PRIVATE);
        String logFile = pref.getString("logfile", c.getString(R.string.logfile));
        if (logFile.length() == 0) {
            logFile = getStorage() + "/tzupdater.log";
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("logfile", logFile);
            editor.apply();
        }
        return logFile;
    }

    /**
     * Get hardware architecture
     *
     * @param arch unformated architecture
     * @return intel, arm or mips
     */
    public static String getArch(String arch) {
        String march = "";
        if (arch.length() > 0) {
            char a = arch.toLowerCase().charAt(0);
            switch (a) {
                case 'a':
                    if (arch.equals("amd64"))
                        march = "intel";
                    else
                        march = "arm";
                    break;
                case 'm':
                    march = "mips";
                    break;
                case 'i':
                case 'x':
                    march = "intel";
                    break;
            }
        }
        return march;
    }

    /**
     * Set application locale
     *
     * @param c context
     */
    public static void setLocale(Context c) {
        String language = getLanguage(c);
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        c.getResources().updateConfiguration(config, c.getResources().getDisplayMetrics());
    }

}