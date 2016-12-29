package com.magicmian.com.simple;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.magicmian.com.library.DatePickerController;
import com.magicmian.com.library.DayPickerView;

public class MainActivity extends AppCompatActivity implements DatePickerController {

    private DayPickerView dayPickerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dayPickerView = (DayPickerView) findViewById(R.id.pickerView);
        dayPickerView.setController(this);
    }

    @Override
    public int getMaxYear() {
        return 2017;
    }

    @Override
    public void onDayOfMonthSelected(int year, int month, int day) {

    }
}
