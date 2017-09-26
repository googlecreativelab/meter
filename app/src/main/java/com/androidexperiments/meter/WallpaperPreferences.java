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

/**
 * Constants for using SharedPreferences
 */
public class WallpaperPreferences
{

    /**
     * the preference file to share
     */
    public static final String PREFERENCES = "WallpaperPreferences";

    //used to get/set whether each one is visible
    public static final String WIFI_CELLULAR = "wifi";
    public static final String BATTERY = "battery";
    public static final String NOTIFICATIONS = "notifications";
}
