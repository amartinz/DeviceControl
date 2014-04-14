/*
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.namelessrom.devicecontrol.wizard;

import android.content.Context;

import org.namelessrom.devicecontrol.Application;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.database.TaskerItem;
import org.namelessrom.devicecontrol.utils.ActionProcessor;
import org.namelessrom.devicecontrol.utils.CpuUtils;
import org.namelessrom.devicecontrol.wizard.model.AbstractWizardModel;
import org.namelessrom.devicecontrol.wizard.model.BranchPage;
import org.namelessrom.devicecontrol.wizard.model.Page;
import org.namelessrom.devicecontrol.wizard.model.PageList;
import org.namelessrom.devicecontrol.wizard.model.SingleFixedChoicePage;

public class TaskerWizardModel extends AbstractWizardModel {
    private TaskerItem mItem;
    private String mCategory = "";
    private String mAction   = "";
    private String mValue    = "";

    public TaskerWizardModel(final Context context) {
        this(context, null);
    }

    public TaskerWizardModel(final Context context, final TaskerItem item) {
        super(context);

        mItem = item;
        if (mItem != null) {
            mCategory = mItem.getCategory();
            mAction = mItem.getName();
            mValue = mItem.getValue();
        }

        setRootPageList(getRootPageList());
    }

    public TaskerItem getItem() {
        return mItem;
    }

    private Page getCategoryPage() {
        return new SingleFixedChoicePage(this,
                "1) " + getString(R.string.category))
                .setChoices(ActionProcessor.CATEGORIES)
                .setValue(mCategory)
                .setRequired(true);
    }

    private Page getActionPage() {
        final BranchPage actionPage = new BranchPage(this, "2) " + getString(R.string.action));

        final String[] freqs = CpuUtils.getAvailableFrequencies();
        final String[] governors = CpuUtils.getAvailableGovernors().split(" ");
        final String[] ioschedulers = CpuUtils.getAvailableIOSchedulers();

        if (freqs != null) {
            actionPage.addBranch(ActionProcessor.ACTION_CPU_FREQUENCY_MAX, getValuePage(freqs));
            actionPage.addBranch(ActionProcessor.ACTION_CPU_FREQUENCY_MIN, getValuePage(freqs));
        }

        if (governors != null) {
            actionPage.addBranch(ActionProcessor.ACTION_CPU_GOVERNOR, getValuePage(governors));
        }

        if (ioschedulers != null) {
            actionPage.addBranch(ActionProcessor.ACTION_IO_SCHEDULER, getValuePage(ioschedulers));
        }

        actionPage.setValue(mAction);

        return actionPage;
    }

    private Page getValuePage(final String... choices) {
        return new SingleFixedChoicePage(this, "3) " + getString(R.string.value))
                .setChoices(choices)
                .setValue(mValue)
                .setRequired(true);
    }

    private String getString(final int id) {
        return Application.applicationContext.getString(id);
    }

    private PageList getRootPageList() {
        PageList pageList;

        if (mItem == null) {
            pageList = new PageList(getCategoryPage(), getActionPage());
        } else {
            pageList = new PageList(
                    new BranchPage(this, "0) " + getString(R.string.edit))
                            .addBranch(getString(R.string.edit_task),
                                    getCategoryPage(),
                                    getActionPage())
                            .addBranch(getString(R.string.delete_task))
                            .setRequired(true)
            );
        }

        return pageList;
    }
}
