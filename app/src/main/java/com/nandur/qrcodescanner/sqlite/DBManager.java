package com.nandur.qrcodescanner.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

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
    contentValue.put(DatabaseHelper.PATH, path);
    contentValue.put(DatabaseHelper.CONTENT, content);
    contentValue.put(DatabaseHelper.DATE_CREATED, date_created);
    database.insert(DatabaseHelper.TABLE_NAME, null, contentValue);
  }

  public Cursor fetch() {
    String[] columns = new String[] { DatabaseHelper._ID, DatabaseHelper.PATH, DatabaseHelper.CONTENT, DatabaseHelper.DATE_CREATED};
    Cursor cursor = database.query(DatabaseHelper.TABLE_NAME, columns, null, null, null, null, null);
    if (cursor != null) {
      cursor.moveToFirst();
    }
    return cursor;
  }

  public int update(long _id, String path, String content, String date_created) {
    ContentValues contentValues = new ContentValues();
    contentValues.put(DatabaseHelper.PATH, path);
    contentValues.put(DatabaseHelper.CONTENT, content);
    contentValues.put(DatabaseHelper.DATE_CREATED, date_created);
    int i = database.update(DatabaseHelper.TABLE_NAME, contentValues, DatabaseHelper._ID + " = " + _id, null);
    return i;
  }

  public void delete(long _id) {
    database.delete(DatabaseHelper.TABLE_NAME, DatabaseHelper._ID + "=" + _id, null);
  }

}
