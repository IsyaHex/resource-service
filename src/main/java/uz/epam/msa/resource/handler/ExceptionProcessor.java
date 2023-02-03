package uz.epam.msa.resource.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import uz.epam.msa.resource.dto.ErrorDto;
import uz.epam.msa.resource.exception.InternalServerErrorException;
import uz.epam.msa.resource.exception.ResourceNotFoundException;
import uz.epam.msa.resource.exception.ResourceValidationException;

@RestControllerAdvice
public class ExceptionProcessor extends ResponseEntityExceptionHandler {
    private static final int PARTIAL_CONTENT = 206;
    private static final int INCORRECT_PARAMETER_VALUE_CODE = 400;
    private static final int RESOURCE_NOT_FOUND_CODE = 404;
    private static final int INTERNAL_SERVER_ERROR = 500;


    @ExceptionHandler(value = ResourceValidationException.class)
    public ResponseEntity<ErrorDto> handleIncorrectParameterValueException(ResourceValidationException exception) {
        return new ResponseEntity<>(new ErrorDto(exception.getMessage(), INCORRECT_PARAMETER_VALUE_CODE), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = ResourceNotFoundException.class)
    public ResponseEntity<ErrorDto> handleResourceNotFoundException(ResourceNotFoundException exception) {
        return new ResponseEntity<>(new ErrorDto(exception.getMessage(), RESOURCE_NOT_FOUND_CODE), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = InternalServerErrorException.class)
    public ResponseEntity<ErrorDto> handleResourceNotFoundException(InternalServerErrorException exception) {
        return new ResponseEntity<>(new ErrorDto(exception.getMessage(), INTERNAL_SERVER_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
