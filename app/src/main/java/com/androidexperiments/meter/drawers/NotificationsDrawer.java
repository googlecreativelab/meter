package com.androidexperiments.meter.drawers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.androidexperiments.meter.NotificationService;
import com.androidexperiments.meter.R;


public class NotificationsDrawer extends Drawer {
    static float lastNumNotifications = -1;

    private final String TAG = this.getClass().getSimpleName();

    private float numNotifications;
    private float _numNotifications = 0;

    private BroadcastReceiver notificationsReceiver;

    // Colors
    private final int[] colors = new int[4];
    private final int backgroundColor;


    private float _weight = 0f;

    public NotificationsDrawer(Context context){
        super(context);

        // Read current number of notifications
        numNotifications = NotificationService.numNotifications;

        // Check if we have a stored numNotifications from last, or we should use the current value
        if(NotificationsDrawer.lastNumNotifications == -1) {
            _numNotifications = (float) (numNotifications - 0.01);
        } else {
            _numNotifications = lastNumNotifications;
        }

        // Store the number of notifications static for next time
        NotificationsDrawer.lastNumNotifications = numNotifications;

        // Register for notification updates
        notificationsReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                numNotifications = intent.getStringArrayExtra(NotificationService.NotificationKey.APPLICATION_PACKAGES).length;
                NotificationsDrawer.lastNumNotifications = numNotifications;
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(NotificationService.NOTIFICATION_UPDATE);
        context.registerReceiver(notificationsReceiver, filter);

        // Load colors
        colors[0] = context.getResources().getColor(R.color.notifications_color_0);
        colors[1] = context.getResources().getColor(R.color.notifications_color_1);
        colors[2] = context.getResources().getColor(R.color.notifications_color_2);
        colors[3] = context.getResources().getColor(R.color.notifications_color_3);
        //background
        backgroundColor = context.getResources().getColor(R.color.notifications_background);

        this.textColor = colors[0];
    }


    @Override
    public void destroy() {
        context.unregisterReceiver(notificationsReceiver);
        super.destroy();
    }

    public boolean shouldDraw(){
        boolean redraw = super.shouldDraw();

        if(_numNotifications != numNotifications){
            redraw = true;
            _numNotifications = (float) animateValue(_numNotifications, numNotifications, 0.1);
        }

        return true; //redraw;
    }

    private float map( float value, float start1, float stop1, float start2, float stop2 ){
        return start2 + (stop2 - start2) * ((value - start1) / (stop1 - start1));
    }

    /**
     * Get the color for box number i
     */
    public int getColor(int i, float max){
        if(max <= 3){
            return colors[i];
        } else {
            // If more then 3 boxes, then interpolate between the 4 colors
            float index = (float)i/max;
            index *= 3;
            int a = (int) Math.floor(index);
            int b = (int) Math.ceil(index);

            return interpolateColor(colors[a], colors[b], index-a);
        }
    }


    public float getRectHeights(float height, float num, float weight){

        //absolute y orientation

        //evenly distributed height
        float h = height / num;
        //smallest is 4px unless there are so many notifications that it needs to be less
        float minHeight = Math.min(12f, h);

        //inverse map it, a higher orientation value equals a smaller height
        return map(Math.abs(weight), 0f, 2f, h, minHeight);

    }

    /**
     * 0 is at bottom
     * @param yBottom
     * @param rectHeight
     * @param notificationLength
     * @return
     */
    public float[] getRectYs(float yBottom, float rectHeight, int notificationLength){
        //the extra one is the top of the last one
        float[] ys = new float[notificationLength+1];
        for( int i=0; i<ys.length; i++ ){
            ys[i] = yBottom - rectHeight * i;
        }

        return ys;
    }

    public void draw(Canvas c){
        super.draw(c);

        Paint p = new Paint();
        p.setAntiAlias(true);
        int x = c.getWidth()/2;
        int y = c.getHeight()/2;

        float squareSize = (float) (c.getWidth()*0.7);
        //the bottom
        float yStart = y+squareSize/2 - (int)(30f*pixelDensity);


        c.drawColor(backgroundColor);


        double _num = _numNotifications - Math.floor(_numNotifications);
        _num = lerp(_num);
        _num = Math.floor(_numNotifications) + _num;

        _weight += (mOrientation[1] - _weight) * 0.1f;

        float barHeight = getRectHeights(squareSize, (float)_num, _weight);

        int notificationLength = (int)Math.ceil(_numNotifications);

        if( notificationLength == 0 ){
            p.setColor(colors[0]);
            c.drawRect(x-squareSize/2, yStart - squareSize, x+squareSize/2, yStart, p);
        }

        float[] ys = getRectYs(yStart, barHeight, notificationLength);

        for(int i=0; i<notificationLength; i++){
            p.setColor(getColor(i, _numNotifications));
            float top = ys[i+1];
            float bottom = ys[i];
            //if its pitched head down then offset everything up the screen
            if( Math.signum(_weight) >= 0) {
                float tiltOffset = squareSize - barHeight * notificationLength;
                top -= tiltOffset;
                if( i != 0 ){
                    bottom -= tiltOffset;
                }
            } else if( Math.signum(_weight) < 0 && i == notificationLength-1 ){
                top = yStart - squareSize;
            }
            c.drawRect(x - squareSize / 2, top, x + squareSize / 2, bottom, p);
        }

        // Text
        int num = Math.round(numNotifications);
        String text1 = Integer.toString(num) +  (num == 1 ? "Notification" : " Notifications");
        String text2 = "";

        if(!NotificationService.permissionsGranted) {
            text1 = "Please open the Meter application";
            text2 = "to enable this wallpaper";
        }

        drawText(text1, text2, x, (int) (yStart+(7f*pixelDensity)), c);
    }
}
