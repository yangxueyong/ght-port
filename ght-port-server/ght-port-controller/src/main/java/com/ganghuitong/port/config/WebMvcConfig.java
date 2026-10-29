package com.ganghuitong.port.config;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.support.config.FastJsonConfig;
import com.alibaba.fastjson2.support.spring6.http.converter.FastJsonHttpMessageConverter;
import com.ganghuitong.port.api.constant.WebConstant;
import com.ganghuitong.port.interceptor.AuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Web MVC 配置：fastjson2 消息转换、鉴权拦截器、跨域
 *
 * @author yangxueyong
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * 鉴权拦截器
     */
    private final AuthInterceptor authInterceptor;

    /**
     * 使用 fastjson2 作为首选 JSON 转换器
     *
     * @param converters 转换器列表
     */
    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        FastJsonHttpMessageConverter converter = new FastJsonHttpMessageConverter();
        FastJsonConfig config = new FastJsonConfig();
        config.setDateFormat(WebConstant.DATE_TIME_PATTERN);
        config.setCharset(StandardCharsets.UTF_8);
        config.setReaderFeatures(JSONReader.Feature.SupportSmartMatch);
        config.setWriterFeatures(JSONWriter.Feature.WriteBigDecimalAsPlain);
        converter.setFastJsonConfig(config);
        converter.setDefaultCharset(StandardCharsets.UTF_8);
        converter.setSupportedMediaTypes(List.of(MediaType.APPLICATION_JSON));
        converters.add(0, converter);
    }

    /**
     * 注册鉴权拦截器
     *
     * @param registry 拦截器注册器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor).addPathPatterns(WebConstant.ALL_PATH);
    }

    /**
     * 允许前端跨域访问
     *
     * @param registry 跨域注册器
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping(WebConstant.ALL_PATH)
                .allowedOriginPatterns(WebConstant.CORS_ALL)
                .allowedMethods(WebConstant.CORS_ALL)
                .allowedHeaders(WebConstant.CORS_ALL)
                .maxAge(WebConstant.CORS_MAX_AGE);
    }
}
