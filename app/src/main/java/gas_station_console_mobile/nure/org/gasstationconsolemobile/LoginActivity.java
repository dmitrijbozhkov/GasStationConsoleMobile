package gas_station_console_mobile.nure.org.gasstationconsolemobile;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

import gas_station_console_mobile.nure.org.gasstationconsolemobile.auth.AuthenticationManager;
import gas_station_console_mobile.nure.org.gasstationconsolemobile.auth.AuthorizationResponse;
import gas_station_console_mobile.nure.org.gasstationconsolemobile.common.Notifications;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.observables.ConnectableObservable;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    // UI references.
    private AutoCompleteTextView mUsernameView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private Button loginButton;
    private AuthenticationManager authenticationManager;
    private Notifications notifications;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authenticationManager = ((GasStationApplication) getApplicationContext()).getAuthenticationManager();
        notifications = ((GasStationApplication) getApplicationContext()).getNotifications();
        setContentView(R.layout.activity_login);
        mUsernameView = (AutoCompleteTextView) findViewById(R.id.username);
        mPasswordView = (EditText) findViewById(R.id.password);
        loginButton = (Button) findViewById(R.id.sign_in_button);
        ObservableOnSubscribe<View> loginAction = new ObservableOnSubscribe<View>() {
            @Override
            public void subscribe(final ObservableEmitter<View> emitter) throws Exception {
                loginButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        emitter.onNext(view);
                    }
                });
            }
        };
        Observable
                .create(loginAction)
                .subscribe(new LoginClickObserver());
        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    @Override
    protected void onResume() {
        super.onResume();
        notifications.showSnackbar(mLoginFormView, "Please login into your admin account", (ContextCompat.getColor(getApplicationContext(), R.color.colorInfo)));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        loginClickSubscription.dispose();
    }



    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin(View view) {
        // Reset errors.
        mUsernameView.setError(null);
        mPasswordView.setError(null);
        // Store values at the time of the login attempt.
        String username = mUsernameView.getText().toString();
        String password = mPasswordView.getText().toString();
        // Check for a valid password, if the user entered one.
        boolean isPasswordEmpty = TextUtils.isEmpty(password);
        boolean isUsernameEmpty = TextUtils.isEmpty(username);
        if (isUsernameEmpty) {
            mUsernameView.setError(getString(R.string.error_empty_username));
            mUsernameView.requestFocus();
        }
        if (isPasswordEmpty) {
            mPasswordView.setError(getString(R.string.error_empty_password));
            mPasswordView.requestFocus();
        }
        if (isUsernameEmpty || isPasswordEmpty) {
            return;
        }
        showProgress(true);
        ConnectableObservable<AuthorizationResponse> auth = authenticationManager.authorize(username, password);
        auth.subscribe(new LoginObserver());
        auth.connect();
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    // Subscriptions
    private Disposable loginSubscription;
    private Disposable loginClickSubscription;

    private class LoginClickObserver implements Observer<View> {

        @Override
        public void onSubscribe(Disposable d) {
            loginClickSubscription = d;
        }

        @Override
        public void onNext(View view) {
            attemptLogin(view);
        }

        @Override
        public void onError(Throwable e) { }

        @Override
        public void onComplete() { }
    }

    private class LoginObserver implements Observer<AuthorizationResponse> {

        @Override
        public void onSubscribe(Disposable d) {
            loginSubscription = d;
        }

        @Override
        public void onNext(AuthorizationResponse response) {
            showProgress(false);
            notifications.showSnackbar(mLoginFormView, response.getMessage(), (ContextCompat.getColor(getApplicationContext(), R.color.colorSuccess)));
            startActivity(new Intent(getBaseContext(), MainMenu.class));
        }

        @Override
        public void onError(Throwable e) {
            showProgress(false);
            notifications.showSnackbar(mLoginFormView, e.getMessage(), (ContextCompat.getColor(getApplicationContext(), R.color.colorDanger)));
        }

        @Override
        public void onComplete() {
            loginSubscription.dispose();
        }
    }
}

