package com.hexm.puzzle.captcha.util;

import com.jhlabs.image.ImageUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

/**
 * @author hexm
 * @date 2020/10/27 15:37
 */
public class ImageConvertUtil {

    /**
     * 将image对象转为base64字符串
     *
     * @param image
     * @return
     */
    public static String toBase64(Image image, String format) {
        return Base64.getEncoder().encodeToString(toBytes(image, format));
    }

    /**
     * 将image对象转为前端img标签识别的base64字符串
     *
     * @param image
     * @param format
     * @return
     */
    public static String toDataUri(Image image, String format) {
        return String.format("data:image/%s;base64,%s", format, toBase64(image, format));
    }

    /**
     * 将image对象转为前端img标签识别的base64字符串
     *
     * @param bytes
     * @param format
     * @return
     */
    public static String toDataUri(byte[] bytes, String format) {
        return String.format("data:image/%s;base64,%s", format, Base64.getEncoder().encodeToString(bytes));
    }

    /**
     * 将image对象转为字节
     *
     * @param image
     * @param format
     * @return
     */
    public static byte[] toBytes(Image image, String format) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            ImageIO.write(ImageUtils.convertImageToARGB(image), format, stream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stream.toByteArray();
    }
}
