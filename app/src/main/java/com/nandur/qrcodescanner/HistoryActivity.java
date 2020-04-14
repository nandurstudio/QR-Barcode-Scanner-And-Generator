package com.nandur.qrcodescanner;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.nandur.qrcodescanner.picture.PictureItem;

import java.io.File;
import java.util.Objects;

import static com.nandur.qrcodescanner.picture.PictureContent.deleteSavedImages;
import static com.nandur.qrcodescanner.picture.PictureContent.downloadRandomImage;
import static com.nandur.qrcodescanner.picture.PictureContent.loadImage;
import static com.nandur.qrcodescanner.picture.PictureContent.loadSavedImages;
import static com.nandur.qrcodescanner.plugin.QrGenerator.logger;

public class HistoryActivity extends AppCompatActivity
        implements ItemFragment.OnListFragmentInteractionListener {
  private HistoryActivity context;
  private DownloadManager downloadManager;
  private RecyclerView.Adapter recyclerViewAdapter;
  private BroadcastReceiver onComplete;
  private View progressBar;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_history);
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    context = this;
    downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

    if (recyclerViewAdapter == null) {
      Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.main_fragment);
      assert currentFragment != null;
      RecyclerView recyclerView = (RecyclerView) currentFragment.getView();
      recyclerViewAdapter = ((RecyclerView) Objects.requireNonNull(currentFragment.getView())).getAdapter();
      assert recyclerView != null;
      DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
              DividerItemDecoration.VERTICAL);
      recyclerView.addItemDecoration(dividerItemDecoration);
    }

    progressBar = findViewById(R.id.indeterminateBar);

    final FloatingActionButton fab = findViewById(R.id.fab);
    fab.setOnClickListener(view -> runOnUiThread(() -> {
      progressBar.setVisibility(View.VISIBLE);
      fab.setVisibility(View.GONE);
      downloadRandomImage(downloadManager, context);
    }));

    onComplete = new BroadcastReceiver() {
      public void onReceive(Context context, Intent intent) {
        String filePath = "";
        DownloadManager.Query q = new DownloadManager.Query();
        q.setFilterById(Objects.requireNonNull(intent.getExtras()).getLong(DownloadManager.EXTRA_DOWNLOAD_ID));
        Cursor c = downloadManager.query(q);

        if (c.moveToFirst()) {
          int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
          if (status == DownloadManager.STATUS_SUCCESSFUL) {
            String downloadFileLocalUri = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
            filePath = Uri.parse(downloadFileLocalUri).getPath();
            logger(context, filePath);
          }
        }
        c.close();
        loadImage(new File(Objects.requireNonNull(filePath)));
        recyclerViewAdapter.notifyItemInserted(0);
        progressBar.setVisibility(View.GONE);
        fab.setVisibility(View.VISIBLE);
      }
    };

    context.registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_history, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_delete) {
//      deleteSavedImages(Objects.requireNonNull(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)));
      deleteSavedImages(context.getCacheDir());
      recyclerViewAdapter.notifyDataSetChanged();
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  protected void onStop() {
//    unregisterReceiver(onComplete);
    super.onStop();
  }

  @Override
  protected void onResume() {
    super.onResume();

    runOnUiThread(() -> {
//      loadSavedImages(Objects.requireNonNull(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)));
//      logger(context, String.valueOf(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)));
      loadSavedImages(Objects.requireNonNull(context.getCacheDir()));
      logger(context, String.valueOf(context.getCacheDir()));
      logger(context, String.valueOf(context.getExternalCacheDir()));
      logger(context, String.valueOf(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)));
      recyclerViewAdapter.notifyDataSetChanged();
    });
  }


  @Override
  public void onListFragmentInteraction(PictureItem item) {
    // This is where you'd handle clicking an item in the list
  }
}
