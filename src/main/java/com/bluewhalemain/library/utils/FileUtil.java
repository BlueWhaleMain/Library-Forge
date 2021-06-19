package com.bluewhalemain.library.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * 文件工具类
 *
 * @author BlueWhaleMain
 * @since 2021/6/19
 */
public class FileUtil {
    /**
     * 从输入流中读取字符串
     *
     * @param inputStream 输入流
     * @return 字符串
     * @throws IOException 流异常
     */
    public static String readString(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }
    }
}
