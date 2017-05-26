package com.udacity.stockhawk.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.udacity.stockhawk.R;

import java.io.IOException;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;
import yahoofinance.quotes.stock.StockQuote;

import butterknife.BindView;
import butterknife.ButterKnife;


public class AddStockDialog extends DialogFragment {

    boolean isValidStock = false;
    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.dialog_stock)
    EditText stock;

    @BindView(R.id.stock_name_adapt)
    TextView stock_name;

    AlertDialog dialog = null;
    private boolean contentDescriptionSet = false;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        @SuppressLint("InflateParams") View custom = inflater.inflate(R.layout.add_stock_dialog, null);

        ButterKnife.bind(this, custom);

        stock.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                //addStock();
                return true;
            }
        });

        TextWatcher inputTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                stock_name.setText("");
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                final String symbol = stock.getText().toString();
                isValidStock = false;
                stock.setTextColor(Color.LTGRAY);
                stock_name.setText("");
                updateButtons();
                if ((!symbol.isEmpty()) && (symbol != null)) {
                    if (isAlpha(symbol)) {
                        new AsyncTask<Void, Void, String>() {
                            String initialSymbol = null;
                            protected String doInBackground(Void... params) {
                                // Background Code
                                Stock stockObject = null;
                                initialSymbol = symbol;
                                try {
                                    stockObject = YahooFinance.get(symbol);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                return stockObject.getName();
                            }
                            protected void onPostExecute(String msg) {
                                if (stock.getText().toString().equals(initialSymbol)) {
                                    if (msg != null) {
                                        isValidStock = true;
                                        stock_name.setText(msg);
                                    } else {
                                        stock_name.setText("Invalid stock");
                                    }

                                    stock.setTextColor(isValidStock ? Color.rgb(0,250,50):Color.RED);
                                } else {
                                    stock.setTextColor(Color.LTGRAY);
                                    stock_name.setText("");
                                }

                                updateButtons();
                            }
                        }.execute();
                    } else {
                        stock_name.setText("Character not allowed");
                        stock.setTextColor(Color.RED);
                    }

                }
            }
        };

        stock.addTextChangedListener(inputTextWatcher);

        builder.setView(custom);

        builder.setMessage(getString(R.string.dialog_title));
        builder.setPositiveButton(getString(R.string.dialog_add),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        addStock();
                    }
                });
        builder.setNegativeButton(getString(R.string.dialog_cancel), null);

        dialog = builder.create();

        Window window = dialog.getWindow();
        if (window != null) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                updateButtons();
            }
        });

        return dialog;
    }

    public boolean isAlpha(String name) {
        char[] chars = name.toCharArray();

        for (char c : chars) {
            if(!Character.isLetter(c)) {
                return false;
            }
        }

        return true;
    }

    private void updateButtons() {
        Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);

        if (button != null) {
            if (!contentDescriptionSet) {
                button.setContentDescription(getString(R.string.validate_addition));
                contentDescriptionSet = true;
            }
            button.setEnabled(isValidStock);
        }

    }

    private void addStock() {
        if (!isValidStock){  return; }
        Activity parent = getActivity();
        if (parent instanceof MainActivity) {
            ((MainActivity) parent).addStock(stock.getText().toString());
        }
        dismissAllowingStateLoss();
    }


}
