package uz.epam.msa.resource.service.impl;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import uz.epam.msa.resource.constant.ExceptionConstants;
import uz.epam.msa.resource.domain.Resource;
import uz.epam.msa.resource.dto.AudioDataBinaryDTO;
import uz.epam.msa.resource.dto.DeletedResourcesDTO;
import uz.epam.msa.resource.dto.ResourceDTO;
import uz.epam.msa.resource.exception.InternalServerError;
import uz.epam.msa.resource.exception.ResourceNotFoundException;
import uz.epam.msa.resource.exception.ResourceValidationException;
import uz.epam.msa.resource.repository.ResourcesRepository;
import uz.epam.msa.resource.service.ResourcesService;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ResourcesServiceImpl implements ResourcesService {

    private final ResourcesRepository repository;
    private final ModelMapper mapper;

    public ResourcesServiceImpl(ResourcesRepository repository, ModelMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public AudioDataBinaryDTO findById(Integer id) {
        // the method must return file or range of bytes
        return repository.findById(id)
                .map(resource -> mapper.map(resource, AudioDataBinaryDTO.class))
                .orElseThrow(() -> new ResourceNotFoundException(ExceptionConstants.RESOURCE_NOT_FOUND_EXCEPTION));
    }

    @Override
    public ResourceDTO saveResource(AudioDataBinaryDTO data) throws ResourceValidationException {
        Resource resource = new Resource();
        try {
            resource.setName(data.getName());
            resource.setSize(data.getSize());
            resource.setContentType(data.getContentType());
        } catch (Exception e) {
            throw new ResourceValidationException(ExceptionConstants.VALIDATION_EXCEPTION);
        }
        return mapper.map(repository.save(resource), ResourceDTO.class);
    }

    @Override
    public DeletedResourcesDTO deleteResources(String ids) {
        if(!ids.matches("((?<!^,)\\\\d+(,(?!$)|$))+"))
            throw new InternalServerError(ExceptionConstants.INTERNAL_SERVER_ERROR);
        DeletedResourcesDTO dto = new DeletedResourcesDTO();
        dto.setIds(Arrays.stream(ids.split(","))
                .filter(id -> id.matches("[0-9]"))
                .map(id -> repository.findById(Integer.parseInt(id)))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .peek(file -> file.setDeleted(true))
                .map(repository::save)
                .map(Resource::getId)
                .collect(Collectors.toList()));
        return dto;
    }

}
