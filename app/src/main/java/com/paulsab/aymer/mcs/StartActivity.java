package com.paulsab.aymer.mcs;

import android.content.Intent;
import android.media.MediaPlayer;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;

public class StartActivity extends AppCompatActivity {

    WebView wv;
    public static MediaPlayer mpMario;
    public static boolean aideActif;

    public static int mute = 0;

    // Used to load the 'native-lib' library on application startup.
    static {
//        System.loadLibrary("d-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        System.out.println("Je hais git");
        aideActif = false;

        mpMario = MediaPlayer.create(this, R.raw.mario);
        mpMario.start();
        mpMario.setLooping(true);

        wv = (WebView)findViewById(R.id.gifWebView);
        wv.loadUrl("file:///android_asset/html/gif.html");

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.muteButton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mute == 0){
                    fab.setImageResource(R.drawable.rsz_mute);
                    mute = 1;
                    mpMario.pause();
                }else{
                    fab.setImageResource(R.drawable.rsz_sound);
                    mpMario.start();
                    mute = 0;
                }
            }
        });


    }

    public void launchAide(View v){

        aideActif = true;
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
        if(!aideActif)
            mpMario.pause();
    }

    @Override
    public void onResume(){
        super.onResume();
        mpMario.start();
    }


}
