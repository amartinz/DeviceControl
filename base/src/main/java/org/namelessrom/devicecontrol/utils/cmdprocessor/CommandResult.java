package org.namelessrom.devicecontrol.utils.cmdprocessor;

import android.os.Parcel;
import android.os.Parcelable;

import org.namelessrom.devicecontrol.Logger;

@SuppressWarnings("AccessOfSystemProperties")
public class CommandResult implements Parcelable {
    private final long mStartTime;
    private final int mExitValue;
    private final String mStdout;
    private final String mStderr;
    private final long mEndTime;

    public CommandResult(final long startTime, final int exitValue, final String stdout,
            final String stderr, final long endTime) {
        mStartTime = startTime;
        mExitValue = exitValue;
        mStdout = stdout;
        mStderr = stderr;
        mEndTime = endTime;

        Logger.d(this, "Time to execute: " + (mEndTime - mStartTime) + " ns (nanoseconds)");
    }

    // pretty much just forward the constructor from parcelable to our main
    // loading constructor
    @SuppressWarnings("CastToConcreteClass")
    public CommandResult(final Parcel inParcel) {
        this(inParcel.readLong(),
                inParcel.readInt(),
                inParcel.readString(),
                inParcel.readString(),
                inParcel.readLong());
    }

    public boolean success() {
        return (mExitValue == 0);
    }

    public long getEndTime() {
        return mEndTime;
    }

    public String getStderr() {
        return mStderr;
    }

    public String getStdout() {
        return mStdout;
    }

    public Integer getExitValue() {
        return mExitValue;
    }

    public long getStartTime() {
        return mStartTime;
    }

    // implement parcelable
    @Override public int describeContents() {
        return 0;
    }

    @Override public void writeToParcel(final Parcel parcel, final int i) {
        parcel.writeLong(mStartTime);
        parcel.writeInt(mExitValue);
        parcel.writeString(mStdout);
        parcel.writeString(mStderr);
        parcel.writeLong(mEndTime);
    }

    @Override public String toString() {
        return "CommandResult{" +
                ", mStartTime=" + mStartTime +
                ", mExitValue=" + mExitValue +
                ", stdout='" + mStdout + '\'' +
                ", stderr='" + mStderr + '\'' +
                ", mEndTime=" + mEndTime +
                '}';
    }

    @Override public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof CommandResult)) return false;

        final CommandResult that = (CommandResult) o;

        return (mStartTime == that.mStartTime &&
                mExitValue == that.mExitValue &&
                mStdout.equals(that.mStdout) &&
                mStderr.equals(that.mStderr) &&
                mEndTime == that.mEndTime);
    }

    @Override public int hashCode() {
        int result = 0;
        result = 31 * result + (int) (mStartTime ^ (mStartTime >>> 32));
        result = 31 * result + mExitValue;
        result = 31 * result + (mStdout != null ? mStdout.hashCode() : 0);
        result = 31 * result + (mStderr != null ? mStderr.hashCode() : 0);
        result = 31 * result + (int) (mEndTime ^ (mEndTime >>> 32));
        return result;
    }
}
