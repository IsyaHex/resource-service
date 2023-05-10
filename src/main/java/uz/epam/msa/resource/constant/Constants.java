package uz.epam.msa.resource.constant;

public class Constants {
    public static final String UNDERSCORE = "_";
    public static final String COMMA_REGEX = ",";
    public static final String VALIDATION_EXCEPTION = "Validation failed or request body is invalid MP3";
    public static final String RECEIVED_RESOURCE_ID = "Resource ID of the saved song -> %s";
    public static final String FILE_ID_PATTERN = "%s_%s";
    public static final String RESOURCE_NOT_FOUND_EXCEPTION = "The resource with the specified id does not exist";
    public static final String PARSING_FILE_EXCEPTION_MESSAGE = "Error while parsing file";
    public static final String INCORRECT_RANGE_HEADER_VALUE = "Input range is incorrect";
    public static final String RANGE_HEADER_PARAMETER_VALUE_KEY = "bytes=";
    public static final String RANGE_SEPARATOR = "-";
    public static final String NUMBER_REGEX = "^\\d+$";
    public static final int RANGES_VALUE_COUNT = 2;
    public static final int RANGE_BEGIN_ARRAY_INDEX = 0;
    public static final int RANGE_END_ARRAY_INDEX = 1;
    public static final String AUDIO_FILE_CONTENT_TYPE = "audio/mpeg";
    public static final String STAGING = "STAGING";
    public static final String PERMANENT = "PERMANENT";

    public static final String AUTHORIZATION = "authorization";
    public static final String LOG_COPY_FILE_BETWEEN_BUCKETS = "Copy the file: %s from the %s to the %s";
    public static final String CIRCUIT_BREAKER_CONFIG_NAME = "STORAGE_SERVICE_GET_STORAGES";
    public static final String STAGING_BUCKET = "msa-resources-bucket-staging";
    public static final String PERMANENT_BUCKET = "msa-resources-bucket-permanent";
}
