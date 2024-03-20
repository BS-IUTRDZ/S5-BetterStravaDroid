package iut.info3.betterstravadroid.tools;

import android.app.DatePickerDialog;
import android.content.Context;
import android.widget.Button;
import android.widget.DatePicker;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DatePickerButton {
    
    private Button linkedButton;
    
    private DatePickerDialog datePickerDialog;

    private DateChangeListener dateChangeListener;

    private String date;
    
    public DatePickerButton(Context context, Button linkedButton) {
        if (linkedButton == null) {
            throw new IllegalArgumentException();
        }
        this.linkedButton = linkedButton;
        datePickerDialog = new DatePickerDialog(context);
        this.linkedButton.setOnClickListener(v -> datePickerDialog.show());
    }
    


    public void setDateChangeListener(DateChangeListener dateChangeListener) {
        this.dateChangeListener = dateChangeListener;
        datePickerDialog.setOnDateSetListener((view, year, month, dayOfMonth) -> {
            String date = LocalDate.of(year,month + 1,dayOfMonth).
                    format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            updateDate(date);
        });
    }

    public interface DateChangeListener {

        void onDateChange(String date);
    }

    public void setDate(String date) {
        updateDate(date);
    }

    private void updateDate(String date) {
        this.date = date;
        linkedButton.setText(date);
        if (dateChangeListener != null) {
            dateChangeListener.onDateChange(date);
        }

    }
    
    
    
}
