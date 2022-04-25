package com.mongo.data.mongoplus.proxy;

import com.mongo.data.mongoplus.anntation.MongoPlusMapperScan;
import com.mongo.data.mongoplus.exception.MongoPlusException;
import com.mongo.data.mongoplus.service.IMongoPlusService;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.maven.surefire.shade.org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.SystemPropertyUtils;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * @author liaoyi
 * @version V1.0
 * @className MongoPlusProxyFactory
 * @description
 * @date 2022/3/10 7:35 PM
 * @since [产品/模块版本]
 **/
@Slf4j
public class MongoPlusImportBeanDefinitionRegistrar implements BeanDefinitionRegistryPostProcessor, ResourceLoaderAware, ApplicationContextAware {

    /**
     * mongo的所有mapper的缓存
     */
    private  final Set<Class<?>> mongoMapperClassSets=new HashSet<>();



     static ApplicationContext applicationContext;


    private  ResourcePatternResolver patternResolver;

    private  MetadataReaderFactory metadataReaderFactory;

    /** 功能描述：扫描指定包，并将扫描到的class对象放到set缓存
     * @param packages 包路径
     * @author liaoyi
     * @date 2022/3/15 3:48 PM
     */
    public void doScan(String packages) throws IOException, ClassNotFoundException {
        String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX.
                concat(ClassUtils.convertClassNameToResourcePath(SystemPropertyUtils.resolvePlaceholders(packages))).concat("/**/*.class");
        Resource[] resources = patternResolver.getResources(packageSearchPath);
        for (Resource resource : resources) {
            if(resource.isReadable()){
                MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
                String className = metadataReader.getClassMetadata().getClassName();
                Class<?> interfaceClass = Class.forName(className);
                //不是IMongoPlusService的子类，才注册到spring容器
                if(interfaceClass.isInterface()&& !IMongoPlusService.class.isAssignableFrom(interfaceClass)){
                    mongoMapperClassSets.add(interfaceClass);
                }
            }
        }
    }



    @Override
    public void setApplicationContext(@NotNull ApplicationContext applicationContext) throws BeansException {
        MongoPlusImportBeanDefinitionRegistrar.applicationContext=applicationContext;
    }

    @Override
    public void setResourceLoader(@NotNull ResourceLoader resourceLoader) {
        this.patternResolver= ResourcePatternUtils.getResourcePatternResolver(resourceLoader);
        this.metadataReaderFactory=new CachingMetadataReaderFactory(resourceLoader);
    }

    @Override
    public void postProcessBeanDefinitionRegistry(@NotNull BeanDefinitionRegistry beanDefinitionRegistry) throws BeansException {
        List<String> startClassPackage = AutoConfigurationPackages.get(applicationContext);
        Reflections reflections = new Reflections(new ConfigurationBuilder().
                setScanners(new TypeAnnotationsScanner(),new SubTypesScanner(false)).forPackages(startClassPackage.get(0)).addClassLoader(this.getClass().getClassLoader()));
        //获取到  @MongoMapperScanAnnotation注解的接口类，最多存在1个,在启动类上
        Set<Class<?>> mongoMapperScanAnnotationClassSet = reflections.getTypesAnnotatedWith(MongoPlusMapperScan.class);
        if (CollectionUtils.isEmpty(mongoMapperScanAnnotationClassSet)) {
            log.error("********************MongoMapper Annotation  not exists********************");
            throw new MongoPlusException("MongoMapper Annotation  not exists");
        }
        String packages= StringUtils.EMPTY;
        if(mongoMapperScanAnnotationClassSet.stream().findFirst().isPresent()){
            Class<?> mongoPlusMapperScanAnnotationClass =mongoMapperScanAnnotationClassSet.stream().findFirst().get();
            MongoPlusMapperScan mongoPlusMapperScan = mongoPlusMapperScanAnnotationClass.getAnnotation(MongoPlusMapperScan.class);
             packages = mongoPlusMapperScan.packages();
        }
        try {
            this.doScan(packages);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(CollectionUtils.isEmpty(mongoMapperClassSets)){
            return;
        }
        mongoMapperClassSets.forEach(aClass -> {
            BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(aClass);
            GenericBeanDefinition beanDefinition = (GenericBeanDefinition)beanDefinitionBuilder.getRawBeanDefinition();
            beanDefinition.getConstructorArgumentValues().addGenericArgumentValue(aClass);
            beanDefinition.setBeanClass(MongoFactoryBean.class);
            beanDefinition.setAutowireMode(GenericBeanDefinition.AUTOWIRE_BY_TYPE);
            String simpleName = aClass.getSimpleName();
            simpleName=simpleName.substring(0,1).toLowerCase(Locale.ROOT)+simpleName.substring(1);
            beanDefinitionRegistry.registerBeanDefinition(simpleName,beanDefinition);
        });
    }

    @Override
    public void postProcessBeanFactory(@NonNull ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {

    }
}