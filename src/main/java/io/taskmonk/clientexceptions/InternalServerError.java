package io.taskmonk.clientexceptions;

public class InternalServerError extends Exception {

    String message;

    public InternalServerError(String message) {
        this.message= message;
    }

}
