package uz.epam.msa.resource.controller;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import uz.epam.msa.resource.ResourceServiceApplication;
import uz.epam.msa.resource.constant.Constants;
import uz.epam.msa.resource.domain.Resource;
import uz.epam.msa.resource.dto.DeletedResourcesDTO;
import uz.epam.msa.resource.exception.InternalServerErrorException;
import uz.epam.msa.resource.repository.ResourcesRepository;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = ResourceServiceApplication.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestPropertySource(locations = "classpath:application-integration-test.properties")
@AutoConfigureMockMvc
public class ResourceServiceIntegrationTest {

    @Autowired
    private TestRestTemplate template;
    @Autowired
    private ResourcesRepository repository;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private AmazonS3 s3Client;

    @Value("${aws.cloud.bucket.name}")
    private String bucketName;

    private static final String URL = "http://localhost:1188/resources";

    @BeforeEach
    void setUp() {
        Resource resource = new Resource();
        resource.setId(1);
        resource.setSize(99999L);
        resource.setDeleted(false);
        resource.setName("test-name");
        resource.setContentType("test/content");
        repository.save(resource);
    }

    @Test
    void uploadResource() throws Exception {
        MockMultipartFile file
                = new MockMultipartFile(
                "file",
                "hello.txt",
                "audio/mpeg",
                "Hello, World!".getBytes()
        );
        mockMvc.perform(multipart("/resources/").file(file))
                .andExpect(status().isCreated());
    }

    @Test
    void getResource() {
        File file = getSampleFile();
        String fileName = 1 + Constants.UNDERSCORE + "test-name";
        s3Client.putObject(new PutObjectRequest(bucketName, fileName, file));

        ResponseEntity<byte[]> response = template.getForEntity(URL + "/" + 1, byte[].class);
        assertEquals(HttpStatus.OK, response.getStatusCode());

//        deleteData(file, fileName);
    }

    @Test
    void deleteResources() {
        File file = getSampleFile();
        String fileName = 1 + Constants.UNDERSCORE + "test-name";
        s3Client.putObject(new PutObjectRequest(bucketName, fileName, file));

        ResponseEntity<DeletedResourcesDTO> responseEntity = template.exchange(URL + "?id=1",
                HttpMethod.DELETE, HttpEntity.EMPTY, DeletedResourcesDTO.class);

        assertEquals(HttpStatus.ACCEPTED, responseEntity.getStatusCode());
//        deleteData(file, fileName);
    }

    private File getSampleFile() {
        File file = new File("test-file.txt");
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write("dummy text".getBytes());
        } catch (IOException e) {
            throw new InternalServerErrorException();
        }
        return file;
    }

    private void deleteData(File file, String fileName) {
        s3Client.deleteObject(bucketName, fileName);
        file.delete();
    }
}