package gas_station_console_mobile.nure.org.gasstationconsolemobile.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FuelDetails {
    @JsonProperty("fuelName")
    private String fuelName;
    @JsonProperty("tariff")
    private TariffDetails tariff;
    @JsonProperty("storage")
    private FuelStorageDetails storage;
}
