package sportbet.client.common.store;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableMBeanExport;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@ComponentScan("sportbet.client.common.store")
@PropertySource("classpath:till.test.properties")
//@EnableMBeanExport
public class EntityStoreManagerCachedAsyncTestConfiguration {

    @Bean
    public static PropertySourcesPlaceholderConfigurer placeholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }
    
    @Bean 
    @Primary
    public EntityStore entityStore(){
        return new EntityStore();
    }
    
    @Bean
    TaskExecutor executor(){
        ThreadPoolTaskExecutor exe=new ThreadPoolTaskExecutor();
        exe.setCorePoolSize(1);
        exe.setMaxPoolSize(2);
        exe.setQueueCapacity(2);
        exe.setThreadGroupName("entityStoreExecutor");
        return exe;
    }
}
