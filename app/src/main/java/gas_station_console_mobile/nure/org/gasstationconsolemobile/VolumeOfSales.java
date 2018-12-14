package gas_station_console_mobile.nure.org.gasstationconsolemobile;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import gas_station_console_mobile.nure.org.gasstationconsolemobile.auth.AuthenticationManager;
import gas_station_console_mobile.nure.org.gasstationconsolemobile.models.FuelVolumeOfSales;
import gas_station_console_mobile.nure.org.gasstationconsolemobile.models.FuelsVolumeOfSales;
import gas_station_console_mobile.nure.org.gasstationconsolemobile.models.VolumeOfSalesRequest;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.observables.ConnectableObservable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

// /volume-of-sales
// default.data.generate
public class VolumeOfSales extends AppCompatActivity {

    // Views
    private Button mDisplayVolumeOfSalesButton;
    private Button mPickBeforeButton;
    private Button mPickAfterButton;
    private LinearLayout mVolumeListItems;
    private TextView mOverallVolumeView;
    private List<View> volumeItems = new ArrayList<>();
    private LayoutInflater inflater;
    private DatePickerDialog beforePickerDialog;
    private DatePickerDialog afterPickerDialog;
    private EditText mBeforeDateElement;
    private EditText mAfterDateElement;
    // Http
    private final ObjectMapper map = new ObjectMapper();
    private final OkHttpClient client = new OkHttpClient();

    private AuthenticationManager authenticationManager;

    private Calendar beforeDate = Calendar.getInstance();
    private Calendar afterDate = Calendar.getInstance();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

    private Disposable displayClicksSubscription;
    private Disposable pickBeforeSubscription;
    private Disposable pickAfterSubscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volume_of_sales);
        mDisplayVolumeOfSalesButton = (Button) findViewById(R.id.display_volume_of_sales);
        mVolumeListItems = (LinearLayout) findViewById(R.id.volume_list_items);
        mOverallVolumeView = (TextView) findViewById(R.id.overall_volume_of_sales);
        mBeforeDateElement = findViewById(R.id.volume_date_before);
        mAfterDateElement = findViewById(R.id.volume_date_after);
        mPickBeforeButton = findViewById(R.id.set_before_button);
        mPickAfterButton = findViewById(R.id.set_after_button);
        Observable<View> displayClicks = Observable.create((emitter) -> mDisplayVolumeOfSalesButton.setOnClickListener(emitter::onNext));
        Observable<View> pickBefore = Observable.create((emitter) -> mPickBeforeButton.setOnClickListener(emitter::onNext));
        Observable<View> pickAfter = Observable.create((emitter -> mPickAfterButton.setOnClickListener(emitter::onNext)));
        pickBeforeSubscription = pickBefore.subscribe(this::onPickBeforeClicked);
        pickAfterSubscription = pickAfter.subscribe(this::onPickAfterClicked);
        displayClicksSubscription = displayClicks.subscribe(this::onDisplayClicked);
        inflater = getLayoutInflater();
        authenticationManager = ((GasStationApplication) getApplicationContext()).getAuthenticationManager();
        Observable<Calendar> beforePicked = Observable.create(emitter -> {
            beforePickerDialog = new DatePickerDialog(
                    VolumeOfSales.this,
                    new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                            emitter.onNext(buildCalendar(year, month, dayOfMonth));
                        }
                    },
                    beforeDate.get(Calendar.YEAR),
                    beforeDate.get(Calendar.MONTH),
                    beforeDate.get(Calendar.DAY_OF_MONTH));
        });
        Observable<Calendar> afterPicked = Observable.create(emitter -> {
            afterPickerDialog = new DatePickerDialog(
                    VolumeOfSales.this,
                    new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                            emitter.onNext(buildCalendar(year, month, dayOfMonth));
                        }
                    },
                    afterDate.get(Calendar.YEAR),
                    afterDate.get(Calendar.MONTH),
                    afterDate.get(Calendar.DAY_OF_MONTH));
        });
        pickBeforeSubscription = beforePicked.subscribe(this::onBeforePicked);
        pickAfterSubscription = afterPicked.subscribe(this::onAfterPicked);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        displayClicksSubscription.dispose();
        pickAfterSubscription.dispose();
        pickBeforeSubscription.dispose();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!authenticationManager.isAuthorized()) {
            startActivity(new Intent(getBaseContext(), LoginActivity.class));
        }
    }

    private Calendar buildCalendar(int year, int month, int dayOfMonth) {
        Calendar nextCalendar = Calendar.getInstance();
        nextCalendar.set(Calendar.YEAR, year);
        nextCalendar.set(Calendar.MONTH, month);
        nextCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        return nextCalendar;
    }

    private void refreshFuelList() {
        Date before;
        Date after;
        try {
             before = dateFormat.parse(mBeforeDateElement.getText().toString());
        } catch (ParseException ex) {
            mBeforeDateElement.setError(getString(R.string.wrong_date_format_error));
            return;
        }
        try {
            after = dateFormat.parse(mAfterDateElement.getText().toString());
        } catch (ParseException ex) {
            mAfterDateElement.setError(getString(R.string.wrong_date_format_error));
            return;
        }
        for (View v : volumeItems) {
            mVolumeListItems.removeView(v);
        }
        volumeItems.clear();
        Observable<Response> response = Observable.defer(() -> {
            RequestBody requestBody = RequestBody.create(
                    MediaType.parse(getString(R.string.requests_type)),
                    map.writeValueAsString(new VolumeOfSalesRequest(before, after)));
            Request request = new Request.Builder()
                    .url(getString(R.string.api_domain) + "/order/volume-of-sales")
                    .header(getString(R.string.token_header), authenticationManager.getPrefixedToken())
                    .post(requestBody)
                    .build();
            return Observable.just(client.newCall(request).execute());
        });
        ConnectableObservable<FuelsVolumeOfSales> parsedVolumes = response
                .map((r) -> {
                    return map.readValue(r.body().string(), FuelsVolumeOfSales.class);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .publish();
        Disposable overallSubscription = parsedVolumes
                .subscribe(this::displayOverallVolumeOfSales);
        Disposable volumeSubscription = parsedVolumes
                .flatMap((f) -> Observable.fromIterable(f.getFuelVolumeOfSales()))
                .subscribe(this::displayFuelVolumeOfSales);
        parsedVolumes.connect();
    }

    private void displayOverallVolumeOfSales(FuelsVolumeOfSales fuelsVolumeOfSales) {
        mOverallVolumeView.setText("$" + Float.toString(fuelsVolumeOfSales.getOverallVolumeOfSales()));
    }

    private void displayFuelVolumeOfSales(FuelVolumeOfSales fuelVolumeOfSales) {
        View currView = inflater.inflate(R.layout.layout_info_list, mVolumeListItems, false);
        TextView fuelNameView = currView.findViewById(R.id.name_view);
        TextView fuelLeftView = currView.findViewById(R.id.amount_view);
        fuelNameView.setText("Fuel name: " + fuelVolumeOfSales.getFuelName());
        fuelLeftView.setText(String.format("Fuel bought for $%s ", Float.toString(fuelVolumeOfSales.getVolumeOfSales())));
        mVolumeListItems.addView(currView);
        volumeItems.add(currView);
    }

    private void onDisplayClicked(View view) {
        refreshFuelList();
    }

    private void onPickBeforeClicked(View view) {
        beforePickerDialog.show();
    }

    private void onPickAfterClicked(View view) {
        afterPickerDialog.show();
    }

    private void onBeforePicked(Calendar calendar) {
        beforeDate = calendar;
        Date date = calendar.getTime();
        mBeforeDateElement.setText(dateFormat.format(date));
    }

    private void onAfterPicked(Calendar calendar) {
        afterDate = calendar;
        Date date = calendar.getTime();
        mAfterDateElement.setText(dateFormat.format(date));
    }
}
