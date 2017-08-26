package ru.meefik.tzupdater;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by anton on 19.09.15.
 */
public class ExecScript extends Thread {

    private Context context;
    private boolean tzdata;
    private boolean icu;

    public ExecScript(Context c, boolean tzdata, boolean icu) {
        this.context = c;
        this.tzdata = tzdata;
        this.icu = icu;
    }

    private void updateTzData() {
        String envDir = PrefStore.getEnvDir(context);
        List<String> params = new ArrayList<>();
        params.add("ENV_DIR=" + envDir);
        params.add(". " + envDir + "/bin/tzdata-updater.sh");
        EnvUtils.exec(context, "su", params);
    }

    private void updateIcuData() {
        String envDir = PrefStore.getEnvDir(context);
        List<String> params = new ArrayList<>();
        params.add("ENV_DIR=" + envDir);
        params.add(". " + envDir + "/bin/icu-updater.sh");
        EnvUtils.exec(context, "su", params);
    }

    @Override
    public void run() {
        Logger.clear();
        // update env
        if (!EnvUtils.update(context)) return;
        // check root
        if (!EnvUtils.isRooted(context)) return;
        // exec command
        if (tzdata) {
            Logger.log(context, ">>> TIME ZONE DATABASE\n");
            updateTzData();
            Logger.log(context, ".\n");
        }
        if (icu) {
            Logger.log(context, ">>> ICU DATA\n");
            updateIcuData();
            Logger.log(context, ".\n");
        }
    }

}
