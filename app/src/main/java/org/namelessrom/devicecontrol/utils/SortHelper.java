package org.namelessrom.devicecontrol.utils;

import org.namelessrom.devicecontrol.objects.AppItem;

import java.io.File;
import java.text.Collator;
import java.util.Comparator;

/**
 * Created by alex on 04.05.14.
 */
public class SortHelper {

    public static final Comparator<AppItem> sAppComparator = new Comparator<AppItem>() {
        public final int compare(final AppItem a, final AppItem b) {
            return collator.compare(a.getLabel(), b.getLabel());
        }

        private final Collator collator = Collator.getInstance();
    };

    public static final Comparator<File> sFileComparator = new Comparator<File>() {
        @Override
        public int compare(final File lhs, final File rhs) {
            return lhs.getName().toLowerCase().compareTo(rhs.getName().toLowerCase());
        }
    };

}
