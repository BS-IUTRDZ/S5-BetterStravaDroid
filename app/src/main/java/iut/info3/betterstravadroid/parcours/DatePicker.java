package iut.info3.betterstravadroid.parcours;

import android.app.DatePickerDialog;
import android.content.Context;

import androidx.annotation.NonNull;


public class DatePicker extends DatePickerDialog {
    public DatePicker(@NonNull Context context, String name) {
        super(context);
        setOnDateSetListener(new DatePickerListener(name));
    }


    public static class DatePickerListener implements DatePickerDialog.OnDateSetListener {


        private String date;
        public DatePickerListener(String date) {
            this.date = date;
        }
        @Override
        public void onDateSet(android.widget.DatePicker view, int year, int month, int dayOfMonth) {
            date = "" + year + month + dayOfMonth;
            new RuntimeException(date).printStackTrace();
        }
    }
}

