package uz.epam.msa.resource.exception;

public class ResourceValidationException extends RuntimeException {

    public ResourceValidationException() {}
    public ResourceValidationException(String message) {
        super(message);
    }
}
