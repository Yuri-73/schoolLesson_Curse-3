package ru.hogwarts.school.controller.webMvcTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.hogwarts.school.controller.AvatarController;
import ru.hogwarts.school.model.Avatar;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.StudentAvatarRepository;
import ru.hogwarts.school.repository.StudentRepository;
import ru.hogwarts.school.service.StudentAvatarService;
import ru.hogwarts.school.service.StudentService;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.function.RequestPredicates.accept;

@WebMvcTest(AvatarController.class)
//@SpringBootTest
//@AutoConfigureMockMvc
public class AvatarControllerWebMvcTest {
    @Autowired
    private MockMvc mockMvc;

    @SpyBean
    private StudentAvatarService studentAvatarService;

    @SpyBean
    private StudentService studentService;

    @MockBean
    private StudentAvatarRepository studentAvatarRepository;

    @MockBean
    private StudentRepository studentRepository;

    @InjectMocks
    private AvatarController avatarController;

    private ObjectMapper mapper = new ObjectMapper();

    //ДЗ-3.6(1)
    @Test
    void uploadAvatarTest() throws Exception {
        //входные условия:
        Student student = new Student(1L, "Nikolay", 30);
        byte[] bytes = Files.readAllBytes(Path.of("src/test/resources/test.jpg"));
        Avatar avatar = new Avatar();
        avatar.setData(bytes);
        avatar.setFilePath("/1L.pdf");
        avatar.setFileSize(11L);
        avatar.setStudent(student);
        avatar.setMediaType(".pdf");

        when(studentRepository.findById(ArgumentMatchers.any(Long.class))).thenReturn(Optional.of(student));
        when(studentAvatarRepository.findByStudentId(ArgumentMatchers.any(Long.class))).thenReturn(Optional.of(avatar));
        when(studentAvatarRepository.save(ArgumentMatchers.any(Avatar.class))).thenReturn(avatar);

        File file = new File("src/test/resources/test.jpg");
        String name = file.getName();

        MockMultipartFile mockMultipartFile = new MockMultipartFile("avat", name, MediaType.MULTIPART_FORM_DATA_VALUE, bytes);

        //тест:
        MvcResult mvcResult = mockMvc.perform(multipart("/" + student.getId() + "/avatar")
                        .file(mockMultipartFile))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andReturn();

        //?
        String jsonResponse = mvcResult.getResponse().getContentAsString();
        System.out.println("!!!");
        System.out.println("jsonResponse" + jsonResponse);

        //контроль:
        assertNotNull(avatar);
        System.out.println(avatar.getFilePath());
        assertEquals(avatar.getFilePath(), "avatars\\1.jpg"); //Объект получил новый путь
        assertEquals(avatar.getStudent(), student);
        assertEquals(avatar.getFileSize(), bytes.length);
        assertTrue(Files.isReadable(Path.of("src/test/resources/test.jpg")));
    }

    //ДЗ-3.6(2)
    @Test
    void downloadAvatarFromDatabaseTest() throws Exception {  //Тест проходит, но проверить вывод массива байт не могу
        //входные условия:
        Student student = new Student(1L, "Nikolay", 30);
        byte[] bytes = Files.readAllBytes(Path.of("src/test/resources/test.jpg"));

        Avatar avatar = new Avatar();
        avatar.setData(bytes);
        avatar.setFilePath("/test.jpg"); //Полный путь необязателен, т.к. он не нужен для поиска аватара в БД
        avatar.setFileSize(11L);
        avatar.setStudent(student);
        avatar.setMediaType("image/jpg");

        when(studentRepository.findById(ArgumentMatchers.any(Long.class))).thenReturn(Optional.of(student));
        when(studentAvatarRepository.findByStudentId(ArgumentMatchers.any(Long.class))).thenReturn(Optional.of(avatar));

        //тест:
        var result = mockMvc.perform(MockMvcRequestBuilders
                        .get("/" + student.getId() + "/avatar/preview")
                        .accept(MediaType.APPLICATION_OCTET_STREAM))
                //контроль:
                .andExpect(status().isOk())
                .andReturn();
        //Сравнение массива байт в начальном условии с выходным массивом байт:
        assertArrayEquals(bytes, result.getResponse().getContentAsByteArray());
    }

    //ДЗ-3.6(3)
    @Test
    void downloadAvatarFromFileTest() throws Exception {
        //входные условия:
        Student student = new Student(1L, "Nikolay", 30);
        byte[] bytes = Files.readAllBytes(Path.of("src/test/resources/test.jpg"));
        Avatar avatar = new Avatar();
        avatar.setData(bytes);
        avatar.setFilePath("src/test/resources/test.jpg"); //Полный путь обязательно!!!
        avatar.setFileSize(11L);
        avatar.setStudent(student);
        avatar.setMediaType("image/jpg");

        when(studentRepository.findById(ArgumentMatchers.any(Long.class))).thenReturn(Optional.of(student));
        when(studentAvatarRepository.findByStudentId(ArgumentMatchers.any(Long.class))).thenReturn(Optional.of(avatar));

        //тест:
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/" + student.getId() + "/avatar")
                        .accept(MediaType.APPLICATION_OCTET_STREAM))
                //контроль:
                .andExpectAll()
                .andExpect(status().isOk())
                .andReturn();
    }

    //ДЗ-4.1(page с валидными параметрами)
    @Test
    public void shouldGetAllAvatarsPage_WhenValidParams_ThenReturnList() throws Exception {
        //входные условия:
        Integer pageNumber = 1;
        Integer pageSize = 1;

        Avatar avatar = new Avatar();
        avatar.setData(new byte[]{});
        avatar.setFilePath("/1L.pdf");
        avatar.setFileSize(11L);
        avatar.setStudent(new Student(1l, "Bob", 22));
        avatar.setMediaType(".pdf");

        Avatar avatar2 = new Avatar();
        avatar2.setData(new byte[]{});
        avatar2.setFilePath("/2L.pdf");
        avatar2.setFileSize(12L);
        avatar2.setStudent(new Student(2l, "Bill", 23));
        avatar2.setMediaType(".pdf");

        //Формирование объекта page:
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize);
        List<Avatar> list = List.of(avatar, avatar2);
        Page<Avatar> avatarPage = new PageImpl<>(list, pageable, 0);

        when(studentAvatarRepository.findAll(pageable)).thenReturn(avatarPage);

        //тест:
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/page-avatars/")
                        .param("page", "" + pageNumber)
                        .param("size", "" + pageSize)
                        .accept(MediaType.APPLICATION_JSON))
                //Контроль:
                .andExpectAll()
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].fileSize").value(11l))
                .andExpect(jsonPath("$[1].fileSize").value(12l))
                .andExpect(jsonPath("$[0].filePath").value("/1L.pdf"))
                .andExpect(jsonPath("$[1].filePath").value("/2L.pdf"))
                .andExpect(jsonPath("$[0].student.id").value(1l))
                .andExpect(jsonPath("$[1].student.id").value(2l))
                //Ещё один способ - через маппер:
                .andExpect(MockMvcResultMatchers.content().json(mapper.writeValueAsString(list)));
    }

    //ДЗ-4.1(недопустимые параметры на входе)
    @Test
    public void shouldGetAllAvatarsPage_WhenAnyInvalidParam_ThenReturnEmptyList() throws Exception {
        //входные условия:
        Integer pageNumber = 0;
        Integer pageSize = 0;

        //тест:
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/page-avatars/")
                        .param("page", "" + pageNumber)
                        .param("size", "" + pageSize)
                        .accept(MediaType.APPLICATION_JSON))
                //контроль:
                .andExpectAll()
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.size()").value(0))
                .andReturn();
    }
}
