package ru.meefik.tzupdater;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class MainActivity extends BaseActivity {

    public static TextView output;
    public static ScrollView scroll;

    /**
     * Show message in TextView, used from Logger
     *
     * @param log message
     */
    public static void showLog(final String log) {
        // show log in TextView
        output.post(() -> {
            output.setText(log);
            // scroll TextView to bottom
            scroll.post(() -> {
                scroll.fullScroll(View.FOCUS_DOWN);
                scroll.clearFocus();
            });
        });
    }

    public static String getTimeZone() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"), Locale.ENGLISH);
        String timeZone = new SimpleDateFormat("Z", Locale.ENGLISH).format(calendar.getTime());
        String zone = TimeZone.getDefault().getID();
        return zone + " (" + timeZone.substring(0, 3) + ":" + timeZone.substring(3, 5) + ")";
    }

    public static String getTzVersion() {
        String tzVersion = "???";
        String[] arr = {
                "/data/misc/zoneinfo/tzdata",
                "/system/usr/share/zoneinfo/tzdata",
                "/system/usr/share/zoneinfo/zoneinfo.version"
        };
        for (String tzdata : arr) {
            FileInputStream fis = null;
            try {
                File file = new File(tzdata);
                if (!file.exists()) continue;
                fis = new FileInputStream(file);
                byte[] buffer = new byte[5];
                if (file.getName().equals("tzdata")) fis.skip(6);
                fis.read(buffer);
                tzVersion = new String(buffer, "UTF-8");
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (tzVersion.length() == 5) break;
        }
        return tzVersion;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scroll = findViewById(R.id.scrollView);
        output = findViewById(R.id.outputView);
        // enable context clickable
        output.setMovementMethod(LinkMovementMethod.getInstance());

        final Button button = findViewById(R.id.button);
        final CheckBox checkTzData = findViewById(R.id.tzdataUpdate);
        final CheckBox checkIcu = findViewById(R.id.icuUpdate);
        checkTzData.setOnCheckedChangeListener((view, isChecked) -> button.setEnabled(isChecked || checkIcu.isChecked()));
        checkIcu.setOnCheckedChangeListener((view, isChecked) -> button.setEnabled(isChecked || checkTzData.isChecked()));
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intentSettings = new Intent(this, SettingsActivity.class);
                startActivity(intentSettings);
                break;
            case R.id.action_help:
                Logger.clear();
                showHelp();
                break;
            case R.id.action_about:
                Intent intentAbout = new Intent(this, AboutActivity.class);
                startActivity(intentAbout);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        // restore font size
        output.setTextSize(TypedValue.COMPLEX_UNIT_SP, PrefStore.getFontSize(this));
        // restore logs
        String log = Logger.get();
        if (log.length() == 0) {
            // show help if empty
            showHelp();
        } else {
            showLog(log);
        }
    }

    public void showHelp() {
        output.setText(R.string.help_text);
        output.append(getString(R.string.tzversion_text, getTimeZone(), getTzVersion()));
    }

    public void buttonOnClick(final View view) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.title_confirm_dialog)
                .setMessage(R.string.message_confirm_dialog)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setCancelable(false)
                .setPositiveButton(android.R.string.yes,
                        (dialog, id) -> updateAction())
                .setNegativeButton(android.R.string.no,
                        (dialog, id) -> dialog.cancel()).show();
    }

    private void updateAction() {
        CheckBox checkTzData = findViewById(R.id.tzdataUpdate);
        CheckBox checkIcu = findViewById(R.id.icuUpdate);
        Thread t = new ExecScript(getApplicationContext(), checkTzData.isChecked(), checkIcu.isChecked());
        t.start();
    }

}
