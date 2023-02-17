package uz.epam.msa.resource.constant;

public class Constants {
    public static final String UNDERSCORE = "_";
    public static final String COMMA_REGEX = ",";
    public static final String VALIDATION_EXCEPTION = "Validation failed or request body is invalid MP3";
    public static final String RECEIVED_RESOURCE_ID = "Resource ID -> %s";
    public static final String RESOURCE_NOT_FOUND_EXCEPTION = "The resource with the specified id does not exist";
    public static final String PARSING_FILE_EXCEPTION_MESSAGE = "Error while parsing file";
    public static final String INCORRECT_RANGE_HEADER_VALUE = "Input range is incorrect";
    public static final String RANGE_HEADER_PARAMETER_VALUE_KEY = "bytes=";
    public static final String RANGE_SEPARATOR = "-";
    public static final String NUMBER_REGEX = "[0-9]";
    public static final int RANGES_VALUE_COUNT = 2;
    public static final int RANGE_BEGIN_ARRAY_INDEX = 0;
    public static final int RANGE_END_ARRAY_INDEX = 1;
    public static final String AUDIO_FILE_CONTENT_TYPE = "audio/mpeg";
}
