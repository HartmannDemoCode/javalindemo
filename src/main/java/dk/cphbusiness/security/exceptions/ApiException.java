package dk.cphbusiness.security.exceptions;

/**
 * Purpose: To handle exceptions in the API
 * Author: Thomas Hartmann
 */
public class ApiException extends Exception{
    private int code;
    public ApiException (int code, String msg) {
        super(msg);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
