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

        InputStream is;
        String htmlData = "";
        try {
            is = this.getAssets().open(uri);
            BufferedReader r = new BufferedReader(new InputStreamReader(is));
            StringBuilder stringBuilder = new StringBuilder();

            String line;
            while( (line=r.readLine()) != null ) {
                stringBuilder.append(line);
            }

            htmlData = stringBuilder.toString();
        } catch( IOException error ) {

        }

        mWebView.loadDataWithBaseURL("file:///android_asset/", htmlData, "text/html", "utf-8", "about:blank");

    }
}
