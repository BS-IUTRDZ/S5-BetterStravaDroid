package iut.info3.betterstravadroid.parcours;

import android.app.DatePickerDialog;
import android.content.Context;
import android.widget.Button;
import android.widget.DatePicker;

import androidx.annotation.NonNull;

import iut.info3.betterstravadroid.PageListeParcours;


public class DatePickerFilter extends DatePickerDialog {
    public DatePickerFilter(@NonNull Context context, Button linkedButton) {
        super(context);
        setOnDateSetListener(new DatePickerListener(linkedButton));
    }


    public static class DatePickerListener implements DatePickerDialog.OnDateSetListener {

        private Button linkedButton;
        public DatePickerListener(Button linkedButton) {
            this.linkedButton = linkedButton;
        }
        @Override
        public void onDateSet(DatePicker picker, int year, int month, int dayOfMonth) {

            linkedButton.setText("" + year + " " + month + " " + dayOfMonth);
        }
    }
}

