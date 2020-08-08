/*
 *  Copyright (C) 2020 Future Studio
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.wlgzs.futurenovel.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import net.wlgzs.futurenovel.AppProperties;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

/**
 * 文件上传服务
 */
@Service
public class FileService {

    private final Path uploadPath;

    public FileService(AppProperties futureNovelConfig) {
        String uploadDir = futureNovelConfig.getUploadDir();
        uploadPath = FileSystems.getDefault().getPath(uploadDir).toAbsolutePath();
        File uploadPathFile = uploadPath.toFile();
        if (!uploadPathFile.exists() && !uploadPathFile.mkdirs()) throw new RuntimeException("Upload path can not access");
    }

    /**
     * 将流保存到文件
     * @param inputStream 要保存的流
     * @return 文件的索引
     * @throws SecurityException 安全检查异常
     * @throws IOException IO 异常
     */
    public String saveFile(@NonNull InputStream inputStream) throws IOException {
        Path tmpFile = Files.createTempFile("md5Temp", null);
        String md5Hex;
        try (var tmpOutput = Files.newOutputStream(tmpFile)) {
            byte[] buffer = new byte[4096];
            int read;
            try {
                var messageDigest = MessageDigest.getInstance("MD5");
                while ((read = inputStream.read(buffer)) != -1) {
                    messageDigest.update(buffer, 0, read);
                    tmpOutput.write(buffer, 0, read); // 1
                }
                tmpOutput.flush();
                md5Hex = toHex(messageDigest.digest());
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException("MD5 algorithm not found", e);
            }
            tmpOutput.flush();
        }
        var savePath = Files.createDirectories(uploadPath.resolve(md5Hex.substring(0, 2)));
        Files.move(tmpFile, savePath.resolve(md5Hex), StandardCopyOption.REPLACE_EXISTING); // 2
        return md5Hex;
    }

    /**
     * 根据文件索引读取文件
     * @param index 文件索引
     * @return 文件的输入流
     * @throws IOException IO 异常
     */
    public InputStream readFile(@NonNull String index) throws IOException {
        Path filePath = uploadPath.resolve(index.substring(0, 2)).resolve(index);
        if (!Files.exists(filePath)) throw new FileNotFoundException(index + " file not found");
        return Files.newInputStream(filePath);
    }

    @NonNull
    private String toHex(@NonNull byte[] bytes) {
        var sb = new StringBuilder();
        for (byte aByte : bytes) {
            sb.append(String.format("%02x", aByte & 0xff));
        }
        return sb.toString();
    }
    
}
