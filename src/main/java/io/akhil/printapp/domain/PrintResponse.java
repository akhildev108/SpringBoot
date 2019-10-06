package io.akhil.printapp.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class PrintResponse implements Serializable {

    public enum Status {
        SUCCESS(200),
        FAILURE(500);

        private final int value;

        Status(final int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    private int statusCode;
    private Status status;
    private List<String> errors;

    public List<String> getErrors() {
        if(errors==null) {
            return new ArrayList<>();
        }
        return errors;
    }

    public void addError(String error) {
        if(error==null || error.trim().isEmpty()) {
            return;
        }
        if(errors==null) {
            this.errors = new ArrayList<>();
        }
        this.errors.add(error);
    }

}
