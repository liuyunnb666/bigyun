package com.bigyun.auth.service;

import com.bigyun.common.core.constant.SecurityConstants;
import com.bigyun.common.core.domain.R;
import com.bigyun.common.core.exception.ServiceException;
import com.bigyun.common.core.utils.StringUtils;
import com.bigyun.config.constant.ProviderConfigConstants;
import com.bigyun.file.domain.SysFile;
import com.bigyun.file.remote.RemoteFileService;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FaceMediaStorageService
{
    private static final Logger log = LoggerFactory.getLogger(FaceMediaStorageService.class);

    private static final int MAX_IMAGE_BYTES = 5 * 1024 * 1024;

    private static final String DEFAULT_IMAGE_CONTENT_TYPE = "image/jpeg";

    @Autowired
    private RemoteFileService remoteFileService;

    public SysFile uploadAliyunFaceImage(String imageBase64, String filenamePrefix)
    {
        DecodedImage image = decodeImage(imageBase64, filenamePrefix);
        R<SysFile> result = remoteFileService.uploadByProvider(
                new ByteArrayMultipartFile(image.filename, image.contentType, image.bytes),
                ProviderConfigConstants.PROVIDER_ALIYUN_OSS);
        if (result == null || R.isError(result) || result.getData() == null)
        {
            throw new ServiceException(result == null ? "人脸图片上传 OSS 失败" : result.getMsg());
        }
        SysFile file = result.getData();
        if (StringUtils.isBlank(file.getObjectKey()))
        {
            throw new ServiceException("人脸图片上传成功但未返回对象 Key");
        }
        return file;
    }

    public String getAccessibleUrl(String fileUrl)
    {
        if (StringUtils.isBlank(fileUrl))
        {
            throw new ServiceException("人脸图片地址不能为空");
        }
        R<String> result = remoteFileService.getTemporaryUrl(fileUrl, SecurityConstants.INNER);
        if (result != null && !R.isError(result) && StringUtils.isNotBlank(result.getData()))
        {
            return result.getData();
        }
        if (fileUrl.startsWith("http://") || fileUrl.startsWith("https://"))
        {
            return fileUrl;
        }
        throw new ServiceException(result == null ? "生成人脸图片临时访问地址失败" : result.getMsg());
    }

    public void deleteQuietly(String fileUrl)
    {
        if (StringUtils.isBlank(fileUrl))
        {
            return;
        }
        try
        {
            remoteFileService.delete(fileUrl);
        }
        catch (Exception e)
        {
            log.warn("Delete temporary face image failed", e);
        }
    }

    private DecodedImage decodeImage(String imageBase64, String filenamePrefix)
    {
        if (StringUtils.isBlank(imageBase64))
        {
            throw new ServiceException("人脸图片不能为空");
        }
        String value = imageBase64.trim();
        String contentType = DEFAULT_IMAGE_CONTENT_TYPE;
        if (value.startsWith("data:"))
        {
            int commaIndex = value.indexOf(',');
            int semicolonIndex = value.indexOf(';');
            if (commaIndex < 0 || semicolonIndex < 0 || semicolonIndex > commaIndex)
            {
                throw new ServiceException("人脸图片格式不正确");
            }
            contentType = value.substring("data:".length(), semicolonIndex);
            value = value.substring(commaIndex + 1);
        }
        value = value.replaceAll("\\s+", "");
        byte[] bytes;
        try
        {
            bytes = Base64.getDecoder().decode(value);
        }
        catch (IllegalArgumentException e)
        {
            throw new ServiceException("人脸图片 Base64 解码失败");
        }
        if (bytes.length <= 0 || bytes.length > MAX_IMAGE_BYTES)
        {
            throw new ServiceException("人脸图片大小不合法");
        }
        String prefix = StringUtils.defaultIfBlank(filenamePrefix, "face-image").replaceAll("[^A-Za-z0-9_-]", "-");
        return new DecodedImage(bytes, contentType, prefix + "-" + System.currentTimeMillis() + "." + extension(contentType));
    }

    private String extension(String contentType)
    {
        if ("image/png".equalsIgnoreCase(contentType))
        {
            return "png";
        }
        if ("image/webp".equalsIgnoreCase(contentType))
        {
            return "webp";
        }
        return "jpg";
    }

    private static final class DecodedImage
    {
        private final byte[] bytes;

        private final String contentType;

        private final String filename;

        private DecodedImage(byte[] bytes, String contentType, String filename)
        {
            this.bytes = bytes;
            this.contentType = contentType;
            this.filename = filename;
        }
    }

    private static final class ByteArrayMultipartFile implements MultipartFile
    {
        private final String filename;

        private final String contentType;

        private final byte[] bytes;

        private ByteArrayMultipartFile(String filename, String contentType, byte[] bytes)
        {
            this.filename = filename;
            this.contentType = contentType;
            this.bytes = bytes;
        }

        @Override
        public String getName()
        {
            return "file";
        }

        @Override
        public String getOriginalFilename()
        {
            return filename;
        }

        @Override
        public String getContentType()
        {
            return contentType;
        }

        @Override
        public boolean isEmpty()
        {
            return bytes.length == 0;
        }

        @Override
        public long getSize()
        {
            return bytes.length;
        }

        @Override
        public byte[] getBytes()
        {
            return bytes.clone();
        }

        @Override
        public InputStream getInputStream()
        {
            return new ByteArrayInputStream(bytes);
        }

        @Override
        public void transferTo(File dest) throws IOException
        {
            Files.write(dest.toPath(), bytes);
        }
    }
}
