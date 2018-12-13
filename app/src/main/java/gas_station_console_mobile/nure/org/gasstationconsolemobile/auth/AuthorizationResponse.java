package gas_station_console_mobile.nure.org.gasstationconsolemobile.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthorizationResponse {
    private boolean isAuthorized;
    private String token;
    private String message;
}
