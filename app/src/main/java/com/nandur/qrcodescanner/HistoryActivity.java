package com.nandur.qrcodescanner;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.nandur.qrcodescanner.history.QrModel;

import static com.nandur.qrcodescanner.history.QrContent.deleteCachedQr;
import static com.nandur.qrcodescanner.history.QrContent.loadQrImageFromSqlPath;
import static com.nandur.qrcodescanner.plugin.QrGenerator.logger;

public class HistoryActivity extends AppCompatActivity
        implements ItemFragment.OnListFragmentInteractionListener {
    private static final String TAG = "HistoryActivity";
    private HistoryActivity context;
    private RecyclerView.Adapter recyclerViewAdapter;
    private AdView adView;

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
            recyclerViewAdapter = ((RecyclerView) currentFragment.requireView()).getAdapter();
            assert recyclerView != null;
            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                    DividerItemDecoration.VERTICAL);
            recyclerView.addItemDecoration(dividerItemDecoration);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            linearLayoutManager.setReverseLayout(true);
            linearLayoutManager.setStackFromEnd(true);
            recyclerView.setLayoutManager(linearLayoutManager);
        }

        MobileAds.initialize(this, initializationStatus -> {
        });

        adView = new AdView(this);
        adView = findViewById(R.id.adView);
        // adView.setAdSize(AdSize.BANNER);
        // adView.setAdUnitId(ADMOB_BANNER_UNIT_ID);
        // TODO: Add adView to your view hierarchy.
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
                logger(getBaseContext(), "onAdLoaded");
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                // Code to be executed when an ad request fails.
                logger(getBaseContext(), "onAdFailedToLoad");
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
                logger(getBaseContext(), "onAdOpened");
            }

            @Override
            public void onAdClicked() {
                // Code to be executed when the user clicks on an ad.
                logger(getBaseContext(), "onAdClicked");
            }

            @Override
            public void onAdLeftApplication() {
                // Code to be executed when the user has left the app.
                logger(getBaseContext(), "onAdLeftApplication");
            }

            @Override
            public void onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
                logger(getBaseContext(), "onAdClosed");
            }
        });

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
        logger(this, "File last modified @ : " + item.getDate());
    }
}
