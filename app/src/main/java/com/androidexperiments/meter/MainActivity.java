package com.androidexperiments.meter;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.androidexperiments.meter.fonts.RobotoBoldTypeface;
import com.androidexperiments.meter.fonts.RobotoLightTypeface;

/**
 * The Main app activity, describes the wallpaper and directs user towards notification settings
 */
public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();

    protected Button mNotificationsEnabled;
    protected Button mSetWallpaperBtn;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //grab button references
        mNotificationsEnabled = (Button) findViewById(R.id.notificationsEnableButton);
        mSetWallpaperBtn = (Button) findViewById(R.id.choseWallpaperButton);

        Typeface robotoLight = RobotoLightTypeface.getInstance(this);
        Typeface robotoBold = RobotoBoldTypeface.getInstance(this);
        mSetWallpaperBtn.setTypeface(robotoBold);

        ((TextView)findViewById(R.id.descriptionTextView)).setTypeface(robotoLight);


        mNotificationsEnabled.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveToNotificationListenerSettings();
            }
        });

        mSetWallpaperBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
                intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                        new ComponentName(getApplication(), MeterWallpaper.class));
                startActivity(intent);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        //toggle the text on the notification button
        mNotificationsEnabled.setText(NotificationService.permissionsGranted ? R.string.revoke_access : R.string.give_access);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.open_settings:
                moveToNotificationListenerSettings();
                break;

            case R.id.about:
                moveToAbout();
                break;
            case R.id.licenses:
            default:
                moveToLicenses();
        }

        return true;
    }

    /**
     * go to the OS-level notification listener settings
     */
    private void moveToNotificationListenerSettings() {
        Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
        startActivity(intent);
    }

    /**
     * go to the about section
     */
    private void moveToAbout() {
        Intent intent = new Intent(this, LocalWebActivity.class);
        intent.putExtra(LocalWebActivity.EXTRA_HTML_URI, "html/about.html");
        startActivity(intent);
    }

    /**
     * go to the licenses section
     */
    private void moveToLicenses() {
        //go to Licenses html here
        Intent intent = new Intent(this, LocalWebActivity.class);
        intent.putExtra(LocalWebActivity.EXTRA_HTML_URI, "html/licenses.html");
        startActivity(intent);
    }

}
