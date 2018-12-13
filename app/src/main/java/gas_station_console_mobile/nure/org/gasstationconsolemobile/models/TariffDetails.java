package gas_station_console_mobile.nure.org.gasstationconsolemobile.models;

import com.fasterxml.jackson.annotation.JsonProperty;

class TariffDetails {
    @JsonProperty("id")
    private long id;
    @JsonProperty("exchangeRate")
    private float exchangeRate;
}
