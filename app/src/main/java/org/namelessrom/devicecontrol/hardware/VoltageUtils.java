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
package org.namelessrom.devicecontrol.hardware;

import org.namelessrom.devicecontrol.modules.cpu.CpuUtils;
import org.namelessrom.devicecontrol.utils.Utils;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class VoltageUtils {

    //----------------------------------------------------------------------------------------------
    public static final String UV_TABLE_FILE = CpuUtils.CPU_BASE + "cpu0/cpufreq/UV_mV_table";
    public static final String VDD_TABLE_FILE = CpuUtils.CPU_BASE + "cpufreq/vdd_table/vdd_levels";
    private static VoltageUtils sInstance;

    private VoltageUtils() { }

    public static VoltageUtils get() {
        if (sInstance == null) {
            sInstance = new VoltageUtils();
        }
        return sInstance;
    }

    //==============================================================================================
    // Methods
    //==============================================================================================
    public static boolean isSupported() {
        return Utils.fileExists(VoltageUtils.UV_TABLE_FILE)
                || Utils.fileExists(VoltageUtils.VDD_TABLE_FILE);
    }

    public String[] getUvValues(final boolean getName) throws IOException {
        final ArrayList<String> valueList = new ArrayList<>();
        FileInputStream fstream = null;
        DataInputStream in = null;
        BufferedReader br = null;
        try {
            File f = new File(VDD_TABLE_FILE);
            if (f.exists()) {
                fstream = new FileInputStream(f);
            } else {
                f = new File(UV_TABLE_FILE);
                if (f.exists()) {
                    fstream = new FileInputStream(f);
                }
            }

            if (fstream != null) {
                in = new DataInputStream(fstream);
                br = new BufferedReader(new InputStreamReader(in));
                String strLine;
                String[] values;
                while ((strLine = br.readLine()) != null) {
                    strLine = strLine.trim();
                    if ((strLine.length() != 0)) {
                        if (getName) {
                            values = strLine.replaceAll(":", "").split("\\s+");
                            valueList.add(values[0]);
                        } else {
                            values = strLine.split("\\s+");
                            valueList.add(values[1]);
                        }
                    }
                }
            }
        } finally {
            if (br != null) br.close();
            if (in != null) in.close();
            if (fstream != null) fstream.close();
        }

        return valueList.toArray(new String[valueList.size() - 1]);
    }
}
