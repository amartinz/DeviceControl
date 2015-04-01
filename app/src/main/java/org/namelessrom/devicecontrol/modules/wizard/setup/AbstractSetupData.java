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

import org.namelessrom.devicecontrol.modules.tasker.TaskerItem;

import java.util.ArrayList;

public abstract class AbstractSetupData implements SetupDataCallbacks {
    protected Context mContext;
    private ArrayList<SetupDataCallbacks> mListeners = new ArrayList<>();
    private PageList mPageList;
    private TaskerItem item = new TaskerItem();

    public AbstractSetupData(Context context) {
        mContext = context;
        mPageList = onNewPageList();
    }

    protected abstract PageList onNewPageList();

    @Override public void onPageLoaded(Page page) {
        for (SetupDataCallbacks mListener : mListeners) {
            mListener.onPageLoaded(page);
        }
    }

    @Override public void onPageTreeChanged() {
        for (SetupDataCallbacks mListener : mListeners) {
            mListener.onPageTreeChanged();
        }
    }

    @Override public void onPageFinished(Page page) {
        for (SetupDataCallbacks mListener : mListeners) {
            mListener.onPageFinished(page);
        }
    }

    @Override public Page getPage(String key) {
        return findPage(key);
    }

    @Override public void setSetupData(final TaskerItem item) {
        this.item = item;
    }

    @Override public TaskerItem getSetupData() {
        return item;
    }

    public Page findPage(String key) {
        return mPageList.findPage(key);
    }

    public void load(Bundle savedValues) {
        for (String key : savedValues.keySet()) {
            mPageList.findPage(key).resetData(savedValues.getBundle(key));
        }
    }

    public Bundle save() {
        Bundle bundle = new Bundle();
        for (Page page : getPageList()) {
            bundle.putBundle(page.getKey(), page.getData());
        }
        return bundle;
    }

    public void addPage(int index, Page page) {
        mPageList.add(index, page);
        onPageTreeChanged();
    }

    public void removePage(Page page) {
        mPageList.remove(page);
        onPageTreeChanged();
    }

    public void registerListener(SetupDataCallbacks listener) {
        mListeners.add(listener);
    }

    public PageList getPageList() {
        return mPageList;
    }

    public void unregisterListener(SetupDataCallbacks listener) {
        mListeners.remove(listener);
    }
}
