package com.bigyun.file.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import com.bigyun.file.domain.SysFile;
import java.util.List;
import java.util.Map;

/**
 * 文件上传接口
 * 
 * @author bigyun
 */
public interface ISysFileService
{
    /**
     * 文件上传接口
     * 
     * @param file 上传的文件
     * @return 访问地址
     * @throws Exception
     */
    public SysFile uploadFile(MultipartFile file) throws Exception;

    /**
     * 指定存储 provider 上传文件。
     *
     * @param file 上传的文件
     * @param providerCode 存储 providerCode
     * @return 文件信息
     * @throws Exception 上传异常
     */
    public SysFile uploadFile(MultipartFile file, String providerCode) throws Exception;

    /**
     * 文件删除接口
     * 
     * @param fileUrl 文件访问URL
     * @throws Exception
     */
    public void deleteFile(String fileUrl) throws Exception;

    /**
     * 获取可供第三方服务直接访问的文件地址。
     *
     * @param fileUrl 文件访问URL
     * @return 可访问URL
     * @throws Exception
     */
    public String getAccessibleUrl(String fileUrl) throws Exception;

    public Map<String, String> getAccessibleUrls(List<String> fileUrls) throws Exception;

    /**
     * 读取公开文件
     *
     * @param objectKey 文件对象键
     * @return 文件资源
     * @throws Exception
     */
    public Resource loadPublicFile(String objectKey) throws Exception;
}
