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

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.stericson.roottools.RootTools;

public abstract class Command {

    ExecutionMonitor executionMonitor = null;
    boolean executing = false;

    Handler mHandler;
    String[] command = { };
    boolean finished = false;
    boolean terminated = false;
    int exitCode = -1;
    int id = 0;
    int timeout = RootTools.default_Command_Timeout;

    public abstract CommandListener getCommandListener();

    /**
     * Constructor for executing a normal shell command
     *
     * @param id      the id of the command being executed
     * @param command the command, or commands, to be executed.
     */
    public Command(int id, String... command) {
        this.command = command;
        this.id = id;
    }

    public Command(final Handler handler, final String... command) {
        this.mHandler = handler;
        this.id = 0;
        this.command = command;
    }

    protected void finishCommand() {
        executing = false;
        finished = true;
        this.notifyAll();
    }

    public String getCommand() {
        StringBuilder sb = new StringBuilder();

        for (final String aCommand : command) { sb.append(aCommand).append('\n'); }

        return sb.toString();
    }

    public boolean isExecuting() { return executing; }

    public boolean isFinished() { return finished; }

    public int getExitCode() { return this.exitCode; }

    protected void setExitCode(final int code) {
        synchronized (this) {
            exitCode = code;
        }
    }

    protected void startExecution() {
        executionMonitor = new ExecutionMonitor();
        executionMonitor.setPriority(Thread.MIN_PRIORITY);
        executionMonitor.start();
        executing = true;
    }

    public void terminate(final String reason) {
        Shell.closeAll();
        RootTools.log("Terminating all shells.");
        terminated(reason);
    }

    protected void commandFinished() {
        if (terminated) {
            return;
        }
        synchronized (this) {
            if (mHandler != null) {
                final Message msg = mHandler.obtainMessage();
                final Bundle bundle = new Bundle();
                bundle.putInt(CommandHandler.ACTION, CommandHandler.COMMAND_COMPLETED);
                bundle.putInt(CommandHandler.ID, id);
                bundle.putInt(CommandHandler.EXIT_CODE, exitCode);
                msg.setData(bundle);
                mHandler.sendMessage(msg);
            } else {
                getCommandListener().commandCompleted(id, exitCode);
            }

            RootTools.log("Command " + id + " finished.");
            finishCommand();
        }
    }

    protected void terminated(final String reason) {
        synchronized (this) {
            if (mHandler != null) {
                final Message msg = mHandler.obtainMessage();
                final Bundle bundle = new Bundle();
                bundle.putInt(CommandHandler.ACTION, CommandHandler.COMMAND_TERMINATED);
                bundle.putInt(CommandHandler.ID, id);
                bundle.putString(CommandHandler.TEXT, reason);
                msg.setData(bundle);
                mHandler.sendMessage(msg);
            } else {
                getCommandListener().commandTerminated(id, reason);
            }

            RootTools.log("Command " + id + " did not finish because it was terminated. " +
                    "Termination reason: " + reason);
            setExitCode(-1);
            terminated = true;
            finishCommand();
        }
    }

    protected void output(int id, String line) {
        if (mHandler != null) {
            final Message msg = mHandler.obtainMessage();
            final Bundle bundle = new Bundle();
            bundle.putInt(CommandHandler.ACTION, CommandHandler.COMMAND_OUTPUT);
            bundle.putInt(CommandHandler.ID, id);
            bundle.putString(CommandHandler.TEXT, line);
            msg.setData(bundle);
            mHandler.sendMessage(msg);
        } else {
            getCommandListener().commandOutput(id, line);
        }
    }

    private class ExecutionMonitor extends Thread {
        public void run() {
            while (!finished) {
                synchronized (this) {
                    try {
                        this.wait(timeout);
                    } catch (InterruptedException ignored) { }
                }

                if (!finished) {
                    RootTools.log("Timeout Exception has occurred.");
                    terminate("Timeout Exception");
                }
            }
        }
    }


}
