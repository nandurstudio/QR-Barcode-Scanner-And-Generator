package com.nandur.qrcodescanner.history;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdRequest;
import com.nandur.qrcodescanner.ItemFragment.OnListFragmentInteractionListener;
import com.nandur.qrcodescanner.R;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

import static androidx.constraintlayout.widget.Constraints.TAG;

/**
 * {@link RecyclerView.Adapter} that can display a {@link QrModel} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
  private static final int ITEM = 0;
  private static final int NATIVE_AD = 1;
  private final OnListFragmentInteractionListener mListener;
  private List<QrModel> qrModels;

  public RecyclerViewAdapter(List<QrModel> qrModels, OnListFragmentInteractionListener listener) {
    this.qrModels = qrModels;
    mListener = listener;
  }

  @NonNull
  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//    View view = LayoutInflater.from(parent.getContext())
//            .inflate(R.layout.fragment_item, parent, false);
//    return new HistoryViewHolder(view);
    //Inflate the layout, initialize the View Holder
    View v;
    if (viewType == ITEM) {
      v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_item, parent, false);
      return new HistoryViewHolder(v);
    } else if (viewType == NATIVE_AD) {
      v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_native_ad, parent, false);
      return new AdMobViewHolder(v);
    }
    return null;
  }

  @Override
  public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {

    if (viewHolder.getItemViewType() == ITEM) {
      HistoryViewHolder holder = (HistoryViewHolder) viewHolder;
      //populate the RecyclerView
      //holder.title.setText(list.get(position).getTitle());
      //holder.description.setText(list.get(position).getDescription());
      holder.qrModel = qrModels.get(position);
      File imgFile = new File(qrModels.get(position).getPath());
      Log.d(TAG, "onBindViewHolder: " + imgFile);
      Picasso
              .get()
              .load(imgFile)
              .centerCrop()
              .fit()
              .placeholder(R.drawable.ic_launcher_background)
              .into(holder.mImageView);
      holder.mQrCreatedDate.setText(qrModels.get(position).getDate());
      holder.mQrContentView.setText(qrModels.get(position).getContent()); //Scanned Content

      holder.mView.setOnClickListener(v -> {
        if (null != mListener) {
          // Notify the active callbacks interface (the activity, if the
          // fragment is attached to one) that an item has been selected.
          mListener.onListFragmentInteraction(holder.qrModel);
        }
      });
    } else if (viewHolder.getItemViewType() == NATIVE_AD) {
      AdMobViewHolder holder = (AdMobViewHolder) viewHolder;
      //Load the Ad
      AdRequest request = new AdRequest.Builder()
              .build();
      holder.adView.loadAd(request);
    }
  }

  @Override
  public int getItemCount() {
    return qrModels.size();
  }

  @Override
  public int getItemViewType(int position) {
    return qrModels.get(position).getViewType();
  }
}
