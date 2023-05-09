package ru.meefik.tzupdater;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Environment;

import java.util.Locale;
import java.util.Objects;

public class PrefStore {

    static final String APP_PREF_NAME = "app_settings";

    /**
     * Get application version
     *
     * @param c context
     * @return version, format versionName-versionCode
     */
    static String getVersion(Context c) {
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
    static String getStorage() {
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    /**
     * Get environment directory
     *
     * @param c context
     * @return path, e.g. /data/data/com.example.app/files
     */
    static String getFilesDir(Context c) {
        return c.getFilesDir().getAbsolutePath();
    }

    /**
     * Get language code
     *
     * @param c context
     * @return language code, e.g. "en"
     */
    static String getLanguage(Context c) {
        SharedPreferences pref = c.getSharedPreferences(APP_PREF_NAME, Context.MODE_PRIVATE);
        String language = pref.getString("language", c.getString(R.string.language));
        if (language.isEmpty()) {
            String countryCode = Locale.getDefault().getLanguage();
            switch (countryCode) {
                case "de":
                case "fr":
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
    static int getTheme(Context c) {
        SharedPreferences pref = c.getSharedPreferences(APP_PREF_NAME, Context.MODE_PRIVATE);
        String theme = pref.getString("theme", c.getString(R.string.theme));
        return Objects.equals(theme, "light") ? R.style.LightTheme : R.style.DarkTheme;
    }

    /**
     * Get font size
     *
     * @param c context
     * @return font size
     */
    static int getFontSize(Context c) {
        int fontSizeInt;
        SharedPreferences pref = c.getSharedPreferences(APP_PREF_NAME, Context.MODE_PRIVATE);
        String fontSize = pref.getString("fontsize", c.getString(R.string.fontsize));
        try {
            fontSizeInt = Integer.parseInt(fontSize);
        } catch (Exception e) {
            fontSize = c.getString(R.string.fontsize);
            fontSizeInt = Integer.parseInt(fontSize);
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("fontsize", fontSize);
            editor.apply();
        }
        return fontSizeInt;
    }

    /**
     * Get maximum limit to scroll
     *
     * @param c context
     * @return number of lines
     */
    static int getMaxLines(Context c) {
        int maxLinesInt;
        SharedPreferences pref = c.getSharedPreferences(APP_PREF_NAME, Context.MODE_PRIVATE);
        String maxLines = pref.getString("maxlines", c.getString(R.string.maxlines));
        try {
            maxLinesInt = Integer.parseInt(maxLines);
        } catch (Exception e) {
            maxLines = c.getString(R.string.maxlines);
            maxLinesInt = Integer.parseInt(maxLines);
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("maxlines", maxLines);
            editor.apply();
        }
        return maxLinesInt;
    }

    /**
     * Timestamp is enabled
     *
     * @param c context
     * @return true if enabled
     */
    static boolean isTimestamp(Context c) {
        SharedPreferences pref = c.getSharedPreferences(APP_PREF_NAME, Context.MODE_PRIVATE);
        return pref.getBoolean("timestamp", c.getString(R.string.timestamp).equals("true"));
    }

    /**
     * Debug mode is enabled
     *
     * @param c context
     * @return true if enabled
     */
    static boolean isDebugMode(Context c) {
        SharedPreferences pref = c.getSharedPreferences(APP_PREF_NAME, Context.MODE_PRIVATE);
        return pref.getBoolean("debug", c.getString(R.string.debug).equals("true"));
    }

    /**
     * Trace mode is enabled
     *
     * @param c context
     * @return true if enabled
     */
    static boolean isTraceMode(Context c) {
        SharedPreferences pref = c.getSharedPreferences(APP_PREF_NAME, Context.MODE_PRIVATE);
        return pref.getBoolean("debug", c.getString(R.string.debug).equals("true")) &&
                pref.getBoolean("trace", c.getString(R.string.trace).equals("true"));
    }

    /**
     * Logging is enabled
     *
     * @param c context
     * @return true if enabled
     */
    static boolean isLogger(Context c) {
        SharedPreferences pref = c.getSharedPreferences(APP_PREF_NAME, Context.MODE_PRIVATE);
        return pref.getBoolean("logger", c.getString(R.string.logger).equals("true"));
    }

    /**
     * Get path of log file
     *
     * @param c context
     * @return path
     */
    static String getLogFile(Context c) {
        SharedPreferences pref = c.getSharedPreferences(APP_PREF_NAME, Context.MODE_PRIVATE);
        String logFile = pref.getString("logfile", c.getString(R.string.logfile));
        if (logFile.isEmpty()) {
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
     * @return arm, arm_64, x86, x86_64
     */
    private static String getArch(String arch) {
        String march = "unknown";
        if (arch.length() > 0) {
            char a = arch.toLowerCase().charAt(0);
            switch (a) {
                case 'a':
                    if (arch.equals("amd64")) march = "x86_64";
                    else if (arch.contains("64")) march = "arm64";
                    else march = "arm";
                    break;
                case 'i':
                case 'x':
                    if (arch.contains("64")) march = "x86_64";
                    else march = "x86";
                    break;
                case 'm':
                    if (arch.contains("64")) march = "mips64";
                    else march = "mips";
                    break;
            }
        }
        return march;
    }

    /**
     * Get current hardware architecture
     *
     * @return arm, arm_64, x86, x86_64
     */
    static String getArch() {
        return getArch(Objects.requireNonNull(System.getProperty("os.arch")));
    }

    /**
     * Set application locale
     *
     * @param c context
     */
    static void setLocale(Context c) {
        String language = getLanguage(c);
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        c.getResources().updateConfiguration(config, c.getResources().getDisplayMetrics());
    }

}
