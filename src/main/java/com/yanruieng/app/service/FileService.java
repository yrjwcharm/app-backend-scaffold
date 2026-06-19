package com.yanruieng.app.service;

import com.yanruieng.app.vo.FileUploadVO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileService {

    FileUploadVO upload(MultipartFile file, String bizType) throws IOException;

    String getDownloadUrl(Long fileId);

    void delete(Long fileId);
}