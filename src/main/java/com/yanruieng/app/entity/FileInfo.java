package com.yanruieng.app.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
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
    @TableField("original_name")
    private String originalName;
    @TableField("file_name")
    private String fileName;
    @TableField("object_key")
    private String objectKey;
    @TableField("url")
    private String url;
    @TableField("bucket_name")
    private String bucketName;
    @TableField("storage_type")
    private Integer storageType;
    @TableField("file_size")
    private Long fileSize;
    @TableField("content_type")
    private String contentType;
    @TableField("file_ext")
    private String fileExt;
    @TableField("file_md5")
    private String fileMd5;
    @TableField("status")
    private Integer status;
}