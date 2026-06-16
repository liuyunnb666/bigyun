package com.bigyun.provider.util;

import com.bigyun.common.core.exception.ServiceException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ProviderSecretUtils
{
    private static final String ALGORITHM = "AES";

    @Value("${bigyun.provider.secret:bigyun-provider-secret}")
    private String secret;

    public String encrypt(String plainText)
    {
        if (plainText == null)
        {
            return null;
        }
        try
        {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, buildSecretKey());
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        }
        catch (Exception e)
        {
            throw new ServiceException("加密 Provider 密钥失败");
        }
    }

    public String decrypt(String cipherText)
    {
        if (cipherText == null)
        {
            return null;
        }
        try
        {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, buildSecretKey());
            byte[] encrypted = Base64.getDecoder().decode(cipherText);
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        }
        catch (Exception e)
        {
            throw new ServiceException("解密 Provider 密钥失败");
        }
    }

    private SecretKeySpec buildSecretKey() throws Exception
    {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] bytes = digest.digest(secret.getBytes(StandardCharsets.UTF_8));
        byte[] key = new byte[16];
        System.arraycopy(bytes, 0, key, 0, key.length);
        return new SecretKeySpec(key, ALGORITHM);
    }
}
