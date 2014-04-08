package org.namelessrom.devicecontrol.database;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;

import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler extends SQLiteOpenHelper implements DeviceConstants {

    private static final int    DATABASE_VERSION = 4;
    private static final String DATABASE_NAME    = "DeviceControl.db";

    private static final String KEY_ID       = "id";
    private static final String KEY_CATEGORY = "category";
    private static final String KEY_NAME     = "name";
    private static final String KEY_FILENAME = "filename";
    private static final String KEY_VALUE    = "value";
    private static final String KEY_ENABLED  = "enabled";

    public static final String TABLE_BOOTUP = "boot_up";
    public static final String TABLE_DC     = "devicecontrol";
    public static final String TABLE_TASKER = "tasker";

    private static final String CREATE_BOOTUP_TABLE = "CREATE TABLE " + TABLE_BOOTUP + '('
            + KEY_ID + " INTEGER PRIMARY KEY," + KEY_CATEGORY + " TEXT," + KEY_NAME + " TEXT,"
            + KEY_FILENAME + " TEXT," + KEY_VALUE + " TEXT)";
    private static final String DROP_BOOTUP_TABLE   = "DROP TABLE IF EXISTS " + TABLE_BOOTUP;

    private static final String CREATE_DEVICE_CONTROL_TABLE = "CREATE TABLE " + TABLE_DC + '('
            + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT," + KEY_VALUE + " TEXT)";
    private static final String DROP_DEVICE_CONTROL_TABLE   = "DROP TABLE IF EXISTS " + TABLE_DC;

    private static final String CREATE_TASKER_TABLE = "CREATE TABLE " + TABLE_TASKER + '('
            + KEY_ID + " INTEGER PRIMARY KEY," + KEY_CATEGORY + " TEXT," + KEY_NAME + " TEXT,"
            + KEY_FILENAME + " TEXT," + KEY_VALUE + " TEXT," + KEY_ENABLED + " TEXT)";
    private static final String DROP_TASKER_TABLE   = "DROP TABLE IF EXISTS " + TABLE_TASKER;

    private static DatabaseHandler sDatabaseHandler = null;
    private static Context mContext;

    private DatabaseHandler(final Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    public static DatabaseHandler getInstance(final Context context) {
        if (sDatabaseHandler == null) {
            sDatabaseHandler = new DatabaseHandler(context);
        }
        return sDatabaseHandler;
    }

    @Override
    public void onCreate(final SQLiteDatabase db) {
        db.execSQL(CREATE_BOOTUP_TABLE);
        db.execSQL(CREATE_DEVICE_CONTROL_TABLE);
        db.execSQL(CREATE_TASKER_TABLE);
    }

    @Override
    public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
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

        if (currentVersion < 4) {
            db.execSQL(DROP_DEVICE_CONTROL_TABLE);
            db.execSQL(CREATE_DEVICE_CONTROL_TABLE);
            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

            // Migrate from shared preferences to database
            insertOrUpdate(EXTENSIVE_LOGGING,
                    prefs.getBoolean(EXTENSIVE_LOGGING, false) ? "1" : "0",
                    TABLE_DC);
            insertOrUpdate(SHOW_LAUNCHER,
                    prefs.getBoolean(SHOW_LAUNCHER, false) ? "1" : "0",
                    TABLE_DC);
            insertOrUpdate(SOB_DEVICE,
                    prefs.getBoolean(SOB_DEVICE, false) ? "1" : "0",
                    TABLE_DC);
            insertOrUpdate(SOB_CPU,
                    prefs.getBoolean(SOB_CPU, false) ? "1" : "0",
                    TABLE_DC);
            insertOrUpdate(SOB_GPU,
                    prefs.getBoolean(SOB_DEVICE, false) ? "1" : "0",
                    TABLE_DC);
            insertOrUpdate(SOB_EXTRAS,
                    prefs.getBoolean(SOB_CPU, false) ? "1" : "0",
                    TABLE_DC);
            insertOrUpdate(SOB_VOLTAGE,
                    prefs.getBoolean(SOB_DEVICE, false) ? "1" : "0",
                    TABLE_DC);
            insertOrUpdate(SOB_VM,
                    prefs.getBoolean(SOB_VM, false) ? "1" : "0",
                    TABLE_DC);
            insertOrUpdate(SOB_SYSCTL,
                    prefs.getBoolean(SOB_SYSCTL, false) ? "1" : "0",
                    TABLE_DC);

            prefs.edit().clear().commit();
            currentVersion = 4;
        }
    }

    //==============================================================================================
    // All CRUD(Create, Read, Update, Delete) Operations
    //==============================================================================================

    public String getValueByName(final String name, final String tableName) {
        final SQLiteDatabase db = getReadableDatabase();

        if (db == null) return null;

        final Cursor cursor = db.query(tableName, new String[]{KEY_VALUE}, KEY_NAME + "=?",
                new String[]{name}, null, null, null, null
        );
        if (cursor != null) { cursor.moveToFirst(); }
        if (cursor == null) return null;

        return cursor.getCount() <= 0 ? null : cursor.getString(cursor.getColumnIndex(KEY_VALUE));
    }

    public boolean insertOrUpdate(final String name, final String value, final String tableName) {
        final SQLiteDatabase db = getReadableDatabase();

        if (db == null) return false;

        final ContentValues values = new ContentValues();
        values.put(KEY_NAME, name);
        values.put(KEY_VALUE, value);

        db.delete(tableName, KEY_NAME + " = ?", new String[]{name});
        db.insert(tableName, null, values);
        db.close();

        return true;
    }

    public boolean addItem(final DataItem item, final String tableName) {
        final SQLiteDatabase db = getWritableDatabase();

        if (db == null) return false;

        final ContentValues values = new ContentValues();
        values.put(KEY_CATEGORY, item.getCategory());
        values.put(KEY_NAME, item.getName());
        values.put(KEY_FILENAME, item.getFileName());
        values.put(KEY_VALUE, item.getValue());

        db.insert(tableName, null, values);
        db.close();
        return true;
    }

    public DataItem getItem(final int id, final String tableName) {
        final SQLiteDatabase db = getReadableDatabase();

        if (db == null) return null;

        final Cursor cursor = db.query(tableName, new String[]{
                        KEY_ID, KEY_CATEGORY, KEY_NAME, KEY_FILENAME, KEY_VALUE}, KEY_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null
        );
        if (cursor != null) { cursor.moveToFirst(); }
        if (cursor == null) return null;

        return new DataItem(Integer.parseInt(cursor.getString(0)), cursor.getString(1),
                cursor.getString(2), cursor.getString(3), cursor.getString(4));
    }

    public List<DataItem> getAllItems(final String tableName, final String category) {
        final List<DataItem> itemList = new ArrayList<DataItem>();
        final String selectQuery = "SELECT * FROM " + tableName + (category.isEmpty()
                ? ""
                : " WHERE " + KEY_CATEGORY + " = '" + category + '\'');

        final SQLiteDatabase db = getWritableDatabase();

        if (db == null) return null;

        final Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            DataItem item;
            do {
                item = new DataItem();
                item.setID(Integer.parseInt(cursor.getString(0)));
                item.setCategory(cursor.getString(1));
                item.setName(cursor.getString(2));
                item.setFileName(cursor.getString(3));
                item.setValue(cursor.getString(4));
                itemList.add(item);
            } while (cursor.moveToNext());
        }

        return itemList;
    }

    public int updateItem(final DataItem item, final String tableName) {
        final SQLiteDatabase db = this.getWritableDatabase();

        if (db == null) return -1;

        final ContentValues values = new ContentValues();
        values.put(KEY_CATEGORY, item.getCategory());
        values.put(KEY_NAME, item.getName());
        values.put(KEY_VALUE, item.getValue());
        values.put(KEY_FILENAME, item.getFileName());

        return db.update(tableName, values, KEY_ID + " = ?",
                new String[]{String.valueOf(item.getID())});
    }

    public boolean deleteItem(final DataItem item, final String tableName) {
        final SQLiteDatabase db = this.getWritableDatabase();

        if (db == null) return false;

        db.delete(tableName, KEY_ID + " = ?", new String[]{String.valueOf(item.getID())});
        db.close();
        return true;
    }

    public boolean deleteItemByName(final String name, final String tableName) {
        final SQLiteDatabase db = this.getWritableDatabase();

        if (db == null) return false;

        db.delete(tableName, KEY_NAME + " = ?", new String[]{name});
        db.close();
        return true;
    }

    public boolean deleteItemById(final int ID, final String tableName) {
        final SQLiteDatabase db = this.getWritableDatabase();

        if (db == null) return false;

        db.delete(tableName, KEY_ID + " = ?", new String[]{String.valueOf(ID)});
        db.close();
        return true;
    }

    public int getTableCount(final String tableName) {
        final String countQuery = "SELECT  * FROM " + tableName;
        final SQLiteDatabase db = this.getReadableDatabase();

        if (db == null) return -1;

        final Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();

        return count;
    }

    public boolean deleteAllItems(final String tableName) {
        SQLiteDatabase db = this.getWritableDatabase();

        if (db == null) return false;

        db.delete(tableName, null, null);
        db.close();
        return true;
    }

    //----------------------------------------------------------------------------------------------
    // Tasker
    //----------------------------------------------------------------------------------------------

    public boolean addTaskerItem(final TaskerItem item) {
        final SQLiteDatabase db = getWritableDatabase();

        if (db == null) return false;

        final ContentValues values = new ContentValues();
        values.put(KEY_CATEGORY, item.getCategory());
        values.put(KEY_NAME, item.getName());
        values.put(KEY_FILENAME, item.getFileName());
        values.put(KEY_VALUE, item.getValue());
        values.put(KEY_ENABLED, (item.getEnabled() ? "1" : "0"));

        db.insert(TABLE_TASKER, null, values);
        db.close();
        return true;
    }

    public TaskerItem getTaskerItem(final int id, final String tableName) {
        final SQLiteDatabase db = getReadableDatabase();

        if (db == null) return null;

        final Cursor cursor = db.query(tableName, new String[]{
                        KEY_ID, KEY_CATEGORY, KEY_NAME, KEY_FILENAME, KEY_VALUE, KEY_ENABLED
                }, KEY_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null
        );
        if (cursor != null) { cursor.moveToFirst(); }
        if (cursor == null) return null;

        final String enabled = cursor.getString(5);
        return new TaskerItem(Integer.parseInt(cursor.getString(0)), cursor.getString(1),
                cursor.getString(2), cursor.getString(3), cursor.getString(4),
                (enabled != null && enabled.equals("1")));
    }

    public List<TaskerItem> getAllTaskerItems(final String category) {
        final List<TaskerItem> itemList = new ArrayList<TaskerItem>();
        final String selectQuery = "SELECT * FROM " + TABLE_TASKER + (category.isEmpty()
                ? ""
                : " WHERE " + KEY_CATEGORY + " = '" + category + '\'');

        final SQLiteDatabase db = getWritableDatabase();

        if (db == null) return null;

        final Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            TaskerItem item;
            do {
                item = new TaskerItem();
                item.setID(Integer.parseInt(cursor.getString(0)));
                item.setCategory(cursor.getString(1));
                item.setName(cursor.getString(2));
                item.setFileName(cursor.getString(3));
                item.setValue(cursor.getString(4));
                final String enabled = cursor.getString(5);
                item.setEnabled(enabled != null && enabled.equals("1"));
                itemList.add(item);
            } while (cursor.moveToNext());
        }

        return itemList;
    }

    public int updateTaskerItem(final TaskerItem item) {
        final SQLiteDatabase db = this.getWritableDatabase();

        if (db == null) return -1;

        final ContentValues values = new ContentValues();
        values.put(KEY_CATEGORY, item.getCategory());
        values.put(KEY_NAME, item.getName());
        values.put(KEY_VALUE, item.getValue());
        values.put(KEY_FILENAME, item.getFileName());
        values.put(KEY_ENABLED, (item.getEnabled() ? "1" : "0"));

        return db.update(TABLE_TASKER, values, KEY_ID + " = ?",
                new String[]{String.valueOf(item.getID())});
    }

    public boolean deleteTaskerItem(final TaskerItem item) {
        final SQLiteDatabase db = this.getWritableDatabase();

        if (db == null) return false;

        db.delete(TABLE_TASKER, KEY_ID + " = ?", new String[]{String.valueOf(item.getID())});
        db.close();
        return true;
    }
}
