package uz.epam.msa.resource.service.impl;

import org.springframework.stereotype.Service;
import uz.epam.msa.resource.constant.Constants;
import uz.epam.msa.resource.domain.Resource;
import uz.epam.msa.resource.repository.ResourcesRepository;
import uz.epam.msa.resource.service.FileService;

@Service
public class FileServiceImpl implements FileService {

    private final ResourcesRepository resourcesRepository;


    public FileServiceImpl(ResourcesRepository resourcesRepository) {
        this.resourcesRepository = resourcesRepository;
    }

    @Override
    public String getFileName(int resourceId) {
        Resource resource = resourcesRepository.findById(resourceId).get();
        return resource.getName();
    }

    @Override
    public void updateStatus(int resourceId) {
        Resource resource = resourcesRepository.findById(resourceId).get();
        resource.setStatus(Constants.PERMANENT);
        resourcesRepository.save(resource);
    }
}
