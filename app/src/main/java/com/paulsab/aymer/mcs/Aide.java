package com.paulsab.aymer.mcs;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class Aide extends AppCompatActivity {

    ArrayList<String> listcommandes = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aide);

        initCommandes();

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
}
