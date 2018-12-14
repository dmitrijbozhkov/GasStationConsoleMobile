package gas_station_console_mobile.nure.org.gasstationconsolemobile;

import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import gas_station_console_mobile.nure.org.gasstationconsolemobile.auth.AuthenticationManager;
import gas_station_console_mobile.nure.org.gasstationconsolemobile.common.Notifications;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class MainMenu extends AppCompatActivity {

    private AuthenticationManager authenticationManager;
    private Notifications notifications;

    private Button mCheckFuelTanksButton;
    private Button mViewVolumeOfSalesButton;

    private Disposable checkFuelTanksClickSubscription;
    private Disposable viewVolumeOfSalesCliclSubscription;

    private boolean isFromLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        mCheckFuelTanksButton = (Button) findViewById(R.id.check_fuel_tanks_button);
        mViewVolumeOfSalesButton = (Button) findViewById(R.id.view_volume_of_sales_button);
        Observable<View> fuelAction = Observable.create((emitter) -> mCheckFuelTanksButton.setOnClickListener(emitter::onNext));
        Observable<View> volumeAction = Observable.create((emitter) -> mViewVolumeOfSalesButton.setOnClickListener(emitter::onNext));
        checkFuelTanksClickSubscription = fuelAction.subscribe(this::onCheckFuelTanksClick);
        viewVolumeOfSalesCliclSubscription = volumeAction.subscribe(this::onViewVolumeOfSalesClick);
        authenticationManager = ((GasStationApplication) getApplicationContext()).getAuthenticationManager();
        notifications = ((GasStationApplication) getApplicationContext()).getNotifications();
        isFromLogin = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!authenticationManager.isAuthorized()) {
            startActivity(new Intent(getBaseContext(), LoginActivity.class));
        }
        Intent intent = getIntent();
        if (isFromLogin) {
            notifications.showSnackbar(findViewById(R.id.activity_main_menu), intent.getStringExtra("responseMessage"), (ContextCompat.getColor(getApplicationContext(), R.color.colorSuccess)));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        checkFuelTanksClickSubscription.dispose();
        viewVolumeOfSalesCliclSubscription.dispose();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        isFromLogin = true;
        authenticationManager.logOut();
    }

    private void onCheckFuelTanksClick(View view) {
        isFromLogin = false;
        startActivity(new Intent(getBaseContext(), ViewFuels.class));
    }

    private void onViewVolumeOfSalesClick(View view) {
        isFromLogin = false;
        startActivity(new Intent(getBaseContext(), VolumeOfSales.class));
    }
}
