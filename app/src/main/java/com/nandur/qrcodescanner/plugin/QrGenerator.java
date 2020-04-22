package com.nandur.qrcodescanner.plugin;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.google.android.material.snackbar.Snackbar;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.File;

public class QrGenerator {

  public static void generateQrCode(ImageView imageViewQR, String qrcontent) {
    try {
      BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
      Bitmap bitmap = barcodeEncoder.encodeBitmap(qrcontent, BarcodeFormat.QR_CODE, 500, 500);
      imageViewQR.setImageBitmap(bitmap);
    } catch (Exception ignored) {
    }
  }

  public static Bitmap getCombinedBitmap(Bitmap qrGeneratedBitmap, Bitmap qrCaptionBitmap, int qrWidth,
                                         int qrHeight, int qrCapHeight) {
    Bitmap drawnBitmap = null;
    try {
      drawnBitmap = Bitmap.createBitmap(qrWidth, qrHeight + qrCapHeight, Bitmap.Config.ARGB_8888);
      Canvas canvas = new Canvas(drawnBitmap);
      // JUST CHANGE TO DIFFERENT Bitmaps and coordinates .
      canvas.drawBitmap(qrGeneratedBitmap, 0, 0, null);
      canvas.drawBitmap(qrCaptionBitmap, 0, qrHeight, null);
      //for more images :
      // canvas.drawBitmap(b3, 0, 0, null);
      // canvas.drawBitmap(b4, 0, 0, null);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return drawnBitmap;
  }

  public static void toast(Context context, String toastMsg) {
    Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show();
  }

  public static void logger(Context context, String msg) {
    Log.d(context.getClass().getSimpleName(), msg);
  }

  public static void snackbar(View view, String msg) {
    Snackbar.make(view, msg, Snackbar.LENGTH_LONG)
            .setAction("Action", null).show();
  }

  public static void grantPermission(Activity activity) {
    //Permission Marshmelo
    ActivityCompat.requestPermissions(activity,
            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
            1);
  }

  public static Bitmap cropImageFromPath(String path) {
    File imgFile = new File(path);
    if (imgFile.exists()) {
      // x refers to width, y refers to height
      // first find startx, starty, endx, endy
      Bitmap qrBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
      int width = qrBitmap.getWidth();
      int height = qrBitmap.getHeight();
      int newWidth = Math.min(height, width);
      int newHeight = (height > width) ? height - (height - width) : height;
      int cropW = (width - height) / 2;
      cropW = Math.max(cropW, 0);
      int cropH = (height - width) / 2;
      cropH = Math.max(cropH, 0);
      return Bitmap.createBitmap(qrBitmap, cropW, cropH, newWidth, newHeight);
    }
    return null;
  }
}
