package uz.epam.msa.resource.dto;

import lombok.Data;

@Data
public class AudioDataBinaryDTO {
    private Long id;
    private Long size;
    private String name;
    private String contentType;
}
