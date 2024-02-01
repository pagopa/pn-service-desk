package it.pagopa.pn.service.desk;

import it.pagopa.pn.commons.configs.listeners.TaskIdApplicationListener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class PnServiceDeskApplication {


    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(PnServiceDeskApplication.class);
        app.addListeners(new TaskIdApplicationListener());
        SpringApplication.run(PnServiceDeskApplication.class, args);
    }


    @RestController
    @RequestMapping("/")
    public static class RootController {

        @GetMapping("/")
        public String home() {
            return "";
        }
    }
}