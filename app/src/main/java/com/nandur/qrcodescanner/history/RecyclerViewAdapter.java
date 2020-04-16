package com.nandur.qrcodescanner.history;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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
public class RecyclerViewAdapter extends RecyclerView.Adapter<HistoryViewHolder> {

  private final List<QrModel> qrModels;
  private final OnListFragmentInteractionListener mListener;

  public RecyclerViewAdapter(List<QrModel> qrModelList, OnListFragmentInteractionListener listener) {
    qrModels = qrModelList;
    mListener = listener;
  }

  @NonNull
  @Override
  public HistoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.fragment_item, parent, false);
    return new HistoryViewHolder(view);
  }

  @Override
  public void onBindViewHolder(final HistoryViewHolder holder, int position) {
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
  }

  @Override
  public int getItemCount() {
    return qrModels.size();
  }
}
