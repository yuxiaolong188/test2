package com.cims.bpm.common;

import io.swagger.annotations.ApiOperation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @author hezh
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {
    /**
    * swagger2的配置文件，这里可以配置swagger2的一些基本的内容，比如扫描的包等等
    */
    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
            .apiInfo(apiInfo())
            .select()
            //加了ApiOperation注解的类，生成接口文档
            .apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class))
            //.apis(RequestHandlerSelectors.withClassAnnotation(Api.class))
            //为当前包路径
            .apis(RequestHandlerSelectors.basePackage("com.cims.bpm.restapi"))
            .paths(PathSelectors.any())
            .build();
    }

    /**
    * 构建 api文档的详细信息函数
    */
    private ApiInfo apiInfo() {
        ApiInfo apiInfo = new ApiInfoBuilder()
            //页面标题
            .title("my流程引擎API文档")
            //创建人
            .contact(new Contact("baidu", "https://www.baidu.com", "850157226@qq.com"))
            //版本号
            .version("1.0")
            //描述
            .description("cims系统接口")
            .build();
        return apiInfo;
    }
}

