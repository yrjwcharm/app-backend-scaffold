package com.example.app;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
import com.baomidou.mybatisplus.generator.fill.Column;


import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 代码生成器
 *
 * @author: yanruifeng
 * @since: 2023/12/30 15:28
 */
public class CodeGenerator {
    private static final  String packageName ="com.example.app";
    private static final String URL = "jdbc:mysql://localhost:3306/prompt"
            + "?useUnicode=true&characterEncoding=utf8"
            + "&serverTimezone=Asia/Shanghai"
            + "&useSSL=false";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "12345678";
    public static void main(String[] args) {
        // 数据源配置
        FastAutoGenerator.create(CodeGenerator.URL, CodeGenerator.USERNAME, CodeGenerator.PASSWORD)
                .globalConfig(builder -> {
                    builder.author("yanruifeng")        // 设置作者
                            .enableSpringdoc()
                            .disableOpenDir()      // 禁止打开输出目录 默认值:true
                            .commentDate("yyyy-MM-dd") // 注释日期
                            .dateType(DateType.TIME_PACK)   //定义生成的实体类中日期类型 DateType.ONLY_DATE 默认值: DateType.TIME_PACK
                            .outputDir(System.getProperty("user.dir") + "/src/main/java"); // 指定输出目录
                })

                .packageConfig(builder -> {
                    builder.parent(CodeGenerator.packageName) // 父包模块名
                            .controller("controller")   //Controller 包名 默认值:controller
                            .entity("entity")           //Entity 包名 默认值:entity
                            .service("service")
                            .serviceImpl("service.impl")//Service 包名 默认值:service
                            .mapper("mapper")           //Mapper 包名 默认值:mapper
                            //.moduleName("xxx")        // 设置父包模块名 默认值:无
                            .pathInfo(Collections.singletonMap(OutputFile.xml, System.getProperty("user.dir") + "/src/main/resources/mapper")); // 设置mapperXml生成路径
                    //默认存放在mapper的xml下
                })

                .injectionConfig(consumer -> {
                    Map<String, String> customFile = new HashMap<>();
                    // DTO、VO
//                    customFile.put("DTO.java", "/templates/entityDTO.java.ftl");
//                    customFile.put("VO.java", "/templates/entityVO.java.ftl");

                    consumer.customFile(customFile);
                })
                .strategyConfig(builder -> {
                    builder.addInclude("constellation") // 设置需要生成的表名 可边长参数“user”, “user1”
                            .addTablePrefix("tb_","sys_", "gms_") // 设置过滤表前缀
                            .serviceBuilder()//service策略配置
                            .formatServiceFileName("%sService")
                            .formatServiceImplFileName("%sServiceImpl")
                            .entityBuilder()// 实体类策略配置
                            .idType(IdType.ASSIGN_ID)//主键策略  雪花算法自动生成的id
                            .addTableFills(new Column("create_time", FieldFill.INSERT)) // 自动填充配置
                            .addTableFills(new Column("update_time", FieldFill.INSERT_UPDATE))
                            .addTableFills(new Column("create_user", FieldFill.INSERT)) // 自动填充配置
                            .addTableFills(new Column("update_user", FieldFill.INSERT_UPDATE))
                            .enableLombok() //开启lombok
                            .enableChainModel()
                            .logicDeleteColumnName("deleted")// 说明逻辑删除是哪个字段
                            .enableTableFieldAnnotation()// 属性加上注解说明
                            .controllerBuilder() //controller 策略配置
                            .formatFileName("%sController")
                            .enableRestStyle() // 开启RestController注解
                            .mapperBuilder()// mapper策略配置
                            .formatMapperFileName("%sMapper")
                            .enableMapperAnnotation()//@mapper注解开启
                            .formatXmlFileName("%sMapper");
                })
                // 使用Freemarker引擎模板，默认的是Velocity引擎模板
                .templateEngine(new FreemarkerTemplateEngine())
//                .templateEngine(new EnhanceFreemarkerTemplateEngine())
                .execute();

    }
}