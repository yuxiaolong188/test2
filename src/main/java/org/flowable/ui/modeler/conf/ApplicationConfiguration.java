package org.flowable.ui.modeler.conf;

import org.flowable.ui.modeler.properties.FlowableModelerAppProperties;
import org.flowable.ui.modeler.servlet.ApiDispatcherServletConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

@Configuration
@EnableConfigurationProperties({FlowableModelerAppProperties.class})
@ComponentScan(
        basePackages = {"org.flowable.ui.modeler.repository", "org.flowable.ui.modeler.service",  "org.flowable.ui.common.conf", "org.flowable.ui.common.filter", "org.flowable.ui.common.service", "org.flowable.ui.common.repository", "org.flowable.ui.common.security", "org.flowable.ui.common.tenant",
                "org.flowable.ui.idm.rest.api","org.flowable.ui.idm.properties"
        }
  //  basePackages = {"org.flowable.ui.modeler.conf", "org.flowable.ui.modeler.repository", "org.flowable.ui.modeler.service", "org.flowable.ui.modeler.security", "org.flowable.ui.common.conf", "org.flowable.ui.common.filter", "org.flowable.ui.common.service", "org.flowable.ui.common.repository", "org.flowable.ui.common.security", "org.flowable.ui.common.tenant"}
)
public class ApplicationConfiguration {
    public ApplicationConfiguration() {
    }

    @Bean
    public ServletRegistrationBean modelerApiServlet(ApplicationContext applicationContext) {
        AnnotationConfigWebApplicationContext dispatcherServletConfiguration = new AnnotationConfigWebApplicationContext();
        dispatcherServletConfiguration.setParent(applicationContext);
        dispatcherServletConfiguration.register(new Class[]{ApiDispatcherServletConfiguration.class});
        DispatcherServlet servlet = new DispatcherServlet(dispatcherServletConfiguration);
        ServletRegistrationBean registrationBean = new ServletRegistrationBean(servlet, new String[]{"/api/*"});
        registrationBean.setName("Flowable Modeler App API Servlet");
        registrationBean.setLoadOnStartup(1);
        registrationBean.setAsyncSupported(true);
        return registrationBean;
    }
}