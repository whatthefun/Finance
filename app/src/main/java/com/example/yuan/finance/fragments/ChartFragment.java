package com.example.yuan.finance.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.yuan.finance.R;
import com.example.yuan.finance.helpers.MyDBHelper;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import java.util.ArrayList;

/**
 * Created by YUAN on 2017/7/26.
 */

public class ChartFragment extends Fragment {

    private static final String TAG = "ChartFragment";
    BarChart mBarChart;
    MyDBHelper helper;

    @Nullable @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
        @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chart, container, false);

        helper = MyDBHelper.getInstance(getActivity());

        mBarChart = (BarChart) view.findViewById(R.id.barChat);
        mBarChart.setDrawGridBackground(true);
        XAxis xAxis = mBarChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        BarData data = new BarData(getMonthXAxisValues(), getDataSet());
        mBarChart.setData(data);
        mBarChart.setDescription("");
        mBarChart.animateXY(1000, 1000);
        mBarChart.invalidate();

        return view;
    }

    @Override public void onResume() {
        Log.d(TAG, "onResume: ");
        super.onResume();
        mBarChart.notifyDataSetChanged();
        mBarChart.invalidate();
    }

    //取得X座標(月份)
    private ArrayList<String> getMonthXAxisValues(){
        ArrayList<String> monthXAxisValues = new ArrayList<>();
        monthXAxisValues.add("Jan");
        monthXAxisValues.add("Feb");
        monthXAxisValues.add("Mar");
        monthXAxisValues.add("Apr");
        monthXAxisValues.add("May");
        monthXAxisValues.add("Jan");
        monthXAxisValues.add("Jul");
        monthXAxisValues.add("Aug");
        monthXAxisValues.add("Sep");
        monthXAxisValues.add("Oct");
        monthXAxisValues.add("Nov");
        monthXAxisValues.add("Dec");

        return monthXAxisValues;
    }

    private ArrayList<BarDataSet> getDataSet(){
        ArrayList<BarDataSet> dataSet = new ArrayList<>();
        ArrayList<BarEntry> barEntries = new ArrayList<>();
        for (int i = 1; i <= 12 ; i++){
            BarEntry barEntry = new BarEntry(querySum("2017", i + ""), i-1);
            barEntries.add(barEntry);
        }

        BarDataSet barDataSet = new BarDataSet(barEntries, "2017 Total Expense");
        barDataSet.setColors(new int[] { R.color.green, R.color.blue}, getActivity());

        dataSet.add(barDataSet);
        return dataSet;
    }

    // query amount
    private int querySum(String y, String m) {
        if (m.length() == 1){m = "0" + m;}

        String query = "date like \'" + y + "/" + m + "%\'";
        Cursor c = helper.getWritableDatabase()
            .query("expense", new String[] { "SUM(amount)" }, query, null, null, null, null);

        if (c.moveToFirst()) {
            return c.getInt(0);
        }
        return 0;
    }
}
