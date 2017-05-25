package com.udacity.stockhawk.ui;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.app.Activity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IFillFormatter;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.EntryXComparator;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;

public class StockHistory extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    @SuppressWarnings("WeakerAccess")

    @BindView(R.id.chart)
    LineChart chart;

    @BindView(R.id.stock_name)
    TextView stockName;

    @BindView(R.id.error)
    TextView error;
    private String symbol = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_history);
        ButterKnife.bind(this);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            symbol = extras.getString("symbol");
            stockName.setText(symbol);
            getSupportLoaderManager().initLoader(0, null, this);
            //The key argument here must match that used in the other activity
        } else {
            error.setText(R.string.quote_invalid);
        }
    }


    public static int getThemeColor (final Context context, int color) {
        final TypedValue value = new TypedValue ();
        context.getTheme ().resolveAttribute (color, value, true);
        return value.data;
    }

    private void drawHistory(Cursor data) {
        if (data.getCount() == 0) {
            error.setText(R.string.no_history_for_quote);
        } else {
            data.moveToFirst();
            String history = data.getString(0);
            String[] lines = history.split(System.getProperty("line.separator"));
            Collections.reverse(Arrays.asList(lines));
            if (lines.length > 0) {
                List<Entry> entries = new ArrayList<Entry>();
                List<String> xValues = new ArrayList<String>();
                Float i = 0f;
                for (String line : lines) {
                    String[] values = line.split(",");
                    if (values.length == 2) {
                        entries.add(new Entry(i, Float.valueOf(values[1])));
                        xValues.add(values[0]);
                    }
                    i += 1f;
                }
                Collections.sort(entries, new EntryXComparator());
                LineDataSet dataSet = new LineDataSet(entries, getString(R.string.stock_history)); // add entries to dataset


                dataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
                dataSet.setColor(getThemeColor(this, R.attr.colorPrimary));
                dataSet.setDrawCircles(false);
                dataSet.setLineWidth(2f);
                dataSet.setCircleRadius(3f);
                dataSet.setFillAlpha(0);
                dataSet.setDrawFilled(true);
                dataSet.setFillColor(getThemeColor(this, R.attr.colorPrimary));
                dataSet.setValueTextColor(getThemeColor(this, R.attr.colorAccent));
                dataSet.setValueTextSize(10f);
                dataSet.setHighLightColor(getThemeColor(this, R.attr.colorAccent));
                dataSet.setDrawCircleHole(false);

                dataSet.setFillFormatter(new IFillFormatter() {
                    @Override
                    public float getFillLinePosition(ILineDataSet dataSet, LineDataProvider dataProvider) {
                        return chart.getAxisLeft().getAxisMinimum();
                    }
                });


                LineData lineData = new LineData(dataSet);
                XAxis xAxis = chart.getXAxis();
                xAxis.setValueFormatter(new MyXAxisValueFormatter(xValues.toArray(new String[0])));
                xAxis.setAxisLineColor(getThemeColor(this, R.attr.colorPrimaryDark));
                xAxis.setTextColor(getThemeColor(this, R.attr.colorPrimaryDark));
                xAxis.setLabelRotationAngle(-45f);

                YAxis leftAxis = chart.getAxisLeft();
                leftAxis.setDrawAxisLine(false);
                leftAxis.setZeroLineColor(getThemeColor(this, R.attr.colorPrimaryDark));
                leftAxis.setTextColor(getThemeColor(this, R.attr.colorPrimaryDark));
//                leftAxis.setDrawZeroLine(false);
                leftAxis.setDrawGridLines(false);

                YAxis rightAxis = chart.getAxisRight();
                rightAxis.setEnabled(false);

                chart.setData(lineData);
                Legend l = chart.getLegend();
                l.setEnabled(false);
                //chart.setBackgroundColor(Color.WHITE);
                //chart.setGridBackgroundColor(Color.argb(150, 51, 181, 229));
                chart.setDrawGridBackground(false);

                chart.setDrawBorders(false);

                // no description text
                chart.getDescription().setEnabled(false);
                chart.setScaleEnabled(true);
                chart.setPinchZoom(true);
                chart.invalidate(); // refresh
            }

        }

    }

    public  String getDateCurrentTimeZone(long timestamp) {
        try{
            Calendar calendar = Calendar.getInstance();
            TimeZone tz = TimeZone.getDefault();
            calendar.setTimeInMillis(timestamp);
            calendar.add(Calendar.MILLISECOND, tz.getOffset(calendar.getTimeInMillis()));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date currenTimeZone = (Date) calendar.getTime();
            return sdf.format(currenTimeZone);
        }catch (Exception e) {
        }
        return "";
    }
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String mSelectionClause = Contract.Quote.COLUMN_SYMBOL + " = ?";
        String[] selectionArgs = {symbol};

        return new CursorLoader(this,
                Contract.Quote.URI,
                new String[]{Contract.Quote.COLUMN_HISTORY},
                mSelectionClause, selectionArgs, Contract.Quote.COLUMN_SYMBOL);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        drawHistory(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private class MyXAxisValueFormatter implements IAxisValueFormatter {
        private String[] mValues;

        public MyXAxisValueFormatter(String[] values) {
            this.mValues = values;
        }

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            // "value" represents the position of the label on the axis (x or y)
            return getDateCurrentTimeZone(Long.parseLong(mValues[(int) value], 10) );
        }
    }
}
