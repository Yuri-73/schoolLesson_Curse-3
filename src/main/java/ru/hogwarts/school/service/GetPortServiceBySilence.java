package ru.hogwarts.school.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;

@Service
public class GetPortServiceBySilence implements GetPortService {
    @Value("${server.port}")
    private String serverPort;
    private Logger logger = LoggerFactory.getLogger(GetPortServiceBySilence.class);

    @PostConstruct
    public void testGetPort(){
        System.out.println("getPort() = " + getPort());
    }

    @Override
    public String getPort() {
        logger.info("Метод GetPortServiceTest прошёл");
        return serverPort;
    }
}
