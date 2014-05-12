package org.namelessrom.devicecontrol.utils;

import org.namelessrom.devicecontrol.objects.AppItem;

import java.text.Collator;
import java.util.Comparator;

/**
 * Created by alex on 04.05.14.
 */
public class SortHelper {

    public final static Comparator<AppItem> sAppcomparator = new Comparator<AppItem>() {
        public final int compare(AppItem a, AppItem b) {
            return collator.compare(a.getLabel(), b.getLabel());
        }

        private final Collator collator = Collator.getInstance();
    };

}
