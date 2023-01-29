package uz.epam.msa.resource.exception;

public class ResourceValidationException extends Throwable {
    private String message;

    public ResourceValidationException() {
        message = "Validation failed or request body is invalid MP3";
    }

    @Override
    public String toString() {
        return "400 - " + message;
    }
}
