package com.yanruieng.app.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("file_info")
public class FileInfo extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String originalName;
    private String fileName;
    private String objectKey;
    private String url;
    private String bucketName;
    private Long fileSize;
    private String contentType;
    private String fileExt;
    private String bizType;
    private Integer storageType;
    private Integer status;
}