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

import org.namelessrom.devicecontrol.modules.tasker.TaskerItem;

public interface SetupDataCallbacks {
    void onPageLoaded(Page page);

    void onPageTreeChanged();

    void onPageFinished(Page page);

    Page getPage(String key);

    TaskerItem getSetupData();

    void setSetupData(final TaskerItem item);
}
