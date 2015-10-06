package com.androidexperiments.meter.fonts;

import android.content.Context;
import android.graphics.Typeface;

/**
 * Singleton for Roboto Light Typeface
 */
public class RobotoLightTypeface
{

    private static Typeface instance;

    public static Typeface getInstance(Context context){
        if(instance == null){
            instance = Typeface.createFromAsset(context.getAssets(), "Roboto-Light.ttf");
        }

        return instance;
    }
}
