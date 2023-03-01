package uz.epam.msa.resource.util;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.util.IOUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uz.epam.msa.resource.constant.Constants;
import uz.epam.msa.resource.exception.InternalServerErrorException;

import java.io.IOException;

@Slf4j
@Component
public class AwsUtil {

    private final AmazonS3 s3Client;

    public AwsUtil(AmazonS3 s3Client) {
        this.s3Client = s3Client;
    }

    public byte[] downloadFile(String fileName) {
        S3Object s3Object = s3Client.getObject(Constants.BUCKET_NAME, fileName);
        try(S3ObjectInputStream inputStream = s3Object.getObjectContent()) {
            log.info(fileName);
            return IOUtils.toByteArray(inputStream);
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new InternalServerErrorException();
        }
    }

    public void deleteFile(String fileName) {
        log.info(fileName);
        s3Client.deleteObject(Constants.BUCKET_NAME, fileName);
    }
}
