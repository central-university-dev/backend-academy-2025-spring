package backend.academy;

import backend.academy.beans.BasicComponent;
import backend.academy.config.AppConfig;
import backend.academy.config.JavaConfig;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Main {

    public static void main(String[] args) {
        ApplicationContext xmlApplicationContext = new ClassPathXmlApplicationContext("application-context.xml");
        //xmlApplicationContext.getBeanDefinitionNames();
       // BasicComponent component1 = xmlApplicationContext.getBean("basicComponent1", BasicComponent.class);
       // BasicComponent component2 = xmlApplicationContext.getBean("basicComponent2", BasicComponent.class);
       // BasicComponent component2 = xmlApplicationContext.getBean("componentFromFactory", BasicComponent.class);


        // ! Можно одновременно иметь несколько контекстов
        // рассказать про @ComponentScan в AppConfig
        ApplicationContext javaConfigApplicationContext = new AnnotationConfigApplicationContext(AppConfig.class);
    }
}
