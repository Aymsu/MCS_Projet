package com.paulsab.aymer.mcs;

import android.animation.Animator;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;

import java.util.ArrayList;

public class Aide extends AppCompatActivity {

    RelativeLayout rootLayoutAide;
    ArrayList<String> listcommandes = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aide);

        rootLayoutAide = (RelativeLayout) findViewById(R.id.activity_aide);

        ViewTreeObserver viewTreeObserver = rootLayoutAide.getViewTreeObserver();

        if(savedInstanceState == null)
            rootLayoutAide.setVisibility(View.INVISIBLE);

        if(viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    circularRevealActivity();
                    rootLayoutAide.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            });
        }

        initCommandes();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.backButton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
            }
        });

    }

    public void initCommandes() {

        listcommandes.add("Arrête toi");
        listcommandes.add("Avance");
        listcommandes.add("Recule");
        listcommandes.add("Droite");
        listcommandes.add("Gauche");
        listcommandes.add("Tourne droite");
        listcommandes.add("Tourne gauche");
        listcommandes.add("Etat d'urgence");

        ListView commandes = (ListView) findViewById(R.id.listCommandes);

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                listcommandes );

        commandes.setAdapter(arrayAdapter);


    }

    private void circularRevealActivity() {

        int cx = rootLayoutAide.getWidth() / 2;
        int cy = rootLayoutAide.getHeight() / 2;

        float finalRadius = Math.max(rootLayoutAide.getWidth(), rootLayoutAide.getHeight());

        // create the animator for this view (the start radius is zero)
        Animator circularReveal = ViewAnimationUtils.createCircularReveal(rootLayoutAide, cx, cy, 0, finalRadius);
        circularReveal.setDuration(1000);

        // make the view visible and start the animation
        System.out.println("TA RACE G RGIZH DEBUG");
        rootLayoutAide.setVisibility(View.VISIBLE);
        circularReveal.start();
    }
}
