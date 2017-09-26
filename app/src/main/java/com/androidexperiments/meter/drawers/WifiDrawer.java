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

package com.androidexperiments.meter.drawers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.androidexperiments.meter.R;


public class WifiDrawer extends TriangleFillDrawer {
    private final String TAG = this.getClass().getSimpleName();

    private boolean firstRead = true;


    public WifiDrawer(Context context){
        super(
                context,
                context.getResources().getColor(R.color.wifi_background),
                context.getResources().getColor(R.color.wifi_triangle_background),
                context.getResources().getColor(R.color.wifi_triangle_foreground),
                context.getResources().getColor(R.color.wifi_triangle_critical)
        );

        this.label1 = "WIFI";

        // Register for Wifi state change notifications
        IntentFilter ifilter = new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        ifilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
        context.registerReceiver(wifiReceiver, ifilter);
    }

    /**
     * Receive WIFI state changes
     */
    BroadcastReceiver wifiReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent){
            // Read wifi signal strength
            if(intent.getAction().equals(WifiManager.RSSI_CHANGED_ACTION)) {

                percent = getWifiStrength(intent);
                //draw this below the top label
                label2 = getWifiNetworkName(context);

                if(firstRead){
                    firstRead = false;
                    _percent = (float) (percent-0.001);
                }
            }

            // Read wifi status
            else if(intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)){
                connected = getWifiConnected(intent);
            }
        }
    };

    /**
     * Parse wifi strength from intent
     */
    public float getWifiStrength(Intent intent){
        float level = intent.getIntExtra(WifiManager.EXTRA_NEW_RSSI, -1);
        level = WifiManager.calculateSignalLevel((int) level, 100);
        level /= 100.0;
        return level;
    }

    /**
     * Parse wifi connection status from intent
     */
    public boolean getWifiConnected(Intent intent){
        NetworkInfo info = (NetworkInfo) intent.getExtras().get(WifiManager.EXTRA_NETWORK_INFO);

        if(info == null) return false;
        return info.getState().equals(NetworkInfo.State.CONNECTED);
    }

    /**
     * Parse wifi for network name
     */
    public String getWifiNetworkName(Context context){
        WifiManager mgr = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = mgr.getConnectionInfo();
        String ssid = wifiInfo.getSSID();
        //for some reason SSID comes wrapped in double-quotes
        if( ssid == null ){
            ssid = "";
        }
        return ssid.replace("\"", "");
    }

}
