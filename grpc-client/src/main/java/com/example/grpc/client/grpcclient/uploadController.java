package com.example.grpc.client.grpcclient;

public class uploadController {
   private String nameOfFile;
   private String typeOfContent;
   private String displayMessage;

    public uploadController(String nameOfFile, String typeOfContent, String displayMessage){
        this.nameOfFile = nameOfFile;
        this.typeOfContent = typeOfContent;
        this.displayMessage = displayMessage;
    }

    public String fetchFileName(){
        return this.nameOfFile;
    }

    public String getContentType(){
        return this.typeOfContent;
    }

    public String fetchMessage(){
        return this.displayMessage;
    }

}
