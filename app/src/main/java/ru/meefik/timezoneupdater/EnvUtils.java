package ru.meefik.timezoneupdater;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by anton on 19.09.15.
 */
public class EnvUtils {

    private final Context context;
    private final Logger logger;
    private final String envDir;
    private boolean debug;
    private boolean trace;

    public EnvUtils(Context context, Logger logger, boolean debug, boolean trace) {
        this.context = context;
        this.logger = logger;
        this.envDir = getEnvDir();
        this.debug = debug;
        this.trace = trace;
    }

    private boolean extractFile(AssetManager assetManager, String rootAsset, String path) {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = assetManager.open(rootAsset + path);
            String fullPath = envDir + path;
            out = new FileOutputStream(fullPath);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }

    private boolean extractDir(AssetManager assetManager, String rootAsset, String path) {
        try {
            String[] assets = assetManager.list(rootAsset + path);
            if (assets.length == 0) {
                if (!extractFile(assetManager, rootAsset, path)) return false;
            } else {
                String fullPath = envDir + path;
                File dir = new File(fullPath);
                if (!dir.exists()) dir.mkdir();
                for (String asset : assets) {
                    if (!extractDir(assetManager, rootAsset, path + "/" + asset)) return false;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void cleanDirectory(File path) {
        if (path == null) return;
        if (path.exists()) {
            for (File f : path.listFiles()) {
                if (f.isDirectory()) cleanDirectory(f);
                f.delete();
            }
        }
    }

    private void setPermissions(File path) {
        if (path == null) return;
        if (path.exists()) {
            for (File f : path.listFiles()) {
                if (f.isDirectory()) setPermissions(f);
                f.setReadable(true);
                f.setExecutable(true, false);
            }
        }
    }

    private String getArch(String arch) {
        String march = "";
        if (arch.length() > 0) {
            char a = arch.toLowerCase().charAt(0);
            switch (a) {
                case 'a':
                    if (arch.equals("amd64"))
                        march = "intel";
                    else
                        march = "arm";
                    break;
                case 'm':
                    march = "mips";
                    break;
                case 'i':
                case 'x':
                    march = "intel";
                    break;
            }
        }
        return march;
    }

    // update version file
    private Boolean setVersion(String version) {
        if (version == null) return false;
        Boolean result = false;
        String f = envDir + "/etc/version";
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(f));
            bw.write(version);
            result = true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    // check latest env version
    private Boolean isLatestVersion(String version) {
        if (version == null) return false;
        Boolean result = false;
        String f = envDir + "/etc/version";
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(f));
            String line = br.readLine();
            if (version.equals(line)) result = true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    // get app version
    public String getVersion() {
        String version = null;
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            version = pi.versionName + "-" + pi.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return version;
    }

    // get env directory
    public String getEnvDir() {
        return context.getFilesDir().getAbsolutePath();
    }

    public boolean update() {
        String version = getVersion();
        if (isLatestVersion(version)) return true;

        // prepare env directory
        File fEnvDir = new File(envDir);
        fEnvDir.mkdirs();
        if (!fEnvDir.exists()) {
            return false;
        }
        cleanDirectory(fEnvDir);

        // extract assets
        AssetManager assetManager = context.getAssets();
        if (!extractDir(assetManager, "all", "")) {
            return false;
        }
        String mArch = getArch(System.getProperty("os.arch"));
        if (!extractDir(assetManager, mArch, "")) {
            return false;
        }

        // set permissions
        setPermissions(fEnvDir);

        // update version
        if (!setVersion(version)) {
            return false;
        }

        return true;
    }

    public boolean remove() {
        File fEnvDir = new File(envDir);
        if (!fEnvDir.exists()) {
            return false;
        }
        cleanDirectory(fEnvDir);
        return true;
    }

    public boolean isRooted() {
        boolean result = false;
        OutputStream stdin = null;
        InputStream stdout = null;
        try {
            Process process = Runtime.getRuntime().exec("su");
            stdin = process.getOutputStream();
            stdout = process.getInputStream();

            DataOutputStream os = null;
            try {
                os = new DataOutputStream(stdin);
                os.writeBytes("ls /data\n");
                os.writeBytes("exit\n");
                os.flush();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (os != null) {
                    try {
                        os.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            int n = 0;
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(stdout));
                while (reader.readLine() != null) {
                    n++;
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

            if (n > 0) {
                result = true;
            } else {
                logger.log("Require superuser privileges (root).\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (stdout != null) {
                try {
                    stdout.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (stdin != null) {
                try {
                    stdin.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    public boolean exec(List<String> params) {
        if (params == null || params.size() == 0) {
            logger.log("No scripts for processing.\n");
            return false;
        }
        boolean result = false;
        OutputStream stdin = null;
        InputStream stdout = null;
        InputStream stderr = null;
        try {
            Process process = Runtime.getRuntime().exec("su");

            stdin = process.getOutputStream();
            stdout = process.getInputStream();
            stderr = process.getErrorStream();

            params.add(0, "PATH=" + envDir + "/bin:$PATH");
            params.add("exit $?");
            if (trace) params.add(0, "set -x");

            DataOutputStream os = null;
            try {
                os = new DataOutputStream(stdin);
                for (String cmd : params) {
                    os.writeBytes(cmd + "\n");
                }
                os.flush();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (os != null) {
                    try {
                        os.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            // show stdout log
            final InputStream out = stdout;
            (new Thread() {
                @Override
                public void run() {
                    logger.log(out);
                }
            }).start();

            // show stderr log
            final InputStream err = stderr;
            if (debug || trace) {
                (new Thread() {
                    @Override
                    public void run() {
                        logger.log(err);
                    }
                }).start();
            }

            process.waitFor();
            if (process.exitValue() == 0) result = true;
        } catch (Exception e) {
            result = false;
            e.printStackTrace();
        } finally {
            if (stdout != null) {
                try {
                    stdout.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (stderr != null) {
                try {
                    stderr.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (stdin != null) {
                try {
                    stdin.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

}
