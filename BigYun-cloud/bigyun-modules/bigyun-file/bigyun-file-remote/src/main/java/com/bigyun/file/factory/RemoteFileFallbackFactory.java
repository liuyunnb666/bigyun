package com.bigyun.file.factory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import com.bigyun.common.core.domain.R;
import com.bigyun.file.remote.RemoteFileService;
import com.bigyun.file.domain.SysFile;
import java.util.List;
import java.util.Map;

/**
 * 文件服务降级处理
 * 
 * @author bigyun
 */
@Component
public class RemoteFileFallbackFactory implements FallbackFactory<RemoteFileService>
{
    private static final Logger log = LoggerFactory.getLogger(RemoteFileFallbackFactory.class);

    @Override
    public RemoteFileService create(Throwable throwable)
    {
        log.error("文件服务调用失败:{}", throwable.getMessage());
        return new RemoteFileService()
        {
            @Override
            public R<SysFile> upload(MultipartFile file)
            {
                return R.fail("上传文件失败:" + throwable.getMessage());
            }

            @Override
            public R<SysFile> uploadByProvider(MultipartFile file, String providerCode)
            {
                return R.fail("上传文件失败:" + throwable.getMessage());
            }

            @Override
            public R<Boolean> delete(String fileUrl)
            {
                return R.fail("删除文件失败:" + throwable.getMessage());
            }
            @Override
            public R<String> getTemporaryUrl(String fileUrl, String source)
            {
                return R.fail("生成文件临时访问地址失败:" + throwable.getMessage());
            }
            @Override
            public R<Map<String, String>> getTemporaryUrls(List<String> fileUrls, String source)
            {
                return R.fail("temporary url batch failed:" + throwable.getMessage());
            }
        };
    }
}
