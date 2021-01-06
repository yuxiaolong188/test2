package com.cims.bpm;

import lombok.extern.slf4j.Slf4j;
import org.flowable.ui.modeler.conf.ApplicationConfiguration;
import org.flowable.ui.modeler.servlet.AppDispatcherServletConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.bind.annotation.RequestMapping;


/**
 * 导入配置
 * @author hezh
 */
@Import({
        ApplicationConfiguration.class,
        AppDispatcherServletConfiguration.class
})
@Controller
@Slf4j
@EnableScheduling
@EnableTransactionManagement
@EnableAspectJAutoProxy
@MapperScan({"com.cims.bpm.business.mapper","com.cims.bpm.dao"})
@org.springframework.boot.autoconfigure.SpringBootApplication(scanBasePackages = {"com.cims.bpm"})
public class SpringBootApplication {

    @RequestMapping("/")
    public String home() {
        log.info("redirect to swagger-ui.html");
        return "redirect:/swagger-ui.html";
    }

    public static void main(String[] args) {

        SpringApplication.run(SpringBootApplication.class, args);

       // log.info("###########################流程后台程序启动成功##################################");
    }

/*    @Bean
    public CommandLineRunner init(final RepositoryService repositoryService,
                                  final RuntimeService runtimeService,
                                  final TaskService taskService) {

        return new CommandLineRunner() {
            @Override
            public void run(String... strings) throws Exception {
                System.out.println("Number of process definitions : "
                        + repositoryService.createProcessDefinitionQuery().count());
                System.out.println("Number of tasks : " + taskService.createTaskQuery().count());
                runtimeService.startProcessInstanceByKey("oneTaskProcess");
                System.out.println("Number of tasks after process start: "
                        + taskService.createTaskQuery().count());
            }
        };
    }*/
}
