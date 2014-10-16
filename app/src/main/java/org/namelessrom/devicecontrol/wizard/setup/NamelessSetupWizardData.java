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

package org.namelessrom.devicecontrol.wizard.setup;

import android.content.Context;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.wizard.pages.ActionPage;
import org.namelessrom.devicecontrol.wizard.pages.CategoryPage;
import org.namelessrom.devicecontrol.wizard.pages.FinishPage;
import org.namelessrom.devicecontrol.wizard.pages.InformationPage;
import org.namelessrom.devicecontrol.wizard.pages.TriggerPage;
import org.namelessrom.devicecontrol.wizard.pages.ValuePage;

public class NamelessSetupWizardData extends AbstractSetupData {

    public NamelessSetupWizardData(final Context context) { super(context); }

    @Override protected PageList onNewPageList() {
        return new PageList(
                new InformationPage(mContext, this, R.string.setup_welcome),
                new TriggerPage(mContext, this, R.string.setup_trigger),
                new CategoryPage(mContext, this, R.string.setup_category),
                new ActionPage(mContext, this, R.string.setup_action),
                new ValuePage(mContext, this, R.string.setup_value),
                new FinishPage(mContext, this, R.string.setup_complete)
        );
    }


}
