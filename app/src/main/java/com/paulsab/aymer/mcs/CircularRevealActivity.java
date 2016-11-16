package com.paulsab.aymer.mcs;

import android.animation.Animator;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

public class CircularRevealActivity extends AppCompatActivity {

    FrameLayout rootLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_circular_reveal);

        rootLayout = (FrameLayout) findViewById(R.id.root_layout);

        if(savedInstanceState == null)
            rootLayout.setVisibility(View.INVISIBLE);


        ViewTreeObserver viewTreeObserver = rootLayout.getViewTreeObserver();

        if(viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    circularRevealActivity();
                    rootLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            });
        }



        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
            }
        });
    }

    private void circularRevealActivity() {

        int cx = rootLayout.getWidth() / 2;
        int cy = rootLayout.getHeight() / 2;

        float finalRadius = Math.max(rootLayout.getWidth(), rootLayout.getHeight());

        // create the animator for this view (the start radius is zero)
        Animator circularReveal = ViewAnimationUtils.createCircularReveal(rootLayout, cx, cy, 0, finalRadius);
        circularReveal.setDuration(1000);

        // make the view visible and start the animation
        rootLayout.setVisibility(View.VISIBLE);
        circularReveal.start();
    }

}
