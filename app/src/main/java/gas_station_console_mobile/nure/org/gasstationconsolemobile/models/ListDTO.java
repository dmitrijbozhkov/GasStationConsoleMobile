package gas_station_console_mobile.nure.org.gasstationconsolemobile.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ListDTO<T> {

    public ListDTO(List<T> content) {
        this.content = content;
    }

    @JsonProperty("content")
    private List<T> content;
}
