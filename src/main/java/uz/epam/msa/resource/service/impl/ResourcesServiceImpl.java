package uz.epam.msa.resource.service.impl;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.util.IOUtils;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import uz.epam.msa.resource.constant.Constants;
import uz.epam.msa.resource.domain.Resource;
import uz.epam.msa.resource.dto.AudioDataBinaryDTO;
import uz.epam.msa.resource.dto.DeletedResourcesDTO;
import uz.epam.msa.resource.dto.ResourceDTO;
import uz.epam.msa.resource.exception.InternalServerErrorException;
import uz.epam.msa.resource.exception.ResourceNotFoundException;
import uz.epam.msa.resource.exception.ResourceValidationException;
import uz.epam.msa.resource.repository.ResourcesRepository;
import uz.epam.msa.resource.service.ResourcesService;

import javax.transaction.Transactional;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ResourcesServiceImpl implements ResourcesService {

    private final ResourcesRepository repository;
    private final ModelMapper mapper;
    private final AmazonS3 s3Client;

    @Value("${application.bucket.name}")
    private String bucketName;


    public ResourcesServiceImpl(ResourcesRepository repository, ModelMapper mapper, AmazonS3 s3Client) {
        this.repository = repository;
        this.mapper = mapper;
        this.s3Client = s3Client;
    }

    @Override
    public ResourceDTO findById(Integer id) {
        Resource resource = repository.findById(id)
                .filter(res -> !res.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException(Constants.RESOURCE_NOT_FOUND_EXCEPTION));
        return new ResourceDTO(resource.getContentType(),
                downloadFile(resource.getId() + Constants.UNDERSCORE + resource.getName()));
    }

    @Override
    @Transactional
    public AudioDataBinaryDTO saveResource(MultipartFile data) throws ResourceValidationException {
        Resource resource = new Resource();
        try {
            resource.setName(StringUtils.cleanPath(Objects.requireNonNull(data.getOriginalFilename())));
            resource.setContentType(data.getContentType());
            resource.setSize(data.getSize());
            resource.setDeleted(false);
            resource = repository.save(resource);
            File file = convertMultipartFileToFile(data);
            s3Client.putObject(new PutObjectRequest(bucketName,
                    resource.getId() + Constants.UNDERSCORE + resource.getName(), file));
            file.delete();
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new ResourceValidationException(Constants.VALIDATION_EXCEPTION);
        }
        return mapper.map(resource, AudioDataBinaryDTO.class);
    }

    @Override
    public DeletedResourcesDTO deleteResources(String ids) {
        DeletedResourcesDTO dto = new DeletedResourcesDTO();
        dto.setIds(Arrays.stream(ids.split(Constants.COMMA_REGEX))
                .filter(id -> id.matches(Constants.NUMBER_REGEX))
                .map(id -> repository.findById(Integer.parseInt(id)))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(file -> !file.isDeleted())
                .peek(file -> file.setDeleted(true))
                .peek(file -> deleteFile(file.getId() + Constants.UNDERSCORE + file.getName()))
                .map(repository::save)
                .map(Resource::getId)
                .collect(Collectors.toList()));
        return dto;
    }

    private File convertMultipartFileToFile(MultipartFile file) {
        File convertedFile = new File(Objects.requireNonNull(file.getOriginalFilename()));
        try(FileOutputStream fos = new FileOutputStream(convertedFile)) {
            fos.write(file.getBytes());
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new InternalServerErrorException();
        }
        return convertedFile;
    }

    private byte[] downloadFile(String fileName) {
        S3Object s3Object = s3Client.getObject(bucketName, fileName);
        try(S3ObjectInputStream inputStream = s3Object.getObjectContent()) {
            return IOUtils.toByteArray(inputStream);
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new InternalServerErrorException();
        }
    }

    private void deleteFile(String fileName) {
        s3Client.deleteObject(bucketName, fileName);
    }
}
