package uz.epam.msa.resource.service;


import org.springframework.web.multipart.MultipartFile;
import uz.epam.msa.resource.dto.AudioDataBinaryDTO;
import uz.epam.msa.resource.dto.DeletedResourcesDTO;
import uz.epam.msa.resource.dto.ResourceDTO;
import uz.epam.msa.resource.exception.ResourceNotFoundException;
import uz.epam.msa.resource.exception.ResourceValidationException;

import javax.naming.SizeLimitExceededException;

public interface ResourcesService {
    ResourceDTO findById(Integer id) throws ResourceNotFoundException;
    ResourceDTO findByIdInternal(Integer id) throws ResourceNotFoundException;
    AudioDataBinaryDTO saveResource(MultipartFile data) throws ResourceValidationException;
    DeletedResourcesDTO deleteResources(String ids) throws SizeLimitExceededException;
}
