package uz.epam.msa.resource.util;

import com.amazonaws.services.s3.AmazonS3;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import uz.epam.msa.resource.constant.Constants;
import uz.epam.msa.resource.dto.GetStorageDTO;
import uz.epam.msa.resource.exception.InternalServerErrorException;
import uz.epam.msa.resource.service.FileService;

import java.io.IOException;

@Component
@Slf4j
public class ResourceConsumer {

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private StorageManager storageManager;
    @Autowired
    private ResourceUtil resourceUtil;
    @Autowired
    private FileService fileService;
    @Autowired
    private AmazonS3 s3Client;


    @KafkaListener(topics = "resources-response-topic")
    public void consume(String message) {
        log.info(String.format(Constants.RECEIVED_RESOURCE_ID, message));
        try {
            int resourceId = objectMapper.readValue(message, int.class);
            moveFileToPermanentBucket(resourceId);
        } catch (IOException e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    private void moveFileToPermanentBucket(int resourceId) {
        try {
            String fileName = String.format(Constants.FILE_ID_PATTERN, resourceId, fileService.getFileName(resourceId));

            GetStorageDTO stagingStorage = resourceUtil.getCircuitBreakerObject(
                    storageManager::getStagingStorage, storageManager.getStagingStorageFallBack());
            GetStorageDTO permanentStorage = resourceUtil.getCircuitBreakerObject(
                    storageManager::getPermanentStorage, storageManager.getPermanentStorageFallBack());
            log.info(String.format("Staging storage id -> %s", stagingStorage.getId()));
            log.info(String.format("Permanent storage id -> %s", permanentStorage.getId()));

            s3Client.copyObject(stagingStorage.getBucket(), fileName, permanentStorage.getBucket(), fileName);
            log.info(String.format(
                    Constants.LOG_COPY_FILE_BETWEEN_BUCKETS,
                    fileName, stagingStorage.getBucket(), permanentStorage.getBucket()));
            fileService.updateStatus(resourceId);
            s3Client.deleteObject(stagingStorage.getBucket(), fileName);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new InternalServerErrorException();
        }
    }
}
