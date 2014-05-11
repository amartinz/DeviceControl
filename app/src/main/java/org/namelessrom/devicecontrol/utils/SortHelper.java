package org.namelessrom.devicecontrol.utils;

import org.namelessrom.devicecontrol.objects.AppItem;

import java.util.Arrays;
import java.util.List;

/**
 * Created by alex on 04.05.14.
 */
public class SortHelper {

    public static List<AppItem> sortApplications(final List<AppItem> appList) {
        final AppItem[] list = appList.toArray(new AppItem[appList.size()]);
        final int length = list.length;
        AppItem tmp;
        for (int i = 0; i < length - 1; i++) {
            for (int j = i + 1; j < length; j++) {
                if (list[i].getLabel().compareToIgnoreCase(list[j].getLabel()) > 0) {
                    tmp = list[i];
                    list[i] = list[j];
                    list[j] = tmp;
                }
            }
        }

        return Arrays.asList(list);
    }

}
