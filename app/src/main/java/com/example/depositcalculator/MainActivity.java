package com.example.depositcalculator;
import androidx.appcompat.app.AppCompatActivity;

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
import android.widget.Spinner;
import android.widget.TextView;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private double usdToRub = 65;

    private EditText tbDeposit;
    private EditText tbPeriod;
    private Spinner spinPeriod;
    private EditText tbPercent;
    private Spinner spinFrequency;
    private Button btnDate;
    private CheckBox cbCapitalization;
    private TextView lblSum;
    private TextView lblIncome;
    private Button btnGraph;
    private DatePickerDialog datePickerDialog;

    public void calculate() {
        if (tbDeposit.getText().toString().equals("") ||
            tbPercent.getText().toString().equals("") ||
            tbPeriod.getText().toString().equals("")) {
        return;
        }

        double deposit = Integer.parseInt(tbDeposit.getText().toString());
        double period = Integer.parseInt(tbPeriod.getText().toString());
        double percent = Double.parseDouble(tbPercent.getText().toString()) / 100;
        double income;
        double sum = 0;

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
                    sum =  deposit * Math.pow((1 + percent / 2), Math.floor(period / 6));
                    break;
                case "Раз в год":
                    if ((spinPeriod.getSelectedItem().toString().equals("месяц(а)(ев)"))) {
                        period = period / 12;
                    }
                    sum =  deposit * Math.pow((1 + percent), period);
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
        lblSum.setText(Double.toString(Math.floor(sum)));
        lblIncome.setText(Double.toString(Math.floor(income)));
    }

    private String getTodaysDate() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        month = month + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        return makeDateString(day, month, year);
    }

    private void initDatePicker() {
        DatePickerDialog.OnDateSetListener dateSetListener = (datePicker, year, month, day) -> {
            month = month + 1;
            String date = makeDateString(day, month, year);
            btnDate.setText(date);
        };

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        int style = AlertDialog.THEME_HOLO_LIGHT;

        datePickerDialog = new DatePickerDialog(this, style, dateSetListener, year, month, day);
    }

    public void openDatePicker(View view) {
        datePickerDialog.show();
    }

    private String makeDateString(int day, int month, int year) {
        if (month < 10) {
            return day + "/0" + month + "/" + year;
        } else {
            return day + "/" + month + "/" + year;
        }
    }

    private String getMonthInString(int month) {
        switch (month) {
            case 1:
                return "январь";
            case 2:
                return "февраль";
            case 3:
                return "март";
            case 4:
                return "апрель";
            case 5:
                return "май";
            case 6:
                return "июнь";
            case 7:
                return "июль";
            case 8:
                return "август";
            case 9:
                return "сентябрь";
            case 10:
                return "октябрь";
            case 11:
                return "ноябрь";
            case 12:
                return "декабрь";
            default:
                return "никогда не произойдёт";
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new AsyncRequest().execute();
        setContentView(R.layout.activity_main);

        tbDeposit = findViewById(R.id.tbDeposit);
        tbPeriod = findViewById(R.id.tbPeriod);
        spinPeriod = findViewById(R.id.spinPeriod);
        tbPercent = findViewById(R.id.tbPercent);
        spinFrequency = findViewById(R.id.spinFrequency);
        btnDate = findViewById(R.id.btnDate);
        cbCapitalization = findViewById(R.id.cbCapitalization);
        lblSum = findViewById(R.id.lblSum);
        lblIncome = findViewById(R.id.lblIncome);
        btnGraph = findViewById(R.id.btnGraph);

        btnDate.setText(getTodaysDate());
        initDatePicker();

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