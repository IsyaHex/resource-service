package uz.epam.msa.resource.exception;

public class InternalServerErrorException extends RuntimeException {


    public InternalServerErrorException() {
    }

    public InternalServerErrorException(String message) {
        super(message);
    }
}
