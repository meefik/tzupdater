package ru.meefik.timezoneupdater;

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

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

public class Logger {

    private volatile List<String> protocol = new ArrayList<>();
    private boolean fragment = false;
    private final Handler outputUpdater = new Handler(Looper.getMainLooper());
    private Activity activity;
    private boolean timestamp;

    public Logger(Activity activity, boolean timestamp) {
        this.activity = activity;
        this.timestamp = timestamp;
    }

    private String getTimeStamp() {
        if (timestamp)
            return "[" + new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH).format(new Date()) + "] ";
        else
            return "";
    }

    private void appendMessage(final String msg) {
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
                        if (protocol.size() > PrefStore.MAX_LINE)
                            protocol.remove(0);
                    }
                    // set fragment
                    fragment = (msg.charAt(msgLength - 1) != '\n');
                    // show log
                    show();
                    // save the message to file
                    if (PrefStore.LOGGING) {
                        saveToFile(msg);
                    }
                }
            });
        }
    }

    private void saveToFile(String msg) {
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

    public void show() {
        final TextView output = (TextView) activity.findViewById(R.id.outputView);
        final ScrollView scroll = (ScrollView) activity.findViewById(R.id.scrollView);
        // show log in TextView
        output.post(new Runnable() {
            @Override
            public void run() {
                output.setText(get());
                // scroll TextView to bottom
                scroll.post(new Runnable() {
                    @Override
                    public void run() {
                        scroll.fullScroll(View.FOCUS_DOWN);
                        scroll.clearFocus();
                    }
                });
            }
        });
    }

    public void clear() {
        protocol.clear();
        fragment = false;
        final TextView output = (TextView) activity.findViewById(R.id.outputView);
        output.setText("");
    }

    public String get() {
        return android.text.TextUtils.join("\n", protocol);
    }

    public void log(String msg) {
        appendMessage(msg);
    }

    public void log(InputStream stream) {
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