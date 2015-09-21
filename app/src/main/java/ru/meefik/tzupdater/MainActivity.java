package ru.meefik.tzupdater;

import android.content.Intent;
import android.os.Bundle;
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

import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {

    public static Logger logger;
    public static EnvUtils envUtils;
    public static TextView output;
    public static ScrollView scroll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        PrefStore.refresh(this);
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

        if (logger == null) {
            logger = new Logger(new Runnable() {
                @Override
                public void run() {
                    // show log in TextView
                    output.post(new Runnable() {
                        @Override
                        public void run() {
                            output.setText(logger.get());
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
            }, false);
        }

        if (envUtils == null) {
            envUtils = new EnvUtils(getApplicationContext(), logger);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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
                logger.clear();
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
        PrefStore.refresh(this);
        TextView output = (TextView) findViewById(R.id.outputView);
        // restore font size
        output.setTextSize(TypedValue.COMPLEX_UNIT_SP, PrefStore.FONT_SIZE);
        // restore logs
        output.setText(logger.get());
        // show help if empty
        if (output.getText().length() == 0) {
            showHelp();
        }
    }

    public void showHelp() {
        TextView output = (TextView) findViewById(R.id.outputView);
        output.setText(R.string.help_text);
        output.append(getString(R.string.tzversion_text, getTimeZone(), getTzVersion()));
    }

    public static String getTimeZone()
    {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"), Locale.getDefault());
        String timeZone = new SimpleDateFormat("Z").format(calendar.getTime());
        String zone = TimeZone.getDefault().getID();
        return zone + " (" + timeZone.substring(0, 3) + ":"+ timeZone.substring(3, 5) + ")";
    }

    public static String getTzVersion() {
        String tzVersion = "???";
        String[] arr = {"/data/misc/zoneinfo/tzdata", "/system/usr/share/zoneinfo/tzdata"};
        for (String tzdata: arr) {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(tzdata);
                byte[] buffer = new byte[5];
                fis.skip(6);
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
        view.setEnabled(false);
        view.postDelayed(new Runnable() {
            @Override
            public void run() {
                view.setEnabled(true);
            }
        }, 30000);
        logger.clear();
        CheckBox checkTzData = (CheckBox) findViewById(R.id.tzdataUpdate);
        CheckBox checkIcu = (CheckBox) findViewById(R.id.icuUpdate);
        Thread t = new ExecScript(envUtils, checkTzData.isChecked(), checkIcu.isChecked());
        t.start();
    }

}
