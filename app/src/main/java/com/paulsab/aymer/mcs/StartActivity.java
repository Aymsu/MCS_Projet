package com.paulsab.aymer.mcs;

import android.content.Intent;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;

public class StartActivity extends AppCompatActivity {

    WebView wv;
    public static MediaPlayer mpMario;

    // Used to load the 'native-lib' library on application startup.
    static {
//        System.loadLibrary("d-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        System.out.println("Je hais git");

        mpMario = MediaPlayer.create(this, R.raw.mario);
        mpMario.start();
        mpMario.setLooping(true);

        wv = (WebView)findViewById(R.id.gifWebView);
        wv.loadUrl("file:///android_asset/html/gif.html");


    }

    public void launchAide(View v){


        Intent i = new Intent(this, Aide.class);
        startActivity(i);

    }

    public void goRecoVocale(View v){

        Intent i = new Intent(this, RecoVocale.class);
        mpMario.pause();
        startActivity(i);

    }

    @Override
    protected void onPause() {
        super.onPause();
        mpMario.pause();
    }

    @Override
    public void onResume(){
        super.onResume();
        mpMario.start();
    }


}
