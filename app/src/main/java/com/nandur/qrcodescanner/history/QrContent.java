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
  public static final List<QrModel> QR_MODELS = new ArrayList<>();
  private static SQLiteDatabase db;

  public static void loadQrImageFromSqlPath(Context context) {
    QR_MODELS.clear(); // Delete existing recycleview
    // Select All Query
    String selectQuery = "SELECT  * FROM " + TABLE_NAME;

    db = new DatabaseHelper(context).getReadableDatabase();
    @SuppressLint("Recycle") Cursor cursor = db.rawQuery(selectQuery, null);

    // Looping through all rows and adding to list
    if (cursor.moveToFirst()) {
      do {
//        int listSize = QR_MODELS.size();
//        Log.d(TAG, "loadQrImageFromSqlPath: QR_SIZE" + QR_MODELS.size());
//        int ITEM = 0;
//        int NATIVE_AD = 1;
//        int[] viewType = new int[listSize];
//        for (int i = 0; i < listSize; i++) {
          QrModel qrModel = new QrModel(
                  cursor.getInt(0),
                  cursor.getString(1),
                  cursor.getString(2),
                  cursor.getString(3),
                  0);
          QR_MODELS.add(qrModel);
//          Log.d(TAG, "loadQrImageFromSqlPath: i " + i);
//          //insert native ads once in five items
//          if (i > 1 && i % 5 == 0) {
//            viewType[i] = NATIVE_AD;
//          } else {
//            viewType[i] = ITEM;
//          }
//        }
      }
      while (cursor.moveToNext());
    }
    db.close();
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
    QR_MODELS.clear(); // Delete RecycleViewList
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
