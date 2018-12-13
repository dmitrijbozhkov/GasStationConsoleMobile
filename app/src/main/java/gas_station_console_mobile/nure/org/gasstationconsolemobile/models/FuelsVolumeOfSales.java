package gas_station_console_mobile.nure.org.gasstationconsolemobile.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FuelsVolumeOfSales {
    @JsonProperty("fuelVolumeOfSales")
    private List<FuelVolumeOfSales> fuelVolumeOfSales;
    @JsonProperty("overallVolumeOfSales")
    private float overallVolumeOfSales;
}
