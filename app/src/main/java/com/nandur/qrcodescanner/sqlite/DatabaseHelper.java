package com.nandur.qrcodescanner.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

  // Table Name
  public static final String TABLE_NAME = "SCANNEDQR";

  // Table columns
  static final String _ID = "_id";
  static final String PATH = "path";
  static final String CONTENT = "result";
  static final String DATE_CREATED = "date_created";

  // Database Information
  private static final String DB_NAME = "NANDUR_QRGENERATOR.DB";

  // database version
  private static final int DB_VERSION = 1;

  // Creating table query
  private static final String CREATE_TABLE = "create table " + TABLE_NAME + "("
          + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
          + PATH + " TEXT NOT NULL, "
          + CONTENT + " TEXT, "
          + DATE_CREATED + " TEXT);";

  public DatabaseHelper(Context context) {
    super(context, DB_NAME, null, DB_VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    db.execSQL(CREATE_TABLE);
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
    onCreate(db);
  }
}
