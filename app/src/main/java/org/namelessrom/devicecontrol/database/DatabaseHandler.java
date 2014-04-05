package org.namelessrom.devicecontrol.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler extends SQLiteOpenHelper {

    private static final int    DATABASE_VERSION = 1;
    private static final String DATABASE_NAME    = "DeviceControl";

    private static final String KEY_ID       = "id";
    private static final String KEY_CATEGORY = "category";
    private static final String KEY_NAME     = "name";
    private static final String KEY_FILENAME = "filename";
    private static final String KEY_VALUE    = "value";

    public static final String TABLE_BOOTUP = "BootUp";

    private static DatabaseHandler sDatabaseHandler = null;

    private DatabaseHandler(final Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static DatabaseHandler getInstance(final Context context) {
        if (sDatabaseHandler == null) {
            sDatabaseHandler = new DatabaseHandler(context);
        }
        return sDatabaseHandler;
    }

    @Override
    public void onCreate(final SQLiteDatabase db) {
        final String CREATE_TABLE = "CREATE TABLE " + TABLE_BOOTUP + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_CATEGORY + " TEXT," + KEY_NAME + " TEXT,"
                + KEY_FILENAME + " TEXT," + KEY_VALUE + " TEXT)";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOTUP);
        onCreate(db);
    }

    //==============================================================================================
    // All CRUD(Create, Read, Update, Delete) Operations
    //==============================================================================================

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
                        KEY_ID, KEY_CATEGORY, KEY_NAME, KEY_VALUE, KEY_FILENAME}, KEY_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null
        );
        if (cursor != null) { cursor.moveToFirst(); }
        if (cursor == null) return null;

        return new DataItem(Integer.parseInt(cursor.getString(0)), cursor.getString(1),
                cursor.getString(2), cursor.getString(3), cursor.getString(4));
    }

    public List<DataItem> getAllItems(final String tableName, final String category) {
        final List<DataItem> itemList = new ArrayList<DataItem>();
        final String selectQuery = "SELECT  * FROM " + tableName + (category.isEmpty()
                ? ""
                : " WHERE " + KEY_CATEGORY + " = '" + category + "'");

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
                item.setValue(cursor.getString(3));
                item.setFileName(cursor.getString(4));
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
}
