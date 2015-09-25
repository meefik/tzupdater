package ru.meefik.tzupdater;

import android.content.Context;
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
import java.util.List;

/**
 * Created by anton on 19.09.15.
 */
public class EnvUtils {

    private static boolean extractFile(AssetManager assetManager, String rootAsset, String path) {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = assetManager.open(rootAsset + path);
            String fullPath = PrefStore.ENV_DIR + path;
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

    private static boolean extractDir(AssetManager assetManager, String rootAsset, String path) {
        try {
            String[] assets = assetManager.list(rootAsset + path);
            if (assets.length == 0) {
                if (!extractFile(assetManager, rootAsset, path)) return false;
            } else {
                String fullPath = PrefStore.ENV_DIR + path;
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

    private static void cleanDirectory(File path) {
        if (path == null) return;
        if (path.exists()) {
            for (File f : path.listFiles()) {
                if (f.isDirectory()) cleanDirectory(f);
                f.delete();
            }
        }
    }

    private static void setPermissions(File path) {
        if (path == null) return;
        if (path.exists()) {
            for (File f : path.listFiles()) {
                if (f.isDirectory()) setPermissions(f);
                f.setReadable(true);
                f.setExecutable(true, false);
            }
        }
    }

    private static String getArch(String arch) {
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
    private static Boolean setVersion() {
        Boolean result = false;
        String f = PrefStore.ENV_DIR + "/etc/version";
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(f));
            bw.write(PrefStore.VERSION);
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
    private static Boolean isLatestVersion() {
        Boolean result = false;
        String f = PrefStore.ENV_DIR + "/etc/version";
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(f));
            String line = br.readLine();
            if (PrefStore.VERSION.equals(line)) result = true;
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

    // get env directory
    public static String getEnvDir(Context c) {
        return c.getFilesDir().getAbsolutePath();
    }

    public static boolean update(Context c) {
        if (isLatestVersion()) return true;

        // prepare env directory
        File fEnvDir = new File(PrefStore.ENV_DIR);
        fEnvDir.mkdirs();
        if (!fEnvDir.exists()) {
            return false;
        }
        cleanDirectory(fEnvDir);

        // extract assets
        AssetManager assetManager = c.getAssets();
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
        return setVersion();

    }

    public static boolean remove() {
        File fEnvDir = new File(PrefStore.ENV_DIR);
        if (!fEnvDir.exists()) {
            return false;
        }
        cleanDirectory(fEnvDir);
        return true;
    }

    public static boolean isRooted() {
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
        if (result == false) {
            Logger.log("Require superuser privileges (root).\n");
        }
        return result;
    }

    public static boolean exec(List<String> params) {
        if (params == null || params.size() == 0) {
            Logger.log("No scripts for processing.\n");
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

            params.add(0, "PATH=" + PrefStore.ENV_DIR + "/bin:$PATH");
            params.add("exit $?");
            if (PrefStore.TRACE_MODE) params.add(0, "set -x");

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
                    Logger.log(out);
                }
            }).start();

            // show stderr log
            final InputStream err = stderr;
            if (PrefStore.DEBUG_MODE || PrefStore.TRACE_MODE) {
                (new Thread() {
                    @Override
                    public void run() {
                        Logger.log(err);
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
