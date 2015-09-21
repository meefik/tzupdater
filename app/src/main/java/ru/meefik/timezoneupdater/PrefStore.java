package ru.meefik.timezoneupdater;

import android.content.Context;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by anton on 18.09.15.
 */
public class PrefStore {
    public static Integer MAX_LINE = 100;
    public static Boolean LOGGING = false;
    public static String LOG_FILE = "";
    public static String ENV_DIR = "";
    public static Boolean DEBUG_MODE = false;
    public static Boolean TRACE_MODE = false;
    public static String MARCH = "";
    public static String VERSION = "";

    public static void get(Context c) {
        //c.getFilesDir().getAbsolutePath()
    }


}
