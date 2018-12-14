package gas_station_console_mobile.nure.org.gasstationconsolemobile.auth;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

import gas_station_console_mobile.nure.org.gasstationconsolemobile.models.ExceptionResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.DeserializationException;
import io.jsonwebtoken.io.Deserializer;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.observables.ConnectableObservable;
import io.reactivex.schedulers.Schedulers;
import lombok.Getter;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AuthenticationManager {

    public AuthenticationManager(String signKey, String authoritiesKey, String tokenPrefix, String apiDomain, String requestsType, String adminRole) {
        this.signKey = signKey;
        this.authoritiesKey = authoritiesKey;
        this.tokenPrefix = tokenPrefix;
        this.apiDomain = apiDomain;
        this.requestsType = requestsType;
        this.adminRole = adminRole;
    }

    // Settings
    private final String signKey;
    private final String authoritiesKey;
    private final String tokenPrefix;
    private final String apiDomain;
    private final String requestsType;
    private final String adminRole;
    // auth
    @Getter
    private String token;
    @Getter
    private boolean isAuthorized;
    // Http
    private final ObjectMapper map = new ObjectMapper();
    private final OkHttpClient client = new OkHttpClient();
    // Subscriptions
    private Disposable authRequestDisposable;

    private AuthorizationResponse parseToken(AuthToken authToken) {
        Claims claims = Jwts
                .parser()
                .setSigningKey(signKey.getBytes())
                .deserializeJsonWith((bytes) -> {
                    try {
                        return map.readValue(bytes, new TypeReference<Map<String, ?>>() {});
                    } catch (Exception ex) {
                        throw new DeserializationException(ex.getMessage());
                    }
                }).parseClaimsJws(authToken.getToken())
                .getBody();
        if (!claims.get(authoritiesKey).equals(adminRole)) {
            return new AuthorizationResponse(false, authToken.getToken(), "Please login into your admin account");
        }
        return new AuthorizationResponse(true, authToken.getToken(), "Authorization successful");
    }

    private AuthToken parseResponse(Response response) throws Exception {
        if (response.code() == 401) {
            throw new Exception("Wrong username or password");
        }
        if (response.code() >= 200 && response.code() < 300) {
            return map.readValue(response.body().string(), AuthToken.class);
        }
        throw new Exception(map.readValue(response.body().string(), ExceptionResponse.class).getExceptionMessage());
    }

    private void onAuthSuccess(AuthorizationResponse response) {
        if (response.isAuthorized()) {
            token = response.getToken();
            isAuthorized = response.isAuthorized();
        } else {
            token = null;
            isAuthorized = response.isAuthorized();
        }
    }

    public ConnectableObservable<AuthorizationResponse> authorize(String username, String password) {
        Observable<Response> response = Observable.defer(() -> {
            RequestBody requestBody = RequestBody.create(MediaType.parse(requestsType), map.writeValueAsString(new UserCredentials(username, password)));
            Request request = new Request.Builder()
                    .url(apiDomain + "/user/login")
                    .post(requestBody)
                    .build();
            return Observable.just(client.newCall(request).execute());
        });
        ConnectableObservable<AuthorizationResponse> authResponse = response
                .map(this::parseResponse)
                .map(this::parseToken)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .publish();
        Disposable subscribe = authResponse.subscribe(this::onAuthSuccess);
        return authResponse;
    }

    public void logOut() {
        token = null;
        isAuthorized = false;
    }

    public String getPrefixedToken() {
        return String.format("%s %s", tokenPrefix, token);
    }
}
