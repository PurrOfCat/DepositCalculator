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
import com.google.android.material.bottomsheet.BottomSheetDialog;

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

    private EditText tbDeposit;
    private EditText tbPeriod;
    private Spinner spinPeriod;
    private Spinner spinCurrency;
    private EditText tbPercent;
    private Spinner spinFrequency;
    private Button btnDate;
    private CheckBox cbCapitalization;
    private TextView lblSum;
    private TextView lblIncome;
    private DatePickerDialog datePickerDialog;

    private BottomSheetBehavior sheetBehavior;
    private ConstraintLayout bottomSheet;

    TextView lblBottomSheet;
    ProgressBar pbBottomSheet;

    private String currency = "₽";
    private Calendar openDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new AsyncRequest().execute();
        setContentView(R.layout.activity_main);

        tbDeposit = findViewById(R.id.tbDeposit);
        tbPeriod = findViewById(R.id.tbPeriod);
        spinPeriod = findViewById(R.id.spinPeriod);
        spinCurrency = findViewById(R.id.spinCurrency);
        tbPercent = findViewById(R.id.tbPercent);
        spinFrequency = findViewById(R.id.spinFrequency);
        btnDate = findViewById(R.id.btnDate);
        cbCapitalization = findViewById(R.id.cbCapitalization);
        lblSum = findViewById(R.id.lblSum);
        lblIncome = findViewById(R.id.lblIncome);
        bottomSheet = findViewById(R.id.bottom_sheet);
        sheetBehavior = BottomSheetBehavior.from(bottomSheet);

        lblBottomSheet = findViewById(R.id.text_view_sum);
        pbBottomSheet = findViewById(R.id.progress_bar);

        btnDate.setText(getTodaysDate());
        initDatePicker();
        setupListeners();
    }

    private String getTodaysDate() {
        openDate = Calendar.getInstance();
        int year = openDate.get(Calendar.YEAR);
        int month = openDate.get(Calendar.MONTH);
        month = month + 1;
        int day = openDate.get(Calendar.DAY_OF_MONTH);
        return makeDateString(day, month, year);
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
            String date = makeDateString(day, month, year);
            btnDate.setText(date);
        };

        openDate = Calendar.getInstance();
        int year = openDate.get(Calendar.YEAR);
        int month = openDate.get(Calendar.MONTH);
        int day = openDate.get(Calendar.DAY_OF_MONTH);

        int style = AlertDialog.THEME_HOLO_LIGHT;

        datePickerDialog = new DatePickerDialog(this, style, dateSetListener, year, month, day);
    }

    private void calculate() {
        if (tbDeposit.getText().toString().equals("") ||
                tbPercent.getText().toString().equals("") ||
                tbPeriod.getText().toString().equals("")) {
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

    private int calculateWeekCount() {
        Calendar endDate = (Calendar) openDate.clone();
        switch (spinPeriod.getSelectedItem().toString()) {
            case "год/года/лет":
                endDate.add(Calendar.YEAR, Integer.parseInt(tbPeriod.getText().toString()));
                break;
            case "месяц(а)(ев)":
                endDate.add(Calendar.MONTH, Integer.parseInt(tbPeriod.getText().toString()));
                break;
        }

        long diffInMillis = Math.abs(openDate.getTimeInMillis() - endDate.getTimeInMillis());
        return (int) Math.floor((double) TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS) / 7);
    }

    private void convertCurrency() {
        if (!Objects.equals(currency, spinCurrency.getSelectedItem().toString())
                && !tbDeposit.getText().toString().equals("")) {
            switch (currency) {
                case "₽": {
                    tbDeposit.setText(String.format(
                            Locale.getDefault(),
                            "%.3f",
                            Double.parseDouble(tbDeposit.getText().toString()) / usdToRub)
                    );
                    break;
                }
                case "$": {
                    tbDeposit.setText(
                            String.format(Locale.getDefault(),
                                    "%.3f",
                                    Double.parseDouble(tbDeposit.getText().toString()) * usdToRub)
                    );
                    break;
                }
            }
        }
    }

    public void openDatePicker(View view) {
        datePickerDialog.show();
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