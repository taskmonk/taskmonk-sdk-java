package io.taskmonk.clientexceptions;

public class ForbiddenException extends Exception {
    String message;

    public ForbiddenException(String message) {
        this.message= message;
    }

}
