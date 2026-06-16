package com.bigyun.config.service.handler.storage;

/**
 * 文件存储请求
 */
public class StorageRequest {

    /**
     * 操作类型
     */
    public enum OperationType {
        UPLOAD,      // 上传
        DOWNLOAD,    // 下载
        DELETE,      // 删除
        GET_URL      // 获取访问URL
    }

    private OperationType operation;
    private String fileName;
    private String filePath;
    private byte[] fileData;
    private String contentType;

    public OperationType getOperation() {
        return operation;
    }

    public void setOperation(OperationType operation) {
        this.operation = operation;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public byte[] getFileData() {
        return fileData;
    }

    public void setFileData(byte[] fileData) {
        this.fileData = fileData;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public static StorageRequest upload(String fileName, String filePath, byte[] fileData, String contentType) {
        StorageRequest request = new StorageRequest();
        request.setOperation(OperationType.UPLOAD);
        request.setFileName(fileName);
        request.setFilePath(filePath);
        request.setFileData(fileData);
        request.setContentType(contentType);
        return request;
    }

    public static StorageRequest delete(String filePath) {
        StorageRequest request = new StorageRequest();
        request.setOperation(OperationType.DELETE);
        request.setFilePath(filePath);
        return request;
    }

    public static StorageRequest getUrl(String filePath) {
        StorageRequest request = new StorageRequest();
        request.setOperation(OperationType.GET_URL);
        request.setFilePath(filePath);
        return request;
    }
}
