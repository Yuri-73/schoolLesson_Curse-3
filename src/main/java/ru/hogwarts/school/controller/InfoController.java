package ru.hogwarts.school.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.hogwarts.school.service.GetPortService;

@RestController
public class InfoController {

    private final GetPortService getPortService;

    private Logger logger = LoggerFactory.getLogger(InfoController.class);

    public InfoController(GetPortService getPortService) {
        this.getPortService = getPortService;
    }

    @GetMapping("/get/port")
    public String getPort() {
        logger.info("Метод getPort() стартовал");
        return getPortService.getPort();
    }
}
