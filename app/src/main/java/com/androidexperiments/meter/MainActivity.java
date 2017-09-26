// Copyright 2017 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.androidexperiments.meter;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.WallpaperManager;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ToggleButton;
import android.widget.TextView;

import com.androidexperiments.meter.fonts.RobotoBoldTypeface;
import com.androidexperiments.meter.fonts.RobotoLightTypeface;


/**
 * The Main app activity, describes the wallpaper and directs user towards notification settings
 */
public class MainActivity extends Activity implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static final String TAG = MainActivity.class.getSimpleName();

    protected SharedPreferences mSettings;

    protected ToggleButton mWifiEnabled;
    protected ToggleButton mBatteryEnabled;
    protected ToggleButton mNotificationsEnabled;
    protected Button mSetWallpaperBtn;


    /**
     * the click listener for all drawers buttons
     */
    protected View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //if none of the buttons are on, this one must stay on
            if (!anyChecked()) {
                ((ToggleButton)v).setChecked(true);
            }
        }

    };

    /**
     * are any of the ToggleButtons currently checked?
     */
    protected boolean anyChecked() {
        ToggleButton[] btns = {mWifiEnabled, mBatteryEnabled, mNotificationsEnabled};

        for (ToggleButton btn : btns) {
            if (btn.isChecked()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //grab button references
        mWifiEnabled = (ToggleButton) findViewById(R.id.wifiEnableButton);
        mBatteryEnabled = (ToggleButton) findViewById(R.id.batteryEnableButton);
        mNotificationsEnabled = (ToggleButton) findViewById(R.id.notificationsEnableButton);
        mSetWallpaperBtn = (Button) findViewById(R.id.choseWallpaperButton);

        Typeface robotoLight = RobotoLightTypeface.getInstance(this);
        Typeface robotoBold = RobotoBoldTypeface.getInstance(this);
        mSetWallpaperBtn.setTypeface(robotoBold);

        //grab shared preferences
        mSettings = getSharedPreferences(WallpaperPreferences.PREFERENCES, MODE_PRIVATE);

        ((TextView)findViewById(R.id.descriptionTextView)).setTypeface(robotoLight);

        //set listeners
        mWifiEnabled.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //use the basic one as well
                mOnClickListener.onClick(v);
                checkLocationPermission();
            }
        });

        mBatteryEnabled.setOnClickListener(mOnClickListener);
        mNotificationsEnabled.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //use the basic one as well
                mOnClickListener.onClick(v);

                if (mNotificationsEnabled.isChecked() && !NotificationService.permissionsGranted) {
                    showNotificationPermissionAlert();
                }
            }
        });

        mSetWallpaperBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER);
                startActivity(intent);
            }
        });


    }

    @Override
    public void onResume() {
        super.onResume();
        updateGUI();
        if (!isNotificationServiceRunning()) {
            mNotificationsEnabled.setChecked(false);
        }

        this.checkLocationPermission();

        //in the case where notifications was the only one selected
        //and its permissions were revoked, turn back on WiFi
        if (!anyChecked()) {
            mBatteryEnabled.setChecked(true);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        this.updateSettings();
    }

    private void updateSettings(){
        //update the shared preferences
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putBoolean("wifi", mWifiEnabled.isChecked());
        editor.putBoolean("battery", mBatteryEnabled.isChecked());
        editor.putBoolean("notifications", mNotificationsEnabled.isChecked());
        editor.apply();
    }

    private void checkLocationPermission(){
        if(mWifiEnabled.isChecked()) {
            // Here, thisActivity is the current activity
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        0);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(permissions[0].equals(Manifest.permission.ACCESS_COARSE_LOCATION)){
            if(grantResults[0] == PackageManager.PERMISSION_DENIED ){
                mWifiEnabled.setChecked(false);
                this.updateSettings();
            }
        }
    }

    private void showNotificationPermissionAlert() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setMessage(getString(R.string.notification_permission));

        alertBuilder
                .setCancelable(false)
                .setPositiveButton("YES",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                moveToNotificationListenerSettings();
                            }
                        })
                .setNegativeButton("NO",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mNotificationsEnabled.setChecked(false);
                            }
                        });


        AlertDialog alertDialog = alertBuilder.create();
        alertDialog.show();
    }


    private boolean isNotificationServiceRunning() {

        ContentResolver resolver = getContentResolver();
        String enabledNotificationListeners = Settings.Secure.getString(resolver, "enabled_notification_listeners");

        String packageName = getPackageName();

        return enabledNotificationListeners != null && enabledNotificationListeners.contains(packageName);
    }


    private void updateGUI() {
        mWifiEnabled.setChecked(mSettings.getBoolean(WallpaperPreferences.WIFI_CELLULAR, false));
        mBatteryEnabled.setChecked(mSettings.getBoolean(WallpaperPreferences.BATTERY, true));
        mNotificationsEnabled.setChecked(mSettings.getBoolean(WallpaperPreferences.NOTIFICATIONS, false));

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
        Intent intent = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
            intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
            startActivity(intent);
        }
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
