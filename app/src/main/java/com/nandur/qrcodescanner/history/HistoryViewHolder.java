package com.nandur.qrcodescanner.history;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.nandur.qrcodescanner.R;

class HistoryViewHolder extends RecyclerView.ViewHolder {
  final View mView;

  final ImageView mImageView; //Scanned QR Image
  final TextView mQrContentView; //Scanned Content
  final TextView mQrCreatedDate;

  QrModel qrModel;

  HistoryViewHolder(View view) {
    super(view);
    mView = view;

    mImageView = view.findViewById(R.id.item_image_view); //Scanned QR Image
    mQrContentView = view.findViewById(R.id.item_qr_content_view); //Scanned Content
    mQrCreatedDate = view.findViewById(R.id.item_created_date);
  }
}
