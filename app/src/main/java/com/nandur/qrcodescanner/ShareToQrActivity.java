package com.nandur.qrcodescanner;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.preference.PreferenceManager;

import com.google.android.material.textfield.TextInputEditText;
import com.nandur.qrcodescanner.plugin.QrGenerator;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.regex.Pattern;

import static com.nandur.qrcodescanner.plugin.QrGenerator.generateQrCode;
import static com.nandur.qrcodescanner.plugin.QrGenerator.getCombinedBitmap;
import static com.nandur.qrcodescanner.plugin.QrGenerator.grantPermission;
import static com.nandur.qrcodescanner.plugin.QrGenerator.logger;
import static com.nandur.qrcodescanner.plugin.QrGenerator.snackbar;
import static com.nandur.qrcodescanner.plugin.QrGenerator.toast;
import static com.nandur.qrcodescanner.vision.ocrreader.OcrCaptureActivity.SHARED_TEXT;

public class ShareToQrActivity extends AppCompatActivity {

  private String QR_CONTENT;
  private SharedPreferences sharedPreferences;
  private SharedPreferences.Editor editor;
  private TextView qrShareCaption;
  private ImageView shareqrGeneratedImage;
  private TextInputEditText shareInputText;
  private File directory;
  private File file;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_share_to_qr);
    sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    editor = PreferenceManager.getDefaultSharedPreferences(ShareToQrActivity.this).edit();
    String rootDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
    directory = new File(rootDirectory + getString(R.string.folder_directory));
    shareqrGeneratedImage = findViewById(R.id.share_generated_qr_image);
    shareInputText = findViewById(R.id.share_qr_input_editText);
    qrShareCaption = findViewById(R.id.share_qr_caption);
    shareInputText.addTextChangedListener(new TextWatcher() {
      //      Ketika inputText di ubah atau di modifikasi, maka qrCaption dan qrGeneratedImage akan otomatis update
      @SuppressLint("CommitPrefEdits")
      public void afterTextChanged(Editable s) {
        QR_CONTENT = Objects.requireNonNull(shareInputText.getText()).toString();
//        Update qrCaption
        qrShareCaption.setText(QR_CONTENT);
//        Update qrGeneratedImage
        QrGenerator.generateQrCode(shareqrGeneratedImage, QR_CONTENT);
//        Simpan inputText value ke shared preferences agar ketika onResume akan lanjut pada value inputText terakhir
        editor.putString(SHARED_TEXT, shareInputText.getText().toString());
        editor.apply();
      }

      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
      }

      public void onTextChanged(CharSequence s, int start, int before, int count) {
      }
    });
    generateQRData("onCreate");
    grantPermission(this);
//    Untuk share qr
//    Jika di fragment, maka harus menyertakan getActivity()
    Bundle extras = ShareToQrActivity.this.getIntent().getExtras();
    byte[] b = new byte[0];
    if (extras != null) {
      b = extras.getByteArray("picture");
    }
    Bitmap bitmap = null;
    if (b != null) {
      bitmap = BitmapFactory.decodeByteArray(b, 0, b.length);
    }
    shareqrGeneratedImage.setImageBitmap(bitmap);

    // Get intent, action and MIME type
    Intent intent = this.getIntent();
    String action = intent.getAction();
    String type = intent.getType();

    if (Intent.ACTION_SEND.equals(action) && type != null) {
      if ("text/plain".equals(type)) {
        handleSendText(intent); // Handle text being sent
      } else if (type.startsWith("image/")) {
        handleSendImage(intent); // Handle single image being sent
      }
    } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
      if (type.startsWith("image/")) {
        handleSendMultipleImages(intent); // Handle multiple images being sent
      }
    } else {
      // Handle other intents, such as being started from the home screen
    }
    shareqrGeneratedImage.setOnClickListener(v -> saveImage(QR_CONTENT, directory, shareqrGeneratedImage, qrShareCaption));
    shareqrGeneratedImage.setOnLongClickListener(v -> shareImage(QR_CONTENT, directory, shareqrGeneratedImage, qrShareCaption));
  }

  @Override
  public void onResume() {
    generateQRData("onResume");
    super.onResume();
  }

  //Permission Marshmelo
  @Override
  public void onRequestPermissionsResult(int requestCode,
                                         @NonNull String[] permissions, @NonNull int[] grantResults) {
    // If request is cancelled, the textViewResult arrays are empty.
    if (requestCode == 1) {
      if (grantResults.length > 0
              && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        Log.d("SDCard", getString(R.string.sdcard_permission_accepted));
        // Toast.makeText(getApplicationContext(), "Akses diijinkan", Toast.LENGTH_SHORT).show();
        // permission was granted, yay! Do the
        // contacts-related task you need to do.

      } else {

        // permission denied, boo! Disable the
        // functionality that depends on this permission.
        toast(this, String.valueOf(R.string.sdcard_permission_declined));
      }

      // other 'case' lines to check for other
      // permissions this app might request
    }
  }

  private void generateQRData(String msg) {
    String ocrData = sharedPreferences.getString(SHARED_TEXT, getString(R.string.app_name));
    logger(this, msg);
    logger(this, ocrData);
    shareInputText.setText(ocrData);
    shareInputText.setSelection(Objects.requireNonNull(shareInputText.getText()).length());
    QR_CONTENT = ocrData;
    generateQrCode(shareqrGeneratedImage, QR_CONTENT);
  }

  private void saveImage(String qrcontent, File dir, ImageView qrimage, TextView qrcaption) {
    Date d = new Date();
    CharSequence timestamp = DateFormat.format("MM-dd-yy HH:mm:ss", d.getTime());
    //    int yYear = Calendar.getInstance().get(Calendar.YEAR);
    //    int wWeek = Calendar.getInstance().get(Calendar.WEEK_OF_YEAR);
    //    membuat directory
    dir.mkdirs();
    String fname = qrcontent + "_" + timestamp + ".png";

    // Pattern regex = Pattern.compile("[$&+,:;=\\\\?@#|/'<>.^*()%!-]");
    // Encode url to string
    Pattern regex = Pattern.compile(String.valueOf(R.string.regex));
    if (regex.matcher(fname).find()) {
      logger(this, "Special chars found");
      fname = fname.replaceAll(String.valueOf(R.string.regex), "-");
    }

    logger(this, fname);
    file = new File(dir, fname);
    if (file.exists())
      file.delete();
    try {
      FileOutputStream out = new FileOutputStream(file);
      qrimage.setDrawingCacheEnabled(true);
      qrimage.buildDrawingCache();
      qrcaption.setDrawingCacheEnabled(true);
      qrcaption.buildDrawingCache();
      Bitmap qrBitmap = qrimage.getDrawingCache();
      Bitmap qrCapBitmap = qrcaption.getDrawingCache();
      int qrWidth = qrimage.getDrawable().getIntrinsicWidth();
      int qrHeight = qrimage.getDrawable().getIntrinsicHeight();
      int qrCapHeight = qrcaption.getMeasuredHeight();
      Bitmap bitmap = Bitmap.createBitmap(getCombinedBitmap(qrBitmap, qrCapBitmap, qrWidth, qrHeight, qrCapHeight));
      qrcaption.setDrawingCacheEnabled(false);
      qrimage.setDrawingCacheEnabled(false);
      bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
      out.flush();
      out.close();
    } catch (Exception e) {
      e.printStackTrace();
    }

    // toast(this, "Hasil disimpan di " + file);
    snackbar(findViewById(R.id.linearLayoutCompact), "Hasil disimpan di " + file);
    // Tell the media scanner about the new file so that it is
    // immediately available to the userEmail.
    MediaScannerConnection.scanFile(this, new String[]{file.toString()}, null,
            (path, uri) -> {
              Log.i("ExternalStorage", "Scanned " + path + ":");
              Log.i("ExternalStorage", "-> uri=" + uri);
            });
  }

  private boolean shareImage(String qrcontent, File dir, ImageView qrimage, TextView qrcaption) {
    saveImage(qrcontent, dir, qrimage, qrcaption);
    Intent shareIntent;
    Uri bmpUri = FileProvider.getUriForFile(this, this.getPackageName() + ".provider", file);
    shareIntent = new Intent(android.content.Intent.ACTION_SEND);
    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
    shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    shareIntent.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
    shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
    shareIntent.putExtra(Intent.EXTRA_TEXT, "Hey please check this application " + "https://play.google.com/store/apps/details?id=" + getPackageName());
    shareIntent.setType("image/png");
    startActivityForResult(Intent.createChooser(shareIntent, "Share with"), 2300);
    return true;
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    //Check if it is from the same code, if yes delete the temp file
    if (requestCode == 2300) {
      try {
        if (file.exists())
          file.delete();
        logger(this, file + " deleted");
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  private void handleSendText(Intent intent) {
    String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
    if (sharedText != null) {
      // Update UI to reflect text being shared
      // put in sharedPref
      SharedPreferences.Editor editor = sharedPreferences.edit();
      editor.putString(SHARED_TEXT, sharedText);
      editor.apply();
      Toast.makeText(this, sharedText, Toast.LENGTH_SHORT).show();
      logger(this, sharedText);
      // Update qrGeneratedImage
      QrGenerator.generateQrCode(shareqrGeneratedImage, sharedText);
      qrShareCaption.setText(sharedText);
      shareInputText.setText(sharedText);
      shareInputText.setSelection(Objects.requireNonNull(shareInputText.getText()).length());
    }
  }

  private void handleSendImage(Intent intent) {
    Uri imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
    if (imageUri != null) {
      // Update UI to reflect image being shared
    }
  }

  private void handleSendMultipleImages(Intent intent) {
    ArrayList<Uri> imageUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
    if (imageUris != null) {
      // Update UI to reflect multiple images being shared
    }
  }
}
