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
