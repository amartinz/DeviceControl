package org.namelessrom.devicecontrol.utils;

import android.content.Context;
import android.support.annotation.MenuRes;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.widget.PopupMenu;

/**
 * Created by alex on 18.08.15.
 */
public class MenuHelper {
    @NonNull public static Menu inflateMenu(Context context, @MenuRes int menuResId) {
        final PopupMenu popupMenu = new PopupMenu(context, null);
        popupMenu.inflate(menuResId);
        return popupMenu.getMenu();
    }
}
