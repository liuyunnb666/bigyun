package com.bigyun.common.core.utils;

import com.bigyun.common.core.exception.ServiceException;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

/**
 * 通用二维码工具。
 *
 * @author bigyun
 */
public final class QrCodeUtils
{
    private static final String PNG_DATA_URI_PREFIX = "data:image/png;base64,";
    private static final int DEFAULT_SIZE = 260;

    private QrCodeUtils()
    {
    }

    public static String toPngDataUri(String content, int size)
    {
        return toPngDataUri(content, size, "生成二维码失败");
    }

    public static String toPngDataUri(String content, int size, String errorMessage)
    {
        if (StringUtils.isBlank(content))
        {
            throw new ServiceException("二维码内容不能为空");
        }
        try
        {
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            hints.put(EncodeHintType.MARGIN, 1);
            int qrSize = size > 0 ? size : DEFAULT_SIZE;
            BitMatrix bitMatrix = new MultiFormatWriter()
                    .encode(content, BarcodeFormat.QR_CODE, qrSize, qrSize, hints);
            BufferedImage image = MatrixToImageWriter.toBufferedImage(bitMatrix);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ImageIO.write(image, "png", output);
            return PNG_DATA_URI_PREFIX + Base64.getEncoder().encodeToString(output.toByteArray());
        }
        catch (Exception ex)
        {
            throw new ServiceException(StringUtils.isBlank(errorMessage) ? "生成二维码失败" : errorMessage);
        }
    }
}
