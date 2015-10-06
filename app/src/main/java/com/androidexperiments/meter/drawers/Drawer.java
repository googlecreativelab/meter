package com.androidexperiments.meter.drawers;


import android.animation.ArgbEvaluator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.Calendar;

import com.androidexperiments.meter.fonts.RobotoLightTypeface;

/**
 * Class inherited by the other drawers
 */
public class Drawer implements SensorEventListener {
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mMagnetometer;

    private float[] mLastAccelerometer = new float[3];
    private float[] mLastMagnetometer = new float[3];
    private boolean mLastAccelerometerSet = false;
    private boolean mLastMagnetometerSet = false;

    private float[] mR = new float[9];
    protected float[] mOrientation = new float[3];


    private long prevTapTime = 0;

    protected double textAlpha = 255;
    protected double _textAlpha = 255;
    protected int textFadeCount = 0;

    protected int textColor = Color.WHITE;

    protected float pixelDensity;

    private final ArgbEvaluator ev = new ArgbEvaluator();


    private Paint textPaint;

    public Context context;

    public Drawer(Context context)
    {
        this.context = context;
        this.pixelDensity = context.getResources().getDisplayMetrics().density;
    }

    public void start(){

        mSensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        mLastAccelerometerSet = false;
        mLastMagnetometerSet = false;
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_NORMAL);

    }

    public void destroy() {
        if( mSensorManager == null ){
            return;
        }
        mSensorManager.unregisterListener(this);
    }

    public void draw(Canvas c){


    };

    public boolean shouldDraw(){
        boolean draw = false;

        return draw;
    };




    public void tap(int x, int y){
        long thistime = Calendar.getInstance().getTimeInMillis();
        if(prevTapTime<thistime) {
            if(thistime - prevTapTime <= 1000){
                doubleTap(x,y);
            }
        }
        prevTapTime = thistime;
    }

    protected void doubleTap(int x, int y){
        textFadeCount = 50;
        textAlpha = (float) 1.0;
    }

    /**
     * Draw the text under the graphics
     */
    protected void drawText(String text1, String text2, int x, int y, Canvas c){
        if( textPaint == null ){
            Paint p = new Paint();
            p.setTypeface(RobotoLightTypeface.getInstance(context));
            p.setColor(textColor);
            p.setTextSize(14 * pixelDensity);
            this.textPaint = p;
        }

        float w = textPaint.measureText(text1, 0, text1.length());
        int offset = (int) w / 2;
        c.drawText(text1, x-offset, y+(18f*pixelDensity), textPaint);

        w = textPaint.measureText(text2, 0, text2.length());
        offset = (int) w / 2;
        c.drawText(text2, x-offset, y+(36f*pixelDensity), textPaint);

    }

    /**
     * Produce a nice lerp between 0...1
     */
    protected  double lerp(double val){
        return val*val*val * (val * (6f*val - 15f) + 10f);
    }

    /**
     * Do a simple animation towards a value with a given speed
     */
    protected double animateValue(double curVal, double goalVal, double speed){
        double ret = curVal;
        if(ret < goalVal){
            ret += speed;
        } else if(ret > goalVal){
            ret -= speed;
        }

        if(Math.abs(ret-goalVal) < speed){
            ret = goalVal;
        }
        return ret;
    }



    /**
     * Interpolate between two HEX colors
     * @param c1 First color
     * @param c2 Second color
     * @param v interpolation value (c1 * (1-v) + c2 * v)
     * @return Interpolated color
     */
    protected int interpolateColor(int c1, int c2, float v){
        return (int) ev.evaluate(v,c1,c2);
    }

    protected float distance(float x1, float y1, float x2, float y2){
        float dX = x2-x1;
        float dY = y2-y1;
        return (float) Math.sqrt(dX*dX+dY*dY);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == mAccelerometer) {
            System.arraycopy(event.values, 0, mLastAccelerometer, 0, event.values.length);
            mLastAccelerometerSet = true;
        } else if (event.sensor == mMagnetometer) {
            System.arraycopy(event.values, 0, mLastMagnetometer, 0, event.values.length);
            mLastMagnetometerSet = true;
        }
        if (mLastAccelerometerSet && mLastMagnetometerSet) {
            SensorManager.getRotationMatrix(mR, null, mLastAccelerometer, mLastMagnetometer);
            SensorManager.getOrientation(mR, mOrientation);
            /*Log.i("OrientationTestActivity", String.format("Orientation: %f, %f, %f",
                    mOrientation[0], mOrientation[1], mOrientation[2]));*/
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
