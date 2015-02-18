package org.namelessrom.devicecontrol.database;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class BootupContentProvider extends ContentProvider {
    private static final String AUTHORITY = "org.namelessrom.devicecontrol.contentprovider";

    // used for the UriMacher
    private static final int BOOTUP = 10;
    private static final int BOOTUP_ID = 20;

    private static final String BASE_PATH = "bootup";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);

    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/bootup";
    public static final String CONTENT_ITEM_TYPE =
            ContentResolver.CURSOR_ITEM_BASE_TYPE + "/bootupitem";

    private DatabaseHandler mDatabaseHandler;

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sURIMatcher.addURI(AUTHORITY, BASE_PATH, BOOTUP);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", BOOTUP_ID);
    }

    @Override public boolean onCreate() {
        mDatabaseHandler = DatabaseHandler.getInstance(getContext());
        return true;
    }

    @Override public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        final SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(DatabaseHandler.TABLE_BOOTUP);

        final int uriType = sURIMatcher.match(uri);
        switch (uriType) {
            case BOOTUP:
                // noop
                break;
            case BOOTUP_ID:
                queryBuilder.appendWhere(DatabaseHandler.KEY_ID + "=" + uri.getLastPathSegment());
                break;
        }

        final Cursor cursor = queryBuilder.query(mDatabaseHandler.getWritableDatabase(),
                projection, selection, selectionArgs, null, null, sortOrder);
        // make sure that potential listeners are getting notified
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override public String getType(Uri uri) {
        return null;
    }

    @Override public Uri insert(Uri uri, ContentValues values) {
        long id = 0;
        final int uriType = sURIMatcher.match(uri);
        switch (uriType) {
            case BOOTUP_ID:
                id = mDatabaseHandler.getWritableDatabase()
                        .insert(DatabaseHandler.TABLE_BOOTUP, null, values);
                break;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(BASE_PATH + "/" + id);
    }

    @Override public int delete(Uri uri, String selection, String[] selectionArgs) {
        int rowsDeleted = 0;
        final int uriType = sURIMatcher.match(uri);
        switch (uriType) {
            case BOOTUP:
                rowsDeleted = mDatabaseHandler.getWritableDatabase()
                        .delete(DatabaseHandler.TABLE_BOOTUP, selection, selectionArgs);
                break;
            case BOOTUP_ID:
                final String name = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = mDatabaseHandler.getWritableDatabase().delete(
                            DatabaseHandler.TABLE_BOOTUP,
                            DatabaseHandler.KEY_NAME + "=" + name, null);
                } else {
                    rowsDeleted = mDatabaseHandler.getWritableDatabase().delete(
                            DatabaseHandler.TABLE_BOOTUP,
                            DatabaseHandler.KEY_NAME + "=" + name + " and " + selection,
                            selectionArgs);
                }
                break;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        int rowsUpdated = 0;
        final int uriType = sURIMatcher.match(uri);
        switch (uriType) {
            case BOOTUP:
                rowsUpdated = mDatabaseHandler.getWritableDatabase()
                        .update(DatabaseHandler.TABLE_BOOTUP, values, selection, selectionArgs);
                break;
            case BOOTUP_ID:
                final String name = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = mDatabaseHandler.getWritableDatabase().update(
                            DatabaseHandler.TABLE_BOOTUP, values,
                            DatabaseHandler.KEY_NAME + "=" + name, null);
                } else {
                    rowsUpdated = mDatabaseHandler.getWritableDatabase().update(
                            DatabaseHandler.TABLE_BOOTUP, values,
                            DatabaseHandler.KEY_NAME + "=" + name + " and " + selection,
                            selectionArgs);
                }
                break;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }
}
