package com.yanruieng.app.controller;

import com.yanruieng.app.common.ApiResponse;
import com.yanruieng.app.common.CustomException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletRequest;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/local-files")
public class CommonController {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.access-prefix}")
    private String accessPrefix;

    @Value("${file.max-size:10485760}")
    private Long maxSize;

    private static final List<String> ALLOWED_TYPES = List.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "application/pdf"
    );

    @PostMapping("/upload")
    public ApiResponse<FileUploadVO> upload(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request
    ) {
        validateFile(file);

        try {
            String originalFilename = file.getOriginalFilename();
            String suffix = getSuffix(originalFilename);

            String newFileName = UUID.randomUUID().toString().replace("-", "") + suffix;
            String datePath = LocalDate.now().toString().replace("-", "/");
            String relativePath = datePath + "/" + newFileName;

            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path targetPath = uploadPath.resolve(relativePath).normalize();

            if (!targetPath.startsWith(uploadPath)) {
                throw new CustomException("非法文件路径");
            }

            Files.createDirectories(targetPath.getParent());

            file.transferTo(targetPath.toFile());

            String fileUrl = buildFileUrl(request, relativePath);

            log.info("文件上传成功，originalName={}, fileName={}, size={}, path={}",
                    originalFilename, newFileName, file.getSize(), targetPath);

            FileUploadVO vo = new FileUploadVO();
            vo.setOriginalName(originalFilename);
            vo.setFileName(newFileName);
            vo.setUrl(fileUrl);
            vo.setRelativePath(relativePath);
            vo.setSize(file.getSize());
            vo.setContentType(file.getContentType());

            return ApiResponse.success(vo);

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new CustomException("文件上传失败");
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new CustomException("上传文件不能为空");
        }

        if (file.getSize() > maxSize) {
            throw new CustomException("文件大小超过限制");
        }

        String originalFilename = file.getOriginalFilename();
        if (!StringUtils.hasText(originalFilename)) {
            throw new CustomException("文件名不能为空");
        }

        String contentType = file.getContentType();
        if (!ALLOWED_TYPES.contains(contentType)) {
            throw new CustomException("不支持的文件类型");
        }
    }

    private String getSuffix(String filename) {
        int index = filename.lastIndexOf(".");
        if (index == -1) {
            throw new CustomException("文件缺少后缀名");
        }

        return filename.substring(index).toLowerCase();
    }

    private String buildFileUrl(HttpServletRequest request, String relativePath) {
        return request.getScheme()
                + "://"
                + request.getServerName()
                + ":"
                + request.getServerPort()
                + accessPrefix
                + "/"
                + relativePath;
    }

    @Data
    public static class FileUploadVO {
        private String originalName;
        private String fileName;
        private String url;
        private String relativePath;
        private Long size;
        private String contentType;
    }
}