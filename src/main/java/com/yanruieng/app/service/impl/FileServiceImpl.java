package com.yanruieng.app.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.ObjectMetadata;
import com.yanruieng.app.common.CustomException;
import com.yanruieng.app.entity.FileInfo;
import com.yanruieng.app.mapper.FileInfoMapper;
import com.yanruieng.app.properties.OssProperties;
import com.yanruieng.app.service.FileService;
import com.yanruieng.app.vo.FileUploadVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final OSS ossClient;
    private final OssProperties ossProperties;
    private final FileInfoMapper fileInfoMapper;

    @Override
    public FileUploadVO upload(MultipartFile file, String bizType) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new CustomException("上传文件不能为空");
        }

        if (ossProperties.getMaxSize() != null && file.getSize() > ossProperties.getMaxSize()) {
            throw new CustomException("文件大小超过限制");
        }

        String originalFilename = file.getOriginalFilename();
        if (!StringUtils.hasText(originalFilename)) {
            throw new CustomException("文件名不能为空");
        }

        String contentType = file.getContentType();
        if (ossProperties.getAllowedTypes() != null
                && !ossProperties.getAllowedTypes().contains(contentType)) {
            throw new CustomException("不支持的文件类型：" + contentType);
        }

        String ext = getFileExt(originalFilename);
        String fileName = UUID.randomUUID().toString().replace("-", "") + ext;

        String datePath = LocalDate.now().toString().replace("-", "/");

        String objectKey = ossProperties.getDir()
                + "/"
                + bizType
                + "/"
                + datePath
                + "/"
                + fileName;

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(contentType);

        ossClient.putObject(
                ossProperties.getBucketName(),
                objectKey,
                file.getInputStream(),
                metadata
        );

        String url = ossProperties.getDomain() + "/" + objectKey;

        FileInfo fileInfo = new FileInfo();
        fileInfo.setOriginalName(originalFilename);
        fileInfo.setFileName(fileName);
        fileInfo.setObjectKey(objectKey);
        fileInfo.setUrl(url);
        fileInfo.setBucketName(ossProperties.getBucketName());
        fileInfo.setFileSize(file.getSize());
        fileInfo.setContentType(contentType);
        fileInfo.setFileExt(ext);
        fileInfo.setStorageType(1);
        fileInfo.setStatus(1);

        fileInfoMapper.insert(fileInfo);

        return new FileUploadVO(
                fileInfo.getId(),
                originalFilename,
                fileName,
                url,
                file.getSize(),
                contentType
        );
    }

    @Override
    public String getDownloadUrl(Long fileId) {
        FileInfo fileInfo = fileInfoMapper.selectById(fileId);
        if (fileInfo == null || fileInfo.getStatus() == 0) {
            throw new CustomException("文件不存在");
        }

        return fileInfo.getUrl();
    }

    @Override
    public void delete(Long fileId) {
        FileInfo fileInfo = fileInfoMapper.selectById(fileId);
        if (fileInfo == null || fileInfo.getStatus() == 0) {
            throw new CustomException("文件不存在");
        }

        ossClient.deleteObject(
                fileInfo.getBucketName(),
                fileInfo.getObjectKey()
        );

        fileInfo.setStatus(0);
        fileInfoMapper.updateById(fileInfo);
    }

    private String getFileExt(String originalFilename) {
        int index = originalFilename.lastIndexOf(".");
        if (index == -1) {
            throw new CustomException("文件缺少后缀名");
        }
        return originalFilename.substring(index).toLowerCase();
    }
}
