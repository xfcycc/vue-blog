package com.minzheng.blog.datamap.impl;

import com.github.houbb.heaven.response.exception.CommonRuntimeException;
import com.github.houbb.heaven.util.io.StreamUtil;
import com.github.houbb.heaven.util.lang.StringUtil;
import com.minzheng.blog.datamap.JingyongDataMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author caiguoyu
 * @date 2022/10/5
 */
@Service
@Slf4j
public class JingyongDataMapDefault implements JingyongDataMap {

    private static final Set<String> JINGYONG_SET;

    private static final String[] JINGYONG_ARRAY;

    private static final String PATH = "classpath:dictionary/jinyong.txt";

    static {
        JINGYONG_SET = new HashSet<>();
        InputStream inputStream = getInputStream(PATH);
        List<String> allLines = readAllLines(inputStream);
        JINGYONG_SET.addAll(allLines);
        JINGYONG_ARRAY = JINGYONG_SET.toArray(new String[0]);
    }

    private static InputStream getInputStream(final String filePath) {
        InputStream inputStream;
        try {
            inputStream = new URL(filePath).openStream();
        } catch (MalformedURLException localMalformedUrlException) {
            try {
                inputStream = new FileInputStream(filePath);
            } catch (Exception localException2) {
                ClassLoader localClassLoader = Thread.currentThread().getContextClassLoader();
                if (localClassLoader == null) {
                    localClassLoader = StreamUtil.class.getClassLoader();
                }
                inputStream = localClassLoader.getResourceAsStream(filePath);
                if (inputStream == null) {
                    throw new CommonRuntimeException("找不到文件: " + filePath);
                }
            }
        } catch (IOException localIOException1) {
            throw new CommonRuntimeException(localIOException1);
        }

        return inputStream;
    }

    private static List<String> readAllLines(InputStream is) {
        try {
            List<String> lines = new ArrayList<>();
            BufferedReader e = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            while (e.ready()) {
                String entry = e.readLine();
                if (StringUtil.isEmpty(entry)) {
                    continue;
                }
                lines.add(entry);
            }
            return lines;
        } catch (IOException e) {
            log.error("读取文件错误", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public Set<String> defaultSet() {
        return JINGYONG_SET;
    }

    @Override
    public String[] defaultArray() {
        return JINGYONG_ARRAY;
    }
}
