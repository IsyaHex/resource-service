package uz.epam.msa.resource.exception;

public class ResourceNotFoundException extends Throwable {

    private String message;

    public ResourceNotFoundException() {
        message = "The resource with the specified id does not exist";
    }

    @Override
    public String toString() {
        return "404 - " + message;
    }
}
