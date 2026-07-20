package com.minzheng.blog.service.impl;

import com.minzheng.blog.dto.GameImageSourceDTO;
import com.minzheng.blog.enums.FilePathEnum;
import com.minzheng.blog.exception.BizException;
import com.minzheng.blog.service.GameImageService;
import com.minzheng.blog.vo.GameImageSourceVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

/**
 * 游戏图片服务
 */
@Service
public class GameImageServiceImpl implements GameImageService {

    private static final int MAX_IMAGE_BYTES = 20 * 1024 * 1024;

    @Value("${upload.local.url:}")
    private String localUrl;

    @Value("${upload.local.path:}")
    private String localPath;

    @Value("${upload.oss.url:}")
    private String ossUrl;

    @Value("${upload.cos.url:}")
    private String cosUrl;

    @Override
    public GameImageSourceDTO getImageSource(GameImageSourceVO sourceVO) {
        if (sourceVO == null || !StringUtils.hasText(sourceVO.getUrl())) {
            throw new BizException("图片地址不能为空");
        }
        String imageUrl = sourceVO.getUrl().trim();
        URI imageUri = parseUri(imageUrl);
        String matchedBaseUrl = findMatchedBaseUrl(imageUri);
        if (!StringUtils.hasText(matchedBaseUrl)) {
            throw new BizException("只允许编辑游戏上传目录中的图片");
        }
        byte[] bytes = matchesBase(imageUri, localUrl)
                ? readLocalImage(imageUri, localUrl) : readRemoteImage(imageUrl);
        String contentType = resolveContentType(bytes);
        BufferedImage image = readImage(bytes);
        return GameImageSourceDTO.builder()
                .dataUrl("data:" + contentType + ";base64," + Base64.getEncoder().encodeToString(bytes))
                .contentType(contentType)
                .width(image.getWidth())
                .height(image.getHeight())
                .build();
    }

    private URI parseUri(String value) {
        try {
            URI uri = URI.create(value);
            if (!("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))
                    || !StringUtils.hasText(uri.getHost())) {
                throw new BizException("图片地址格式不正确");
            }
            return uri;
        } catch (IllegalArgumentException e) {
            throw new BizException("图片地址格式不正确");
        }
    }

    private String findMatchedBaseUrl(URI imageUri) {
        List<String> baseUrlList = Arrays.asList(localUrl, ossUrl, cosUrl);
        return baseUrlList.stream()
                .filter(StringUtils::hasText)
                .filter(item -> matchesBase(imageUri, item))
                .findFirst()
                .orElse(null);
    }

    private boolean matchesBase(URI imageUri, String baseUrl) {
        if (!StringUtils.hasText(baseUrl)) {
            return false;
        }
        try {
            URI baseUri = URI.create(appendSlash(baseUrl));
            if (!StringUtils.hasText(baseUri.getScheme()) || !StringUtils.hasText(baseUri.getHost())) {
                return false;
            }
            String gamePath = appendSlash(baseUri.getPath()) + FilePathEnum.GAME.getPath();
            return baseUri.getScheme().equalsIgnoreCase(imageUri.getScheme())
                    && baseUri.getHost().equalsIgnoreCase(imageUri.getHost())
                    && normalizePort(baseUri) == normalizePort(imageUri)
                    && imageUri.getPath().startsWith(gamePath);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private int normalizePort(URI uri) {
        if (uri.getPort() >= 0) {
            return uri.getPort();
        }
        return "https".equalsIgnoreCase(uri.getScheme()) ? 443 : 80;
    }

    private byte[] readLocalImage(URI imageUri, String baseUrl) {
        if (!StringUtils.hasText(localPath)) {
            throw new BizException("本地图片目录未配置");
        }
        try {
            URI baseUri = URI.create(appendSlash(baseUrl));
            String relativePath = imageUri.getPath().substring(appendSlash(baseUri.getPath()).length());
            Path root = Paths.get(localPath).toAbsolutePath().normalize();
            Path filePath = root.resolve(relativePath).normalize();
            if (!filePath.startsWith(root)) {
                throw new BizException("图片地址不在允许目录中");
            }
            File file = filePath.toFile();
            if (!file.isFile() || file.length() > MAX_IMAGE_BYTES) {
                throw new BizException("图片不存在或超过20MB");
            }
            try (InputStream inputStream = new FileInputStream(file)) {
                return readLimited(inputStream);
            }
        } catch (IOException e) {
            throw new BizException("读取游戏图片失败");
        }
    }

    private byte[] readRemoteImage(String imageUrl) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(imageUrl).openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(10000);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            if (responseCode < 200 || responseCode >= 300) {
                throw new BizException("读取游戏图片失败");
            }
            if (connection.getContentLengthLong() > MAX_IMAGE_BYTES) {
                throw new BizException("图片超过20MB");
            }
            try (InputStream inputStream = connection.getInputStream()) {
                return readLimited(inputStream);
            }
        } catch (IOException e) {
            throw new BizException("读取游戏图片失败");
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private byte[] readLimited(InputStream inputStream) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int total = 0;
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            total += length;
            if (total > MAX_IMAGE_BYTES) {
                throw new BizException("图片超过20MB");
            }
            outputStream.write(buffer, 0, length);
        }
        return outputStream.toByteArray();
    }

    private BufferedImage readImage(byte[] bytes) {
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
            if (image == null) {
                throw new BizException("文件不是有效图片");
            }
            return image;
        } catch (IOException e) {
            throw new BizException("文件不是有效图片");
        }
    }

    private String resolveContentType(byte[] bytes) {
        try {
            String contentType = URLConnection.guessContentTypeFromStream(new ByteArrayInputStream(bytes));
            if ("image/png".equals(contentType) || "image/jpeg".equals(contentType)) {
                return contentType;
            }
        } catch (IOException ignored) {
        }
        throw new BizException("仅支持PNG或JPEG图片");
    }

    private String appendSlash(String value) {
        return value.endsWith("/") ? value : value + "/";
    }
}
