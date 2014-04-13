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
import org.namelessrom.devicecontrol.utils.ActionProcessor;
import org.namelessrom.devicecontrol.utils.CpuUtils;
import org.namelessrom.devicecontrol.wizard.model.AbstractWizardModel;
import org.namelessrom.devicecontrol.wizard.model.BranchPage;
import org.namelessrom.devicecontrol.wizard.model.PageList;
import org.namelessrom.devicecontrol.wizard.model.SingleFixedChoicePage;

public class TaskerWizardModel extends AbstractWizardModel {
    public TaskerWizardModel(final Context context) {
        super(context);
    }

    private BranchPage getActionPage() {
        final BranchPage actionPage = new BranchPage(this, "3) " + getString(R.string.action));

        final String[] frequencies = CpuUtils.getAvailableFrequencies();
        final String[] governors = CpuUtils.getAvailableGovernors().split(" ");
        final String[] ioschedulers = CpuUtils.getAvailableIOSchedulers();

        if (frequencies != null) {
            actionPage.addBranch(ActionProcessor.ACTION_CPU_FREQUENCY_MAX,
                    new SingleFixedChoicePage(this, "4) " + getString(R.string.value))
                            .setChoices(frequencies)
                            .setRequired(true)
            );
            actionPage.addBranch(ActionProcessor.ACTION_CPU_FREQUENCY_MIN,
                    new SingleFixedChoicePage(this, "4) " + getString(R.string.value))
                            .setChoices(frequencies)
                            .setRequired(true)
            );
        }

        if (governors != null) {
            actionPage.addBranch(ActionProcessor.ACTION_CPU_GOVERNOR,
                    new SingleFixedChoicePage(this, "4) " + getString(R.string.value))
                            .setChoices(governors)
                            .setRequired(true)
            );
        }

        if (ioschedulers != null) {
            actionPage.addBranch(ActionProcessor.ACTION_IO_SCHEDULER,
                    new SingleFixedChoicePage(this, "4) " + getString(R.string.value))
                            .setChoices(ioschedulers)
                            .setRequired(true)
            );
        }

        return actionPage;
    }

    private String getString(final int id) {
        return Application.applicationContext.getString(id);
    }

    @Override
    protected PageList onNewRootPageList() {
        return new PageList(
                new BranchPage(this, "1) " + getString(R.string.task_type))
                        .addBranch(getString(R.string.event_based),
                                new SingleFixedChoicePage(this,
                                        "2) " + getString(R.string.category))
                                        .setChoices(ActionProcessor.CATEGORIES)
                                        .setRequired(true),
                                getActionPage()
                        )
                        .setRequired(true)
        );
    }
}
