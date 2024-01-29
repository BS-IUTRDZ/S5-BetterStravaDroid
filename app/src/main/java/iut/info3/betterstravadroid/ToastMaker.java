package iut.info3.betterstravadroid;

import android.content.Context;
import android.widget.Toast;

public class ToastMaker {


    public Toast makeText(Context context, CharSequence text, int duration) {
        return Toast.makeText(context, text , duration);
    }
}
