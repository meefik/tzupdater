package ru.meefik.timezoneupdater;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.Properties;

public class MainActivity extends AppCompatActivity {

    private Logger logger;
    private EnvUtils envUtils;
    private boolean debug;
    private boolean trace;
    private boolean logging;
    private int font;
    private int scroll;
    private String theme;
    private String logFile;

    public static final Properties pref = new Properties();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        logger = new Logger(this, false);
        envUtils = new EnvUtils(getApplicationContext(), logger, true, false);

        final CheckBox checkTzData = (CheckBox) findViewById(R.id.tzdataUpdate);
        final CheckBox checkIcu = (CheckBox) findViewById(R.id.icuUpdate);
        final Button button = (Button) findViewById(R.id.button);
        final TextView outputView = (TextView) findViewById(R.id.outputView);

        // enable context clickable
        outputView.setMovementMethod(LinkMovementMethod.getInstance());

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
                TextView outputView = (TextView) findViewById(R.id.outputView);
                outputView.setText(R.string.help_text);
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
        initPrefs();
    }

    private void initPrefs() {
        SharedPreferences sPref = getPreferences(MODE_PRIVATE);
        pref.put("font_size", sPref.getInt("fontsize", Integer.parseInt(getString(R.string.fontsize))));
        pref.setProperty("max_lines", sPref.getString("maxlines", getString(R.string.maxlines)));
        pref.setProperty("max_lines", sPref.getString("maxlines", getString(R.string.maxlines)));
    }

    public void buttonOnClick(final View view) {
        view.setEnabled(false);
        view.postDelayed(new Runnable() {
            @Override
            public void run() {
                view.setEnabled(true);
            }
        }, 10000);
        logger.clear();
        CheckBox checkTzData = (CheckBox) findViewById(R.id.tzdataUpdate);
        CheckBox checkIcu = (CheckBox) findViewById(R.id.icuUpdate);
        Thread t = new ExecScript(envUtils, checkTzData.isChecked(), checkIcu.isChecked());
        t.start();
    }

}
