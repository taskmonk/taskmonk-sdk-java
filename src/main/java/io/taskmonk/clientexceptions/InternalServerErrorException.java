package io.taskmonk.clientexceptions;

public class InternalServerErrorException extends Exception {

    String message;

    public InternalServerErrorException(String message) {
        this.message= message;
    }

}
