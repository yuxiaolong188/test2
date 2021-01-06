package com.cims.bpm.common;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.*;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.util.StringValueResolver;

import java.util.Locale;

@Component
public class SpringContextUtil implements ApplicationContextAware, EmbeddedValueResolverAware, EnvironmentAware, MessageSourceAware, ResourceLoaderAware, ApplicationEventPublisherAware, BeanFactoryAware {
    public static ApplicationContext applicationContext = null;
    public static StringValueResolver stringValueResolver = null;
    public static Environment environment = null;
    public static MessageSource messageSource = null;
    public static ResourceLoader resourceLoader = null;
    public static ApplicationEventPublisher applicationEventPublisher = null;
    public static BeanFactory beanFactory = null;
    public static boolean isDev = false;


    // 国际化使用
    public static String getMessage(String key) {
        //return applicationContext.getMessage(key, null, Locale.getDefault());
        return messageSource.getMessage(key, null, Locale.getDefault());
    }

    public static Object getBean(String name) {
        return applicationContext.getBean(name);
    }

    public static <T> T getBean(Class<T> clazz) {
        return applicationContext.getBean(clazz);
    }

    public static <T> T getBean(String name, Class<T> clazz) {
        return applicationContext.getBean(name, clazz);
    }

    public static String getActiveProfile() {
        //return applicationContext.getEnvironment().getActiveProfiles()[0];
        return environment.getActiveProfiles()[0];
    }

    public static void registerSingleton(String name, Object object) {
        ((DefaultListableBeanFactory) beanFactory).registerSingleton(name, object);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringContextUtil.applicationContext = applicationContext;
    }

    @Override
    public void setEmbeddedValueResolver(StringValueResolver stringValueResolver) {
        SpringContextUtil.stringValueResolver = stringValueResolver;
    }

    @Override
    public void setEnvironment(Environment environment) {
        SpringContextUtil.environment = environment;
        //isDev ="dev".equals(environment.getActiveProfiles()[0]);
    }

    @Override
    public void setMessageSource(MessageSource messageSource) {
        SpringContextUtil.messageSource = messageSource;
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        SpringContextUtil.resourceLoader = resourceLoader;
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        SpringContextUtil.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        SpringContextUtil.beanFactory = beanFactory;
    }

    /**
     * 如果BeanFactory包含一个与所给名称匹配的bean定义，则返回true
     *
     * @param name
     * @return boolean
     */
    public static boolean containsBean(String name) {
        return applicationContext.containsBean(name);
    }

    /**
     * 判断以给定名字注册的bean定义是一个singleton还是一个prototype。
     * 如果与给定名字相应的bean定义没有被找到，将会抛出一个异常（NoSuchBeanDefinitionException）
     *
     * @param name
     * @return boolean
     * @throws NoSuchBeanDefinitionException
     */
    public static boolean isSingleton(String name) throws NoSuchBeanDefinitionException {
        return applicationContext.isSingleton(name);
    }
    /**
     * @param name
     * @return Class 注册对象的类型
     * @throws NoSuchBeanDefinitionException
     */
    public static Class getType(String name) throws NoSuchBeanDefinitionException {
        return applicationContext.getType(name);
    }

    /**
     * 如果给定的bean名字在bean定义中有别名，则返回这些别名
     *
     * @param name
     * @return
     * @throws NoSuchBeanDefinitionException
     */
    public static String[] getAliases(String name) throws NoSuchBeanDefinitionException {
        return applicationContext.getAliases(name);
    }
}
