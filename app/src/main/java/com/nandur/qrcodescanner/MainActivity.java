package com.nandur.qrcodescanner;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = "MainActivity";
  private AppBarConfiguration mAppBarConfiguration;
  public static String versName;
  public static int versCode;
  private SharedPreferences.Editor editor;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    FloatingActionButton fab = findViewById(R.id.fab);
    fab.setOnClickListener(view -> Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
            .setAction("Action", null).show());
    DrawerLayout drawer = findViewById(R.id.drawer_layout);
    NavigationView navigationView = findViewById(R.id.nav_view);
    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    editor = sharedPreferences.edit();
    // Passing each menu ID as a set of Ids because each
    // menu should be considered as top level destinations.
    mAppBarConfiguration = new AppBarConfiguration.Builder(
            R.id.nav_qr_scanner, R.id.nav_qr_generator, R.id.nav_slideshow)
            .setDrawerLayout(drawer)
            .build();
    NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
    NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
    NavigationUI.setupWithNavController(navigationView, navController);

    FirebaseInstanceId.getInstance().getInstanceId()
            .addOnCompleteListener(task -> {
              if (!task.isSuccessful()) {
                Log.w(TAG, "getInstanceId failed", task.getException());
                return;
              }

              // Get new Instance ID token
              String token = Objects.requireNonNull(task.getResult()).getToken();
              editor.putString("fiam_instance_id",token);
              editor.apply();
              // Log and toast
              String msg = getString(R.string.msg_token_fmt, token);
              Log.d(TAG, msg);
              Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
            });

//    To re-enable FCM, make a runtime call:
//    FirebaseMessaging.getInstance().setAutoInitEnabled(true);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      // Create channel to show notifications.
      String channelId = getString(R.string.update_notification_channel_id);
      String channelName = getString(R.string.update_notification_channel_name);
      NotificationManager notificationManager =
              getSystemService(NotificationManager.class);
      assert notificationManager != null;
      notificationManager.createNotificationChannel(new NotificationChannel(channelId,
              channelName, NotificationManager.IMPORTANCE_DEFAULT));
    }

    // If a notification message is tapped, any data accompanying the notification
    // message is available in the intent extras. In this sample the launcher
    // intent is fired when the notification is tapped, so any accompanying data would
    // be handled here. If you want a different intent fired, set the click_action
    // field of the notification message to the desired intent. The launcher intent
    // is used when no click_action is specified.
    //
    // Handle possible data accompanying notification message.
    // [START handle_data_extras]
    if (getIntent().getExtras() != null) {
      for (String key : getIntent().getExtras().keySet()) {
        Object value = getIntent().getExtras().get(key);
        Log.d(TAG, "Key: " + key + " Value: " + value);
      }
    }
    // [END handle_data_extras]

    // This line of yours should contain the activity that you want to launch.
    // You are currently just passing empty new Intent()
    final Intent intent = new Intent(this, HistoryActivity.class);
    //intent.setData(data);
    intent.putExtra("key", "value");
    final PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);


    //getVersionName
    try {
      PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
      versName = pInfo.versionName;
      versCode = pInfo.versionCode;
      Log.d("MyApp", "Version Name : " + versName + "\n Version Code : " + versCode);
    } catch (PackageManager.NameNotFoundException e) {
      e.printStackTrace();
      Log.d("MyApp", "PackageManager Catch : " + e.toString());
    }

  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    switch (id) {
      case R.id.action_settings:
        goToSetting();
        return true;
//      case R.id.action_text_to_qr:
//        goToShareToQR();
//        return true;
      case R.id.action_history:
        goToHistory();
        return true;
    }

    return super.onOptionsItemSelected(item);
  }

  private void goToHistory() {
    Intent goToHistory = new
            Intent(MainActivity.this, HistoryActivity.class);
    startActivity(goToHistory);
  }

//  private void goToShareToQR() {
//    Intent shareToQR = new
//            Intent(MainActivity.this, ShareToQrActivity.class);
//    startActivity(shareToQR);
//  }

  private void goToSetting() {
    Intent settingsIntent = new
            Intent(MainActivity.this, SettingsActivity.class);
    startActivity(settingsIntent);
  }

  @Override
  public boolean onSupportNavigateUp() {
    NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
    return NavigationUI.navigateUp(navController, mAppBarConfiguration)
            || super.onSupportNavigateUp();
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    processIntent(intent);
  }

  private void processIntent(Intent intent) {
    //get your extras
  }
}
