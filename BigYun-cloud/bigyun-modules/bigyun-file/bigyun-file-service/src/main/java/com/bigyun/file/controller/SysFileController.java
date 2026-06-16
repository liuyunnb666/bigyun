package com.bigyun.file.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.multipart.MultipartFile;
import com.bigyun.common.core.domain.R;
import com.bigyun.common.core.exception.file.FileException;
import com.bigyun.common.core.exception.file.FileUploadException;
import com.bigyun.common.core.utils.StringUtils;
import com.bigyun.common.security.annotation.InnerAuth;
import com.bigyun.file.service.ISysFileService;
import com.bigyun.file.domain.SysFile;

import java.net.URLConnection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 文件请求处理
 * 
 * @author bigyun
 */
@RestController
public class SysFileController
{
    private static final Logger log = LoggerFactory.getLogger(SysFileController.class);

    private static final String PROFILE_PATTERN = "/profile/**";

    private static final String PUBLIC_PATTERN = "/public/**";

    @Autowired
    private ISysFileService sysFileService;

    /**
     * 文件上传请求
     */
    @PostMapping(value = "upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<SysFile> upload(@RequestPart("file") MultipartFile file)
    {
        try
        {
            return R.ok(sysFileService.uploadFile(file));
        }
        catch (FileException e)
        {
            log.warn("upload file rejected: {}", e.getDefaultMessage());
            return R.fail(e.getDefaultMessage());
        }
        catch (FileUploadException e)
        {
            log.warn("upload file rejected: {}", e.getMessage());
            return R.fail("File type is not allowed");
        }
        catch (Exception e)
        {
            log.error("上传文件失败", e);
            return R.fail("File upload failed");
        }
    }

    /**
     * 文件删除请求
     */
    @DeleteMapping("delete")
    public R<Boolean> delete(String fileUrl)
    {
        try
        {
            if (StringUtils.isBlank(fileUrl))
            {
                return R.fail("File url must not be blank");
            }
            sysFileService.deleteFile(fileUrl);
            return R.ok();
        }
        catch (Exception e)
        {
            log.error("删除文件失败", e);
            return R.fail("File delete failed");
        }
    }

    @InnerAuth
    @GetMapping("/temporary-url")
    public R<String> temporaryUrl(@RequestParam("fileUrl") String fileUrl)
    {
        try
        {
            if (StringUtils.isBlank(fileUrl))
            {
                return R.fail("File url must not be blank");
            }
            return R.ok(sysFileService.getAccessibleUrl(fileUrl));
        }
        catch (Exception e)
        {
            log.error("生成文件临时访问地址失败", e);
            return R.fail("Temporary url generation failed");
        }
    }

    @InnerAuth
    @PostMapping("/temporary-url/batch")
    public R<Map<String, String>> temporaryUrls(@RequestBody(required = false) List<String> fileUrls)
    {
        try
        {
            return R.ok(sysFileService.getAccessibleUrls(fileUrls));
        }
        catch (Exception e)
        {
            log.error("temporary url batch failed", e);
            return R.fail("Temporary url generation failed");
        }
    }

    @PostMapping(value = "upload/provider", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<SysFile> uploadByProvider(@RequestPart("file") MultipartFile file,
            @RequestParam("providerCode") String providerCode)
    {
        try
        {
            return R.ok(sysFileService.uploadFile(file, providerCode));
        }
        catch (FileException e)
        {
            log.warn("provider upload file rejected: {}", e.getDefaultMessage());
            return R.fail(e.getDefaultMessage());
        }
        catch (FileUploadException e)
        {
            log.warn("provider upload file rejected: {}", e.getMessage());
            return R.fail("File type is not allowed");
        }
        catch (Exception e)
        {
            log.error("指定存储上传文件失败", e);
            return R.fail("File upload failed");
        }
    }

    /**
     * 公开文件访问请求
     */
    @GetMapping({"/profile/**", "/public/**"})
    public ResponseEntity<Resource> publicFile(HttpServletRequest request) throws Exception
    {
        String objectKey = extractObjectKey(request);
        Resource resource = sysFileService.loadPublicFile(objectKey);
        return ResponseEntity.ok()
                .contentType(resolveMediaType(objectKey))
                .cacheControl(CacheControl.maxAge(7, TimeUnit.DAYS).cachePublic())
                .body(resource);
    }

    private String extractObjectKey(HttpServletRequest request)
    {
        String pattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        if (pattern == null)
        {
            pattern = path != null && path.startsWith("/public/") ? PUBLIC_PATTERN : PROFILE_PATTERN;
        }
        return new AntPathMatcher().extractPathWithinPattern(pattern, path);
    }

    private MediaType resolveMediaType(String objectKey)
    {
        String contentType = URLConnection.guessContentTypeFromName(objectKey);
        return contentType == null ? MediaType.APPLICATION_OCTET_STREAM : MediaType.parseMediaType(contentType);
    }
}
