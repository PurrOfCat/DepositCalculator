package com.example.depositcalculator;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private double usdToRub = 65;

    private Button btnDate;
    private TextView lblSum;
    private TextView lblClosing;
    private TextView lblIncome;
    private EditText tbDeposit;
    private EditText tbPeriod;
    private EditText tbPercent;
    private Spinner spinPeriod;
    private Spinner spinCurrency;
    private Spinner spinFrequency;
    private CheckBox cbCapitalization;
    private DatePickerDialog datePickerDialog;

    TextView lblBottomSheet;
    ProgressBar pbBottomSheet;
    private String currency = "₽";
    private Calendar openingDate;
    private Calendar closingDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new AsyncRequest().execute();
        setContentView(R.layout.activity_main);

        btnDate = findViewById(R.id.btnDate);
        lblSum = findViewById(R.id.lblSum);
        lblIncome = findViewById(R.id.lblIncome);
        lblClosing = findViewById(R.id.lblClosing);
        tbDeposit = findViewById(R.id.tbDeposit);
        tbPeriod = findViewById(R.id.tbPeriod);
        tbPercent = findViewById(R.id.tbPercent);
        spinPeriod = findViewById(R.id.spinPeriod);
        spinCurrency = findViewById(R.id.spinCurrency);
        spinFrequency = findViewById(R.id.spinFrequency);
        cbCapitalization = findViewById(R.id.cbCapitalization);

        ConstraintLayout bottomSheet = findViewById(R.id.bottom_sheet);
        BottomSheetBehavior.from(bottomSheet);

        lblBottomSheet = findViewById(R.id.text_view_sum);
        pbBottomSheet = findViewById(R.id.progress_bar);

        setupListeners();
        btnDate.setText(getTodaysDate());
        initDatePicker();
    }

    private String getTodaysDate() {
        openingDate = Calendar.getInstance();
        int day = openingDate.get(Calendar.DAY_OF_MONTH);
        int month = openingDate.get(Calendar.MONTH) + 1;
        int year = openingDate.get(Calendar.YEAR);
        return makeDateString(day, month, year);
    }

    private void setClosingDate() {
        if (!tbPeriod.getText().toString().equals("")) {
            closingDate = (Calendar) openingDate.clone();
            switch (spinPeriod.getSelectedItem().toString()) {
                case "год/года/лет":
                    closingDate.add(Calendar.YEAR, Integer.parseInt(tbPeriod.getText().toString()));
                    break;
                case "месяц(а)(ев)":
                    closingDate.add(Calendar.MONTH, Integer.parseInt(tbPeriod.getText().toString()));
                    break;
            }
            lblClosing.setText(
                    makeDateString(closingDate.get(Calendar.DAY_OF_MONTH), (closingDate.get(Calendar.MONTH) + 1), closingDate.get(Calendar.YEAR)));
        } else {
            lblClosing.setText("");
        }
    }

    private String makeDateString(int day, int month, int year) {
        if (month < 10) {
            return day + "/0" + month + "/" + year;
        } else {
            return day + "/" + month + "/" + year;
        }
    }

    private void initDatePicker() {
        DatePickerDialog.OnDateSetListener dateSetListener = (datePicker, year, month, day) -> {
            month = month + 1;
            btnDate.setText(makeDateString(day, month, year));
            openingDate.set(year, month, day);
            setClosingDate();
        };
        datePickerDialog = new DatePickerDialog(this, AlertDialog.THEME_HOLO_LIGHT, dateSetListener, openingDate.get(Calendar.YEAR), openingDate.get(Calendar.MONTH), openingDate.get(Calendar.DAY_OF_MONTH));
    }

    public void openingDate(View view) {
        datePickerDialog.show();
    }

    private void calculate() {
        if (tbDeposit.getText().toString().equals("") ||
            tbPercent.getText().toString().equals("") ||
            tbPeriod.getText().toString().equals("")) {
            lblSum.setText("");
            lblIncome.setText("");
            return;
        }

        double deposit = Double.parseDouble(tbDeposit.getText().toString());
        double period = Integer.parseInt(tbPeriod.getText().toString());
        double percent = Double.parseDouble(tbPercent.getText().toString()) / 100;
        double income;
        double sum = 0;

        if (Objects.equals(currency, "$")) {
            deposit *= usdToRub;
        }

        if (cbCapitalization.isChecked()) {
            switch (spinFrequency.getSelectedItem().toString()) {
                case "Раз в месяц":
                    if (spinPeriod.getSelectedItem().toString().equals("год/года/лет")) {
                        period = period * 12;
                    }
                    sum = deposit * Math.pow((1 + percent / 12), period);
                    break;
                case "Раз в квартал":
                    if (spinPeriod.getSelectedItem().toString().equals("год/года/лет")) {
                        period = period * 12;
                    }
                    sum = deposit * Math.pow((1 + (percent / 4)), Math.floor(period / 3));
                    break;
                case "Раз в полгода":
                    if (spinPeriod.getSelectedItem().toString().equals("год/года/лет")) {
                        period = period * 12;
                    }
                    sum = deposit * Math.pow((1 + percent / 2), Math.floor(period / 6));
                    break;
                case "Раз в год":
                    if ((spinPeriod.getSelectedItem().toString().equals("месяц(а)(ев)"))) {
                        period = period / 12;
                    }
                    sum = deposit * Math.pow((1 + percent), period);
                    break;
                default:
                    break;
            }
        } else {
            if (spinPeriod.getSelectedItem().toString().equals("месяц(а)(ев)")) {
                percent = percent / 12;
            }
            sum = deposit * (1 + percent * period);
        }
        income = sum - deposit;
        setClosingDate();
        lblBottomSheet.setText(String.format(Locale.getDefault(), "%.3f₽", sum));
        pbBottomSheet.setProgress((int) (deposit / sum * 100));
        lblSum.setText(String.format(Locale.getDefault(), "%.3f₽ / %.3f$", sum, sum / usdToRub));
        lblIncome.setText(String.format(Locale.getDefault(), "%.3f₽ / %.3f$", income, income / usdToRub));
    }

    private void setupListeners() {
        spinFrequency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                calculate();
            }
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        spinPeriod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                calculate();
            }
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        spinCurrency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                convertCurrency();
                currency = spinCurrency.getSelectedItem().toString();
                calculate();
            }
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        cbCapitalization.setOnCheckedChangeListener((buttonView, isChecked) -> calculate());

        tbDeposit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                calculate();
            }
        });

        tbPeriod.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                calculate();
            }
        });

        tbPercent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                calculate();
            }
        });
    }

    private void convertCurrency() {
        if (!Objects.equals(currency, spinCurrency.getSelectedItem().toString())
                && !tbDeposit.getText().toString().equals("")) {
            switch (currency) {
                case "₽": {
                    tbDeposit.setText(String.format(
                            Locale.getDefault(),
                            "%.3f",
                            Double.parseDouble(tbDeposit.getText().toString()) / usdToRub).replace(',', '.')
                    );
                    break;
                }
                case "$": {
                    tbDeposit.setText(
                            String.format(Locale.getDefault(),
                                    "%.3f",
                                    Double.parseDouble(tbDeposit.getText().toString()) * usdToRub).replace(',', '.')
                    );
                    break;
                }
            }
        }
    }



    private int calculateWeekCount() {
        Calendar closingDate = (Calendar) openingDate.clone();
        switch (spinPeriod.getSelectedItem().toString()) {
            case "год/года/лет":
                closingDate.add(Calendar.YEAR, Integer.parseInt(tbPeriod.getText().toString()));
                break;
            case "месяц(а)(ев)":
                closingDate.add(Calendar.MONTH, Integer.parseInt(tbPeriod.getText().toString()));
                break;
        }
        long diffInMillis = Math.abs(openingDate.getTimeInMillis() - closingDate.getTimeInMillis());
        return (int) Math.floor((double) TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS) / 7);
    }

    class AsyncRequest {
        private final Executor executor = Executors.newSingleThreadExecutor();
        Runnable parseDollarToRub = () -> {
            try {
                JSONObject json = new JSONObject(
                        IOUtils.toString(new URL(
                                "https://free.currconv.com/api/v7/convert?apiKey=efdeb145deda59536479&q=USD_RUB&compact=ultra"
                        ), StandardCharsets.UTF_8)
                );
                usdToRub = json.getDouble("USD_RUB");
                Log.i("USD_RUB", "Current exchange rate: " + usdToRub);
            } catch (JSONException | IOException exception) {
                exception.printStackTrace();
            }
        };
        public void execute() {
            executor.execute(parseDollarToRub);
        }
    }
}