package ru.meefik.tzupdater;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
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
import java.util.Objects;

public class EnvUtils {

    /**
     * Closeable helper
     *
     * @param c closable object
     */
    private static void close(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Extract file to env directory
     *
     * @param c         context
     * @param rootAsset root asset name
     * @param path      path to asset file
     * @return false if error
     */
    private static boolean extractFile(Context c, String rootAsset, String path) {
        AssetManager assetManager = c.getAssets();
        InputStream in = null;
        OutputStream out = null;
        try {
            in = assetManager.open(rootAsset + path);
            String fullPath = PrefStore.getFilesDir(c) + path;
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
            close(in);
            close(out);
        }
        return true;
    }

    /**
     * Extract path to env directory
     *
     * @param c         context
     * @param rootAsset root asset name
     * @param path      path to asset directory
     * @return false if error
     */
    private static boolean extractDir(Context c, String rootAsset, String path) {
        AssetManager assetManager = c.getAssets();
        try {
            String[] assets = assetManager.list(rootAsset + path);
            if (assets.length == 0) {
                if (!extractFile(c, rootAsset, path)) return false;
            } else {
                String fullPath = PrefStore.getFilesDir(c) + path;
                File dir = new File(fullPath);
                if (!dir.exists()) dir.mkdir();
                for (String asset : assets) {
                    if (!extractDir(c, rootAsset, path + "/" + asset)) return false;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Recursive remove all from directory
     *
     * @param path path to directory
     */
    private static void cleanDirectory(File path) {
        if (path == null) return;
        if (path.exists()) {
            for (File f : Objects.requireNonNull(path.listFiles())) {
                if (f.isDirectory()) cleanDirectory(f);
                f.delete();
            }
        }
    }

    /**
     * Recursive set permissions to directory
     *
     * @param path path to directory
     */
    private static void setPermissions(File path) {
        if (path == null) return;
        if (path.exists()) {
            for (File f : Objects.requireNonNull(path.listFiles())) {
                if (f.isDirectory()) setPermissions(f);
                f.setReadable(true, false);
                f.setExecutable(true, false);
            }
        }
    }

    /**
     * Update version file
     *
     * @param c context
     * @return false if error
     */
    private static Boolean setVersion(Context c) {
        boolean result = false;
        String f = PrefStore.getFilesDir(c) + "/etc/version";
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(f));
            bw.write(PrefStore.getVersion(c));
            result = true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close(bw);
        }
        return result;
    }

    /**
     * Check latest env version
     *
     * @param c context
     * @return false if error
     */
    private static Boolean isLatestVersion(Context c) {
        boolean result = false;
        String f = PrefStore.getFilesDir(c) + "/etc/version";
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(f));
            String line = br.readLine();
            if (PrefStore.getVersion(c).equals(line)) result = true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close(br);
        }
        return result;
    }

    /**
     * Update env directory
     *
     * @param c context
     * @return false if error
     */
    static boolean update(Context c) {
        if (isLatestVersion(c)) return true;

        // prepare env directory
        String binDir = PrefStore.getFilesDir(c) + "/bin";
        File fd = new File(binDir);
        if (!fd.exists()) {
            if (!fd.mkdirs()) return false;
        }
        cleanDirectory(fd);

        // extract assets
        if (!extractDir(c, "all", "")) {
            return false;
        }
        String mArch = PrefStore.getArch();
        if (Objects.equals(mArch, "arm64")) mArch = "arm";
        if (Objects.equals(mArch, "x86_64")) mArch = "x86";
        if (!extractDir(c, mArch, "")) {
            return false;
        }

        // create .nomedia
        File noMedia = new File(PrefStore.getFilesDir(c) + "/.nomedia");
        try {
            noMedia.createNewFile();
        } catch (IOException ignored) {
        }

        // set permissions
        setPermissions(fd);

        // update version
        return setVersion(c);
    }

    /**
     * Check root permissions
     *
     * @param c context
     * @return false if error
     */
    static boolean isRooted(Context c) {
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
                close(os);
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
                close(reader);
            }

            if (n > 0) {
                result = true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close(stdout);
            close(stdin);
        }
        if (!result) {
            Logger.log(c, "Require superuser privileges (root).\n");
        }
        return result;
    }

    /**
     * Execute commands from system shell
     *
     * @param c      context
     * @param params list of commands
     * @return false if error
     */
    static boolean exec(final Context c, final String shell, final List<String> params) {
        if (params == null || params.size() == 0) {
            Logger.log(c, "No scripts for processing.\n");
            return false;
        }
        boolean result = false;
        OutputStream stdin = null;
        InputStream stdout = null;
        InputStream stderr = null;
        try {
            Process process = Runtime.getRuntime().exec(shell);

            stdin = process.getOutputStream();
            stdout = process.getInputStream();
            stderr = process.getErrorStream();

            params.add(0, "PATH=" + PrefStore.getFilesDir(c) + "/bin:$PATH");
            params.add("exit $?");
            if (PrefStore.isTraceMode(c)) params.add(0, "set -x");

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
                close(os);
            }

            // show stdout log
            final InputStream out = stdout;
            (new Thread() {
                @Override
                public void run() {
                    Logger.log(c, out);
                }
            }).start();

            // show stderr log
            final InputStream err = stderr;
            if (PrefStore.isDebugMode(c) || PrefStore.isTraceMode(c)) {
                (new Thread() {
                    @Override
                    public void run() {
                        Logger.log(c, err);
                    }
                }).start();
            }

            process.waitFor();
            if (process.exitValue() == 0) result = true;
        } catch (Exception e) {
            result = false;
            e.printStackTrace();
        } finally {
            close(stdout);
            close(stderr);
            close(stdin);
        }
        return result;
    }

}
