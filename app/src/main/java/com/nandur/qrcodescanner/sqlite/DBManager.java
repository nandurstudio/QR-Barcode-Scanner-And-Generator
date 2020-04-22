package com.nandur.qrcodescanner.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import static com.nandur.qrcodescanner.sqlite.DatabaseHelper.CONTENT;
import static com.nandur.qrcodescanner.sqlite.DatabaseHelper.DATE_CREATED;
import static com.nandur.qrcodescanner.sqlite.DatabaseHelper.PATH;
import static com.nandur.qrcodescanner.sqlite.DatabaseHelper.TABLE_NAME;
import static com.nandur.qrcodescanner.sqlite.DatabaseHelper._ID;

public class DBManager {

  private DatabaseHelper dbHelper;

  private Context context;

  private SQLiteDatabase database;

  public DBManager(Context c) {
    context = c;
  }

  public DBManager open() throws SQLException {
    dbHelper = new DatabaseHelper(context);
    database = dbHelper.getWritableDatabase();
    return this;
  }

  public void close() {
    dbHelper.close();
  }

  public void insert(String path, String content, String date_created) {
    ContentValues contentValue = new ContentValues();
    contentValue.put(PATH, path);
    contentValue.put(CONTENT, content);
    contentValue.put(DATE_CREATED, date_created);
    database.insert(TABLE_NAME, null, contentValue);
  }

  public Cursor fetch() {
    String[] columns = new String[]{_ID, PATH, CONTENT, DATE_CREATED};
    Cursor cursor = database.query(TABLE_NAME, columns, null, null, null, null, null);
    if (cursor != null) {
      cursor.moveToFirst();
    }
    return cursor;
  }

  public int update(long _id, String path, String content, String date_created) {
    ContentValues contentValues = new ContentValues();
    contentValues.put(PATH, path);
    contentValues.put(CONTENT, content);
    contentValues.put(DATE_CREATED, date_created);
    int i = database.update(TABLE_NAME, contentValues, _ID + " = " + _id, null);
    return i;
  }

  public void delete(long _id) {
    database.delete(TABLE_NAME, _ID + "=" + _id, null);
  }


  public static String getLastDataByColumn(String tableName, String columnName, Context context) {
//    int _id = 0;
    SQLiteDatabase db = new DatabaseHelper(context).getReadableDatabase();
//    Cursor cursor = db.query(TABLE_NAME,
//            new String[]{BaseColumns._ID},
//            null,
//            null,
//            null,
//            null,
//            null);
    Cursor cursor = db.query(tableName,
            new String[]{columnName},
            null,
            null,
            null,
            null,
            null);
    if (cursor.moveToLast()) {
//      _id = cursor.getInt(0);
      columnName = cursor.getString(0);
    }
    cursor.close();
    db.close();
    return columnName;
  }

}
