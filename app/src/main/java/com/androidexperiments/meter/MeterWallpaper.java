package com.androidexperiments.meter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.SurfaceHolder;

import java.util.ArrayList;

import com.androidexperiments.meter.drawers.BatteryDrawer;
import com.androidexperiments.meter.drawers.CombinedWifiCellularDrawer;
import com.androidexperiments.meter.drawers.Drawer;
import com.androidexperiments.meter.drawers.NotificationsDrawer;

import com.actionlauncher.api.LiveWallpaperSource;

/**
 * The Live Wallpaper Service and rendering Engine
 */
public class MeterWallpaper extends WallpaperService {

    private final String TAG = this.getClass().getSimpleName();

    private Drawer mDrawer;

    // Variable containing the index of the drawer last shown
    private int mDrawerIndex = -1;

    // Whether we have to update actionlauncher with a new wallpaper
    // ONLY set to true if actionlauncher was updated SUCCESSFULLY
    private boolean mUpdateActionLauncher;

    @Override
    public Engine onCreateEngine() {
        WallpaperEngine engine = new WallpaperEngine(this);

        return engine;
    }


    /**
     * The wallpaper engine that will handle the rendering
     */
    private  class WallpaperEngine extends WallpaperService.Engine {

        public Context mContext;

        private boolean mVisible = false;
        private final Handler mHandler = new Handler();

        public WallpaperEngine(Context context) {
            this.mContext = context;
        }

        /**
         * Handle tap commands
         */
        public Bundle onCommand(String action, int x, int y, int z, Bundle extras, boolean resultRequested){
            //taps work on Nexus devices but not all, for example Samsung
            if(action.equals("android.wallpaper.tap")){
                if( mDrawer != null ) {
                    mDrawer.tap(x, y);
                }
            }
            return super.onCommand(action,x,y,z,extras,resultRequested);
        }

        /**
         * Draw runloop
         */
        private final Runnable mUpdateDisplay = new Runnable() {
            @Override
            public void run() {
                draw();
            }
        };

        /**
         * Draw function doing the context locking and rendering
         */
        private void draw() {
            if(mDrawer == null) return;

            // Ask the drawer if wants to draw in this frame
            if(mDrawer.shouldDraw()) {
                SurfaceHolder holder = getSurfaceHolder();
                Canvas c = null;
                Bitmap bmp = null;
                // Draw into a Bitmap which will then be sent to ActionLauncher
                if (mUpdateActionLauncher) {
                    Rect surfaceFrame = holder.getSurfaceFrame();
                    bmp = Bitmap.createBitmap(surfaceFrame.width(), surfaceFrame.height(), Bitmap.Config.ARGB_8888);
                    c = new Canvas(bmp);
                    if (c != null) {
                        // Let the drawer render to the canvas
                        mDrawer.draw(c);
                    }
                    updateActionLauncher(bmp);
                }
                try {
                    // Lock the drawing canvas
                    c = holder.lockCanvas();
                    if (c != null) {
                        if (bmp != null) {
                            c.drawBitmap(bmp, 0, 0, null);
                        } else {
                            // Let the drawer render to the canvas
                            mDrawer.draw(c);
                        }
                    }
                } finally {
                    if (c != null) holder.unlockCanvasAndPost(c);
                }
            }
            mHandler.removeCallbacks(mUpdateDisplay);
            if (mVisible) {
                // Wait one frame, and redraw
                mHandler.postDelayed(mUpdateDisplay, 33);
            }
        }

        // Taken from the ActionLauncher API Documentation
        private void updateActionLauncher(Bitmap bmp) {
            if (bmp != null) {
                try {
                    LiveWallpaperSource.with(mContext)
                            .setBitmapSynchronous(bmp)
                            .run();
                    mUpdateActionLauncher = false;
                } catch (OutOfMemoryError outOfMemoryError) {
                    // Palette generation was unable to process the Bitmap passed in to
                    // setBitmapSynchronous(). Consider using a smaller image.
                    // See ActionPalette.DEFAULT_RESIZE_BITMAP_MAX_DIMENSION
                    Log.e(TAG, "Error setting ActionLauncher LiveWallpaper", outOfMemoryError);
                } catch (IllegalArgumentException illegalArgumentEx) {
                    // Raised during palette generation. Check your Bitmap.
                    Log.e(TAG, "Error setting ActionLauncher LiveWallpaper", illegalArgumentEx);
                } catch (IllegalStateException illegalStateException) {
                    // Raised during palette generation. Check your Bitmap.
                   Log.e(TAG, "Error setting ActionLauncher LiveWallpaper", illegalStateException);
                }
            }
        }

        /**
         * Toggle visibility of the wallpaper
         * In this function we create new drawers every time the wallpaper
         * is visible again, cycling through the available ones
         * @param visible whether the wallpaper is currently visible
         */
        @Override
        public void onVisibilityChanged(boolean visible) {
            mVisible = visible;
            if (visible) {

                ArrayList<Class> drawerClasses = new ArrayList<Class>();

                //always include wifi + battery
                drawerClasses.add(CombinedWifiCellularDrawer.class);
                drawerClasses.add(BatteryDrawer.class);
                //only include notifications if it has permission
                if(NotificationService.permissionsGranted){
                    drawerClasses.add(NotificationsDrawer.class);
                }

                mDrawerIndex++;
                if( mDrawerIndex >= drawerClasses.size() ){
                    mDrawerIndex = 0;
                }
                Class cls = drawerClasses.get(mDrawerIndex);
                if(cls == NotificationsDrawer.class) {
                    mDrawer = new NotificationsDrawer(mContext);
                } else if(cls == BatteryDrawer.class) {
                    mDrawer = new BatteryDrawer(mContext);
                } else {
                    mDrawer = new CombinedWifiCellularDrawer(mContext);
                }

                mDrawer.start();

                // Start the drawing loop and make sure ActionLauncher will get updated
                mUpdateActionLauncher = true;
                draw();

            } else {
                if( mDrawer != null ) {
                    mDrawer.destroy();
                    mDrawer = null;
                }
                mHandler.removeCallbacks(mUpdateDisplay);
            }
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            mUpdateActionLauncher = true;
            draw();
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
            mVisible = false;
            mHandler.removeCallbacks(mUpdateDisplay);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            mVisible = false;
            mHandler.removeCallbacks(mUpdateDisplay);
        }
    }

}


