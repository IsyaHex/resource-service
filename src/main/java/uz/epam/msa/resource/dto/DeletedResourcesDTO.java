package uz.epam.msa.resource.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class DeletedResourcesDTO {
    @JsonProperty(value = "ids")
    private List<Integer> ids;
}
