package gas_station_console_mobile.nure.org.gasstationconsolemobile;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

import gas_station_console_mobile.nure.org.gasstationconsolemobile.auth.AuthenticationManager;
import gas_station_console_mobile.nure.org.gasstationconsolemobile.models.FuelDetails;
import gas_station_console_mobile.nure.org.gasstationconsolemobile.models.ListDTO;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ViewFuels extends AppCompatActivity {

    // Views
    private ImageButton mRefreshFuelsButton;
    private LinearLayout mFuelListItems;
    private List<View> fuelItems = new ArrayList<>();
    private LayoutInflater inflater;
    // Http
    private final ObjectMapper map = new ObjectMapper();
    private final OkHttpClient client = new OkHttpClient();

    private AuthenticationManager authenticationManager;

    private Disposable refreshClicksSubscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_fuels);
        mRefreshFuelsButton = (ImageButton) findViewById(R.id.refresh_fuels_button);
        mFuelListItems = (LinearLayout) findViewById(R.id.fuel_list_items);
        Observable<View> refreshClicks = Observable.create((emitter) -> mRefreshFuelsButton.setOnClickListener(emitter::onNext));
        refreshClicksSubscription = refreshClicks.subscribe(this::onRefreshClicksClicked);
        inflater = getLayoutInflater();
        authenticationManager = ((GasStationApplication) getApplicationContext()).getAuthenticationManager();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        refreshClicksSubscription.dispose();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!authenticationManager.isAuthorized()) {
            startActivity(new Intent(getBaseContext(), LoginActivity.class));
        }
        refreshFuelList();
    }

    private void refreshFuelList() {
        for (View v : fuelItems) {
            mFuelListItems.removeView(v);
        }
        fuelItems.clear();
        Observable<Response> response = Observable.defer(() -> {
            Request request = new Request.Builder()
                    .url(getString(R.string.api_domain) + "/fuel/get-all")
                    .get()
                    .build();
            return Observable.just(client.newCall(request).execute());
        });
        Disposable refreshSubscription = response
                .flatMap(this::parseResponse)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::refreshViews);
    }

    private ObservableSource<FuelDetails> parseResponse(Response response) throws Exception {
        ListDTO<FuelDetails> fuelDetails = map.readValue(response.body().string(), new TypeReference<ListDTO<FuelDetails>>() {});
        return Observable.fromIterable(fuelDetails.getContent());
    }

    private void refreshViews(FuelDetails fuelDetails) {
        View currView = inflater.inflate(R.layout.layout_info_list, mFuelListItems, false);
        TextView fuelNameView = currView.findViewById(R.id.name_view);
        TextView fuelLeftView = currView.findViewById(R.id.amount_view);
        fuelNameView.setText("Fuel name: " + fuelDetails.getFuelName());
        fuelLeftView.setText(String.format("Fuel left: %s L", Float.toString(fuelDetails.getStorage().getFuelAmount())));
        mFuelListItems.addView(currView);
        fuelItems.add(currView);
    }

    private void onRefreshClicksClicked(View view) {
        refreshFuelList();
    }
}
