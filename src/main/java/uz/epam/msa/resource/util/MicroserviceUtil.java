package uz.epam.msa.resource.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import uz.epam.msa.resource.constant.Constants;
import uz.epam.msa.resource.dto.GetStorageDTO;
import uz.epam.msa.resource.exception.InternalServerErrorException;
import uz.epam.msa.resource.exception.ResourceNotFoundException;

import java.util.List;

@Component
@Slf4j
public class MicroserviceUtil {

    @Value("${api.get.storages}")
    private String API_GET_STORAGES;

    public GetStorageDTO getStagingStorage() {
        return getStorages().stream().filter(
                storage -> storage.getStorageType().equals(Constants.STAGING_STORAGE_TYPE))
                .findAny()
                .orElseThrow(ResourceNotFoundException::new);
    }

    public GetStorageDTO getPermanentStorage() {
        return getStorages().stream().filter(
                storage -> storage.getStorageType().equals(Constants.PERMANENT_STORAGE_TYPE))
                .findAny()
                .orElseThrow(ResourceNotFoundException::new);
    }


    public GetStorageDTO getStagingStorageFallBack() {
        GetStorageDTO stagingStorage = new GetStorageDTO();
        stagingStorage.setId(-1);
        stagingStorage.setStorageType(Constants.STAGING_STORAGE_TYPE);
        stagingStorage.setBucket(Constants.STAGING_BUCKET);

        return stagingStorage;
    }

    public GetStorageDTO getPermanentStorageFallBack() {
        GetStorageDTO permanentStorage = new GetStorageDTO();
        permanentStorage.setId(-2);
        permanentStorage.setStorageType(Constants.PERMANENT_STORAGE_TYPE);
        permanentStorage.setBucket(Constants.PERMANENT_BUCKET);

        return permanentStorage;
    }

    private List<GetStorageDTO> getStorages() {
        RestTemplate restTemplate = new RestTemplate();
        List<GetStorageDTO> list;
        try {
            list = restTemplate.exchange(API_GET_STORAGES, HttpMethod.GET, null,
                    new ParameterizedTypeReference<List<GetStorageDTO>>(){}).getBody();
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
        return list;
    }

}
