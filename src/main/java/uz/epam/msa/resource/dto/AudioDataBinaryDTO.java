package uz.epam.msa.resource.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class AudioDataBinaryDTO implements Serializable {
    private Integer id;
    private Long size;
    private String name;
    private String contentType;
    private String path;
}
