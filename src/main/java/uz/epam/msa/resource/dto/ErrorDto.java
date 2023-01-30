package uz.epam.msa.resource.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ErrorDto {
    @JsonProperty("error message")
    private String errorMessage;
    @JsonProperty("error code")
    private int errorCode;
}
