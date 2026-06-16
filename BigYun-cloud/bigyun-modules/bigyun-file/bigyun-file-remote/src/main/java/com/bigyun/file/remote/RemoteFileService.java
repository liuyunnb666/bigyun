package com.bigyun.file.remote;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import com.bigyun.common.core.constant.SecurityConstants;
import com.bigyun.common.core.constant.ServiceNameConstants;
import com.bigyun.common.core.domain.R;
import com.bigyun.file.domain.SysFile;
import com.bigyun.file.factory.RemoteFileFallbackFactory;
import java.util.List;
import java.util.Map;

/**
 * 文件服务
 * 
 * @author bigyun
 */
@FeignClient(contextId = "remoteFileService", value = ServiceNameConstants.FILE_SERVICE, fallbackFactory = RemoteFileFallbackFactory.class)
public interface RemoteFileService
{
    /**
     * 上传文件
     *
     * @param file 文件信息
     * @return 结果
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<SysFile> upload(@RequestPart(value = "file") MultipartFile file);

    /**
     * 指定存储 provider 上传文件。
     *
     * @param file 文件
     * @param providerCode 存储 providerCode
     * @return 结果
     */
    @PostMapping(value = "/upload/provider", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<SysFile> uploadByProvider(@RequestPart(value = "file") MultipartFile file,
            @RequestParam("providerCode") String providerCode);

    /**
     * 删除文件
     *
     * @param fileUrl 文件地址
     * @return 结果
     */
    @DeleteMapping(value = "/delete", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public R<Boolean> delete(@RequestParam("fileUrl") String fileUrl);

    /**
     * 获取可供 OCR 等第三方服务访问的临时地址。
     *
     * @param fileUrl 文件地址
     * @param source 内部调用标识
     * @return 可访问地址
     */
    @GetMapping("/temporary-url")
    public R<String> getTemporaryUrl(@RequestParam("fileUrl") String fileUrl,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    @PostMapping("/temporary-url/batch")
    public R<Map<String, String>> getTemporaryUrls(@RequestBody List<String> fileUrls,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source);
}
