package com.paulsab.aymer.mcs;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;

public class StartActivity extends AppCompatActivity {

    WebView wv;

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        System.out.println("Je hais git");

        wv = (WebView)findViewById(R.id.gifWebView);
        wv.loadUrl("file:///android_asset/html/gif.html");


    }

    public void launchAide(View v){


        Intent i = new Intent(this, Aide.class);
        startActivity(i);

    }

    public void goRecoVocale(View v){

        Intent i = new Intent(this, RecoVocale.class);
        startActivity(i);

    }

    public void goTest( View v) {
        Intent i = new Intent(this, CircularRevealActivity.class);
        startActivity(i);
    }

}
