package com.nandur.qrcodescanner.ui.scanner;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.nandur.qrcodescanner.AnyOrientationActivity;
import com.nandur.qrcodescanner.R;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

public class QrScannerFragment extends Fragment {
  private WebView myWebView;
  private TextView scanResult;

  @SuppressLint("SetJavaScriptEnabled")
  public View onCreateView(@NonNull LayoutInflater inflater,
                           ViewGroup container, Bundle savedInstanceState) {
    View root = inflater.inflate(R.layout.fragment_qr_scanner, container, false);

    myWebView = root.findViewById(R.id.webview);
    scanResult = root.findViewById(R.id.scan_result);
    FloatingActionButton fabButton = Objects.requireNonNull(getActivity()).findViewById(R.id.fab);
    fabButton.setOnClickListener(v -> startScan());

    WebSettings webSettings = myWebView.getSettings();
    webSettings.setJavaScriptEnabled(true);

    //startScan();

    return root;
  }

  private void startScan() {
    IntentIntegrator.forSupportFragment(this)
            .setCaptureActivity(AnyOrientationActivity.class)
            .setPrompt(getString(R.string.scan_message))
            .setCameraId(0)  // Use a specific camera of the device
            .setBeepEnabled(false)
            .setBarcodeImageEnabled(true)
            .setOrientationLocked(false)
            .initiateScan();
  }

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
    if (result != null) {
      if (result.getContents() == null) {
        Toast.makeText(getActivity(), "Cancelled", Toast.LENGTH_LONG).show();
      } else {
        Toast.makeText(getActivity(), "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
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
