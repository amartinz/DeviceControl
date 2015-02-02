package org.namelessrom.devicecontrol.utils.cmdprocessor;

import android.util.Log;

import org.namelessrom.devicecontrol.Logger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;

// convenience import for quick referencing of this method

public final class CMDProcessor {
    private static final String TAG = "aCMDProcessor";

    public final sh su = new sh("su");

    public CMDProcessor() { }

    /* Run a system command with full redirection */
    public static ChildProcess startSysCmd(String[] cmdarray, String childStdin) {
        return new ChildProcess(cmdarray, childStdin);
    }

    public static ChildProcess startShellCommand(String cmd) {
        String[] cmdarray = new String[3];
        cmdarray[0] = "sh";
        cmdarray[1] = "-c";
        cmdarray[2] = cmd;
        return startSysCmd(cmdarray, null);
    }

    public static CommandResult runShellCommand(String cmd) {
        ChildProcess proc = startShellCommand(cmd);
        proc.waitFinished();
        return proc.getResult();
    }

    public static ChildProcess startSuCommand(String cmd) {
        String[] cmdarray = new String[3];
        cmdarray[0] = "su";
        cmdarray[1] = "-c";
        cmdarray[2] = cmd;
        return startSysCmd(cmdarray, null);
    }

    public static CommandResult runSuCommand(String cmd) {
        ChildProcess proc = startSuCommand(cmd);
        proc.waitFinished();
        return proc.getResult();
    }

    public class CommandResult2 {
        public final String stdout;
        public final String stderr;
        public final Integer exit_value;

        CommandResult2(final Integer exit_value_in, final String stdout_in,
                final String stderr_in) {
            exit_value = exit_value_in;
            stdout = stdout_in;
            stderr = stderr_in;
        }

        public boolean success() {
            return exit_value != null && exit_value == 0;
        }
    }

    public class sh {
        private String SHELL = "sh";

        public sh(final String SHELL_in) {
            SHELL = SHELL_in;
        }

        private String getStreamLines(final InputStream is) {
            String out = null;
            StringBuffer buffer = null;
            DataInputStream dis = new DataInputStream(is);

            try {
                if (dis.available() > 0) {
                    //noinspection deprecation
                    buffer = new StringBuffer(dis.readLine());
                    while (dis.available() > 0) {
                        //noinspection deprecation
                        buffer.append('\n').append(dis.readLine());
                    }
                }
                dis.close();
            } catch (final Exception ex) {
                Log.e(TAG, ex.getMessage());
            } finally {
                try {
                    dis.close();
                } catch (Exception ignored) { }
            }
            if (buffer != null) {
                out = buffer.toString();
            }
            return out;
        }

        public Process run(final String s) {
            Process process;
            try {
                process = Runtime.getRuntime().exec(SHELL);
                final DataOutputStream toProcess = new DataOutputStream(process.getOutputStream());
                toProcess.writeBytes("exec " + s + '\n');
                toProcess.flush();
            } catch (final Exception e) {
                Logger.e(TAG, "Exception while trying to run: '" + s + "' " + e.getMessage());
                process = null;
            }
            return process;
        }

        public CommandResult2 runWaitFor(final String s) {
            final Process process = run(s);
            Integer exit_value = null;
            String stdout = null;
            String stderr = null;
            if (process != null) {
                try {
                    exit_value = process.waitFor();

                    stdout = getStreamLines(process.getInputStream());
                    stderr = getStreamLines(process.getErrorStream());
                } catch (final Exception e) {
                    Logger.e(TAG, "runWaitFor " + e.toString());
                }
            }
            return new CommandResult2(exit_value, stdout, stderr);
        }
    }
}
