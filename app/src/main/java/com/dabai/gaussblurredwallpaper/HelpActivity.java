package com.dabai.gaussblurredwallpaper;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;

public class HelpActivity extends AppCompatActivity {

    WebView webview;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        setTitle("帮助文档");


        webview = findViewById(R.id.webview);
        webview.loadUrl("file:////android_asset/help.html");



    }
}
