package com.nandur.qrcodescanner.ui.generator;

import android.Manifest;
import android.app.Activity;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.nandur.qrcodescanner.MainActivity;
import com.nandur.qrcodescanner.R;
import com.nandur.qrcodescanner.plugin.QrGenerator;
import com.nandur.qrcodescanner.vision.ocrreader.OcrCaptureActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.Objects;
import java.util.regex.Pattern;

import static com.nandur.qrcodescanner.plugin.QrGenerator.generateQrCode;
import static com.nandur.qrcodescanner.plugin.QrGenerator.getCombinedBitmap;
import static com.nandur.qrcodescanner.plugin.QrGenerator.grantPermission;
import static com.nandur.qrcodescanner.plugin.QrGenerator.logger;
import static com.nandur.qrcodescanner.plugin.QrGenerator.toast;
import static com.nandur.qrcodescanner.vision.ocrreader.OcrCaptureActivity.SHARED_TEXT;

public class GeneratorFragment extends Fragment {

    private File directory;
    private File file;
    private String QR_CONTENT;
    private TextInputEditText inputText;
    private SharedPreferences sharedPreferences;
    private ImageView qrGeneratedImage;
    private TextView qrCaption;
    private SharedPreferences.Editor editor;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_qr_generator, container, false);
        Button ocrButton = root.findViewById(R.id.ocr_button);
        qrGeneratedImage = root.findViewById(R.id.generated_qr_image);
        qrCaption = root.findViewById(R.id.qr_caption);
        ocrButton.setOnClickListener(v -> launchOcr());
        String rootDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
        directory = new File(rootDirectory + getString(R.string.folder_directory));
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireActivity());
        inputText = root.findViewById(R.id.qr_input_editText);
        inputText.addTextChangedListener(new TextWatcher() {
            //      Ketika inputText di ubah atau di modifikasi, maka qrCaption dan qrGeneratedImage akan otomatis update
            public void afterTextChanged(Editable s) {
                QR_CONTENT = Objects.requireNonNull(inputText.getText()).toString();
//        Update qrCaption
                qrCaption.setText(QR_CONTENT);
//        Update qrGeneratedImage
                QrGenerator.generateQrCode(qrGeneratedImage, QR_CONTENT);
//        Simpan inputText value ke shared preferences agar ketika onResume akan lanjut pada value inputText terakhir
                editor = PreferenceManager.getDefaultSharedPreferences(requireActivity()).edit();
                editor.putString(SHARED_TEXT, inputText.getText().toString());
                editor.apply();
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        generateQRData("onCreate");
        grantPermission(getActivity());

        //Permission Marshmelo
        ActivityCompat.requestPermissions(requireActivity(),
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                1);
//    Untuk share qr
//    Jika di fragment, maka harus menyertakan getActivity()
        Bundle extras = requireActivity().getIntent().getExtras();
        byte[] b = new byte[0];
        if (extras != null) {
            b = extras.getByteArray("picture");
        }
        Bitmap bitmap = null;
        if (b != null) {
            bitmap = BitmapFactory.decodeByteArray(b, 0, b.length);
        }
        qrGeneratedImage.setImageBitmap(bitmap);
        qrGeneratedImage.setOnClickListener(v -> saveImage(getActivity(), QR_CONTENT, directory, qrGeneratedImage, qrCaption, editor));
        qrGeneratedImage.setOnLongClickListener(v -> shareImage(getActivity(), QR_CONTENT, directory, qrGeneratedImage, qrCaption, editor));
        return root;
    }

    @Override
    public void onResume() {
        generateQRData("onResume");
        super.onResume();
    }

    private void generateQRData(String msg) {
        String ocrData = sharedPreferences.getString(SHARED_TEXT, getString(R.string.app_name));
        logger(requireActivity(), msg);
        logger(requireActivity(), ocrData);
        inputText.setText(ocrData);
        inputText.setSelection(Objects.requireNonNull(inputText.getText()).length());
        QR_CONTENT = ocrData;
        generateQrCode(qrGeneratedImage, QR_CONTENT);
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
                toast(this.getContext(), String.valueOf(R.string.sdcard_permission_declined));
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void saveImage(Activity activity, String qrcontent, File dir, ImageView qrimage, TextView qrcaption, SharedPreferences.Editor editor) {
        Date d = new Date();
        CharSequence timestamp = DateFormat.format("MM-dd-yy HH:mm:ss", d.getTime());
        //    int yYear = Calendar.getInstance().get(Calendar.YEAR);
        //    int wWeek = Calendar.getInstance().get(Calendar.WEEK_OF_YEAR);
        //    membuat directory
        dir.mkdirs();
        String fname = qrcontent + "_" + timestamp + ".png";

        // Pattern regex = Pattern.compile("[$&+,:;=\\\\?@#|/'<>.^*()%!-]");
        Pattern regex = Pattern.compile(String.valueOf(R.string.regex));

        if (regex.matcher(fname).find()) {
            logger(requireActivity(), "Special chars found");
            fname = fname.replaceAll(String.valueOf(R.string.regex), "-");
        }

        logger(requireActivity(), "File name " + fname);
        file = new File(dir, fname);
        editor.putString("file_name", fname);
        editor.apply();
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
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        toast(getActivity(), "Hasil disimpan di " + file);
        // Tell the media scanner about the new file so that it is
        // immediately available to the userEmail.
        MediaScannerConnection.scanFile(activity, new String[]{file.toString()}, null,
                (path, uri) -> {
                    Log.i("ExternalStorage", "Scanned " + path + ":");
                    Log.i("ExternalStorage", "-> uri=" + uri);
                });
    }

    private boolean shareImage(Activity activity, String qrcontent, File dir, ImageView qrimage, TextView qrcaption, SharedPreferences.Editor editor) {
        //saveImage(QR_CONTENT, directory, qrGeneratedImage, qrCaption, editor);
        saveImage(activity, qrcontent, dir, qrimage, qrcaption, editor);
        Intent shareIntent;
        Uri bmpUri = FileProvider.getUriForFile(activity, activity.getApplicationContext().getPackageName() + ".provider", file);
        shareIntent = new Intent(android.content.Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        shareIntent.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
        shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Hey please check this application " + "https://play.google.com/store/apps/details?id=" + requireActivity().getPackageName());
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
                logger(requireActivity(), file + " deleted");
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
}