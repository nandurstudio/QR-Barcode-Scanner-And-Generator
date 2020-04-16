package com.nandur.qrcodescanner.history;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.nandur.qrcodescanner.sqlite.DatabaseHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static androidx.constraintlayout.widget.Constraints.TAG;
import static com.nandur.qrcodescanner.sqlite.DatabaseHelper.TABLE_NAME;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class QrContent {
  public static final List<QrModel> PICTURE_ITEMS = new ArrayList<>();
  private static SQLiteDatabase db;

  public static void loadQrImageFromSqlPath(Context context) {
    PICTURE_ITEMS.clear(); // Delete existing recycleview
    // Select All Query
    String selectQuery = "SELECT  * FROM " + TABLE_NAME;

    db = new DatabaseHelper(context).getReadableDatabase();
    @SuppressLint("Recycle") Cursor cursor = db.rawQuery(selectQuery, null);

    // Looping through all rows and adding to list
    if (cursor.moveToFirst()) {
      do {
        QrModel qrModel = new QrModel(
                cursor.getInt(0),
                cursor.getString(1),
                cursor.getString(2),
                cursor.getString(3));
        PICTURE_ITEMS.add(qrModel);
      } while (cursor.moveToNext());
    }
  }

  public static void deleteCachedQr(File dir) {
    if (dir.exists()) {
      File[] files = dir.listFiles();
      for (File file : Objects.requireNonNull(files)) {
        String absolutePath = file.getAbsolutePath();
        String extension = absolutePath.substring(absolutePath.lastIndexOf("."));
        if (extension.equals(".jpg")) {
          file.delete();
        }
      }
    }
    deleteSqlData();
    PICTURE_ITEMS.clear(); // Delete RecycleViewList
  }

  private static void deleteSqlData() {
    db.delete(TABLE_NAME, null, null);
    db.close();
    Log.d(TAG, "deleteSqlData: sql" + TABLE_NAME + "deleted");
  }

//  public static void downloadRandomImage(DownloadManager downloadmanager, Context context) {
//
//    long ts = System.currentTimeMillis();
//    Uri uri = Uri.parse(context.getString(R.string.image_download_url));
//
//    DownloadManager.Request request = new DownloadManager.Request(uri);
//    request.setTitle("My File");
//    request.setDescription("Downloading");
//    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
//    request.setVisibleInDownloadsUi(false);
//    String fileName = ts + ".jpg";
//    request.setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, fileName);
//
//    downloadmanager.enqueue(request);
//  }
//
//  private static String getDateFromUri(Uri uri) {
//    String[] split = Objects.requireNonNull(uri.getPath()).split("/");
//    String fileName = split[split.length - 1];
//    String fileNameNoExt = fileName.split("\\.")[0];
//    @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//    String dateString = format.format(new Date(Long.parseLong(fileNameNoExt)));
//    return dateString;
//  }
//
//  private static String getFileNameFromUri(Uri uri) {
//    String[] split = Objects.requireNonNull(uri.getPath()).split("/");
//    String fileName = split[split.length - 1];
//    String fileNameNoExt = fileName.split("\\.")[0];
//    return fileNameNoExt;
//  }
//
//  private static void addItem(QrModel item) {
//    PICTURE_ITEMS.add(0, item);
//  }
//
//  public static void loadSavedImages(File dir) {
//    PICTURE_ITEMS.clear();
//    if (dir.exists()) {
//      File[] files = dir.listFiles();
//      for (File file : Objects.requireNonNull(files)) {
//        String absolutePath = file.getAbsolutePath();
//        String extension = absolutePath.substring(absolutePath.lastIndexOf("."));
//        if (extension.equals(".jpg")) {
//          loadImage(file);
//        }
//      }
//    }
//  }
//
//  public static void loadImage(File file) {
//    QrModel newItem = new QrModel();
//    newItem.uri = Uri.fromFile(file);
//    newItem.content = getFileNameFromUri(newItem.uri);
//    newItem.date = getDateFromUri(newItem.uri);
//    addItem(newItem);
//  }
}
