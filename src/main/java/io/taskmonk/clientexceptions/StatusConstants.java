package io.taskmonk.clientexceptions;

public class StatusConstants {


    public enum  StatusCode {
        FORBIDDEN(403, "Unauthorised Access"),
        INTERNALSERVERERROR(500, "An internal server error occured"),
        NOTFOUND(404, " Object not found"),
        UNHANDLED(00, "Unhandled exception"),
        OK(200,"Ok"),
        CREATED(201, "Created") ;


        private final int code;
        private final String display;


         StatusCode(int code, String display){
            this.code = code;
            this.display = display;
        }

        public int getCode() {
            return this.code;


        }

        public String getDisplay() {
            return this.display;
        }
    }
}
