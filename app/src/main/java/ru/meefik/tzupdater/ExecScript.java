package ru.meefik.tzupdater;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by anton on 19.09.15.
 */
public class ExecScript extends Thread {

    private EnvUtils envUtils;
    private boolean tzdata;
    private boolean icu;

    public ExecScript(EnvUtils envUtils, boolean tzdata, boolean icu) {
        this.envUtils = envUtils;
        this.tzdata = tzdata;
        this.icu = icu;
    }

    @Override
    public void run() {
        // update env
        if (!envUtils.update()) return;
        // check root
        if (!envUtils.isRooted()) return;
        // exec command
        String envDir = envUtils.getEnvDir();
        List<String> params = new ArrayList<>();
        params.add("ENV_DIR=" + envDir);
        if (tzdata) {
            params.add("printf '>>> TIME ZONE DATABASE \n'");
            params.add("(. " + envDir + "/bin/tzdata-updater.sh 2015f)");
            params.add("printf '.\n'");
        }
        if (icu) {
            params.add("printf '>>> ICU DATA \n'");
            params.add("(. " + envDir + "/bin/icu-updater.sh 2015z)");
            params.add("printf '.\n'");
        }
        envUtils.exec(params);
    }
}
