package it.pagopa.pn.service.desk.config;

import io.awspring.cloud.autoconfigure.messaging.SqsAutoConfiguration;
import it.pagopa.pn.service.desk.LocalStackTestConfig;
import it.pagopa.pn.service.desk.middleware.queue.producer.InternalQueueMomProducer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.function.context.config.ContextFunctionCatalogAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;


@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Import(LocalStackTestConfig.class)
public abstract class BaseTest {


    @Slf4j
    @SpringBootTest
    @EnableAutoConfiguration(exclude= {SqsAutoConfiguration.class, ContextFunctionCatalogAutoConfiguration.class})
    @ActiveProfiles("test")
    public static class WithMockServer {
        @Autowired
        private MockServerBean mockServer;

        @MockBean
        private InternalQueueMomProducer internalQueueMomProducer;


        @BeforeEach
        public void init(){
            log.info(this.getClass().getSimpleName());
            setExpection(this.getClass().getSimpleName()+ "-webhook.json");
        }

        @AfterEach
        public void kill(){
            log.info("Killed");
            this.mockServer.stop();
        }

        public void setExpection(String file){
            this.mockServer.initializationExpection(file);
        }
    }


}
