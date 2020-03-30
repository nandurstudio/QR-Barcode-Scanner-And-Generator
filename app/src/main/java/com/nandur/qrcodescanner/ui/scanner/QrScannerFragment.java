package com.nandur.qrcodescanner.ui.scanner;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.nandur.qrcodescanner.AnyOrientationActivity;
import com.nandur.qrcodescanner.R;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Objects;

public class QrScannerFragment extends Fragment {
  private WebView myWebView;
  private TextView scanResult;
  private ImageView scanResultImg;

  @SuppressLint("SetJavaScriptEnabled")
  public View onCreateView(@NonNull LayoutInflater inflater,
                           ViewGroup container, Bundle savedInstanceState) {
    View root = inflater.inflate(R.layout.fragment_qr_scanner, container, false);

    myWebView = root.findViewById(R.id.webview);
    scanResult = root.findViewById(R.id.scan_result_text);
    scanResultImg = root.findViewById(R.id.scan_result_image);
    FloatingActionButton fabButton = Objects.requireNonNull(getActivity()).findViewById(R.id.fab);
    fabButton.setOnClickListener(v -> startScan());

    WebSettings webSettings = myWebView.getSettings();
    webSettings.setJavaScriptEnabled(true);

    try {
      startScan();
    } catch (Exception e) {
      e.printStackTrace();
      Toast.makeText(getActivity(), Arrays.toString(e.getStackTrace()), Toast.LENGTH_LONG).show();
    }

    return root;
  }

  private void startScan() {
    IntentIntegrator integrator = IntentIntegrator.forSupportFragment(this);
    integrator.setCaptureActivity(AnyOrientationActivity.class);
    integrator.setPrompt(getString(R.string.scan_message));
    integrator.setCameraId(0); // Use a specific camera of the device
    integrator.setBeepEnabled(false);
    integrator.setBarcodeImageEnabled(true);
    integrator.setOrientationLocked(false);
    integrator.initiateScan();
  }

//  private void startScan() {
//    IntentIntegrator.forSupportFragment(this)
//            .setCaptureActivity(AnyOrientationActivity.class)
//            .setPrompt(getString(R.string.scan_message))
//            .setCameraId(0)  // Use a specific camera of the device
//            .setBeepEnabled(false)
//            .setBarcodeImageEnabled(true)
//            .setOrientationLocked(false)
//            .initiateScan();
//  }


  // URL Validator
  private boolean isValid(String urlString) {
    try {
      URL url = new URL(urlString);
      return URLUtil.isValidUrl(String.valueOf(url)) && Patterns.WEB_URL.matcher(String.valueOf(url)).matches();
    } catch (MalformedURLException ignored) {
    }
    return false;
  }

  // Get the results:
  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
    result.getBarcodeImagePath();
    result.getErrorCorrectionLevel();
    if (result != null) {
      if (result.getContents() == null) {
        Toast.makeText(getActivity(), "Cancelled", Toast.LENGTH_LONG).show();
      } else {
        Toast.makeText(getActivity(), "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
        File imgFile = new File(result.getBarcodeImagePath());
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
          Bitmap cropImg = Bitmap.createBitmap(qrBitmap, cropW, cropH, newWidth, newHeight);
          scanResultImg.setImageBitmap(cropImg);
        }
        try {
          Log.d("Scan Result", result.getBarcodeImagePath());
          Log.d("Scan Result", result.getContents());
          Log.d("Scan Result", result.getErrorCorrectionLevel());
          Log.d("Scan Result", result.getFormatName());
          Log.d("Scan Result", Arrays.toString(result.getRawBytes()));
        } catch (Exception e) {
          e.printStackTrace();
        }
        if (isValid(result.getContents())) {
          myWebView.setVisibility(View.VISIBLE);
          myWebView.loadUrl(result.getContents());
        } else {
          try {
            myWebView.setVisibility(View.INVISIBLE);
          } catch (Exception e) {
            e.printStackTrace();
          }
          scanResult.setText(result.getContents());
        }
      }
    } else {
      super.onActivityResult(requestCode, resultCode, data);
    }
  }
}
