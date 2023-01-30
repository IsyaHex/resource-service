package uz.epam.msa.resource.exception;

public class InternalServerError extends Error {


    public InternalServerError() {
    }

    public InternalServerError(String message) {
        super(message);
    }
}
