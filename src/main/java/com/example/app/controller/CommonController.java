package com.example.app.controller;

import com.example.app.common.ApiResponse;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

/**
 * 文件上传与下载
 */
@Slf4j
@RestController
@RequestMapping("/common")
public class CommonController {
    @Value("${prompt.path}")
    private String basePath;
    public static String UPLOAD_DIRECTORY = System.getProperty("user.dir");
    @PostMapping("/upload")
    public ApiResponse<String> upload(MultipartFile file, HttpServletRequest req) throws IOException {
        //file是一个临时文件,需要转存到指定位置，否则本次请求完成后临时文件会删除
        log.info("file:{}", file);
        //使用UUID重新生成文件名，防止文件名称重复造成文件覆盖
//        file.transferTo(new File("/Users/yanruifeng/Desktop/my-react-app/1.jpg"));
        //创建一个目录对象
        String originFilename = file.getOriginalFilename();
        String suffix=originFilename.substring(originFilename.lastIndexOf("."));
        String newFileName=  UUID.randomUUID()+suffix;
        String filePath = req.getScheme() + "://" + req.getServerName() + ":"
                + req.getServerPort() + "/uploads/"+newFileName;

        file.transferTo(new File(UPLOAD_DIRECTORY+basePath+newFileName));
        return ApiResponse.success(filePath);
    }
    @DeleteMapping("/deleteFile")
    public ApiResponse<String> upload(String imageUrl) {
        try {
             URL  url = new URL(imageUrl);
            String path = url.getPath();
            // 通常文件名位于路径的最后一部分
            int lastIndex = path.lastIndexOf('/');
            if (lastIndex != -1) {
                String fileName = path.substring(lastIndex + 1);
                File file = new File(UPLOAD_DIRECTORY +basePath+ fileName);
                if (file.exists()) {
                    boolean isDeleted = file.delete();
                    if (isDeleted) {
                        return ApiResponse.success("文件删除成功！");
                    } else {
                        return ApiResponse.failed("文件删除失败，请检查权限或文件状态");
                    }
                } else {
                    return ApiResponse.failed("要删除的文件不存在");
                }
            } else {
                return ApiResponse.failed("无法从URL中提取文件名");
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return ApiResponse.failed("url解析失败");

        }
    }
    @GetMapping("/download")
    public void downloadFile(String name, HttpServletResponse response){
        try {
            //通过输入流读取文件内容
            FileInputStream fileInputStream = new FileInputStream(new File(UPLOAD_DIRECTORY+basePath+name));
            //输出流，通过输出流将文件写回浏览器，在浏览器展示图片
            ServletOutputStream outputStream = response.getOutputStream();
            response.setContentType("image/jpeg");
            int len;
            byte[] bytes = new byte[1024];
            while ((len=fileInputStream.read(bytes))!=-1){
                    outputStream.write(bytes,0,len);
                    outputStream.flush();
            }
            //关闭资源
            outputStream.close();
            fileInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


//前端图片展示 文件上传后 后端直接返回阿里云OSS图片地址，后端自己拼接访问图片，前端请求文件下载的响应输出流，
