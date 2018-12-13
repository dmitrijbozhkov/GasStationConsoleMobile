package gas_station_console_mobile.nure.org.gasstationconsolemobile;

import android.app.Application;
import android.app.Notification;

import gas_station_console_mobile.nure.org.gasstationconsolemobile.auth.AuthenticationManager;
import gas_station_console_mobile.nure.org.gasstationconsolemobile.common.Notifications;

public class GasStationApplication extends Application {

    private AuthenticationManager authenticationManager;
    private Notifications notifications;

    public AuthenticationManager getAuthenticationManager() {
        return authenticationManager;
    }

    public Notifications getNotifications() {
        return notifications;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        authenticationManager = new AuthenticationManager(
                getString(R.string.sign_key),
                getString(R.string.authorities_key),
                getString(R.string.token_prefix),
                getString(R.string.api_domain),
                getString(R.string.requests_type),
                getString(R.string.admin_authority)
        );
        notifications = new Notifications();
    }
}
