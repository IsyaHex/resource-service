package uz.epam.msa.resource.service.impl;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import uz.epam.msa.resource.constant.Constants;
import uz.epam.msa.resource.domain.Resource;
import uz.epam.msa.resource.dto.AudioDataBinaryDTO;
import uz.epam.msa.resource.dto.DeletedResourcesDTO;
import uz.epam.msa.resource.dto.GetStorageDTO;
import uz.epam.msa.resource.dto.ResourceDTO;
import uz.epam.msa.resource.exception.ResourceNotFoundException;
import uz.epam.msa.resource.exception.ResourceValidationException;
import uz.epam.msa.resource.repository.ResourcesRepository;
import uz.epam.msa.resource.service.ResourcesService;
import uz.epam.msa.resource.util.AwsUtil;
import uz.epam.msa.resource.util.MicroserviceUtil;
import uz.epam.msa.resource.util.ResourceUtil;

import javax.transaction.Transactional;
import java.io.File;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ResourcesServiceImpl implements ResourcesService {

    private final ResourcesRepository repository;
    private final ModelMapper mapper;
    private final AmazonS3 s3Client;
    private final AwsUtil awsUtil;
    private final ResourceUtil resourceUtil;
    private final MicroserviceUtil microserviceUtil;
    @Autowired
    private CircuitBreaker circuitBreaker;

    public ResourcesServiceImpl(ResourcesRepository repository, ModelMapper mapper, AmazonS3 s3Client, AwsUtil awsUtil, ResourceUtil resourceUtil, MicroserviceUtil microserviceUtil) {
        this.repository = repository;
        this.mapper = mapper;
        this.s3Client = s3Client;
        this.awsUtil = awsUtil;
        this.resourceUtil = resourceUtil;
        this.microserviceUtil = microserviceUtil;
    }

    @Override
    public ResourceDTO findById(Integer id) {
        Resource resource = repository.findById(id)
                .filter(res -> !res.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException(Constants.RESOURCE_NOT_FOUND_EXCEPTION));

        GetStorageDTO permanentStorage = resourceUtil.getCircuitBreakerObject(
                microserviceUtil::getPermanentStorage, microserviceUtil.getPermanentStorageFallBack());
        log.info(String.format("Permanent storage id -> %s", permanentStorage.getId()));
        return new ResourceDTO(resource.getContentType(),
                awsUtil.downloadFile(resource.getId() + Constants.UNDERSCORE + resource.getName(), permanentStorage.getBucket()));
    }

    @Override
    public ResourceDTO findByIdInternal(Integer id) {
        Resource resource = repository.findById(id)
                .filter(res -> !res.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException(Constants.RESOURCE_NOT_FOUND_EXCEPTION));
        GetStorageDTO stagingStorage = resourceUtil.getCircuitBreakerObject(
                microserviceUtil::getStagingStorage, microserviceUtil.getStagingStorageFallBack());
        log.info(String.format("Staging storage id -> %s", stagingStorage.getId()));
        return new ResourceDTO(resource.getContentType(),
                awsUtil.downloadFile(resource.getId() + Constants.UNDERSCORE + resource.getName(), stagingStorage.getBucket()));
    }

    @Override
    @Transactional
    public AudioDataBinaryDTO saveResource(MultipartFile data) throws ResourceValidationException {
        if(!Constants.AUDIO_FILE_CONTENT_TYPE.equals(data.getContentType()))
            throw new ResourceValidationException(Constants.VALIDATION_EXCEPTION);
        Resource resource = new Resource();
        try {
            resource.setName(StringUtils.cleanPath(Objects.requireNonNull(data.getOriginalFilename())));
            resource.setContentType(data.getContentType());
            resource.setSize(data.getSize());
            resource.setDeleted(false);
            resource = repository.save(resource);
            File file = resourceUtil.convertMultipartFileToFile(data);
            GetStorageDTO stagingStorage = resourceUtil.getCircuitBreakerObject(
                    microserviceUtil::getStagingStorage, microserviceUtil.getStagingStorageFallBack());
            s3Client.putObject(new PutObjectRequest(stagingStorage.getBucket(),
                    resource.getId() + Constants.UNDERSCORE + resource.getName(), file));
            file.delete();
            log.info(String.format("Staging storage id -> %s", stagingStorage.getId()));
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new ResourceValidationException(Constants.VALIDATION_EXCEPTION);
        }
        return mapper.map(resource, AudioDataBinaryDTO.class);
    }

    @Override
    public DeletedResourcesDTO deleteResources(String ids) {
        DeletedResourcesDTO dto = new DeletedResourcesDTO();

        GetStorageDTO stagingStorage = resourceUtil.getCircuitBreakerObject(
                microserviceUtil::getStagingStorage, microserviceUtil.getStagingStorageFallBack());
        GetStorageDTO permanentStorage = resourceUtil.getCircuitBreakerObject(
                microserviceUtil::getPermanentStorage, microserviceUtil.getPermanentStorageFallBack());
        log.info(String.format("Staging storage id -> %s", stagingStorage.getId()));
        log.info(String.format("Permanent storage id -> %s", permanentStorage.getId()));

        dto.setIds(Arrays.stream(ids.split(Constants.COMMA_REGEX))
                .filter(id -> id.matches(Constants.NUMBER_REGEX))
                .map(id -> repository.findById(Integer.parseInt(id)))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(file -> !file.isDeleted())
                .peek(file -> file.setDeleted(true))
                .peek(file -> awsUtil.deleteFile(file.getId() + Constants.UNDERSCORE + file.getName(), stagingStorage.getBucket()))
                .peek(file -> awsUtil.deleteFile(file.getId() + Constants.UNDERSCORE + file.getName(), permanentStorage.getBucket()))
                .map(repository::save)
                .map(Resource::getId)
                .collect(Collectors.toList()));
        return dto;
    }

}
