/*
 * Copyright (C) 2013 The MoKee OpenSource Project
 * Modifications Copyright (C) 2014 The NamelessRom Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.namelessrom.devicecontrol.modules.wizard.setup;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;

public abstract class Page implements PageNode {

    public static final String KEY_PAGE_ARGUMENT = "key_arg";

    private SetupDataCallbacks mCallbacks;

    private Bundle mData = new Bundle();
    private String mTitle;
    private int mTitleResourceId;
    private boolean mRequired = false;
    private boolean mCompleted = false;

    protected Page(Context context, SetupDataCallbacks callbacks, int titleResourceId) {
        mCallbacks = callbacks;
        mTitleResourceId = titleResourceId;
        mTitle = context.getString(mTitleResourceId);
    }

    public Bundle getData() {
        return mData;
    }

    public String getTitle() {
        return mTitle;
    }

    public boolean isRequired() {
        return mRequired;
    }

    @Override
    public Page findPage(String key) {
        return getKey().equals(key) ? this : null;
    }

    @Override
    public Page findPage(int id) {
        return getId() == id ? this : null;
    }

    public abstract Fragment createFragment();

    public abstract int getNextButtonResId();

    public int getId() {
        return mTitleResourceId;
    }

    public String getKey() {
        return mTitle;
    }

    public boolean isCompleted() {
        return mCompleted;
    }

    public void setCompleted(boolean completed) {
        mCompleted = completed;
    }

    public void resetData(Bundle data) {
        mData = data;
        notifyDataChanged();
    }

    public void notifyDataChanged() {
        mCallbacks.onPageLoaded(this);
    }

    public Page setRequired(boolean required) {
        mRequired = required;
        return this;
    }

    public abstract void refresh();
}
