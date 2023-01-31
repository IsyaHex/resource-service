package uz.epam.msa.resource.util;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Component
public class LinkGenerator {
    public String createFileDownloadLink(Integer fileId) {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/resources/")
                .path(String.valueOf(fileId))
                .toUriString();
    }
}
