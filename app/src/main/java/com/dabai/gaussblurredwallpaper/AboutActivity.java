package com.dabai.gaussblurredwallpaper;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        setTitle("关于本程序");
        WebView webview;
        webview = findViewById(R.id.webview);
        webview.loadUrl("file:////android_asset/about.html");

    }
}
