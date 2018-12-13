package gas_station_console_mobile.nure.org.gasstationconsolemobile.common;

import android.support.design.widget.Snackbar;
import android.view.View;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class Notifications {

    public void showSnackbar(View view, String text, int color) {
        Snackbar snackbar = Snackbar.make(view, text, Snackbar.LENGTH_LONG);
        View snackBarView = snackbar.getView();
        snackBarView.setBackgroundColor(color);
        snackbar.show();
    }
}
