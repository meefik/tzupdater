package ru.meefik.tzupdater;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by anton on 18.09.15.
 */
public class MainActivity extends AppCompatActivity {

    public static TextView output;
    public static ScrollView scroll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        PrefStore.setLocale(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final CheckBox checkTzData = (CheckBox) findViewById(R.id.tzdataUpdate);
        final CheckBox checkIcu = (CheckBox) findViewById(R.id.icuUpdate);
        final Button button = (Button) findViewById(R.id.button);
        output = (TextView) findViewById(R.id.outputView);
        scroll = (ScrollView) findViewById(R.id.scrollView);

        // enable context clickable
        output.setMovementMethod(LinkMovementMethod.getInstance());

        checkTzData.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton view, boolean isChecked) {
                if (isChecked || checkIcu.isChecked()) {
                    button.setEnabled(true);
                } else {
                    button.setEnabled(false);
                }
            }
        });

        checkIcu.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton view, boolean isChecked) {
                if (isChecked || checkTzData.isChecked()) {
                    button.setEnabled(true);
                } else {
                    button.setEnabled(false);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        PrefStore.setLocale(this);
        // Inflate the menu; this adds items to the action bar if it is present.
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
    public void setTheme(int resid) {
        super.setTheme(PrefStore.getTheme(this));
    }

    @Override
    public void onResume() {
        super.onResume();
        TextView outputView = (TextView) findViewById(R.id.outputView);
        // restore font size
        outputView.setTextSize(TypedValue.COMPLEX_UNIT_SP, PrefStore.getFontSize(this));
        // restore logs
        String log = Logger.get();
        if (log.length() == 0) {
            // show help if empty
            showHelp();
        } else {
            showLog(log);
        }
    }

    /**
     * Show message in TextView, used from Logger
     * @param log message
     */
    public static void showLog(final String log) {
        // show log in TextView
        output.post(new Runnable() {
            @Override
            public void run() {
                output.setText(log);
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

    public void showHelp() {
        TextView outputView = (TextView) findViewById(R.id.outputView);
        outputView.setText(R.string.help_text);
        outputView.append(getString(R.string.tzversion_text, getTimeZone(), getTzVersion()));
    }

    public static String getTimeZone() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"), Locale.getDefault());
        String timeZone = new SimpleDateFormat("Z", Locale.US).format(calendar.getTime());
        String zone = TimeZone.getDefault().getID();
        return zone + " (" + timeZone.substring(0, 3) + ":" + timeZone.substring(3, 5) + ")";
    }

    public static String getTzVersion() {
        String tzVersion = "???";
        String[] arr = {"/data/misc/zoneinfo/tzdata", "/system/usr/share/zoneinfo/tzdata", "/system/usr/share/zoneinfo/zoneinfo.version"};
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

    public void buttonOnClick(final View view) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.title_confirm_dialog)
                .setMessage(R.string.message_confirm_dialog)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setCancelable(false)
                .setPositiveButton(android.R.string.yes,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                updateAction();
                            }
                        })
                .setNegativeButton(android.R.string.no,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        }).show();
    }

    private void updateAction() {
        CheckBox checkTzData = (CheckBox) findViewById(R.id.tzdataUpdate);
        CheckBox checkIcu = (CheckBox) findViewById(R.id.icuUpdate);
        Thread t = new ExecScript(this, checkTzData.isChecked(), checkIcu.isChecked());
        t.start();
    }

}
