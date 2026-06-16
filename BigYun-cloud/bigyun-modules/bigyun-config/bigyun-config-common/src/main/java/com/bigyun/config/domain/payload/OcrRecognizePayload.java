package com.bigyun.config.domain.payload;

import java.util.Map;

public class OcrRecognizePayload extends ProviderPayload
{
    private static final long serialVersionUID = 1L;

    private String imageBase64;
    private String imageUrl;
    private String contentType;
    private String fileName;
    private Long fileSize;
    private String ocrType;
    private String reportType;

    public static OcrRecognizePayload forBase64(String imageBase64, String ocrType)
    {
        OcrRecognizePayload payload = new OcrRecognizePayload();
        payload.setImageBase64(imageBase64);
        payload.setOcrType(ocrType);
        return payload;
    }

    public static OcrRecognizePayload forImageUrl(String imageUrl, String ocrType)
    {
        OcrRecognizePayload payload = new OcrRecognizePayload();
        payload.setImageUrl(imageUrl);
        payload.setOcrType(ocrType);
        return payload;
    }

    @Override
    public Map<String, Object> toContextMap()
    {
        Map<String, Object> context = newContext();
        putIfNotBlank(context, "imageBase64", imageBase64);
        putIfNotBlank(context, "imageData", imageBase64 != null ? imageBase64 : imageUrl);
        putIfNotBlank(context, "image", imageBase64);
        putIfNotBlank(context, "body", imageBase64);
        if (imageBase64 != null && contentType != null)
        {
            context.put("imageDataUrl", "data:" + contentType + ";base64," + imageBase64);
        }
        putIfNotBlank(context, "imageUrl", imageUrl);
        putIfNotBlank(context, "contentType", contentType);
        putIfNotBlank(context, "fileName", fileName);
        putIfNotNull(context, "fileSize", fileSize);
        putIfNotBlank(context, "ocrType", ocrType);
        putIfNotBlank(context, "ocrScene", ocrType);
        putIfNotBlank(context, "action", ocrType);
        putIfNotBlank(context, "Action", ocrType);
        putIfNotBlank(context, "reportType", reportType);
        return context;
    }

    public String getImageBase64() { return imageBase64; }
    public void setImageBase64(String imageBase64) { this.imageBase64 = imageBase64; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    public String getOcrType() { return ocrType; }
    public void setOcrType(String ocrType) { this.ocrType = ocrType; }
    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }
}
