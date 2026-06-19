package com.yanruieng.app.controller;

import com.yanruieng.app.common.ApiResponse;
import com.yanruieng.app.service.FileService;
import com.yanruieng.app.vo.FileUploadVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/files")
public class FileController {

    private final FileService fileService;

    @PostMapping("/upload")
    public ApiResponse<FileUploadVO> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "common") String bizType
    ) throws IOException {
        return ApiResponse.success(fileService.upload(file, bizType));
    }

    @GetMapping("/{fileId}/url")
    public ApiResponse<String> getDownloadUrl(@PathVariable Long fileId) {
        return ApiResponse.success(fileService.getDownloadUrl(fileId));
    }

    @DeleteMapping("/{fileId}")
    public ApiResponse<String> delete(@PathVariable Long fileId) {
        fileService.delete(fileId);
        return ApiResponse.success("删除成功");
    }
}