package io.akhil.printapp.domain;

import lombok.Data;

@Data
public class PrintDoc {

    private String contentType;
    private String content;
//    private String fileType;
    private String orientation;
    private int numOfCopies = 1;

}
