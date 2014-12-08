package org.namelessrom.devicecontrol.hardware;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class VoltageUtils {

    //----------------------------------------------------------------------------------------------
    public static final String UV_TABLE_FILE  = CpuUtils.CPU_BASE + "cpu0/cpufreq/UV_mV_table";
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
