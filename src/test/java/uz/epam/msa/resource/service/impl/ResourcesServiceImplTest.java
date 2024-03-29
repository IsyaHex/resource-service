package uz.epam.msa.resource.service.impl;

import com.amazonaws.services.s3.AmazonS3;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import uz.epam.msa.resource.domain.Resource;
import uz.epam.msa.resource.dto.AudioDataBinaryDTO;
import uz.epam.msa.resource.dto.DeletedResourcesDTO;
import uz.epam.msa.resource.dto.GetStorageDTO;
import uz.epam.msa.resource.dto.ResourceDTO;
import uz.epam.msa.resource.repository.ResourcesRepository;
import uz.epam.msa.resource.service.ResourcesService;
import uz.epam.msa.resource.util.AwsUtil;
import uz.epam.msa.resource.util.StorageManager;
import uz.epam.msa.resource.util.ResourceUtil;

import javax.naming.SizeLimitExceededException;
import java.io.File;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ResourcesServiceImplTest {

    private ResourcesService service;

    @Mock
    private ModelMapper mapper;
    @Mock
    private ResourcesRepository repository;
    @Mock
    private AmazonS3 s3Client;
    @Mock
    private AwsUtil awsUtil;
    @Mock
    private ResourceUtil resourceUtil;
    @Mock
    private StorageManager storageManager;

    @BeforeEach
    void init () {
        service = new ResourcesServiceImpl(repository, mapper, s3Client, awsUtil, resourceUtil, storageManager);
    }

    @Test
    void findById() {
        String contentType = "audio/mpeg";
        Resource resource = getResource();
        String fileName = resource.getId() + "_" + resource.getName();
        String bucketName = "testBucket";

        ResourceDTO dto = new ResourceDTO();
        dto.setResource(new byte[]{});
        dto.setContentType(contentType);

        GetStorageDTO storageDTO = new GetStorageDTO();
        storageDTO.setId(-1);
        storageDTO.setPath("");
        storageDTO.setBucket("testBucket");
        storageDTO.setStorageType("testStorageType");

        resourceUtil.getCircuitBreakerObject(
                storageManager::getPermanentStorage, storageManager.getPermanentStorageFallBack());

        when(repository.findById(0)).thenReturn(Optional.of(resource));
        when(awsUtil.downloadFile(fileName, bucketName)).thenReturn(new byte[]{});
        when(resourceUtil.getCircuitBreakerObject(any(), any())).thenReturn(storageDTO);

        ResourceDTO result = service.findById(0);

        assertEquals(dto, result);
    }

    @Test
    void saveResource() {
        String name = "test";
        String contentType = "audio/mpeg";
        long size = 99999L;

        Resource resource = getResource();

        AudioDataBinaryDTO dto = new AudioDataBinaryDTO();
        dto.setName(name);
        dto.setContentType(contentType);
        dto.setSize(size);
        MultipartFile file = new MockMultipartFile(name, name, contentType, new byte[99999]);

        GetStorageDTO storageDTO = new GetStorageDTO();
        storageDTO.setId(-1);
        storageDTO.setPath("");
        storageDTO.setBucket("testBucket");
        storageDTO.setStorageType("testStorageType");

        when(repository.save(any(Resource.class))).thenReturn(resource);
        when(mapper.map(resource, AudioDataBinaryDTO.class)).thenReturn(dto);
        when(resourceUtil.convertMultipartFileToFile(file)).thenReturn(new File(file.getName()));
        when(resourceUtil.getCircuitBreakerObject(any(), any())).thenReturn(storageDTO);

        assertEquals(dto, service.saveResource(file));
    }

    @Test
    void deleteResources() throws SizeLimitExceededException {
        List<Integer> ids = List.of(99999);
        DeletedResourcesDTO dto = new DeletedResourcesDTO();
        dto.setIds(ids);

        Resource resource = getResource();

        GetStorageDTO storageDTO = new GetStorageDTO();
        storageDTO.setId(-1);
        storageDTO.setPath("");
        storageDTO.setBucket("testBucket");
        storageDTO.setStorageType("testStorageType");

        when(repository.findById(any(Integer.class))).thenReturn(Optional.of(resource));
        when(repository.save(any(Resource.class))).thenReturn(resource);
        when(resourceUtil.getCircuitBreakerObject(any(), any())).thenReturn(storageDTO);

        assertEquals(dto, service.deleteResources("99999"));
    }

    private Resource getResource() {
        Resource resource = new Resource();
        resource.setId(99999);
        resource.setSize(99999L);
        resource.setName("test");
        resource.setDeleted(false);
        resource.setContentType("audio/mpeg");
        resource.setStatus("PERMANENT");

        return resource;
    }
}