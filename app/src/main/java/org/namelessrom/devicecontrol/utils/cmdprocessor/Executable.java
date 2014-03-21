package org.namelessrom.devicecontrol.utils.cmdprocessor;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.util.Arrays;

/**
 * Created with IntelliJ IDEA.
 * User: jbird
 * Date: 1/22/13
 * Time: 5:19 AM
 */
public class Executable implements Parcelable {
    private String  TAG   = "Executable";
    private boolean DEBUG = false;

    private final String[] mCommandsArray;
    private final long     mStartTime;
    private       Status   mStatus;
    private long mFinishTime = -1;

    public enum Status {
        Queried, // Executable default status
        Staged, // beginning execution
        Finished // done.
    }

    public Executable(String... commands) {
        this.mCommandsArray = commands;
        this.mStartTime = System.nanoTime();
        this.mStatus = Status.Queried;
    }

    public Executable(Parcel parcel) {
        this.mCommandsArray = parcel.createStringArray();
        this.mStartTime = parcel.readLong();
        this.mStatus = Status.valueOf(parcel.readString());
        this.mFinishTime = parcel.readLong();
    }

    public long getStartTime() {
        return mStartTime;
    }

    public String[] getCommandsArray() {
        // if the status is not finished we
        // now consider the script Staged
        if (getStatus() != Status.Finished) {
            setStatus(Status.Staged);
        }
        return mCommandsArray;
    }

    public Status getStatus() {
        return mStatus;
    }

    public Executable toggleDebug(boolean debug) {
        this.DEBUG = debug;
        return this;
    }

    public Executable setStatus(Status status) {
        this.mStatus = status;
        return this;
    }

    public long getFinishTime() {
        return mFinishTime;
    }

    public Executable setFinishTime(long finishTime) {
        mFinishTime = finishTime;
        if (DEBUG) {
            Log.d(TAG, "Executable completed execution in " + (mFinishTime - mStartTime) + " ms");
        }
        if (Status.Staged == mStatus) {
            setStatus(Status.Finished);
        }
        return this;
    }

    // parcel implementation
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeStringArray(mCommandsArray);
        parcel.writeLong(mStartTime);
        parcel.writeString(mStatus.toString());
        parcel.writeLong(mFinishTime);
    }

    public static final Creator<Executable> CREATOR = new Creator<Executable>() {
        public Executable createFromParcel(Parcel in) {
            return new Executable(in);
        }

        public Executable[] newArray(int size) {
            return new Executable[size];
        }
    };

    @Override
    public String toString() {
        return "Executable{" +
                "TAG='" + TAG + '\'' +
                ", mCommandsArray=" + (mCommandsArray == null ? null
                : Arrays.asList(mCommandsArray)) +
                ", mStartTime=" + mStartTime +
                ", mStatus=" + mStatus +
                ", mFinishTime=" + mFinishTime +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Executable)) return false;
        Executable that = (Executable) o;
        if (mFinishTime != that.mFinishTime) return false;
        if (mStartTime != that.mStartTime) return false;
        if (!TAG.equals(that.TAG)) return false;
        if (!Arrays.equals(mCommandsArray, that.mCommandsArray)) return false;
        if (mStatus != that.mStatus) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = TAG.hashCode();
        result = 31 * result + Arrays.hashCode(mCommandsArray);
        result = 31 * result + (int) (mStartTime ^ (mStartTime >>> 32));
        result = 31 * result + mStatus.hashCode();
        result = 31 * result + (int) (mFinishTime ^ (mFinishTime >>> 32));
        return result;
    }
}
