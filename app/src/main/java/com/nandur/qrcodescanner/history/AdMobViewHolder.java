package com.nandur.qrcodescanner.history;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdView;
import com.nandur.qrcodescanner.R;

class AdMobViewHolder extends RecyclerView.ViewHolder {
  AdView adView;

  AdMobViewHolder(View v) {
    super(v);
    adView = v.findViewById(R.id.nativeAdView);
  }
}
