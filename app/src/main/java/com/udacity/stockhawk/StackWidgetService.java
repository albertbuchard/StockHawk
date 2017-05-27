package com.udacity.stockhawk;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.data.Contract;

import java.util.ArrayList;

/**
 * Created by Pro on 25.05.17.
 */

public class StackWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new StackRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}

class StackRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    public Cursor cursor = null;
    public ArrayList<StockData> data = null;
    private Context mContext;
    private int mAppWidgetId;

    public StackRemoteViewsFactory(Context context, Intent intent) {
        mContext = context;
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    // Initialize the data set.
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {
        final long identityToken = Binder.clearCallingIdentity();
        if (cursor != null) {
            cursor.close();
        }

        cursor = mContext.getContentResolver().query(Contract.Quote.URI,
                new String[]{Contract.Quote.COLUMN_SYMBOL, Contract.Quote.COLUMN_PERCENTAGE_CHANGE, Contract.Quote.COLUMN_ABSOLUTE_CHANGE},
                null,
                null,
                Contract.Quote.COLUMN_SYMBOL);
        for (int i = 0; i < cursor.getCount(); i++) {

        }
        Binder.restoreCallingIdentity(identityToken);
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        if (cursor == null)
            return 0;
        else
            return cursor.getCount();
    }


    public RemoteViews getViewAt(int position) {

        String symbol = "-";
        String percentVariation = "-";
        float rawAbsoluteChange = 1;

        if (cursor.moveToPosition(position)) {
            symbol =  cursor.getString(0);
            float percentageChange = cursor.getFloat(1);
            rawAbsoluteChange = cursor.getFloat(2);
            percentVariation = String.valueOf(percentageChange) + "%";
        }

        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.widget_stock);
        rv.setTextViewText(R.id.symbol, symbol);
        rv.setTextViewText(R.id.change, percentVariation);


        if (rawAbsoluteChange > 0) {
            rv.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_green);
        } else {
            rv.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_red);
        }


        Intent fillInIntent = new Intent();
        fillInIntent.putExtra("symbol", symbol);
        rv.setOnClickFillInIntent(R.id.listItemLayout, fillInIntent);

        return rv;
    }

    public RemoteViews getLoadingView() {
        return null;
    }
    public int getViewTypeCount() {
        return 1;
    }
    public long getItemId(int position) {
        return position;
    }
    public boolean hasStableIds() {
        return true;
    }

}