package com.nandur.qrcodescanner;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.github.javiersantos.appupdater.AppUpdater;
import com.github.javiersantos.appupdater.enums.Display;
import com.github.javiersantos.appupdater.enums.UpdateFrom;

import java.text.MessageFormat;
import java.util.Objects;

import static com.nandur.qrcodescanner.MainActivity.versCode;
import static com.nandur.qrcodescanner.MainActivity.versName;

public class SettingsActivity extends AppCompatActivity {

  private static final String SCHEME = "package";
  private static final String APP_PKG_NAME_21 = "com.android.settings.ApplicationPkgName";
  private static final String APP_PKG_NAME_22 = "pkg";
  private static final String APP_DETAILS_PACKAGE_NAME = "com.android.settings";
  private static final String APP_DETAILS_CLASS_NAME = "com.android.settings.InstalledAppDetails";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_settings);
    getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.settings, new SettingsFragment())
            .commit();
    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setDisplayHomeAsUpEnabled(true);
    }
  }

  public static class SettingsFragment extends PreferenceFragmentCompat {
    @SuppressWarnings("ConstantConditions")
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
      setPreferencesFromResource(R.xml.root_preferences, rootKey);

      //Preference prefAbout = findPreference("about");
      Preference prefVersion = findPreference("current_version");
      Preference prefCheckUpdate = findPreference("check_update");
      Preference prefSendFeedback = findPreference("feedback");

      //getVersionName
      if (prefVersion != null) {
        String versi = getResources().getString(R.string.version_title);
        String build = getResources().getString(R.string.build_title);
        prefVersion.setSummary(versi+" "+versName+" "+build+" "+versCode);
      }

      if (prefVersion != null) {
        prefVersion.setOnPreferenceClickListener(preference -> {
          showInstalledAppDetails(getContext(), Objects.requireNonNull(getActivity()).getPackageName());
          return true;
        });
      }

      if (prefCheckUpdate != null) {
        // String update_xml = getResources().getString(R.string.update_xml_resource);
        prefCheckUpdate.setOnPreferenceClickListener(preference -> {
          new AppUpdater(Objects.requireNonNull(getContext()))
                  .showEvery(250)
                  //.setUpdateFrom(UpdateFrom.GITHUB)
                  //.setGitHubUserAndRepo("javiersantos", "AppUpdater")
                  .setUpdateFrom(UpdateFrom.GOOGLE_PLAY)
                  //.setUpdateXML(update_xml)
                  .setDisplay(Display.DIALOG)
                  .setButtonDoNotShowAgain(null)
                  .showAppUpdated(true)
                  .start();
          return true;
        });
      }

      if (prefSendFeedback != null) {
        prefSendFeedback.setOnPreferenceClickListener(preference -> {
          sendFeedback(Objects.requireNonNull(getContext()));
          return true;
        });
      }
    }

    private void showInstalledAppDetails(Context context, String packageName) {
      Intent intent = new Intent();
      final int apiLevel = Build.VERSION.SDK_INT;
      if (apiLevel >= 9) { // above 2.3
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts(SCHEME, packageName, null);
        intent.setData(uri);
      } else { // below 2.3
        final String appPkgName = (apiLevel == 8 ? APP_PKG_NAME_22
                : APP_PKG_NAME_21);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setClassName(APP_DETAILS_PACKAGE_NAME,
                APP_DETAILS_CLASS_NAME);
        intent.putExtra(appPkgName, packageName);
      }
      context.startActivity(intent);

    }
  }

  /**
   * Email client intent to send support mail
   * Appends the necessary device information to email body
   * useful when providing support
   */
  public static void sendFeedback(Context context) {
    String feedBody = context.getString(R.string.feedback_body);
    String appName = context.getString(R.string.app_name);
    String emailClient = context.getString(R.string.choose_email_client);
    String targetMail = context.getResources().getString(R.string.nav_header_subtitle);
    String patterMailBody = context.getResources().getString(R.string.pattern_mailbody);
    String body = MessageFormat.format(patterMailBody, Build.VERSION.RELEASE, versName, Build.BRAND, Build.MODEL, Build.MANUFACTURER, versCode);
    Intent intent = new Intent(Intent.ACTION_SEND);
    intent.setType("message/rfc822");
    intent.putExtra(Intent.EXTRA_EMAIL, new String[]{targetMail});
    intent.putExtra(Intent.EXTRA_SUBJECT, MessageFormat.format("{0} {1} v{2} b{3}", feedBody, appName, versName, versCode));
    intent.putExtra(Intent.EXTRA_TEXT, body);
    context.startActivity(Intent.createChooser(intent, emailClient));
  }
}
