package csye6225.cloud.noteapp.exception;

import java.lang.Exception;

public class AppException extends Exception{

    private int status;

    public AppException(String errMessage) {
        super(errMessage);
        this.status = 501;
    }

    public AppException(int status, String errMessage) {
        super(errMessage);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
