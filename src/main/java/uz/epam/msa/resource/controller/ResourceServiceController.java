package uz.epam.msa.resource.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import uz.epam.msa.resource.constant.Constants;
import uz.epam.msa.resource.dto.AudioDataBinaryDTO;
import uz.epam.msa.resource.dto.DeletedResourcesDTO;
import uz.epam.msa.resource.dto.ResourceDTO;
import uz.epam.msa.resource.exception.ResourceNotFoundException;
import uz.epam.msa.resource.exception.ResourceValidationException;
import uz.epam.msa.resource.service.ResourcesService;
import uz.epam.msa.resource.util.ResourceProducer;
import uz.epam.msa.resource.util.ResourceUtil;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Max;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/resources")
public class ResourceServiceController {

    private final ResourcesService service;
    private final ResourceUtil util;
    private final ResourceProducer producer;
    private final HttpServletRequest request;

    public ResourceServiceController(ResourcesService service, ResourceUtil generator, ResourceProducer producer, HttpServletRequest request) {
        this.service = service;
        this.util = generator;
        this.producer = producer;
        this.request = request;
    }

    @PostMapping(
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AudioDataBinaryDTO> uploadResource(@RequestParam("file") MultipartFile data) throws ResourceValidationException {
        AudioDataBinaryDTO dto = service.saveResource(data);
        dto.setPath(util.createFileDownloadLink(dto.getId()));
        List<String> list = new ArrayList<>();
        list.add(String.valueOf(dto.getId()));
        list.add(request.getHeader(Constants.AUTHORIZATION));
        producer.sendMessage(list.toString());
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> getResource(
            @RequestHeader(value = "Range", required = false) String rangeHeader,
            @PathVariable("id") Integer id) throws ResourceNotFoundException {
        ResourceDTO dto = service.findById(id);
        return Objects.isNull(rangeHeader) ?
                ResponseEntity.status(200)
                        .contentType(MediaType.valueOf(dto.getContentType()))
                        .body(dto.getResource()) :
                ResponseEntity.status(206)
                        .contentType(MediaType.valueOf(dto.getContentType()))
                        .headers(util.createHeadersForRangeRequest(dto, rangeHeader))
                        .body(util.createMp3Range(dto, rangeHeader));
    }

    @DeleteMapping
    public ResponseEntity<DeletedResourcesDTO> deleteResources(@RequestParam(value = "id") @Max(200) String ids) throws Exception {
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(service.deleteResources(ids));
    }

}
