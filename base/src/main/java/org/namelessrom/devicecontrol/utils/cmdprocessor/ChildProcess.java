package org.namelessrom.devicecontrol.utils.cmdprocessor;

import org.namelessrom.devicecontrol.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static java.lang.System.nanoTime;

public class ChildProcess {
    private static final int PIPE_SIZE = 1024;

    private class ChildReader extends Thread {
        final InputStream mStream;
        final StringBuilder mBuffer;

        ChildReader(final InputStream is, final StringBuilder buf) {
            mStream = is;
            mBuffer = buf;
        }

        public void run() {
            byte[] buf = new byte[PIPE_SIZE];
            try {
                int len;
                String s;
                while ((len = mStream.read(buf)) != -1) {
                    s = new String(buf, 0, len);
                    mBuffer.append(s);
                }
            } catch (IOException e) {
                // Ignore
            } finally {
                try {
                    if (mStream != null) mStream.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
        }
    }

    private class ChildWriter extends Thread {
        final OutputStream mStream;
        final String mBuffer;

        ChildWriter(final OutputStream os, final String buf) {
            mStream = os;
            mBuffer = buf;
        }

        public void run() {
            int off = 0;
            byte[] buf = mBuffer.getBytes();
            try {
                while (off < buf.length) {
                    int len = Math.min(PIPE_SIZE, buf.length - off);
                    mStream.write(buf, off, len);
                    off += len;
                }
            } catch (IOException e) {
                // Ignore
            } finally {
                try {
                    if (mStream != null) mStream.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
        }
    }

    private final long mStartTime;

    private Process mChildProc;
    private ChildWriter mChildStdinWriter;
    private ChildReader mChildStdoutReader;
    private ChildReader mChildStderrReader;
    private StringBuilder mChildStdout;
    private StringBuilder mChildStderr;
    private int mExitValue;
    private long mEndTime;

    public ChildProcess(final String[] cmdarray, final String childStdin) {
        mStartTime = nanoTime();
        try {
            mChildProc = Runtime.getRuntime().exec(cmdarray);
            if (childStdin != null) {
                mChildStdinWriter = new ChildWriter(mChildProc.getOutputStream(), childStdin);
                mChildStdinWriter.start();
            }
            mChildStdout = new StringBuilder();
            mChildStdoutReader = new ChildReader(mChildProc.getInputStream(), mChildStdout);
            mChildStdoutReader.start();
            mChildStderr = new StringBuilder();
            mChildStderrReader = new ChildReader(mChildProc.getErrorStream(), mChildStderr);
            mChildStderrReader.start();
        } catch (IOException e) {
            Logger.e(this, e.getMessage());
        }
    }

    public boolean isFinished() {
        boolean finished = true;
        if (mChildProc != null) {
            try {
                mChildProc.exitValue();
            } catch (final IllegalStateException e) {
                finished = false;
            }
        }
        return finished;
    }

    public int waitFinished() {
        while (mChildProc != null) {
            try {
                mExitValue = mChildProc.waitFor();
            } catch (InterruptedException ignored) { }
            mChildProc = null;

            try {
                mChildStderrReader.join();
            } catch (InterruptedException ignored) { } finally {
                mChildStderrReader = null;
            }

            try {
                mChildStdoutReader.join();
            } catch (InterruptedException ignored) { } finally {
                mChildStdoutReader = null;
            }

            if (mChildStdinWriter != null) {
                try {
                    mChildStdinWriter.join();
                } catch (InterruptedException ignored) { } finally {
                    mChildStdinWriter = null;
                }
            }

            mEndTime = nanoTime();
        }
        return mExitValue;
    }

    public CommandResult getResult() {
        if (!isFinished()) {
            throw new IllegalThreadStateException("Child process running");
        }

        final String childStdOut = mChildStdout != null ? mChildStdout.toString() : "";
        final String childStdErr = mChildStderr != null ? mChildStderr.toString() : "";

        return new CommandResult(
                mStartTime,
                mExitValue,
                childStdOut,
                childStdErr,
                mEndTime);
    }
}
