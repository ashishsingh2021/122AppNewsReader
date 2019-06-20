package com.example.a122appnewsreader;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.TextView;

public class DisplayContent extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_content);


        Intent intent =getIntent();
        String title=intent.getStringExtra("Title");
        String content=intent.getStringExtra("Content");

        WebView webView = (WebView) findViewById(R.id.wV);

        webView.getSettings().getJavaScriptEnabled();

        webView.setWebViewClient(new WebViewClient()); //Imp to open in our app else opens in chrome or deAULT browser

        webView.loadData(content, "text/html", "UTF-8");






    }
}
