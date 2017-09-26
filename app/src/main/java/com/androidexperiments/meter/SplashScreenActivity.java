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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.androidexperiments.meter.R;

/**
 * The first activity that shows the logo and then moves you to the main app
 */
public class SplashScreenActivity extends Activity
{


    @Override
    public void onCreate(Bundle savedBundleInstance)
    {
        super.onCreate(savedBundleInstance);
        setContentView(R.layout.activity_splashscreen);

        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                moveToMainActivity();
            }
        }, 2000);

    }


    protected void moveToMainActivity()
    {
        Intent intent = new Intent(this, MainActivity.class);
        //take splash off the stack
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

}
