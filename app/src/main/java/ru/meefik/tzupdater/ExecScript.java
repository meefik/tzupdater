package ru.meefik.tzupdater;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

public class ExecScript extends Thread {

    private final Context context;
    private final boolean tzdata;
    private final boolean icu;

    ExecScript(Context c, boolean tzdata, boolean icu) {
        this.context = c;
        this.tzdata = tzdata;
        this.icu = icu;
    }

    private void updateTzData() {
        String envDir = PrefStore.getFilesDir(context);
        List<String> params = new ArrayList<>();
        params.add("ENV_DIR=" + envDir);
        params.add(". " + envDir + "/bin/tzdata-updater.sh");
        EnvUtils.exec(context, "su", params);
    }

    private void updateIcuData() {
        String envDir = PrefStore.getFilesDir(context);
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
