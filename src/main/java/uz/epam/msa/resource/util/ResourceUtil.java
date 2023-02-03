package uz.epam.msa.resource.util;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import uz.epam.msa.resource.constant.Constants;
import uz.epam.msa.resource.dto.ResourceDTO;
import uz.epam.msa.resource.exception.InternalServerErrorException;
import uz.epam.msa.resource.exception.ResourceValidationException;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Objects;

@Component
public class ResourceUtil {
    public String createFileDownloadLink(Integer fileId) {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/resources/")
                .path(String.valueOf(fileId))
                .toUriString();
    }

    public HttpHeaders createHeadersForRangeRequest(ResourceDTO dto, String rangeHeader) {
        HttpHeaders httpHeaders = new HttpHeaders();
        String[] audioRange = parseRangeHeader(rangeHeader);

        int beginIndex = Integer.parseInt(audioRange[Constants.RANGE_BEGIN_ARRAY_INDEX]);
        int endIndex = Integer.parseInt(audioRange[Constants.RANGE_END_ARRAY_INDEX]);
        int rangeLength = endIndex - beginIndex + 1;

        httpHeaders.add("Content-Type", Constants.AUDIO_FILE_CONTENT_TYPE);
        httpHeaders.add("Content-Length", String.valueOf(rangeLength));
        httpHeaders.add("Content-Range",   beginIndex + Constants.RANGE_SEPARATOR + endIndex
                + "/" + dto.getResource().length);
        return httpHeaders;
    }

    public byte[] createMp3Range(ResourceDTO dto, String rangeHeader) {
        String[] audioRange = parseRangeHeader(rangeHeader);

        if (Objects.isNull(audioRange) || validateRanges(audioRange, dto.getResource().length)) {
            throw new ResourceValidationException(Constants.INCORRECT_RANGE_HEADER_VALUE);
        }
        int beginIndex = Integer.parseInt(audioRange[Constants.RANGE_BEGIN_ARRAY_INDEX]);
        int endIndex = Integer.parseInt(audioRange[Constants.RANGE_END_ARRAY_INDEX]);
        int rangeLength = endIndex - beginIndex + 1;

        return createCuttingAudio(dto, rangeLength, beginIndex);
    }

    private byte[] createCuttingAudio(ResourceDTO dto, int rangeLength, int beginIndex) {
        byte[] result = new byte[rangeLength];

        try(ByteArrayInputStream buffer = new ByteArrayInputStream(dto.getResource(), beginIndex, rangeLength)) {
            buffer.read(result);
        } catch (Exception e) {
            throw new InternalServerErrorException(Constants.PARSING_FILE_EXCEPTION_MESSAGE);
        }
        return result;
    }

    private String[] parseRangeHeader(String rangeHeader) {
        String[] result = null;
        if (rangeHeader.contains(Constants.RANGE_HEADER_PARAMETER_VALUE_KEY)) {
            result = rangeHeader.substring(Constants.RANGE_HEADER_PARAMETER_VALUE_KEY.length())
                    .split(Constants.RANGE_SEPARATOR);
        }
        return result;
    }

    private boolean validateRanges(String[] ranges, int audioFileLength) {
        return Arrays.stream(ranges)
                .filter(range -> range.matches(Constants.NUMBER_REGEX) && Integer.parseInt(range) < audioFileLength)
                .count() == Constants.RANGES_VALUE_COUNT;
    }
}
