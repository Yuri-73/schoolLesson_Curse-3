package ru.hogwarts.school.controller.testRestTemplate;

//import jakarta.transaction.Transactional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;

import org.springframework.boot.test.web.server.LocalServerPort;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import ru.hogwarts.school.controller.AvatarController;


import ru.hogwarts.school.model.Avatar;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.StudentAvatarRepository;
import ru.hogwarts.school.repository.StudentRepository;
import ru.hogwarts.school.service.StudentAvatarService;
import ru.hogwarts.school.service.StudentService;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;


import static jdk.dynalink.linker.support.Guards.isNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
public class AvatarControllerIntegro {
    @LocalServerPort
    private int port;

    @Autowired
    private AvatarController avatarController;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private StudentAvatarRepository studentAvatarRepository;

    @Autowired
    private StudentAvatarService out;

    @Autowired
    private StudentService studentService;

    private String baseUrl;

    @BeforeEach
    public void setup() {
        baseUrl = "http://localhost:" + port;

        Student student = new Student();
        student.setId(1L);
        student.setName("Тестовый");
        student.setAge(20);
        studentRepository.save(student);

    }

    @Test
    public void contextLoads() throws Exception {
        Assertions.assertThat(avatarController).isNotNull();
    }

//    @Test
//    public void testUploadAvatar() throws IOException {
//        final String path = "./src/test/resources";
//        byte[] image;
//
//
//
//        out = new StudentAvatarService(studentService, studentAvatarRepository, path);
//        Student student = new Student(1L, "Nikolay", 30);
//        studentRepository.save(student);
//
//        try (InputStream is = Files.newInputStream(Path.of(path + "/test.jpg"));  //открываем поток из файла, нах-ся в директории теста проекта
//             BufferedInputStream bis = new BufferedInputStream(is, 1024);
//             ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        ) {
//            bis.transferTo(baos);
//            this.image = baos.toByteArray(); //укладываем в массив байт
//        }
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
//        File file = new File("/src/test/resources/test.jpg");
//        byte[] avatarContent = Files.readAllBytes(new ClassPathResource("/src/test/resources/test.jpg").getFile().toPath());
//
//        Avatar avatar = new Avatar();
//        avatar.setData(avatarContent);
//        avatar.setFilePath("/1.jpg");
//        avatar.setFileSize(11L);
//        avatar.setStudent(student);
//        avatar.setMediaType("image/jpg");
//
//        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
//        body.add("avatars", new FileSystemResource("/src/test/resources/test.jpg"));
//        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
//        ResponseEntity<String> response = restTemplate.exchange("/" + student.getId() + "/avatar", HttpMethod.POST, requestEntity, String.class);
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//    }
}
