package ru.meefik.tzupdater;

import android.os.Handler;
import android.os.Looper;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by anton on 19.09.15.
 */
public class Logger {

    private static final Handler outputUpdater = new Handler(Looper.getMainLooper());
    private static volatile List<String> protocol = new ArrayList<>();
    private static boolean fragment = false, timestamp = false;

    private static String getTimeStamp() {
        if (timestamp)
            return "[" + new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH).format(new Date()) + "] ";
        else
            return "";
    }

    private static void appendMessage(final String msg) {
        final int msgLength = msg.length();
        if (msgLength > 0) {
            outputUpdater.post(new Runnable() {
                public void run() {
                    String[] tokens = msg.split("\\n");
                    int lastIndex = protocol.size() - 1;
                    for (int i = 0, l = tokens.length; i < l; i++) {
                        // update last record from List if fragment
                        if (i == 0 && fragment && lastIndex >= 0) {
                            String last = protocol.get(lastIndex);
                            protocol.set(lastIndex, last + tokens[i]);
                            continue;
                        }
                        // add the message to List
                        protocol.add(getTimeStamp() + tokens[i]);
                        // remove first line if overflow
                        if (protocol.size() > PrefStore.MAX_LINES)
                            protocol.remove(0);
                    }
                    // set fragment
                    fragment = (msg.charAt(msgLength - 1) != '\n');
                    // show log
                    MainActivity.showLog(get());
                    // save the message to file
                    if (PrefStore.LOGGER) {
                        saveToFile(msg);
                    }
                }
            });
        }
    }

    private static void saveToFile(String msg) {
        byte[] data = msg.getBytes();
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(PrefStore.LOG_FILE, true);
            fos.write(data);
            fos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void setTimestamp(boolean ts) {
        timestamp = ts;
    }

    public static void clear() {
        protocol.clear();
        fragment = false;
    }

    public static String get() {
        return android.text.TextUtils.join("\n", protocol);
    }

    public static void log(String msg) {
        appendMessage(msg);
    }

    public static void log(InputStream stream) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(stream));
            int n;
            char[] buffer = new char[1024];
            while ((n = reader.read(buffer)) != -1) {
                String msg = String.valueOf(buffer, 0, n);
                appendMessage(msg);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}