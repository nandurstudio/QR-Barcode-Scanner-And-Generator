package com.nandur.qrcodescanner.ui.scanner;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.zxing.client.android.BeepManager;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.nandur.qrcodescanner.AnyOrientationActivity;
import com.nandur.qrcodescanner.HistoryActivity;
import com.nandur.qrcodescanner.MainActivity;
import com.nandur.qrcodescanner.R;
import com.nandur.qrcodescanner.sqlite.DBManager;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;

import static com.nandur.qrcodescanner.plugin.QrGenerator.cropImageFromPath;
import static com.nandur.qrcodescanner.plugin.QrGenerator.logger;
import static com.nandur.qrcodescanner.sqlite.DBManager.getLastDataByColumn;
import static com.nandur.qrcodescanner.sqlite.DatabaseHelper.CONTENT;
import static com.nandur.qrcodescanner.sqlite.DatabaseHelper.PATH;
import static com.nandur.qrcodescanner.sqlite.DatabaseHelper.TABLE_NAME;

public class QrScannerFragment extends Fragment {
    public static final String BARCODE_IMAGE_PATH = "barcode_image_path";
    private WebView myWebView;
    private TextView scanResult;
    private ImageView scanResultImg;
    private SharedPreferences sharedPreferences;
    private DBManager dbManager;
    private boolean sndSetting;
    private boolean vibratorSetting;
    private BeepManager beepManager;
    private String getArgument;

    @SuppressLint("SetJavaScriptEnabled")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_qr_scanner, container, false);
        FloatingActionButton fabButton = Objects.requireNonNull((MainActivity) getActivity()).findViewById(R.id.fab);
        try {
            fabButton.setOnClickListener(v -> startScan());
        } catch (Exception e) {
            e.printStackTrace();
        }
        // SQLite
        dbManager = new DBManager(getActivity());
        dbManager.open();

        myWebView = root.findViewById(R.id.webview);
        scanResult = root.findViewById(R.id.scan_result_text);
        scanResultImg = root.findViewById(R.id.scan_result_image);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireActivity());

        // Preferences
        boolean autoScan = sharedPreferences.getBoolean(getString(R.string.auto_scan), false);
        sndSetting = sharedPreferences.getBoolean(getString(R.string.enable_sound), false);
        vibratorSetting = sharedPreferences.getBoolean(getString(R.string.enable_vibrate), false);

        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        beepManager = new BeepManager(requireActivity());

        // Start scanner on launch
        if (autoScan) {
            try {
                getArgument = sharedPreferences.getString("from", "oncreate");
                Log.d("TAG", "onCreateView: " + getArgument);
                if (getArgument.equals("oncreate")) {
                    startScan();
                } else if (getArgument.equals("OnBack")) {
                    Log.d("TAG", "onBackClose: ");
                    //getActivity().finish();
                }
            } catch (Exception e) {
                e.printStackTrace();
                //Toast.makeText(getActivity(), Arrays.toString(e.getStackTrace()), Toast.LENGTH_LONG).show();
            }
        }

        dataQrPlaceholder();

        return root;
    }

    private void dataQrPlaceholder() {
        String contentFromSQL = getLastDataByColumn(TABLE_NAME, CONTENT, getContext());
        String pathFromSQL = getLastDataByColumn(TABLE_NAME, PATH, getContext());
        scanResult.setText(contentFromSQL);
        scanResultImg.setImageBitmap(cropImageFromPath(pathFromSQL));
    }

    private void startScan() {
        Log.d("TAG", "startScan: ");
        IntentIntegrator integrator = IntentIntegrator.forSupportFragment(this);
        integrator.setCaptureActivity(AnyOrientationActivity.class);
        integrator.setPrompt(getString(R.string.scan_message));
        // Set the camera from preferences
        String cameraSource = sharedPreferences.getString(getString(R.string.camera_list), "0");
        assert cameraSource != null;
        if (cameraSource.equals("1")) {
            integrator.setCameraId(1);
        } else {
            integrator.setCameraId(0); // Use a specific camera of the device
        }
        logger(requireContext(), cameraSource);
        integrator.setBarcodeImageEnabled(true);
        integrator.setOrientationLocked(false);
        integrator.initiateScan();
        if (sndSetting) {
            integrator.setBeepEnabled(true);
        } else {
            integrator.setBeepEnabled(false);
        }
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

        if (result.getContents() == null) {
            Toast.makeText(getActivity(), "Cancelled", Toast.LENGTH_LONG).show();
            //requireActivity().onBackPressed();
        } else {
            Toast.makeText(getActivity(), "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
            // Put image path to sharedPreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(BARCODE_IMAGE_PATH, result.getBarcodeImagePath());
            editor.apply();

            // Beep Manager
            if (vibratorSetting) {
                beepManager.setVibrateEnabled(true);
            }

            File file = new File(result.getBarcodeImagePath());
            Date lastModDate = new Date(file.lastModified());

            // SQLite
            final String path = result.getBarcodeImagePath();
            final String contents = result.getContents();
            final String date_created = lastModDate.toString();
            dbManager.open();
            dbManager.insert(path, contents, date_created);
            dbManager.close();
            dataQrPlaceholder();

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
                    myWebView.setVisibility(View.GONE);
                    goToHistory();
                    scanResult.setText(result.getContents());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void goToMainMenu() {
        Intent intent = new
                Intent(getActivity(), MainActivity.class);
        startActivity(intent);
    }

    private void goToHistory() {
        Intent goToHistory = new
                Intent(getActivity(), HistoryActivity.class);
        startActivity(goToHistory);
    }

    @Override
    public void onResume() {
        Log.d("TAG", "onResume: ");
        super.onResume();
    }

    @Override
    public void onPause() {
        beepManager.setVibrateEnabled(false);
        beepManager.setBeepEnabled(false);
        super.onPause();
    }

}
