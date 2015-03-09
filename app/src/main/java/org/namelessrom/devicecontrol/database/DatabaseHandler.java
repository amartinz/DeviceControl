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
package org.namelessrom.devicecontrol.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;
import android.util.Log;

import org.namelessrom.devicecontrol.Application;
import org.namelessrom.devicecontrol.DeviceConstants;
import org.namelessrom.devicecontrol.Logger;
import org.namelessrom.devicecontrol.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 9;
    private static final String DATABASE_NAME = "DeviceControl.db";

    private static final String KEY_ID = "id";
    private static final String KEY_CATEGORY = "category";
    private static final String KEY_NAME = "name";
    private static final String KEY_FILENAME = "filename";
    private static final String KEY_TRIGGER = "trigger";
    private static final String KEY_VALUE = "value";
    private static final String KEY_ENABLED = "enabled";

    public static final String TABLE_BOOTUP = "boot_up";
    public static final String TABLE_DC = "devicecontrol";
    public static final String TABLE_TASKER = "tasker";

    public static final String CATEGORY_DEVICE = "device";
    public static final String CATEGORY_CPU = "cpu";
    public static final String CATEGORY_GPU = "gpu";
    public static final String CATEGORY_EXTRAS = "extras";
    public static final String CATEGORY_SYSCTL = "sysctl";

    private static final String CREATE_BOOTUP_TABLE = "CREATE TABLE " + TABLE_BOOTUP + '('
            + KEY_ID + " INTEGER PRIMARY KEY," + KEY_CATEGORY + " TEXT," + KEY_NAME + " TEXT,"
            + KEY_FILENAME + " TEXT," + KEY_VALUE + " TEXT)";
    private static final String DROP_BOOTUP_TABLE = "DROP TABLE IF EXISTS " + TABLE_BOOTUP;

    private static final String CREATE_DEVICE_CONTROL_TABLE = "CREATE TABLE " + TABLE_DC + '('
            + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT," + KEY_VALUE + " TEXT)";
    private static final String DROP_DEVICE_CONTROL_TABLE = "DROP TABLE IF EXISTS " + TABLE_DC;

    private static final String CREATE_TASKER_TABLE = "CREATE TABLE " + TABLE_TASKER + '('
            + KEY_ID + " INTEGER PRIMARY KEY," + KEY_CATEGORY + " TEXT," + KEY_NAME + " TEXT,"
            + KEY_TRIGGER + " TEXT," + KEY_VALUE + " TEXT," + KEY_ENABLED + " TEXT)";
    private static final String DROP_TASKER_TABLE = "DROP TABLE IF EXISTS " + TABLE_TASKER;

    private static DatabaseHandler sDatabaseHandler = null;
    private static SQLiteDatabase sDb;

    private DatabaseHandler(final Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        try {
            sDb = getWritableDatabase();
        } catch (SQLiteException sqle) {
            Logger.wtf(this, "Could not get writable database.", sqle);
        }
    }

    public static synchronized DatabaseHandler getInstance() {
        if (sDatabaseHandler == null) {
            sDatabaseHandler = new DatabaseHandler(Application.get());
        }
        return sDatabaseHandler;
    }

    public static synchronized void tearDown() {
        Logger.i(DatabaseHandler.class, "tearDown()");
        if (sDb != null) {
            sDb.close();
            sDb = null;
        }
        sDatabaseHandler = null;
    }

    @Override
    public void onCreate(final SQLiteDatabase db) {
        db.execSQL(CREATE_BOOTUP_TABLE);
        db.execSQL(CREATE_DEVICE_CONTROL_TABLE);
        db.execSQL(CREATE_TASKER_TABLE);
    }

    @Override
    public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        Log.e("DeviceControl", "onUpgrade"
                + " | oldVersion: " + String.valueOf(oldVersion)
                + " | newVersion: " + String.valueOf(newVersion));
        int currentVersion = oldVersion;

        if (currentVersion < 1) {
            db.execSQL(DROP_BOOTUP_TABLE);
            db.execSQL(CREATE_BOOTUP_TABLE);
            currentVersion = 1;
        }

        if (currentVersion < 3) { // new tasker table, renamed bootup table
            db.execSQL(DROP_BOOTUP_TABLE);
            db.execSQL(CREATE_BOOTUP_TABLE);
            db.execSQL(DROP_TASKER_TABLE);
            db.execSQL(CREATE_TASKER_TABLE);
            currentVersion = 3;
        }

        if (currentVersion < 8) {
            db.execSQL(DROP_BOOTUP_TABLE);
            db.execSQL(CREATE_BOOTUP_TABLE);
            db.execSQL(DROP_DEVICE_CONTROL_TABLE);
            db.execSQL(CREATE_DEVICE_CONTROL_TABLE);
            currentVersion = 8;
        }

        if (currentVersion < 9) {
            db.execSQL(String.format("ALTER TABLE %s RENAME TO tmp;", TABLE_TASKER));
            db.execSQL(CREATE_TASKER_TABLE);
            db.execSQL(String.format(
                    "INSERT INTO %s(%s,%s,%s,%s,%s) SELECT %s,%s,%s,%s,%s FROM tmp;",
                    TABLE_TASKER,
                    KEY_ID, KEY_CATEGORY, KEY_NAME, KEY_VALUE, KEY_ENABLED,
                    KEY_ID, KEY_CATEGORY, KEY_NAME, KEY_VALUE, KEY_ENABLED));
            db.execSQL("DROP TABLE tmp;");
        }

        if (currentVersion != DATABASE_VERSION) {
            wipeDb(db);
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.e("DeviceControl", "onDowngrade"
                + " | oldVersion: " + String.valueOf(oldVersion)
                + " | newVersion: " + String.valueOf(newVersion));
        wipeDb(db);
        try {
            final File downgradeFile = new File(Application.get().getFilesDirectory(),
                    DeviceConstants.DC_DOWNGRADE);
            Logger.d(this, "creating downgrade file -> %s", downgradeFile.createNewFile());
        } catch (Exception e) {
            Logger.e(this, "could not create downgrade file", e);
        }
    }

    private void wipeDb(final SQLiteDatabase db) {
        db.execSQL(DROP_BOOTUP_TABLE);
        db.execSQL(DROP_DEVICE_CONTROL_TABLE);
        db.execSQL(DROP_TASKER_TABLE);
        onCreate(db);
    }

    //==============================================================================================
    // All CRUD(Create, Read, Update, Delete) Operations
    //==============================================================================================

    @Nullable public String getValueByName(final String name, final String tableName) {
        if (sDb == null) return null;

        final Cursor cursor = sDb.query(tableName, new String[]{ KEY_VALUE }, KEY_NAME + "=?",
                new String[]{ name }, null, null, null, null
        );
        if (cursor != null) { cursor.moveToFirst(); }
        if (cursor == null) return null;

        final String result = ((cursor.getCount() <= 0)
                ? null : cursor.getString(cursor.getColumnIndex(KEY_VALUE)));

        cursor.close();
        return result;
    }

    public boolean insertOrUpdate(final String name, final String value,
            final String tableName) {
        if (sDb == null) return false;

        final ContentValues values = new ContentValues();
        values.put(KEY_NAME, name);
        values.put(KEY_VALUE, value);

        sDb.delete(tableName, KEY_NAME + " = ?", new String[]{ name });
        sDb.insert(tableName, null, values);
        return true;
    }

    public boolean updateBootup(final DataItem item) {
        if (sDb == null) return false;

        final ContentValues values = new ContentValues(5);
        values.put(KEY_CATEGORY, item.getCategory());
        values.put(KEY_NAME, item.getName());
        values.put(KEY_FILENAME, item.getFileName());
        values.put(KEY_VALUE, item.getValue());

        sDb.delete(TABLE_BOOTUP, KEY_NAME + " = ?", new String[]{ item.getName() });
        sDb.insert(TABLE_BOOTUP, null, values);
        return true;
    }

    public List<DataItem> getAllItems(final String tableName, final String category) {
        final List<DataItem> itemList = new ArrayList<>();
        if (sDb == null) return itemList;

        final String selectQuery = "SELECT * FROM " + tableName + (category.isEmpty()
                ? ""
                : " WHERE " + KEY_CATEGORY + " = '" + category + '\'');

        final Cursor cursor = sDb.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            DataItem item;
            do {
                item = new DataItem();
                item.setID(Utils.parseInt(cursor.getString(0)));
                item.setCategory(cursor.getString(1));
                item.setName(cursor.getString(2));
                item.setFileName(cursor.getString(3));
                item.setValue(cursor.getString(4));
                itemList.add(item);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return itemList;
    }

    //----------------------------------------------------------------------------------------------
    // Tasker
    //----------------------------------------------------------------------------------------------
    public List<TaskerItem> getAllTaskerItems(final String category) {
        final List<TaskerItem> itemList = new ArrayList<>();
        if (sDb == null) return itemList;

        final String selectQuery = "SELECT * FROM " + TABLE_TASKER + (category.isEmpty()
                ? ""
                : " WHERE " + KEY_CATEGORY + " = '" + category + '\'');

        final Cursor cursor = sDb.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            TaskerItem item;
            do {
                item = new TaskerItem();
                item.id = Utils.parseInt(cursor.getString(0));
                item.category = cursor.getString(1);
                item.name = cursor.getString(2);
                item.trigger = cursor.getString(3);
                item.value = cursor.getString(4);
                final String enabled = cursor.getString(5);
                item.enabled = (enabled != null && enabled.equals("1"));
                itemList.add(item);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return itemList;
    }

    public List<TaskerItem> getAllTaskerItemsByTrigger(final String trigger) {
        final List<TaskerItem> itemList = new ArrayList<>();
        if (sDb == null) return itemList;

        final String selectQuery = "SELECT * FROM " + TABLE_TASKER + (trigger.isEmpty()
                ? ""
                : " WHERE " + KEY_TRIGGER + " = '" + trigger + '\'');

        final Cursor cursor = sDb.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            TaskerItem item;
            do {
                item = new TaskerItem();
                item.id = Utils.parseInt(cursor.getString(0));
                item.category = cursor.getString(1);
                item.name = cursor.getString(2);
                item.trigger = cursor.getString(3);
                item.value = cursor.getString(4);
                final String enabled = cursor.getString(5);
                item.enabled = (enabled != null && enabled.equals("1"));
                itemList.add(item);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return itemList;
    }

    public int updateOrInsertTaskerItem(final TaskerItem item) {
        if (sDb == null) return -1;

        final ContentValues values = new ContentValues();
        values.put(KEY_CATEGORY, item.category);
        values.put(KEY_NAME, item.name);
        values.put(KEY_VALUE, item.value);
        values.put(KEY_TRIGGER, item.trigger);
        values.put(KEY_ENABLED, (item.enabled ? "1" : "0"));

        final int rowsAffected = sDb.update(TABLE_TASKER, values, KEY_ID + " = ?",
                new String[]{ String.valueOf(item.id) });

        if (rowsAffected <= 0) {
            sDb.insert(TABLE_TASKER, null, values);
            return 1;
        }

        return rowsAffected;
    }

    public boolean deleteTaskerItem(final TaskerItem item) {
        if (sDb == null) return false;

        sDb.delete(TABLE_TASKER, KEY_ID + " = ?", new String[]{ String.valueOf(item.id) });
        return true;
    }

}
