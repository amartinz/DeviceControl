/*
 *  Copyright (C) 2013 - 2014 Alexander "Evisceration" Martinz
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
 *
 */
package org.namelessrom.devicecontrol.objects;

import android.text.TextUtils;

import org.namelessrom.devicecontrol.utils.Utils;

public class CpuCore {
    public String core;
    public int max;
    public int current;
    public String governor;

    public CpuCore(String core, String current, String max, String governor) {
        setCore(core);
        setCurrent(current);
        setMax(max);
        setGovernor(governor);
    }

    public CpuCore setCore(String core) {
        this.core = (!TextUtils.isEmpty(core) ? core : "0");
        return this;
    }

    public CpuCore setCurrent(String current) {
        this.current = Utils.tryParse(current, 0);
        return this;
    }

    public CpuCore setMax(String max) {
        this.max = Utils.tryParse(max, 0);
        return this;
    }

    public CpuCore setGovernor(String governor) {
        this.governor = (!TextUtils.isEmpty(governor) ? governor : "0");
        return this;
    }

    @Override public String toString() {
        return String.format("core: %s | max: %s | current: %s | gov: %s",
                core, max, current, governor);
    }
}
