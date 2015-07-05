/*
 * This file is part of the RootTools Project: http://code.google.com/p/roottools/
 *
 * Copyright (c) 2012 Stephen Erickson, Chris Ravenscroft, Dominik Schuermann, Adam Shanks
 *
 * This code is dual-licensed under the terms of the Apache License Version 2.0 and
 * the terms of the General Public License (GPL) Version 2.
 * You may use this code according to either of these licenses as is most appropriate
 * for your project on a case-by-case basis.
 *
 * The terms of each license can be found in the root directory of this project's repository as
 * well as at:
 *
 * * http://www.apache.org/licenses/LICENSE-2.0
 * * http://www.gnu.org/licenses/gpl-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under these Licenses is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See each License for the specific language governing permissions and
 * limitations under that License.
 */
package com.stericson.roottools.execution;

import android.text.TextUtils;

import com.stericson.roottools.RootTools;
import com.stericson.roottools.exceptions.RootDeniedException;

import org.namelessrom.devicecontrol.Application;
import org.namelessrom.devicecontrol.models.DeviceConfig;
import org.namelessrom.devicecontrol.utils.Utils;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class Shell {

    private final Process proc;
    private final BufferedReader inputStream;
    private final BufferedReader errorStream;
    private final OutputStreamWriter outputStream;
    private final List<Command> commands = new ArrayList<>();

    //indicates whether or not to close the shell
    private boolean close = false;

    private static String error = "";
    private static final String token = "F*D^W@#FGF";
    private static Shell rootShell = null;
    private static Shell shell = null;
    private static Shell customShell = null;

    private static int shellTimeout = 25000;
    public static boolean isExecuting = false;
    public static boolean isReading = false;
    public boolean isClosed = false;

    private static final int maxCommands = 1000;

    private int read = 0;
    private int write = 0;
    private int totalExecuted = 0;
    private int totalRead = 0;
    private boolean isCleaning = false;

    public static final String CONTEXT_NORMAL = "normal";

    private String shellContext = CONTEXT_NORMAL;

    //private constructor responsible for opening/constructing the shell
    private Shell(String cmd) throws IOException, TimeoutException, RootDeniedException {
        String extra = null;
        if ("su".equals(cmd)) {
            shellContext = DeviceConfig.get().suShellContext;

            if (!CONTEXT_NORMAL.equals(shellContext)) {
                extra = String.format("--context %s", shellContext);
            }
        }

        RootTools.log("Starting shell: " + cmd);
        RootTools.log("Context: " + shellContext);

        if (!TextUtils.isEmpty(extra)) {
            proc = new ProcessBuilder(cmd, extra).redirectErrorStream(true).start();
        } else {
            proc = new ProcessBuilder(cmd).redirectErrorStream(true).start();
        }
        inputStream = new BufferedReader(new InputStreamReader(proc.getInputStream(), "UTF-8"));
        errorStream = new BufferedReader(new InputStreamReader(proc.getErrorStream(), "UTF-8"));
        outputStream = new OutputStreamWriter(proc.getOutputStream(), "UTF-8");

        /**
         * Thread responsible for carrying out the requested operations
         */
        final Worker worker = new Worker(proc, inputStream, outputStream);
        worker.start();

        try {
            /**
             * The flow of execution will wait for the thread to die or wait until the
             * given timeout has expired.
             *
             * The result of the worker, which is determined by the exit code of the worker,
             * will tell us if the operation was completed successfully or it the operation
             * failed.
             */
            worker.join(shellTimeout);

            /**
             * The operation could not be completed before the timeout occured.
             */
            if (worker.exit == -911) {

                try {
                    proc.destroy();
                } catch (Exception ignored) { }

                closeQuietly(inputStream);
                closeQuietly(errorStream);
                closeQuietly(outputStream);

                throw new TimeoutException(error);
            }
            /**
             * Root access denied?
             */
            else if (worker.exit == -42) {

                try {
                    proc.destroy();
                } catch (Exception ignored) { }

                closeQuietly(inputStream);
                closeQuietly(errorStream);
                closeQuietly(outputStream);

                throw new RootDeniedException("Root Access Denied");
            }
            /**
             * Normal exit
             */
            else {
                /**
                 * The shell is open.
                 *
                 * Start two threads, one to handle the input and one to handle the output.
                 *
                 * input, and output are runnables that the threads execute.
                 */
                final Thread si = new Thread(input, "Shell Input");
                si.setPriority(Thread.NORM_PRIORITY);
                si.start();

                final Thread so = new Thread(output, "Shell Output");
                so.setPriority(Thread.NORM_PRIORITY);
                so.start();
            }
        } catch (InterruptedException ex) {
            worker.interrupt();
            Thread.currentThread().interrupt();
            throw new TimeoutException();
        }
    }

    public boolean isClosed() { return close; }

    public Command add(final Command command) {
        if (close) {
            throw new IllegalStateException("Unable to add commands to a closed shell");
        }

        //noinspection StatementWithEmptyBody
        while (isCleaning) {
            // Don't add commands while cleaning
        }
        commands.add(command);

        notifyThreads();

        return command;
    }

    public void useCWD() {
        add(new CommandCapture(-1, "cd " + Application.get().getFilesDirectory()));
    }

    private void cleanCommands() {
        isCleaning = true;
        int toClean = Math.abs(maxCommands - (maxCommands / 4));
        RootTools.log("Cleaning up: " + toClean);
        for (int i = 0; i < toClean; i++) {
            commands.remove(0);
        }

        read = commands.size() - 1;
        write = commands.size() - 1;
        isCleaning = false;
    }

    private void closeQuietly(final Reader input) {
        try {
            if (input != null) { input.close(); }
        } catch (Exception ignored) { }
    }

    private void closeQuietly(final Writer output) {
        try {
            if (output != null) { output.close(); }
        } catch (Exception ignored) { }
    }

    public void close() {
        synchronized (commands) {
            /**
             * instruct the two threads monitoring input and output
             * of the shell to close.
             */
            this.close = true;
            notifyThreads();
        }

        int count = 0;
        while (isExecuting) {
            RootTools.log("Waiting on shell to finish executing before closing...");
            count++;

            //failsafe to keep from hanging...
            if (count > 1000) {
                break;
            }
        }

        RootTools.log("Shell Closed!");

        if (this == Shell.rootShell) {
            Shell.rootShell = null;
        } else if (this == Shell.shell) {
            Shell.shell = null;
        } else if (this == Shell.customShell) {
            Shell.customShell = null;
        }
    }

    public static void closeCustomShell() {
        if (customShell == null) { return; }
        customShell.close();
    }

    public static void closeRootShell() {
        if (rootShell == null) { return; }
        rootShell.close();
    }

    public static void closeShell() {
        if (shell == null) { return; }
        shell.close();
    }

    public static void closeAll() {
        closeShell();
        closeRootShell();
        closeCustomShell();
    }

    public int getCommandQueuePosition(final Command cmd) { return commands.indexOf(cmd); }

    public String getCommandQueuePositionString(final Command cmd) {
        return "Command is in position " + getCommandQueuePosition(cmd) +
                " currently executing command at position " + write;
    }

    public static Shell getOpenShell() {
        if (customShell != null) {
            return customShell;
        } else if (rootShell != null) {
            return rootShell;
        } else { return shell; }
    }

    public static boolean isShellOpen() { return shell != null; }

    public static boolean isCustomShellOpen() { return customShell != null; }

    public static boolean isRootShellOpen() { return rootShell != null; }

    public static boolean isAnyShellOpen() {
        return shell != null || rootShell != null || customShell != null;
    }

    /**
     * Runnable to write commands to the open shell.
     * <p/>
     * When writing commands we stay in a loop and wait for new
     * commands to added to "commands"
     * <p/>
     * The notification of a new command is handled by the method add in this class
     */
    private final Runnable input = new Runnable() {
        public void run() {
            try {
                while (true) {

                    synchronized (commands) {
                        /**
                         * While loop is used in the case that notifyAll is called
                         * and there are still no commands to be written, a rare
                         * case but one that could happen.
                         */
                        while (!close && write >= commands.size()) {
                            isExecuting = false;
                            commands.wait();
                        }
                    }

                    if (write >= maxCommands) {

                        /**
                         * wait for the read to catch up.
                         */
                        while (read != write) {
                            RootTools.log("Waiting for read and write to catch up before cleanup.");
                        }
                        /**
                         * Clean up the commands, stay neat.
                         */
                        cleanCommands();
                    }

                    /**
                     * Write the new command
                     *
                     * We write the command followed by the token to indicate
                     * the end of the command execution
                     */
                    if (write < commands.size()) {
                        isExecuting = true;
                        Command cmd = commands.get(write);
                        cmd.startExecution();
                        RootTools.log("Executing: " + cmd.getCommand());

                        outputStream.write(cmd.getCommand());
                        String line = "\necho " + token + ' ' + totalExecuted + " $?\n";
                        outputStream.write(line);
                        outputStream.flush();
                        write++;
                        totalExecuted++;
                    } else if (close) {
                        /**
                         * close the thread, the shell is closing.
                         */
                        isExecuting = false;
                        outputStream.write("\nexit 0\n");
                        outputStream.flush();
                        RootTools.log("Closing shell");
                        return;
                    }
                }
            } catch (IOException | InterruptedException e) {
                RootTools.log(e.getMessage(), 2, e);
            } finally {
                write = 0;
                closeQuietly(outputStream);
            }
        }
    };

    protected void notifyThreads() {
        new Thread() {
            public void run() {
                synchronized (commands) {
                    commands.notifyAll();
                }
            }
        }.start();
    }

    /**
     * Runnable to monitor the responses from the open shell.
     */
    private final Runnable output = new Runnable() {
        public void run() {
            try {
                Command command = null;

                while (!close) {
                    isReading = false;
                    String outputLine = inputStream.readLine();
                    isReading = true;

                    /**
                     * If we recieve EOF then the shell closed
                     */
                    if (outputLine == null) { break; }

                    if (command == null) {
                        if (read >= commands.size()) {
                            if (close) { break; }

                            continue;
                        }
                        command = commands.get(read);
                    }

                    /**
                     * trying to determine if all commands have been completed.
                     *
                     * if the token is present then the command has finished execution.
                     */
                    int pos = outputLine.indexOf(token);


                    if (pos == -1) {
                        /**
                         * send the output for the implementer to process
                         */
                        command.output(command.id, outputLine);
                    } else if (pos > 0) {
                        /**
                         * token is suffix of output, send output part to implementer
                         */
                        command.output(command.id, outputLine.substring(0, pos));
                    }
                    if (pos >= 0) {
                        outputLine = outputLine.substring(pos);
                        String fields[] = outputLine.split(" ");

                        if (fields.length >= 2 && fields[1] != null) {
                            int id = 0;

                            try {
                                id = Utils.parseInt(fields[1]);
                            } catch (NumberFormatException ignored) { }

                            int exitCode = -1;

                            try {
                                exitCode = Utils.parseInt(fields[2]);
                            } catch (NumberFormatException ignored) { }

                            if (id == totalRead) {
                                processErrors(command);
                                command.setExitCode(exitCode);
                                command.commandFinished();
                                command = null;

                                read++;
                                totalRead++;
                            }
                        }
                    }
                }

                RootTools.log("Read all output");
                try {
                    proc.waitFor();
                    proc.destroy();
                } catch (Exception ignored) { }

                closeQuietly(outputStream);
                closeQuietly(errorStream);
                closeQuietly(inputStream);

                while (read < commands.size()) {
                    if (command == null) { command = commands.get(read); }

                    command.terminated("Unexpected Termination.");
                    command = null;
                    read++;
                }

                read = 0;

            } catch (IOException e) {
                RootTools.log(e.getMessage(), 2, e);
            } finally {
                RootTools.log("Shell destroyed");
                isClosed = true;
                isReading = false;
            }
        }
    };

    public void processErrors(Command command) {
        try {
            while (errorStream.ready() && command != null) {
                String line = errorStream.readLine();

                /**
                 * If we recieve EOF then the shell closed?
                 */
                if (line == null) {
                    break;
                }

                /**
                 * send the output for the implementer to process
                 */
                command.output(command.id, line);
            }
        } catch (Exception e) {
            RootTools.log(e.getMessage(), 2, e);
        }
    }

    public static void runRootCommand(final Command command)
            throws IOException, TimeoutException, RootDeniedException {
        startRootShell().add(command);
    }

    public static void runCommand(final Command command) throws IOException, TimeoutException {
        startShell().add(command);
    }

    public static Shell startRootShell() throws IOException, TimeoutException, RootDeniedException {
        return Shell.startRootShell(20000, 3);
    }

    public static Shell startRootShell(final int timeout)
            throws IOException, TimeoutException, RootDeniedException {
        return Shell.startRootShell(timeout, 3);
    }

    public static Shell startRootShell(final int timeout, final int retry)
            throws IOException, TimeoutException, RootDeniedException {

        Shell.shellTimeout = timeout;

        if (rootShell == null) {
            RootTools.log("Starting Root Shell!");
            String cmd = "su";
            // keep prompting the user until they accept for x amount of times...
            int retries = 0;
            while (rootShell == null) {
                try {
                    rootShell = new Shell(cmd);
                } catch (IOException e) {
                    if (retries++ >= retry) {
                        RootTools.log("IOException, could not start shell");
                        throw e;
                    }
                }
            }
        } else {
            RootTools.log("Using Existing Root Shell!");
        }

        return rootShell;
    }

    public static Shell startCustomShell(final String shellPath)
            throws IOException, TimeoutException, RootDeniedException {
        return Shell.startCustomShell(shellPath, 20000);
    }

    public static Shell startCustomShell(final String shellPath, final int timeout)
            throws IOException, TimeoutException, RootDeniedException {
        Shell.shellTimeout = timeout;

        if (customShell == null) {
            RootTools.log("Starting Custom Shell!");
            customShell = new Shell(shellPath);
        } else { RootTools.log("Using Existing Custom Shell!"); }

        return customShell;
    }

    public static Shell startShell() throws IOException, TimeoutException {
        return Shell.startShell(20000);
    }

    public static Shell startShell(final int timeout) throws IOException, TimeoutException {
        Shell.shellTimeout = timeout;

        try {
            if (shell == null) {
                RootTools.log("Starting Shell!");
                shell = new Shell("/system/bin/sh");
            } else { RootTools.log("Using Existing Shell!"); }
            return shell;
        } catch (RootDeniedException e) {
            //Root Denied should never be thrown.
            throw new IOException();
        }
    }

    protected static class Worker extends Thread {
        public int exit = -911;

        public final Process proc;
        public final BufferedReader in;
        public final OutputStreamWriter out;

        private Worker(Process proc, BufferedReader in, OutputStreamWriter out) {
            this.proc = proc;
            this.in = in;
            this.out = out;
        }

        public void run() {

            /**
             * Trying to open the shell.
             *
             * We echo "Started" and we look for it in the output.
             *
             * If we find the output then the shell is open and we return.
             *
             * If we do not find it then we determine the error and report
             * it by setting the value of the variable exit
             */
            try {
                out.write("echo Started\n");
                out.flush();

                String line;
                while (true) {
                    line = in.readLine();
                    if (line == null) {
                        throw new EOFException();
                    }
                    if (line.isEmpty()) { continue; }
                    if ("Started".equals(line)) {
                        this.exit = 1;
                        setShellOom();
                        break;
                    }

                    Shell.error = "unkown error occured.";
                }
            } catch (IOException e) {
                exit = -42;
                if (e.getMessage() != null) { Shell.error = e.getMessage(); } else {
                    Shell.error = "RootAccess denied?.";
                }
            }

        }

        /*
         * setOom for shell processes (sh and su if root shell)
         * and discard outputs
         *
         */
        private void setShellOom() {
            try {
                final Class<?> processClass = proc.getClass();
                Field field;
                try {
                    field = processClass.getDeclaredField("pid");
                } catch (NoSuchFieldException e) {
                    field = processClass.getDeclaredField("id");
                }
                field.setAccessible(true);
                final int pid = (Integer) field.get(proc);
                out.write("(echo -17 > /proc/" + pid + "/oom_adj) &> /dev/null\n");
                out.write("(echo -17 > /proc/$$/oom_adj) &> /dev/null\n");
                out.flush();
            } catch (Exception ignored) { }
        }
    }
}
