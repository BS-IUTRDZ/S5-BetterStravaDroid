package iut.info3.betterstravadroid.tools;

import android.app.DatePickerDialog;
import android.content.Context;
import android.widget.Button;
import android.widget.DatePicker;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Link a button to a DatePickerDialog.
 */
public class DatePickerButton {
    /**
     * Button that show a datePickerDialog when clicked.
     */
    private Button linkedButton;
    /**
     * DatePickerDialog allowing user to select a date
     */
    private DatePickerDialog datePickerDialog;
    /**
     * Customs Operation to perform when the date of this class change.
     */
    private DateChangeListener dateChangeListener;
    /**
     * Last date selected or settled
     */
    private String date;

    /**
     * Create a new DatePickerDialog from the given context.
     * And link it to the given button.
     * When the button is clicked, the dialog open.
     * @param context context wich serve to create the DatePickerDialog
     * @param linkedButton the button to link to the dialog
     */
    public DatePickerButton(Context context, Button linkedButton) {
        if (linkedButton == null) {
            throw new IllegalArgumentException();
        }
        this.linkedButton = linkedButton;
        datePickerDialog = new DatePickerDialog(context);
        this.linkedButton.setOnClickListener(v -> datePickerDialog.show());
    }


    /**
     * Redirect the OnDateSet event of the date picker to our custom
     * Date Change Listener with the date at format dd/MM/yyyy.
     * change the date of this class and set the text of the button
     * to the selected date.
     * @param dateChangeListener operations to perform when the date change
     */
    public void setDateChangeListener(DateChangeListener dateChangeListener) {
        this.dateChangeListener = dateChangeListener;
        datePickerDialog.setOnDateSetListener((view, year, month, dayOfMonth) -> {
            String date = LocalDate.of(year,month + 1,dayOfMonth).
                    format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            updateDate(date);
        });
    }


    /**
     * Set manually the date of this class, trigger the listener
     * and set the text of the button to the date.
     * @param date given date
     */
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

    public interface DateChangeListener {

        void onDateChange(String date);
    }
    
    
    
}
