package iut.info3.betterstravadroid.parcours;

import android.app.DatePickerDialog;
import android.content.Context;
import android.widget.Button;
import android.widget.DatePicker;

import androidx.annotation.NonNull;

import iut.info3.betterstravadroid.PageListeParcours;


public class DatePickerFilter extends DatePickerDialog {
    public DatePickerFilter(@NonNull Context context, PageListeParcours view, Button linkedButton) {
        super(context);
        setOnDateSetListener(new DatePickerListener(view, linkedButton));
    }


    public static class DatePickerListener implements DatePickerDialog.OnDateSetListener {

        private PageListeParcours view;
        private Button linkedButton;
        public DatePickerListener(PageListeParcours view, Button linkedButton) {
            this.linkedButton = linkedButton;
            this.view = view;
        }
        @Override
        public void onDateSet(DatePicker picker, int year, int month, int dayOfMonth) {

            linkedButton.setText("" + year + " " + month + " " + dayOfMonth);
        }
    }
}

