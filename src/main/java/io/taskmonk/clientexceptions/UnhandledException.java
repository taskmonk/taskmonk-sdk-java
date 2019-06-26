package io.taskmonk.clientexceptions;

public class UnhandledException extends Exception {

    String message;

    public UnhandledException(String message) {
        this.message= message;
    }

}
