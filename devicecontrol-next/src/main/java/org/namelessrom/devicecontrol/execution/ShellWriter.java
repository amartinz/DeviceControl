/*
 * Copyright (C) 2013 - 2015 Alexander Martinz
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.namelessrom.devicecontrol.execution;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;

import alexander.martinz.libs.hardware.utils.IoUtils;
import alexander.martinz.libs.logger.Logger;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by alex on 25.08.15.
 */
public class ShellWriter {
    private String value;
    private File file;

    private boolean useRoot;

    @Override public String toString() {
        return String.format("value: %s | file: %s | useRoot: %s", value, file, useRoot);
    }

    private ShellWriter() {
        useRoot = true;
    }

    public static ShellWriter with() {
        return new ShellWriter();
    }

    public ShellWriter write(@NonNull String value) {
        this.value = value;
        return this;
    }

    public ShellWriter into(@NonNull String path) {
        return into(new File(path));
    }

    public ShellWriter into(@NonNull File file) {
        this.file = file;
        return this;
    }

    public ShellWriter disableRoot() {
        this.useRoot = false;
        return this;
    }

    public ShellWriter enableRoot() {
        this.useRoot = true;
        return this;
    }

    public Observable<Boolean> create() {
        final ShellWriter shellWriter = this;
        Logger.v(this, shellWriter.toString());
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override public void call(Subscriber<? super Boolean> subscriber) {
                if (subscriber.isUnsubscribed()) {
                    return;
                }

                // do our expensive operation
                final boolean success = IoUtils.writeToFile(shellWriter.file, shellWriter.value, shellWriter.useRoot);

                // notify the subscriber
                subscriber.onNext(success);

                // tell the subscriber we are done
                if (!subscriber.isUnsubscribed()) {
                    subscriber.onCompleted();
                }
            }
        });
    }

    public Observable<Boolean> start() {
        return start(null);
    }

    public Observable<Boolean> start(@Nullable final Action1<Boolean> action) {
        final Observable<Boolean> observable = create().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
        if (action == null) {
            observable.subscribe();
        } else {
            observable.subscribe(action);
        }
        return observable;
    }

}
