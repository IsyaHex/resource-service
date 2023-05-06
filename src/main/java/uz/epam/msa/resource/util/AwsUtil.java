package uz.epam.msa.resource.util;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.util.IOUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uz.epam.msa.resource.constant.Constants;
import uz.epam.msa.resource.dto.GetStorageDTO;
import uz.epam.msa.resource.exception.ResourceNotFoundException;

import java.io.IOException;

@Slf4j
@Component
public class AwsUtil {

    private final AmazonS3 s3Client;
    private final StorageManager storageManager;
    private final ResourceUtil resourceUtil;

    public AwsUtil(AmazonS3 s3Client, StorageManager storageManager, ResourceUtil resourceUtil) {
        this.s3Client = s3Client;
        this.storageManager = storageManager;
        this.resourceUtil = resourceUtil;
    }

    public byte[] downloadFile(String fileName, String bucketName) {
        GetStorageDTO stagingStorage = resourceUtil.getCircuitBreakerObject(
                storageManager::getStagingStorage, storageManager.getStagingStorageFallBack());
        log.info(String.format("Staging storage id -> %s", stagingStorage.getId()));
        try {
            return getObjectFromBucket(bucketName, fileName);
        } catch (Exception e) {
            log.info(e.getMessage());
            try {
                return getObjectFromBucket(stagingStorage.getBucket(), fileName);
            } catch (Exception ex) {
                log.error(ex.getMessage());
                throw new ResourceNotFoundException(Constants.RESOURCE_NOT_FOUND_EXCEPTION);
            }
        }
    }

    private byte[] getObjectFromBucket(String bucketName, String fileName) throws IOException {
        S3Object s3Object = s3Client.getObject(bucketName, fileName);
        try(S3ObjectInputStream inputStream = s3Object.getObjectContent()) {
            log.info(fileName);
            return IOUtils.toByteArray(inputStream);
        }
    }


    public void deleteFile(String fileName, String bucketName) {
        log.info(fileName);
        s3Client.deleteObject(bucketName, fileName);
    }
}