package com.bigyun.provider.service;

import com.bigyun.provider.domain.ProviderCredential;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Provider凭据服务测试
 */
@SpringBootTest
@Transactional
public class ProviderCredentialServiceIT
{
    @Autowired
    private IProviderCredentialService credentialService;

    @Test
    public void testSaveAndGetCredential()
    {
        // 测试保存单个凭据
        Long configId = 999L;
        String key = "apiKey";
        String value = "sk-test-123456";

        int result = credentialService.saveCredential(configId, key, value);
        assertEquals(1, result);

        // 测试读取凭据
        String retrieved = credentialService.getCredential(configId, key);
        assertEquals(value, retrieved);

        System.out.println("✅ 测试通过：保存和读取单个凭据");
    }

    @Test
    public void testSaveAndGetCredentialsMap()
    {
        // 测试批量保存凭据
        Long configId = 998L;
        Map<String, String> credentials = new HashMap<>();
        credentials.put("accessKey", "LTAI5txxxxx");
        credentials.put("secretKey", "xxxxxxxx");
        credentials.put("endpoint", "https://oss-cn-hangzhou.aliyuncs.com");

        int result = credentialService.saveCredentials(configId, credentials);
        assertEquals(3, result);

        // 测试批量读取凭据
        Map<String, String> retrieved = credentialService.getCredentialsMap(configId);
        assertEquals(3, retrieved.size());
        assertEquals("LTAI5txxxxx", retrieved.get("accessKey"));
        assertEquals("xxxxxxxx", retrieved.get("secretKey"));
        assertEquals("https://oss-cn-hangzhou.aliyuncs.com", retrieved.get("endpoint"));

        System.out.println("✅ 测试通过：批量保存和读取凭据");
    }

    @Test
    public void testEncryption()
    {
        // 测试加密存储
        Long configId = 997L;
        String plainText = "my-secret-key-12345";

        credentialService.saveCredential(configId, "secretKey", plainText);

        // 直接查询数据库，验证是否加密
        ProviderCredential credential = credentialService.selectByConfigId(configId).get(0);
        assertNotEquals(plainText, credential.getCredentialValue());
        System.out.println("数据库中存储的加密值: " + credential.getCredentialValue());

        // 通过服务读取，验证是否正确解密
        String decrypted = credentialService.getCredential(configId, "secretKey");
        assertEquals(plainText, decrypted);

        System.out.println("✅ 测试通过：凭据加密存储和解密读取");
    }

    @Test
    public void testDeleteCredentials()
    {
        // 测试删除凭据
        Long configId = 996L;
        credentialService.saveCredential(configId, "apiKey", "test-key");

        int deleted = credentialService.deleteCredentials(configId);
        assertEquals(1, deleted);

        // 验证已删除
        String retrieved = credentialService.getCredential(configId, "apiKey");
        assertNull(retrieved);

        System.out.println("✅ 测试通过：删除凭据");
    }

    @Test
    public void testUpdateCredential()
    {
        // 测试更新凭据
        Long configId = 995L;
        String key = "apiKey";

        // 第一次保存
        credentialService.saveCredential(configId, key, "old-value");
        assertEquals("old-value", credentialService.getCredential(configId, key));

        // 更新
        credentialService.saveCredential(configId, key, "new-value");
        assertEquals("new-value", credentialService.getCredential(configId, key));

        System.out.println("✅ 测试通过：更新凭据");
    }
}
