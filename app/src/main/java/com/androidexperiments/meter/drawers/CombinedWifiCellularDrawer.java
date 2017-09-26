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

import android.content.Context;
import android.graphics.Canvas;

/**
 * Combine the two Drawers into one which toggles between them
 * depending on if you are on wifi
 */
public class CombinedWifiCellularDrawer extends Drawer
{

    protected CellularDrawer mCellularDrawer;
    protected WifiDrawer mWifiDrawer;
    protected Drawer mActiveDrawer;


    public CombinedWifiCellularDrawer(Context context){
        super(context);
        mCellularDrawer = new CellularDrawer(context);
        mWifiDrawer = new WifiDrawer(context);
        selectActiveDrawer();
    }


    @Override
    public void tap(int x, int y){
        mActiveDrawer.tap(x, y);
    }

    @Override
    public void destroy(){
        super.destroy();
        mCellularDrawer.destroy();
        mWifiDrawer.destroy();
    }

    @Override
    public void start() {
        mWifiDrawer.start();
        mCellularDrawer.start();
    }

    protected void selectActiveDrawer(){
        mActiveDrawer = mWifiDrawer.isCritical() ? mCellularDrawer : mWifiDrawer;
    }

    @Override
    public boolean shouldDraw(){
        Drawer activeDrawer = mActiveDrawer;
        selectActiveDrawer();
        //if the drawer switched then definitely draw
        if( activeDrawer != mActiveDrawer ){
            //no matter what call shouldDraw, cause there might be setup code in it
            mActiveDrawer.shouldDraw();
            return true;
        }

        return mActiveDrawer.shouldDraw();
    }


    @Override
    public void draw(Canvas c){
        mActiveDrawer.draw(c);
    }

}
