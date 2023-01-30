package uz.epam.msa.resource.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.epam.msa.resource.dto.AudioDataBinaryDTO;
import uz.epam.msa.resource.dto.DeletedResourcesDTO;
import uz.epam.msa.resource.dto.ResourceDTO;
import uz.epam.msa.resource.exception.ResourceNotFoundException;
import uz.epam.msa.resource.exception.ResourceValidationException;
import uz.epam.msa.resource.service.ResourcesService;

import javax.validation.constraints.Max;


@RestController
@RequestMapping("/resources")
public class ResourceServiceController {

    private final ResourcesService service;

    public ResourceServiceController(ResourcesService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ResourceDTO> uploadResource(AudioDataBinaryDTO data) throws ResourceValidationException {
        ResourceDTO dto = service.saveResource(data);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> getResource(@PathVariable("id") Integer id) throws ResourceNotFoundException {
        AudioDataBinaryDTO dto = service.findById(id);
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf(dto.getContentType()))
//                .body(dto.getData());
                .body(dto.getName());
    }

    @DeleteMapping("?id={ids}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public DeletedResourcesDTO deleteResources(@PathVariable("ids") @Max(200) String ids) throws Exception {
        return service.deleteResources(ids);
    }

}
