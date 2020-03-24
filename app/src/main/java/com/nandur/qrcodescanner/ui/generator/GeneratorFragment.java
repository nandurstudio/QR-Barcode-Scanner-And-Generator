package com.nandur.qrcodescanner.ui.generator;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.nandur.qrcodescanner.R;
import com.nandur.qrcodescanner.vision.ocrreader.OcrCaptureActivity;

import java.util.Objects;

import static com.nandur.qrcodescanner.vision.ocrreader.OcrCaptureActivity.OCR_DATA_FREPS;

public class GeneratorFragment extends Fragment {

  private String QR_CONTENT;
  private EditText inputText;
  private View root;

  public View onCreateView(@NonNull LayoutInflater inflater,
                           ViewGroup container, Bundle savedInstanceState) {
    root = inflater.inflate(R.layout.fragment_qr_generator, container, false);
    generateQrCode();
    Button ocrButton = root.findViewById(R.id.ocr_button);
    ocrButton.setOnClickListener(v -> launchOcr());
    inputText = root.findViewById(R.id.qr_input_editText);
    inputText.addTextChangedListener(new TextWatcher() {
      public void afterTextChanged(Editable s) {
        // you can call or do what you want with your EditText here
        QR_CONTENT = inputText.getText().toString();
        generateQrCode();
        // yourEditText...
      }

      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
      }

      public void onTextChanged(CharSequence s, int start, int before, int count) {
      }
    });

    return root;
  }

  @Override
  public void onResume() {
    if (isAdded() && isVisible() && getUserVisibleHint()) {
      // ... do your thing
      SharedPreferences sharedPreferences = Objects.requireNonNull(this.getContext()).getSharedPreferences(OCR_DATA_FREPS, Context.MODE_PRIVATE);
      String ocrData = sharedPreferences.getString("ocr_data", getString(R.string.app_name));
      //returns value for the given key.
      Toast.makeText(getActivity(), ocrData, Toast.LENGTH_SHORT).show();
      inputText.setText(ocrData);
      QR_CONTENT= ocrData;
      generateQrCode();
    }
    super.onResume();
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