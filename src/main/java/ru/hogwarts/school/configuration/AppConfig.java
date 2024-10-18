  //Этот конфиг-класс излишен (но с ним параллельно тоже работает), т.к. поставил @Primary в каждый из тест-сервисов:
// GetPortServiceTest и GetPortServiceTest2 (не перезапускался Spring на конфигах `spring.profiles.active=test' и 'test2' в основном ресурсе при создании третьего имплемента GetPortServiceBySilence()).
// До его создания, наоборот, Spring не запускался при пустой строке в основном ресурсе: `spring.profiles.active=`, т.е. не удавалось вернуться к порту, что был по умолчанию.

package ru.hogwarts.school.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import ru.hogwarts.school.service.GetPortService;
import ru.hogwarts.school.service.GetPortServiceTest;

@Configuration
@PropertySource("classpath:application-test.properties")
public class AppConfig {

    @Bean
    public GetPortService getPortService() {
        return new GetPortServiceTest();
    }
}
