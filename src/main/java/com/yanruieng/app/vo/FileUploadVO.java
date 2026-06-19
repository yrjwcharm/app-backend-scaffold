package com.yanruieng.app.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FileUploadVO {

    private Long fileId;

    private String originalName;

    private String fileName;

    private String url;

    private Long size;

    private String contentType;
}