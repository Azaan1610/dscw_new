package com.example.grpc.client.grpcclient;

public class uploadController{
    private String matrixFileName ;
    private String matrixContentType;
    private String displayMessage;

    public uploadController(String m_fileName, String m_contentType, String m_displayMessage){
        this.matrixFileName = m_fileName;
        this.matrixContentType = m_contentType;
        this.displayMessage = m_displayMessage;
    }

    public String getFileName(){
        return this.matrixFileName;
    }
    public String getContentType(){
        return this.matrixContentType;
    }
    public String getMessage(){
        return this.displayMessage;
    }
}
