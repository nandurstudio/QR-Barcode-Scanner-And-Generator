package com.nandur.qrcodescanner.ui.generator;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.nandur.qrcodescanner.R;

public class GeneratorFragment extends Fragment {

  private String QR_CONTENT = String.valueOf(R.string.nav_header_title);
  private EditText inputText;
  private View root;

  public View onCreateView(@NonNull LayoutInflater inflater,
                           ViewGroup container, Bundle savedInstanceState) {
    root = inflater.inflate(R.layout.fragment_qr_generator, container, false);
    generateQrCode();
    inputText = root.findViewById(R.id.editText);
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

  private void generateQrCode() {

    try {
      BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
      Bitmap bitmap = barcodeEncoder.encodeBitmap(QR_CONTENT, BarcodeFormat.QR_CODE, 400, 400);
      ImageView imageViewQrCode = root.findViewById(R.id.generated_qr_image);
      imageViewQrCode.setImageBitmap(bitmap);
    } catch (Exception ignored) {

    }
  }
}
