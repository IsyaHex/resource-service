package uz.epam.msa.resource.service.impl;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.util.IOUtils;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import uz.epam.msa.resource.constant.ExceptionConstants;
import uz.epam.msa.resource.domain.Resource;
import uz.epam.msa.resource.dto.AudioDataBinaryDTO;
import uz.epam.msa.resource.dto.DeletedResourcesDTO;
import uz.epam.msa.resource.exception.InternalServerError;
import uz.epam.msa.resource.exception.ResourceNotFoundException;
import uz.epam.msa.resource.exception.ResourceValidationException;
import uz.epam.msa.resource.repository.ResourcesRepository;
import uz.epam.msa.resource.service.ResourcesService;

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

    @Value("${application.bucket.name}")
    private String bucketName;

    @Autowired
    private AmazonS3 s3Client;

    public ResourcesServiceImpl(ResourcesRepository repository, ModelMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public ByteArrayResource findById(Integer id) {
        // the method must return file or range of bytes
        Resource resource = repository.findById(id)
                .filter(res -> !res.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException(ExceptionConstants.RESOURCE_NOT_FOUND_EXCEPTION));
        byte[] file = downloadFile(resource.getName());
        return new ByteArrayResource(file);
    }

    @Override
    public AudioDataBinaryDTO saveResource(MultipartFile data) throws ResourceValidationException {
        Resource resource = new Resource();
        try {
            resource.setName(StringUtils.cleanPath(Objects.requireNonNull(data.getOriginalFilename())));
            resource.setContentType(data.getContentType());
            resource.setSize(data.getSize());
            resource.setDeleted(false);
            resource.setPath("");
            File file = convertMultipartFileToFile(data);
            s3Client.putObject(new PutObjectRequest(bucketName, data.getOriginalFilename(), file));
//            file.delete();
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new ResourceValidationException(ExceptionConstants.VALIDATION_EXCEPTION);
        }
        return mapper.map(repository.save(resource), AudioDataBinaryDTO.class);
    }

    @Override
    public void savePath(Integer id, String path) {
        Resource resource = repository.findById(id)
                .filter(res -> !res.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException(ExceptionConstants.RESOURCE_NOT_FOUND_EXCEPTION));
        resource.setPath(path);
        repository.save(resource);
    }

    @Override
    public DeletedResourcesDTO deleteResources(String ids) {
        DeletedResourcesDTO dto = new DeletedResourcesDTO();
        dto.setIds(Arrays.stream(ids.split(","))
                .filter(id -> id.matches("[0-9]"))
                .map(id -> repository.findById(Integer.parseInt(id)))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .peek(file -> file.setDeleted(true))
                .peek(file -> deleteFile(file.getName()))
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
            throw new InternalServerError();
        }
        return convertedFile;
    }

    private byte[] downloadFile(String fileName) {
        S3Object s3Object = s3Client.getObject(bucketName, fileName);
        S3ObjectInputStream inputStream = s3Object.getObjectContent();
        try {
            return IOUtils.toByteArray(inputStream);
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new InternalServerError();
        }
    }

    private void deleteFile(String fileName) {
        s3Client.deleteObject(bucketName, fileName);
    }
}
