package com.nandur.qrcodescanner.ui.generator;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.nandur.qrcodescanner.R;
import com.nandur.qrcodescanner.vision.ocrreader.OcrCaptureActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.Objects;
import java.util.regex.Pattern;

import static com.nandur.qrcodescanner.vision.ocrreader.OcrCaptureActivity.SHARED_TEXT;

public class GeneratorFragment extends Fragment {

  private String QR_CONTENT;
  private EditText inputText;
  private View root;
  private SharedPreferences sharedPreferences;
  private ImageView qrGeneratedImage;
  private File myDir;
  private File file;
  private TextView qrCaption;
  private SharedPreferences.Editor editor;

  public View onCreateView(@NonNull LayoutInflater inflater,
                           ViewGroup container, Bundle savedInstanceState) {
    root = inflater.inflate(R.layout.fragment_qr_generator, container, false);
    generateQrCode();
    Button ocrButton = root.findViewById(R.id.ocr_button);
    qrGeneratedImage = root.findViewById(R.id.generated_qr_image);
    qrCaption = root.findViewById(R.id.qr_caption);
    ocrButton.setOnClickListener(v -> launchOcr());
    String rootDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
    myDir = new File(rootDirectory + "/OCR to QR/");
    sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Objects.requireNonNull(getActivity()));
    inputText = root.findViewById(R.id.qr_input_editText);

    inputText.addTextChangedListener(new TextWatcher() {
      //      Ketika inputText di ubah atau di modifikasi, maka qrCaption dan qrGeneratedImage akan otomatis update
      public void afterTextChanged(Editable s) {
        QR_CONTENT = inputText.getText().toString();
//        Update qrCaption
        qrCaption.setText(QR_CONTENT);
//        Update qrGeneratedImage
        generateQrCode();
//        Simpan inputText value ke shared preferences agar ketika onResume akan lanjut pada value inputText terakhir
        editor = PreferenceManager.getDefaultSharedPreferences(Objects.requireNonNull(getActivity())).edit();
        editor.putString(SHARED_TEXT, inputText.getText().toString());
        editor.apply();
      }

      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
      }

      public void onTextChanged(CharSequence s, int start, int before, int count) {
      }
    });

    //Permission Marshmelo
    ActivityCompat.requestPermissions(Objects.requireNonNull(getActivity()),
            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
            1);
//    Untuk share qr
//    Jika di fragment, maka harus menyertakan getActivity()
    Bundle extras = getActivity().getIntent().getExtras();
    byte[] b = new byte[0];
    if (extras != null) {
      b = extras.getByteArray("picture");
    }
    Bitmap bitmap = null;
    if (b != null) {
      bitmap = BitmapFactory.decodeByteArray(b, 0, b.length);
    }
    qrGeneratedImage.setImageBitmap(bitmap);
    qrGeneratedImage.setOnClickListener(v -> saveImage());
    qrGeneratedImage.setOnLongClickListener(v -> shareImage());
    return root;
  }

  private Bitmap getCombinedBitmap(Bitmap qrGeneratedBitmap, Bitmap qrCaptionBitmap) {
    Bitmap drawnBitmap = null;

    try {
      int qrWidth = qrGeneratedImage.getDrawable().getIntrinsicWidth();
      int qrHeight = qrGeneratedImage.getDrawable().getIntrinsicHeight();
      int qrCapHeight = qrCaption.getMeasuredHeight();
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

  //Permission Marshmelo
  @Override
  public void onRequestPermissionsResult(int requestCode,
                                         @NonNull String[] permissions, @NonNull int[] grantResults) {
    // If request is cancelled, the textViewResult arrays are empty.
    if (requestCode == 1) {
      if (grantResults.length > 0
              && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        Log.d("SDCard", "Akses diizinkan");
        // Toast.makeText(getApplicationContext(), "Akses diijinkan", Toast.LENGTH_SHORT).show();
        // permission was granted, yay! Do the
        // contacts-related task you need to do.

      } else {

        // permission denied, boo! Disable the
        // functionality that depends on this permission.
        Toast.makeText(getActivity(), "Akses ke SDCARD tidak diijinkan, aplikasi tidak bisa menyimpan gambar", Toast.LENGTH_SHORT).show();
      }

      // other 'case' lines to check for other
      // permissions this app might request
    }
  }

  @Override
  public void onResume() {
    String ocrData = sharedPreferences.getString(SHARED_TEXT, getString(R.string.app_name));
    Toast.makeText(getActivity(), ocrData, Toast.LENGTH_SHORT).show();
    inputText.setText(ocrData);
    QR_CONTENT = ocrData;
    generateQrCode();
    super.onResume();
  }

  private void saveImage() {
    Date d = new Date();
    CharSequence timestamp = DateFormat.format("MM-dd-yy HH:mm:ss", d.getTime());
    //    int yYear = Calendar.getInstance().get(Calendar.YEAR);
    //    int wWeek = Calendar.getInstance().get(Calendar.WEEK_OF_YEAR);
    //    membuat directory
    myDir.mkdirs();
    String fname = QR_CONTENT + "_" + timestamp + ".png";

    // Pattern regex = Pattern.compile("[$&+,:;=\\\\?@#|/'<>.^*()%!-]");
    Pattern regex = Pattern.compile(getString(R.string.regex));

    if (regex.matcher(fname).find()) {
      Log.d("TTT", "SPECIAL CHARS FOUND");
      fname = fname.replaceAll(getString(R.string.regex), "-");
    }

    Log.d("File name", fname);
    file = new File(myDir, fname);
    if (file.exists())
      file.delete();
    try {
      FileOutputStream out = new FileOutputStream(file);
      qrGeneratedImage.setDrawingCacheEnabled(true);
      qrGeneratedImage.buildDrawingCache();
      qrCaption.setDrawingCacheEnabled(true);
      qrCaption.buildDrawingCache();
      Bitmap bitmap = Bitmap.createBitmap(getCombinedBitmap(qrGeneratedImage.getDrawingCache(), qrCaption.getDrawingCache()));
      qrCaption.setDrawingCacheEnabled(false);
      qrGeneratedImage.setDrawingCacheEnabled(false);
      bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
      out.flush();
      out.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
    Toast.makeText(getContext(), "Hasil disimpan di " + file, Toast.LENGTH_LONG).show();
    // Tell the media scanner about the new file so that it is
    // immediately available to the userEmail.
    MediaScannerConnection.scanFile(getActivity(), new String[]{file.toString()}, null,
            (path, uri) -> {
              Log.i("ExternalStorage", "Scanned " + path + ":");
              Log.i("ExternalStorage", "-> uri=" + uri);
            });
  }

  private boolean shareImage() {
    saveImage();
    if (Build.VERSION.SDK_INT >= 24) {
      try {
        Method m = StrictMode.class.getMethod(getString(R.string.disableDeathOnFileUriExposure));
        m.invoke(null);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    /*
      Used by lame internal apps that haven't done the hard work to get
      themselves off file:// Uris yet.
      https://stackoverflow.com/questions/38200282/android-os-fileuriexposedexception-file-storage-emulated-0-test-txt-exposed
    */
    Intent shareIntent;
    Uri bmpUri = Uri.fromFile(file);
    shareIntent = new Intent(android.content.Intent.ACTION_SEND);
    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
    shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
    shareIntent.putExtra(Intent.EXTRA_TEXT, "Hey please check this application " + "https://play.google.com/store/apps/details?id=" + Objects.requireNonNull(getContext()).getPackageName());
    shareIntent.setType("image/png");
    startActivityForResult(Intent.createChooser(shareIntent, "Share with"), 2301);
    return true;
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    //Check if it is from the same code, if yes delete the temp file
    if (requestCode == 2301) {
      try {
        if (file.exists())
          file.delete();
        Log.d("share", file + "deleted");
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  private void launchOcr() {
    try {
      Intent k = new Intent(getActivity(), OcrCaptureActivity.class);
      startActivity(k);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void generateQrCode() {
    try {
      BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
      Bitmap bitmap = barcodeEncoder.encodeBitmap(QR_CONTENT, BarcodeFormat.QR_CODE, 500, 500);
      ImageView imageViewQrCode = root.findViewById(R.id.generated_qr_image);
      imageViewQrCode.setImageBitmap(bitmap);
    } catch (Exception ignored) {
    }
  }
}