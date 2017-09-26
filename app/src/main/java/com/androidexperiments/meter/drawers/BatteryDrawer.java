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
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.BatteryManager;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import com.androidexperiments.meter.R;

public class BatteryDrawer extends Drawer {
    private final String TAG = this.getClass().getSimpleName();

    private float batteryPct;
    private float _batteryPct=-1;

    private double colorTransitionToCharged = 0;
    private double _colorTransitionToCharged = 0;

    private double colorTransitionToCritical = 0;
    private double _colorTransitionToCritical = 0;

    private final Vector2D zero = new Vector2D(0,0);

    private final double circleSize = 0.7*0.5;

    // Colors
    private final int color_battery_background;
    private final int color_background_decharge;
    private final int color_foreground_decharge;

    private final int color_background_charging;
    private final int color_foreground_charging;

    private final int color_background_critical;
    private final int color_foreground_critical;

    private Paint paint = new Paint();

    // Movement
    private Vector2D pos, _pos;
    private Vector2D vel;

    public BatteryDrawer(Context context){
        super(context);

        // Register a receiver for battery state changes
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(batteryLevelReceiver, ifilter);


        // Read initial battery status
        batteryPct = getBatteryPct(batteryStatus);

        if(getBatteryIsCharging(batteryStatus)){
            _colorTransitionToCharged = colorTransitionToCharged = 1.0;
        } else {
            _colorTransitionToCharged = colorTransitionToCharged = 0.0;
            if(batteryPct <= 0.15) _colorTransitionToCritical = colorTransitionToCritical = 1.0;
        }


        // Load the colors
        color_battery_background = context.getResources().getColor(R.color.battery_background);
        color_background_decharge = context.getResources().getColor(R.color.battery_circle_discharging_background);
        color_foreground_decharge = context.getResources().getColor(R.color.battery_circle_discharging);

        color_foreground_critical = context.getResources().getColor(R.color.battery_circle_critical);
        color_background_critical = context.getResources().getColor(R.color.battery_circle_critical_background);

        color_background_charging = context.getResources().getColor(R.color.battery_circle_charging_background);
        color_foreground_charging = context.getResources().getColor(R.color.battery_circle_charging);

        this.textColor = context.getResources().getColor(R.color.battery_text);
    }

    /**
     * The battery level change receiver
     */
    BroadcastReceiver batteryLevelReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent){
            batteryPct = getBatteryPct(intent);

            // Are we charging?
            if(getBatteryIsCharging(intent)){
                colorTransitionToCharged = 1.0;
                colorTransitionToCritical = 0.0;
            } else {
                colorTransitionToCharged = 0.0;

                if(batteryPct <= 0.15){
                    colorTransitionToCritical = 1.0;
                } else {
                    colorTransitionToCritical = 0.0;
                }
            }
        }
    };

    /**
     * Function that determines if the canvas needs to be redrawn
     */
    public boolean shouldDraw(){
        boolean redraw = super.shouldDraw();

        if(vel == null) vel = new Vector2D(0,0);
        if(pos == null) pos = new Vector2D(0,0);
        if(_pos == null) _pos = new Vector2D(0,0);

        Vector2D a = new Vector2D(mOrientation[2]*0.01, -mOrientation[1]*0.01);
        a = a.add(pos.scalarMultiply(-0.01));

        vel = vel.scalarMultiply(0.9);
        vel = vel.add(a);
        pos = pos.add(vel);


        float dist = (float) pos.distance(zero);
        float maxDist = (float) (circleSize-circleSize*Math.sqrt(batteryPct));
        if(dist > maxDist){
            Vector2D n = pos.normalize().scalarMultiply(-1);
            Vector2D reflection = vel.subtract(n.scalarMultiply(2*vel.dotProduct(n)));

            pos = n.scalarMultiply(-maxDist);

            vel = reflection.scalarMultiply(0.5);
        }

        if(_colorTransitionToCharged != colorTransitionToCharged || _colorTransitionToCritical != _colorTransitionToCritical){
            redraw = true;
        }
        if(_batteryPct != batteryPct){
            redraw = true;
        }
        if(_textAlpha != textAlpha){
            redraw = true;
        }
        if(_pos.distance(pos) > 0.001){
            redraw = true;
        }

        if(redraw){
            // Circle color
            _colorTransitionToCharged = animateValue(_colorTransitionToCharged, colorTransitionToCharged, 0.03);
            _colorTransitionToCritical = animateValue(_colorTransitionToCritical, colorTransitionToCritical, 0.03);
            _pos = pos;

            _batteryPct = batteryPct;
            return true;
        }

        return false;
    }

    /**
     * Draw the circle to the canvas
     */
    public void draw(Canvas c){
        super.draw(c);

        paint.setAntiAlias(true);

        // Background
        paint.setColor(color_battery_background);
        c.drawRect(0, 0, c.getWidth(), c.getHeight(), paint);

        int x = c.getWidth()/2;
        int y = c.getHeight()/2 - (int)(30f*pixelDensity);

        int canvasSize = c.getWidth();
        if(c.getWidth() > c.getHeight()) {
            canvasSize = c.getHeight();
        }
        float _circleSize = (float) (canvasSize*circleSize);
        int textPos = (int) (y+circleSize*canvasSize);

        // Outer circle
        int bgCircleColor = interpolateColor(color_background_decharge, color_background_charging, (float) lerp(_colorTransitionToCharged));
        bgCircleColor = interpolateColor(bgCircleColor, color_background_critical, (float) lerp(_colorTransitionToCritical));
        paint.setColor(bgCircleColor);
        c.drawCircle(x,y, _circleSize, paint);

        // Inner circle
        int fgCircleColor = interpolateColor(color_foreground_decharge, color_foreground_charging, (float) lerp(_colorTransitionToCharged));
        fgCircleColor = interpolateColor(fgCircleColor, color_foreground_critical, (float) lerp(_colorTransitionToCritical));
        paint.setColor(fgCircleColor);
        c.drawCircle(
                (float)(x+canvasSize*pos.getX()),
                (float)(y+canvasSize*pos.getY()),
                (float) (_circleSize * Math.sqrt(batteryPct)), paint);

        // Text
        String label1 = "Battery " + Integer.toString(Math.round(batteryPct*100)) + "%";
        drawText(label1, "", x, textPos, c);
    }


    /**
     * Parse the battery percentage from the battery change intent
     */
    public float getBatteryPct(Intent intent){
        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        return level/(float)scale;
    }

    /**
     * Parse the battery charging status from the battery change intent
     */
    public boolean getBatteryIsCharging(Intent intent){
        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        return status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;
    }
}
