package ru.meefik.tzupdater;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

/**
 * Created by anton on 19.09.15.
 */
public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        PrefStore.setLocale(this);
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle(getString(R.string.title_activity_about));
        setContentView(R.layout.activity_about);

        TextView versionView = (TextView) findViewById(R.id.versionView);
        versionView.setText(getString(R.string.app_version,
                PrefStore.getVersion(getApplicationContext())));

        // enable context clickable
        TextView aboutView = (TextView) findViewById(R.id.aboutView);
        aboutView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    public void setTheme(int resid) {
        super.setTheme(PrefStore.getTheme(this));
    }

}
