package com.nandur.qrcodescanner;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.nandur.qrcodescanner.ui.generator.GeneratorFragment;

import java.util.ArrayList;

import static com.nandur.qrcodescanner.vision.ocrreader.OcrCaptureActivity.SHARED_TEXT;

public class MainActivity extends AppCompatActivity {

  private AppBarConfiguration mAppBarConfiguration;
  public static String versName;
  public static int versCode;
  private Toolbar toolbar;
  private NavigationView navigationView;
  private SharedPreferences sharedPrefs;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
    FloatingActionButton fab = findViewById(R.id.fab);
    fab.setOnClickListener(view -> Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
            .setAction("Action", null).show());
    DrawerLayout drawer = findViewById(R.id.drawer_layout);
    navigationView = findViewById(R.id.nav_view);
    // Passing each menu ID as a set of Ids because each
    // menu should be considered as top level destinations.
    mAppBarConfiguration = new AppBarConfiguration.Builder(
            R.id.nav_qr_scanner, R.id.nav_qr_generator, R.id.nav_slideshow)
            .setDrawerLayout(drawer)
            .build();
    NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
    NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
    NavigationUI.setupWithNavController(navigationView, navController);

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

    // Get intent, action and MIME type
    Intent intent = this.getIntent();
    String action = intent.getAction();
    String type = intent.getType();

    if (Intent.ACTION_SEND.equals(action) && type != null) {
      //set default fragment
      loadFragment(new GeneratorFragment(), getString(R.string.qr_generator));
      if ("text/plain".equals(type)) {
        handleSendText(intent); // Handle text being sent
      } else if (type.startsWith("image/")) {
        handleSendImage(intent); // Handle single image being sent
      }
    } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
      if (type.startsWith("image/")) {
        handleSendMultipleImages(intent); // Handle multiple images being sent
      }
    } else {
      // Handle other intents, such as being started from the home screen
    }

  }

  private void loadFragment(Fragment fragment, String toolbarTitle) {
    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
    transaction.replace(R.id.nav_host_fragment, fragment);
    transaction.commit();
    toolbar.setTitle(toolbarTitle);
    navigationView.setCheckedItem(R.id.nav_qr_generator);
  }

  private void handleSendText(Intent intent) {
    String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
    if (sharedText != null) {
      // Update UI to reflect text being shared
      // put in sharedPref
      SharedPreferences.Editor editor = sharedPrefs.edit();
      editor.putString(SHARED_TEXT, sharedText);
      editor.apply();
      Toast.makeText(this, sharedText, Toast.LENGTH_SHORT).show();
      Log.d("Text shared", sharedText);
    }
  }

  private void handleSendImage(Intent intent) {
    Uri imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
    if (imageUri != null) {
      // Update UI to reflect image being shared
    }
  }

  private void handleSendMultipleImages(Intent intent) {
    ArrayList<Uri> imageUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
    if (imageUris != null) {
      // Update UI to reflect multiple images being shared
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

    if (id == R.id.action_settings) {
      goToSetting();
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

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
}
