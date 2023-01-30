package uz.epam.msa.resource.service;


import uz.epam.msa.resource.dto.AudioDataBinaryDTO;
import uz.epam.msa.resource.dto.DeletedResourcesDTO;
import uz.epam.msa.resource.dto.ResourceDTO;
import uz.epam.msa.resource.exception.ResourceNotFoundException;
import uz.epam.msa.resource.exception.ResourceValidationException;

import javax.naming.SizeLimitExceededException;

public interface ResourcesService {
    AudioDataBinaryDTO findById(Integer id) throws ResourceNotFoundException;
    ResourceDTO saveResource(AudioDataBinaryDTO data) throws ResourceValidationException;
    DeletedResourcesDTO deleteResources(String ids) throws SizeLimitExceededException;
}
