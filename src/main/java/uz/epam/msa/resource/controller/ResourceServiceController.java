package uz.epam.msa.resource.controller;

import org.springframework.web.bind.annotation.*;
import uz.epam.msa.resource.domain.Resource;
import uz.epam.msa.resource.dto.AudioDataBinaryDTO;
import uz.epam.msa.resource.dto.ResourceDTO;
import uz.epam.msa.resource.dto.ResourcesDTO;
import uz.epam.msa.resource.exception.ResourceNotFoundException;
import uz.epam.msa.resource.exception.ResourceValidationException;
import uz.epam.msa.resource.service.ResourcesService;

@RestController
@RequestMapping("/resources")
public class ResourceServiceController {

    private final ResourcesService service;

    public ResourceServiceController(ResourcesService service) {
        this.service = service;
    }

    @PostMapping
    public ResourceDTO uploadResource(AudioDataBinaryDTO data) throws ResourceValidationException {
        return service.saveResource(data);
    }

    @GetMapping("/{id}")
    public AudioDataBinaryDTO getResource(@PathVariable("id") Integer id) throws ResourceNotFoundException {
        return service.findById(id);
    }

    @DeleteMapping("?id={ids}")
    public ResourcesDTO deleteResources(@PathVariable String ids) throws Exception {
        if(ids.matches("((?<!^,)\\\\d+(,(?!$)|$))+") || ids.length() >= 200)
            return service.deleteResources(ids);
        else
            throw new Exception("An internal server error has occurred");
    }

}
