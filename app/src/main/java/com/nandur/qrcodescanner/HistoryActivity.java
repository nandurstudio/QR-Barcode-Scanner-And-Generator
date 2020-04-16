package com.nandur.qrcodescanner;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.nandur.qrcodescanner.history.QrModel;

import java.util.Objects;

import static com.nandur.qrcodescanner.history.QrContent.deleteCachedQr;
import static com.nandur.qrcodescanner.history.QrContent.loadQrImageFromSqlPath;
import static com.nandur.qrcodescanner.plugin.QrGenerator.logger;

public class HistoryActivity extends AppCompatActivity
        implements ItemFragment.OnListFragmentInteractionListener {
  private HistoryActivity context;
  private RecyclerView.Adapter recyclerViewAdapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_history);
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setDisplayHomeAsUpEnabled(true);
      actionBar.setHomeButtonEnabled(true);
//      https://stackoverflow.com/questions/28438030/how-to-make-back-icon-to-behave-same-as-physical-back-button-in-android
    }

    toolbar.setNavigationOnClickListener(view -> finish());

    context = this;
//    downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

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
    // loadSavedImages(Objects.requireNonNull(context.getCacheDir()));
    loadQrImageFromSqlPath(this);

/*    progressBar = findViewById(R.id.indeterminateBar);

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

    context.registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));*/
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_delete_all, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    switch (id) {
      case R.id.action_delete:
//      deleteCachedQr(Objects.requireNonNull(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)));
        deleteCachedQr(context.getCacheDir());
        recyclerViewAdapter.notifyDataSetChanged();
        break;
      case R.id.home:
        // todo: goto back activity from here
/*        Intent intent = new Intent(HistoryActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();*/
        onBackPressed();
        return true;
      default:
        return super.onOptionsItemSelected(item);
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
      //loadSavedImages(Objects.requireNonNull(context.getCacheDir()));
      loadQrImageFromSqlPath(this);
      recyclerViewAdapter.notifyDataSetChanged();
    });
  }


  @Override
  public void onListFragmentInteraction(QrModel item) {
    // This is where you'd handle clicking an item in the list
    logger(this, "id: " + item.getId());
    logger(this, "path: " + item.getPath());
    logger(this, "content: " + item.getContent());
    logger(this, "File last modified @ : "+ item.getDate());
  }
}
