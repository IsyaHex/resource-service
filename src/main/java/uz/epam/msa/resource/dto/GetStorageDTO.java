package uz.epam.msa.resource.dto;

import lombok.Data;

@Data
public class GetStorageDTO {
    private int id;
    private String storageType;
    private String bucket;
    private String path;
}
