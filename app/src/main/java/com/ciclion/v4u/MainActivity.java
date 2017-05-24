package com.ciclion.v4u;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;

import io.vov.vitamio.LibsChecker;

/**
 * Browser activity with a webView to browse PBE server
 * It has been developed by Pau
 */
public class MainActivity extends AppCompatActivity {


    EditText urlText;
    WebView webView;
    Button goButton;

    String serverURL = "http://192.168.1.100";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(!LibsChecker.checkVitamioLibs(this)){
            return;
        }
        setContentView(R.layout.activity_main);


        urlText = (EditText) findViewById(R.id.urlEditText);
        urlText.setHint(serverURL);

        webView = (WebView) findViewById(R.id.webView);
        webView.setWebViewClient(new CustomWebViewClient());
        webView.getSettings().setLoadsImagesAutomatically(true);
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

        webView.loadUrl(serverURL);


        goButton = (Button) findViewById(R.id.goButton);
        goButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!urlText.getText().toString().equals("")){
                    webView.loadUrl(urlText.getText().toString());
                }
            }
        });
    }


    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Custom WebViewClient made to fire an intent to PlayerActivity when a m3u8 file is browsed
     */
    private class CustomWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {


            if(Helper.extension(url, '.').equals("m3u8")){
                Intent in = new Intent(MainActivity.this, PlayerActivity.class);
                in.putExtra("playlistURL", url);
                in.putExtra("serverURL", serverURL);
                startActivity(in);
                return true;
            }else {
                view.loadUrl(url);
                return true;
            }
        }
    }
}
