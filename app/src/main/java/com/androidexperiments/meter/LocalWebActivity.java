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
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageButton;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


/**
 * Activity for opening html files within this projects asset directory
 */
public class LocalWebActivity extends Activity
{

    public static final String EXTRA_HTML_URI = "extra_html_uri";

    protected WebView mWebView;
    protected ImageButton mCloseBtn;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_licenses);


        Intent incoming = getIntent();
        Bundle extras = incoming.getExtras();

        String uri = "html/licenses.html";
        if(extras != null){
            uri = extras.getString(EXTRA_HTML_URI);
        }

        mWebView = (WebView) findViewById(R.id.webView);
        mCloseBtn = (ImageButton) findViewById(R.id.closeButton);

        mCloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mWebView.loadUrl("file:///android_asset/"+uri);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
