package uz.epam.msa.resource.util;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.util.IOUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uz.epam.msa.resource.constant.Constants;
import uz.epam.msa.resource.dto.GetStorageDTO;
import uz.epam.msa.resource.exception.InternalServerErrorException;
import uz.epam.msa.resource.exception.ResourceNotFoundException;

import java.io.IOException;

@Slf4j
@Component
public class AwsUtil {

    private final AmazonS3 s3Client;
    private final MicroserviceUtil microserviceUtil;

    private final ResourceUtil resourceUtil;

    public AwsUtil(AmazonS3 s3Client, MicroserviceUtil microserviceUtil, ResourceUtil resourceUtil) {
        this.s3Client = s3Client;
        this.microserviceUtil = microserviceUtil;
        this.resourceUtil = resourceUtil;
    }

    public byte[] downloadFile(String fileName, String bucketName) {
        GetStorageDTO stagingStorage = resourceUtil.getCircuitBreakerObject(
                microserviceUtil::getStagingStorage, microserviceUtil.getStagingStorageFallBack());
        log.info(String.format("Staging storage id -> %s", stagingStorage.getId()));
        try {
            return getObjectFromBucket(bucketName, fileName, stagingStorage);
        } catch (Exception e) {
            log.info(e.getMessage());
            try {
                return getObjectFromBucket(stagingStorage.getBucket(), fileName, stagingStorage);
            } catch (Exception ex) {
                log.error(ex.getMessage());
                throw new ResourceNotFoundException(Constants.RESOURCE_NOT_FOUND_EXCEPTION);
            }
        }
    }

    private byte[] getObjectFromBucket(String bucketName, String fileName, GetStorageDTO storageDTO) throws IOException {
        S3Object s3Object = s3Client.getObject(bucketName, fileName);
        try(S3ObjectInputStream inputStream = s3Object.getObjectContent()) {
            log.info(fileName);
            if (bucketName.equals(storageDTO.getBucket())) {
                moveFileToPermanentBucket(fileName);
            }
            return IOUtils.toByteArray(inputStream);
        }
    }

    private void moveFileToPermanentBucket(String fileName) {
        try {
            GetStorageDTO stagingStorage = resourceUtil.getCircuitBreakerObject(
                    microserviceUtil::getStagingStorage, microserviceUtil.getStagingStorageFallBack());
            GetStorageDTO permanentStorage = resourceUtil.getCircuitBreakerObject(
                    microserviceUtil::getPermanentStorage, microserviceUtil.getPermanentStorageFallBack());
            log.info(String.format("Staging storage id -> %s", stagingStorage.getId()));
            log.info(String.format("Permanent storage id -> %s", permanentStorage.getId()));

            s3Client.copyObject(stagingStorage.getBucket(), fileName, permanentStorage.getBucket(), fileName);
            log.info(String.format(
                    Constants.LOG_COPY_FILE_BETWEEN_BUCKETS,
                    fileName, stagingStorage.getBucket(), permanentStorage.getBucket()));
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new InternalServerErrorException();
        }
    }

    public void deleteFile(String fileName, String bucketName) {
        log.info(fileName);
        s3Client.deleteObject(bucketName, fileName);
    }

}
