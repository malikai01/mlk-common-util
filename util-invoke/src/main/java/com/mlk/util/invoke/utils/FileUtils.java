package com.mlk.util.invoke.utils;

import com.mlk.util.invoke.model.FileTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

/**
 * @author malikai
 * @date 2021年06月15日 14:51
 */
@Slf4j
public class FileUtils {

    /**
     * 获取图片文件实际类型,若不是图片则返回null
     *
     * @param file
     * @return java.lang.String
     * @author malikai
     * @date 2021-6-15 14:55
     */
    public static String getImageFileType(File file) {
        if (isImage(file)) {
            try {
                ImageInputStream iis = ImageIO.createImageInputStream(file);
                Iterator<ImageReader> iter = ImageIO.getImageReaders(iis);
                if (!iter.hasNext()) {
                    return null;
                }
                ImageReader reader = iter.next();
                iis.close();
                return reader.getFormatName();
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    /**
     * 获取文件类型,包括图片,若格式不是已配置的,则返回null
     *
     * @param file
     * @return java.lang.String
     * @author malikai
     * @date 2021-6-15 14:55
     */
    public static String getFileByFile(File file) {
        String filetype = null;
        byte[] b = new byte[50];
        try {
            InputStream is = new FileInputStream(file);
            is.read(b);
            filetype = getFileTypeByStream(b);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return filetype;
    }

    /**
     * 通过数据流（二进制数据）判断文件类型
     *
     * @param b
     * @return java.lang.String
     * @author malikai
     * @date 2021-6-15 14:55
     */
    public static String getFileTypeByStream(byte[] b) {
        String magicNumberCode = String.valueOf(getFileHexString(b));

        if (StringUtils.isBlank(magicNumberCode)) {
            return FileTypeEnum.getByMagicNumberCode(magicNumberCode.toUpperCase()).getFileTypeName();

        }
        return FileTypeEnum.NOT_EXITS_ENUM.getFileTypeName();
    }

    /**
     * isImage,判断文件是否为图片
     *
     * @param file
     * @return boolean
     * @author malikai
     * @date 2021-6-15 14:56
     */
    public static boolean isImage(File file) {
        boolean flag = false;
        try {
            BufferedImage bufreader = ImageIO.read(file);
            int width = bufreader.getWidth();
            int height = bufreader.getHeight();
            flag = width != 0 && height != 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }


    /**
     * 通过文件路径判断文件类型
     *
     * @param path
     * @return com.hujiang.dict.FileTypeEnum
     * @author malikai
     * @date 2021-6-15 14:56
     */
    public static FileTypeEnum getFileTypeByPath(String path) {
        // 获取文件头
        String magicNumberCode = null;
        try {
            magicNumberCode = getFileHeader(path);
        } catch (Exception e) {
            e.printStackTrace();
            return FileTypeEnum.NOT_EXITS_ENUM;
        }

        if (StringUtils.isNotBlank(magicNumberCode)) {
            return FileTypeEnum.getByMagicNumberCode(magicNumberCode.toUpperCase());

        }

        return FileTypeEnum.NOT_EXITS_ENUM;
    }


    /**
     * 通过文件路径获取文件头（即文件魔数）
     *
     * @param path
     * @return java.lang.String
     * @author malikai
     * @date 2021-6-15 14:57
     */
    public static String getFileHeader(String path) throws Exception {
        byte[] b = new byte[16];
        InputStream inputStream = null;

        try {
            inputStream = new FileInputStream(path);
            inputStream.read(b, 0, 16);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }

        return getFileHexString(b);
    }

    /**
     * 把文件二进制流转换成十六进制数据
     *
     * @param b
     * @return java.lang.String
     * @author malikai
     * @date 2021-6-15 14:57
     */
    public static String getFileHexString(byte[] b) {
        StringBuilder builder = new StringBuilder();
        if (b == null || b.length <= 0) {
            return null;
        }

        for (byte aB : b) {
            int v = aB & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                builder.append(0);
            }
            builder.append(hv);
        }
        return builder.toString();
    }

    /**
     * 将16进制转换为二进制
     *
     * @param hexStr
     * @return byte[]
     * @author malikai
     * @date 2021-6-15 14:57
     */
    public static byte[] parseHexStr2Byte(String hexStr) {
        if (hexStr.length() < 1)
            return null;
        byte[] result = new byte[hexStr.length() / 2];
        for (int i = 0; i < hexStr.length() / 2; i++) {
            int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
            int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2),
                    16);
            result[i] = (byte) (high * 16 + low);
        }
        return result;
    }

    /**
     * 替换filePath文件的头字节
     * 根据传入的header长度替换相应长度字节
     *
     * @param header
     * @param filePath
     * @author malikai
     * @date 2021-6-15 14:52
     */
    public static void modifyFileHeader(byte[] header, String filePath) {
        try (RandomAccessFile src = new RandomAccessFile(filePath, "rw")) {
            int srcLength = (int) src.length();
            src.skipBytes(header.length);
            byte[] buff = new byte[srcLength - header.length];
            src.read(buff);
            src.seek(0);
            src.write(header);
            src.seek(header.length);
            src.write(buff);

        } catch (Exception e) {
            log.error("修改文件{}的前两个字节失败!", filePath);
        }
    }

    /**
     * 字符串转二进制
     *
     * @param str
     * @return byte[]
     * @author malikai
     * @date 2021-6-16 10:56
     */
    public static byte[] parseStr2Byte(String str) {
        return str.getBytes(StandardCharsets.UTF_8);
    }
}
