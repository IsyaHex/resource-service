package uz.epam.msa.resource.controller;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import uz.epam.msa.resource.dto.AudioDataBinaryDTO;
import uz.epam.msa.resource.dto.DeletedResourcesDTO;
import uz.epam.msa.resource.exception.ResourceNotFoundException;
import uz.epam.msa.resource.exception.ResourceValidationException;
import uz.epam.msa.resource.service.ResourcesService;
import uz.epam.msa.resource.util.LinkGenerator;

import javax.validation.constraints.Max;

@RestController
@RequestMapping("/resources")
public class ResourceServiceController {

    private final ResourcesService service;
    private final LinkGenerator generator;

    public ResourceServiceController(ResourcesService service, LinkGenerator generator) {
        this.service = service;
        this.generator = generator;
    }

    @PostMapping
    public ResponseEntity<AudioDataBinaryDTO> uploadResource(@RequestParam("file") MultipartFile data) throws ResourceValidationException {
        AudioDataBinaryDTO dto = service.saveResource(data);
        dto.setPath(generator.createFileDownloadLink(dto.getId()));
        service.savePath(dto.getId(), dto.getPath());
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ByteArrayResource> getResource(@PathVariable("id") Integer id) throws ResourceNotFoundException {
        ByteArrayResource data = service.findById(id);
        return ResponseEntity.ok()
                .contentLength(data.contentLength())
                .body(data);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.OK)
    public DeletedResourcesDTO deleteResources(@RequestParam(value = "id") @Max(200) String ids) throws Exception {
        return service.deleteResources(ids);
    }

}
