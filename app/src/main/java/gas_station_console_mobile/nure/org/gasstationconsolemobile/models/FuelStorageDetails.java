package gas_station_console_mobile.nure.org.gasstationconsolemobile.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
class FuelStorageDetails {
    @JsonProperty("id")
    private long id;
    @JsonProperty("fuelAmount")
    private float fuelAmount;
}
