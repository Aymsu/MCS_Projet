package com.paulsab.aymer.mcs.Charts;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.v4.view.ViewCompat;
import android.util.Log;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.paulsab.aymer.mcs.AnalyzeActivity.Constante;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by benjidu11 on 13/11/2016.
 */

public class DrawLineChart {

    /**
      * Current graph show in the app
      */
    private LineChart graph;

    /**
     * Data will be convert in this list
     */
    private List<Entry> entries = new ArrayList<>();

    /**
     * LineDataSet is used for styling lines
     */
    private LineDataSet dataSet;

    /**
     * LineData is the object set into the chart
     */
    private LineData lineData;

    private RectF axisBounds;

    private int canvasWidth, canvasHeight;

    public DrawLineChart (LineChart contextGraph) {

        this.graph = contextGraph;
        axisBounds = new RectF(0,0,contextGraph.getHeight(),contextGraph.getWidth());
        canvasHeight = contextGraph.getHeight();
        canvasWidth = contextGraph.getWidth();

        denyInteractions();
    }

    public void denyInteractions () {
       /* graph.setTouchEnabled(false);
        graph.setDragEnabled(false);
        graph.setScaleEnabled(false);
        graph.setPinchZoom(false);
        graph.setDoubleTapToZoomEnabled(false);*/

        graph.getXAxis().setEnabled(false);
        graph.getAxisLeft().setEnabled(false);
        graph.getAxisRight().setEnabled(false);

        graph.setDescription(null);    // Hide the description
        graph.getAxisLeft().setDrawLabels(false);
        graph.getAxisRight().setDrawLabels(false);
        graph.getXAxis().setDrawLabels(false);

        graph.getLegend().setEnabled(false);
        //graph.getAxis(YAxis.AxisDependency.LEFT).setEnabled(false);
        //graph.getXAxis().setEnabled(false);

    }

    private double clamp(double value) {
        if (value < axisBounds.bottom) {
            value = axisBounds.bottom;
        } else if (value > axisBounds.top) {
            value = axisBounds.top;
        }
        return value;
    }

    public double[] convertFreq (int sizeVector) {
        double[] m_freq = new double[sizeVector/2];
        for ( int i = 0 ; i < sizeVector/2 ; i++) {
            m_freq[i] = i*Constante.SAMPLE_RATE/sizeVector;
        }
        return m_freq;
    }

    public void recompute(double[] db, int count) {
        entries.clear();

        if (canvasHeight < 1) {
            return;
        }
        // TODO: ecouter le buffer et afficher des valeurs uniquement si on parle
        double[] m_freq = convertFreq(Constante.FFT_BINS);

        for ( int i = 0 ; i < count/2 ; i++ ){
            //Log.i(Constante.TAG,"freq = "+m_freq[i]+" magn = "+db[i]);
            Entry b = new Entry((float)m_freq[i],(float)db[i]);
            entries.add(b);
        }
        dataSet = new LineDataSet(entries,"Label");
        dataSet.setDrawCircles(false);
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        //dataSet.setColor(Color.DKGRAY);
        lineData = new LineData(dataSet);
        //dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        graph.setData(lineData);
        graph.invalidate();


    }
}
