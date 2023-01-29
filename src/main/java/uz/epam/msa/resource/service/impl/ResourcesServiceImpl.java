package uz.epam.msa.resource.service.impl;

import org.springframework.stereotype.Service;
import uz.epam.msa.resource.domain.Resource;
import uz.epam.msa.resource.dto.AudioDataBinaryDTO;
import uz.epam.msa.resource.dto.ResourceDTO;
import uz.epam.msa.resource.dto.ResourcesDTO;
import uz.epam.msa.resource.exception.ResourceNotFoundException;
import uz.epam.msa.resource.exception.ResourceValidationException;
import uz.epam.msa.resource.repository.ResourcesRepository;
import uz.epam.msa.resource.service.ResourcesService;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ResourcesServiceImpl implements ResourcesService {

    private final ResourcesRepository repository;

    public ResourcesServiceImpl(ResourcesRepository repository) {
        this.repository = repository;
    }

    @Override
    public AudioDataBinaryDTO findById(Integer id) throws ResourceNotFoundException {
        Resource r = repository.findById(id).orElseThrow(ResourceNotFoundException::new);
        AudioDataBinaryDTO data = new AudioDataBinaryDTO();

        return data;
    }

    @Override
    public ResourceDTO saveResource(AudioDataBinaryDTO data) throws ResourceValidationException {
        Resource resource = new Resource();
        // audio data add
        Resource r = repository.save(resource);
        if(r == null)
            throw new ResourceValidationException();
        else
            return new ResourceDTO(r.getId());
    }

    @Override
    public ResourcesDTO deleteResources(String ids) {
        List<Integer> list = Arrays.stream(ids.split(",")).map(Integer::valueOf).collect(Collectors.toList());
        repository.deleteAllById(list);
        return new ResourcesDTO(list.toArray(Integer[]::new));
    }

}
