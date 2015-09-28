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

    @Override
    public void run() {
        Logger.clear();
        // update env
        if (!EnvUtils.update(context)) return;
        // check root
        if (!EnvUtils.isRooted(context)) return;
        // exec command
        String envDir = PrefStore.getEnvDir(context);
        List<String> params = new ArrayList<>();
        params.add("ENV_DIR=" + envDir);
        if (tzdata) {
            params.add("printf '>>> TIME ZONE DATABASE \n'");
            params.add("(. " + envDir + "/bin/tzdata-updater.sh)");
            params.add("printf '.\n'");
        }
        if (icu) {
            params.add("printf '>>> ICU DATA \n'");
            params.add("(. " + envDir + "/bin/icu-updater.sh)");
            params.add("printf '.\n'");
        }
        EnvUtils.exec(context, params);
    }

}
