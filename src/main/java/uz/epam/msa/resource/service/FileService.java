package uz.epam.msa.resource.service;

public interface FileService {
    String getFileName(int resourceId);
    void updateStatus(int resourceId);
}
