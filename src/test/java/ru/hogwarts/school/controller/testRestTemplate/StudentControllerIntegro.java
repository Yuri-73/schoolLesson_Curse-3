package ru.hogwarts.school.controller.testRestTemplate;

import net.bytebuddy.description.field.FieldList;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.hogwarts.school.controller.StudentController;
import ru.hogwarts.school.exception.NullAgeException;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.FacultyRepository;
import ru.hogwarts.school.repository.StudentRepository;
import ru.hogwarts.school.service.StudentAvatarService;
import ru.hogwarts.school.service.StudentService;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static ch.qos.logback.core.util.AggregationType.NOT_FOUND;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class StudentControllerIntegro {
    @LocalServerPort
    private int port;

    @Autowired
    private StudentController studentController;

    @Autowired
    private StudentRepository repository;

    @Autowired
    private FacultyRepository facultyRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TestRestTemplate postTemplate;

    @Autowired
    private TestRestTemplate putTemplate;

    @Autowired
    private StudentService studentService;

    @MockBean
    private StudentAvatarService studentAvatarService;  //Без этого бина все тесты отказываются работать. Для чего-то тестовой БД он нужен!

    private static Long id = 164L;
    private static String name = "Bob";
    private static int age = 22;
    private static String faculty_id = "green";

    @Test
    void contextLoads() throws Exception { // Инициализация бина контроллера студента
        Assertions.assertThat(studentController).isNotNull();
    }

    @Test
    public void createStudentTest() {
        //initial data:
        var s = student(name, age);
        //test:
        var result = restTemplate.postForObject("/student", s, Student.class);
        //check:
        Assertions.assertThat(result.getName()).isEqualTo(name);
        Assertions.assertThat(result.getAge()).isEqualTo(age);
        Assertions.assertThat(result.getId()).isNotNull();
        Assertions.assertThat(result).isNotNull();
        //cleaning:
        repository.deleteById(result.getId());  //Очищение от тестовых данных. Но теперь не имеет смысла, т.к. тест-БД очищается сама.
    }

    @Test
    public void findStudentTest() {
        //initial data:
        var s = student(name, age);
        //test:
        var saved = restTemplate.postForObject("/student", s, Student.class);
        var result = restTemplate.getForObject("/student/" + saved.getId(), Student.class);
        //check:
        Assertions.assertThat(result.getName()).isEqualTo(name);
        Assertions.assertThat(result.getAge()).isEqualTo(age);
        Assertions.assertThat(result).isNotNull();
        //cleaning:
        repository.deleteById(result.getId());  //Очищение от тестовых данных. Но теперь не имеет смысла, т.к. тест-БД очищается сама.
    }

    @Test
    public void updateStudentTest() {
        //initial data:
        var s = student(name, age);
        //test:
        var saved = restTemplate.postForObject("/student", s, Student.class);  //работа метода контроллера через шаблон TestRestTemplate
        System.out.println("saved.getName: " + saved.getName());
        saved.setName("name2");  //Имя студента изменили, но его ID остался прежним
        System.out.println("saved.getName: " + saved.getName());
        ResponseEntity<Student> studentEntityPut = restTemplate.exchange(
                "/student", HttpMethod.PUT, new HttpEntity<>(saved), Student.class
        );
        //check:
        assertThat(studentEntityPut.getBody().getName()).isEqualTo("name2");
        assertThat(studentEntityPut.getBody().getAge()).isEqualTo(age);
        //cleaning:
        repository.deleteById(saved.getId());  //Очищение от тестовых данных. Но теперь не имеет смысла, т.к. тест-БД очищается сама.
    }

    @Test
    public void deleteStudentTest() {
        //initial data:
        var s = student(name, age);
        var saved = restTemplate.postForObject("/student", s, Student.class);
        //test:
        ResponseEntity<Student> studentEntity = restTemplate.exchange(
                "/student/" + saved.getId(),
                HttpMethod.DELETE,
                null,
                Student.class);
        //check:
        Assertions.assertThat(studentEntity.getBody().getName()).isEqualTo(name);
        Assertions.assertThat(studentEntity.getBody().getAge()).isEqualTo(age);
        //test:
        var deletedS1 = restTemplate.getForObject("/student/" + saved.getId(), Student.class);
        //check:
        Assertions.assertThat(deletedS1.getName()).isEqualTo(null);
        Assertions.assertThat(deletedS1.getAge()).isEqualTo(0);
        Assertions.assertThat(deletedS1.getId()).isEqualTo(null);
        //test:
        ResponseEntity<Student> resultAfterDelete = restTemplate.exchange("/student/" + saved.getId(),
                HttpMethod.GET, null, Student.class);
        //check:
        assertThat(resultAfterDelete.getStatusCodeValue()).isEqualTo(404);
    }

    @Test
    void getAllStudentTest() {
        //initial data:
        var s1 = restTemplate.postForObject("/student", student("test1", 24), Student.class);
        var s2 = restTemplate.postForObject("/student", student("test2", 25), Student.class);
        var s3 = restTemplate.postForObject("/student", student("test3", 26), Student.class);
        //test:
        var result = restTemplate.exchange("/student",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Collection<Student>>() {
                });

        var students = result.getBody();
        //check:
        Assertions.assertThat(students).isNotNull();
        Assertions.assertThat(students.size()).isEqualTo(3);  //В тест-базе только тестовые студенты
//        Assertions.assertThat(students).contains(new Student(4l, "Елена", 35));
        Assertions.assertThat(students).contains(new Student(s1.getId(), "test1", 24));
        Assertions.assertThat(students).contains(new Student(s2.getId(), "test2", 25));
        //cleaning:
        repository.deleteById(s1.getId());  //Очищение от тестовых данных. Но теперь не имеет смысла, т.к. тест-БД очищается сама.
        repository.deleteById(s2.getId());
        repository.deleteById(s3.getId());
    }

    @Test
    public void getStudentByAgeTest() {
        //initial data:
        var s = student(name, age);
        var saved = restTemplate.postForObject("/student", s, Student.class);
//        //test:
//         result[] = restTemplate.getForObject("/student/get/by-age?age=22", Student[].class);
        //test:
        var result = restTemplate.exchange("//student/get/by-age?age=22",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Collection<Student>>() {
                });

        var students = result.getBody();

        //check:
        Assertions.assertThat(students).contains(saved);
        Assertions.assertThat(students).isNotNull();
        Assertions.assertThat(students.size()).isEqualTo(1);
        //cleaning:
        repository.deleteById(saved.getId());  //Очищение от тестовых данных.
        //test:
        ResponseEntity<Student> resultAfterDelete = restTemplate.exchange("/student/" + saved.getId(),
                HttpMethod.GET, null, Student.class);
        //check:
        assertThat(resultAfterDelete.getStatusCodeValue()).isEqualTo(404);
    }

    @Test
    public void findByAgeBetweenStudentTest() { //Тест по промежутку возраста
        //initial data:
        var s1 = restTemplate.postForObject("/student", student("test1", 16), Student.class);
        var s2 = restTemplate.postForObject("/student", student("test2", 17), Student.class);
        var s3 = restTemplate.postForObject("/student", student("test3", 18), Student.class);
        var s4 = restTemplate.postForObject("/student", student("test4", 19), Student.class);
        var s5 = restTemplate.postForObject("/student", student("test5", 18), Student.class);
        //test:
        ResponseEntity<Collection<Student>> result = restTemplate.exchange("/student/age?min=16&max=17",
                HttpMethod.GET, null, new ParameterizedTypeReference<Collection<Student>>() {
                });
        var students = result.getBody(); //Выуживание коллекции students из ResponseEntity
        //check:
        Assertions.assertThat(students).isNotNull();
        Assertions.assertThat(students.size()).isEqualTo(2);
        Assertions.assertThat(students).containsExactly(s1, s2);

        //Очищение от тестовых данных. Но теперь не имеет смысла, т.к. тест-БД очищается сама.
        ResponseEntity<Student> studentEntity1 = restTemplate.exchange(
                "/student/" + s1.getId(),
                HttpMethod.DELETE, null, Student.class
        );
        ResponseEntity<Student> studentEntity2 = restTemplate.exchange(
                "/student/" + s2.getId(),
                HttpMethod.DELETE, null, Student.class
        );
        ResponseEntity<Student> studentEntity3 = restTemplate.exchange(
                "/student/" + s3.getId(),
                HttpMethod.DELETE, null, Student.class
        );
        ResponseEntity<Student> studentEntity4 = restTemplate.exchange(
                "/student/" + s4.getId(),
                HttpMethod.DELETE, null, Student.class
        );
        ResponseEntity<Student> studentEntity5 = restTemplate.exchange(
                "/student/" + s5.getId(),
                HttpMethod.DELETE, null, Student.class
        );
    }

    @Test
    public void getFacultyOfStudentTest() {
        //initial data:
        Faculty savedFaculty = restTemplate.postForObject("/faculty", faculty("ppp", "green"), Faculty.class);
        Student s = student(name, age);
        s.setFaculty(savedFaculty);
        Student saved = restTemplate.postForObject("/student", s, Student.class);

//        Faculty result = restTemplate.getForObject("http://localhost:" + port + "/student/" + saved.getId() + "/faculty", Faculty.class); //Можно и так

        //test:
        ResponseEntity<Faculty> responseEntity = restTemplate.exchange(
                "/student/" + saved.getId() + "/faculty",
                HttpMethod.GET,
                null,
                Faculty.class
        );
        //check:
        Assertions
                .assertThat(responseEntity)
                .isNotNull();
        Assertions.assertThat(responseEntity.getBody().getName()).isEqualTo("ppp");
        Assertions.assertThat(responseEntity.getBody().getColor()).isEqualTo("green");

        Assertions.assertThat(saved.getName()).isEqualTo(name);
        Assertions.assertThat(saved.getAge()).isEqualTo(age);

        //Очищение от тестовых данных. Но теперь не имеет смысла, т.к. тест-БД очищается сама.
        repository.deleteById(saved.getId());  //Удаляем тестового студента
        facultyRepository.deleteById(savedFaculty.getId());  //Удаляем тестовый факультет
    }

    @Test
    public void findStudentsByFacultyNameTest() {
        //initial data:
        Faculty f = restTemplate.postForObject("/faculty", faculty("Нормоконтроль", "green"), Faculty.class);
        Student s1 = student("Пётр", 44);
        s1.setFaculty(f);
        Student s2 = student("Борис", 46);
        s2.setFaculty(f);
        //test:
        Student saved1 = restTemplate.postForObject("/student", s1, Student.class);
        Student saved2 = restTemplate.postForObject("/student", s2, Student.class);

        ResponseEntity<Collection<Student>> result = restTemplate.exchange("/student/faculty?facultyName=Нормоконтроль",
                HttpMethod.GET, null, new ParameterizedTypeReference<Collection<Student>>() {
                });
        var students = result.getBody();
        //check:
        Assertions.assertThat(students).isNotNull();
        Assertions.assertThat(students.size()).isEqualTo(2);
        Assertions.assertThat(students).containsExactly(saved1, saved2);
        //cleaning students:
        ResponseEntity<Student> studentEntity1 = restTemplate.exchange(
                "/student/" + saved1.getId(),
                HttpMethod.DELETE, null, Student.class
        );
        ResponseEntity<Student> studentEntity2 = restTemplate.exchange(
                "/student/" + saved2.getId(),
                HttpMethod.DELETE, null, Student.class
        );
        //cleaning faculty:
        facultyRepository.deleteById(f.getId());  //Очищение от тестовых данных. Но теперь не имеет смысла, т.к. тест-БД очищается сама.
    }

    private static Student student(String name, int age) {
        var s = new Student();
        s.setName(name);
        s.setAge(age);
        return s;
    }

    static Faculty faculty(String name, String color) {
        var f = new Faculty();
        f.setName(name);
        f.setColor(color);
        return f;
    }

    //ДЗ-4.1(1) (@Query)
    @Test
    public void getCountAllStudentsTest() {  //Тест на получениеобщего количества студентов
        //initial data:
        Faculty f = restTemplate.postForObject("/faculty", faculty("Нормоконтроль", "green"), Faculty.class);
        Student s1 = student("Пётр", 44);
        s1.setFaculty(f);
        Student s2 = student("Борис", 46);
        s2.setFaculty(f);
        Student s3 = student("Павел", 48);
        s3.setFaculty(f);
        //test:
        Student saved1 = restTemplate.postForObject("/student", s1, Student.class);
        Student saved2 = restTemplate.postForObject("/student", s2, Student.class);
        Student saved3 = restTemplate.postForObject("/student", s3, Student.class);

        var result = restTemplate.getForObject("/student/all-count-students", Integer.class);
        //check:
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result).isEqualTo(3);
    }

    //ДЗ-4.1(2) (@Query)
    @Test
    public void getMidlAgeStudentsTest() {  //Тест на получение среднего возраста студентов
        //initial data:
        Faculty f = restTemplate.postForObject("/faculty", faculty("Нормоконтроль", "green"), Faculty.class);
        Student s1 = student("Пётр", 44);
        s1.setFaculty(f);
        Student s2 = student("Борис", 46);
        s2.setFaculty(f);
        Student s3 = student("Павел", 48);
        s3.setFaculty(f);
        int midlAge = (s1.getAge() + s2.getAge() + s3.getAge()) / 3;
        //test:
        Student saved1 = restTemplate.postForObject("/student", s1, Student.class);
        Student saved2 = restTemplate.postForObject("/student", s2, Student.class);
        Student saved3 = restTemplate.postForObject("/student", s3, Student.class);

        var result = restTemplate.getForObject("/student/midl-age-students", Integer.class);
        //check:
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result).isEqualTo(midlAge);
    }

    //ДЗ-4.1(3) (@Query)
    @Test
    public void getFiveLastBackStudentsTest() {  //Тест на получение 5 последних студентов
        //initial data:
        Faculty f = restTemplate.postForObject("/faculty", faculty("Нормоконтроль", "green"), Faculty.class);
        Student s1 = student("Пётр", 44);
        s1.setFaculty(f);
        Student s2 = student("Борис", 46);
        s2.setFaculty(f);
        Student s3 = student("Павел", 48);
        s3.setFaculty(f);

        Student s4 = student("Пётр2", 49);
        s1.setFaculty(f);
        Student s5 = student("Борис2", 41);
        s2.setFaculty(f);
        Student s6 = student("Павел2", 42);
        s3.setFaculty(f);

        //test:
        Student saved1 = restTemplate.postForObject("/student", s1, Student.class);
        Student saved2 = restTemplate.postForObject("/student", s2, Student.class);
        Student saved3 = restTemplate.postForObject("/student", s3, Student.class);
        Student saved4 = restTemplate.postForObject("/student", s4, Student.class);
        Student saved5 = restTemplate.postForObject("/student", s5, Student.class);
        Student saved6 = restTemplate.postForObject("/student", s6, Student.class);

        ResponseEntity<Collection<Student>> result = restTemplate.exchange("/student/last-five-students",
                HttpMethod.GET, null, new ParameterizedTypeReference<Collection<Student>>() {
                });
        var students = result.getBody();
        //check:
        Assertions.assertThat(students).isNotNull();
        Assertions.assertThat(students.size()).isEqualTo(5);
        Assertions.assertThat(students).containsExactly(saved6, saved5, saved4, saved3, saved2);
    }


    //Тесты к ДЗ-4.5 Параллельные стримы:
    @Test
    public void getAllNameStartsWithATest() { //Тест на получение коллекции имён на первую букву имени в большом регистре
        //initial data:
        var s1 = restTemplate.postForObject("/student", student("t1", 16), Student.class);
        var s2 = restTemplate.postForObject("/student", student("r2", 17), Student.class);
        var s3 = restTemplate.postForObject("/student", student("r3", 18), Student.class);
        var s4 = restTemplate.postForObject("/student", student("r4", 19), Student.class);
        var s5 = restTemplate.postForObject("/student", student("s5", 18), Student.class);
        //test:
        ResponseEntity<Collection<String>> result = restTemplate.exchange("/student/all-starts-name/r",
                HttpMethod.GET, null, new ParameterizedTypeReference<Collection<String>>() {
                });
        var students = result.getBody(); //Выуживание коллекции students из ResponseEntity
        //check:
        Assertions.assertThat(students).isNotNull();
        Assertions.assertThat(students.size()).isEqualTo(3);
        Assertions.assertThat(students).containsExactly("R2", "R3", "R4");
    }

    @Test
    public void getAllNameStartsWithA_NotFound_Test() { //Тест на получение коллекции имён на отсутствующую букву в имени
        //initial data:
        var s1 = restTemplate.postForObject("/student", student("t1", 16), Student.class);
        var s2 = restTemplate.postForObject("/student", student("r2", 17), Student.class);
        var s3 = restTemplate.postForObject("/student", student("r3", 18), Student.class);
        var s4 = restTemplate.postForObject("/student", student("r4", 19), Student.class);
        var s5 = restTemplate.postForObject("/student", student("s5", 18), Student.class);
        //test:
        ResponseEntity<Collection<String>> result = restTemplate.exchange("/student/all-starts-name/k",
                HttpMethod.GET, null, new ParameterizedTypeReference<Collection<String>>() {
                });
        System.out.println("result: " +result);
        var students = result.getBody(); //Выуживание коллекции students из ResponseEntity
        //check:
        Assertions.assertThat(students).isNull();
        Assertions.assertThat(result.getStatusCode().toString()).isEqualTo("404 NOT_FOUND");
        Assertions.assertThat(result.getStatusCodeValue()).isEqualTo(404); //Можно и так
    }

    @Test
    public void getMidlAgeAllStudentsTest() {  //Тест на получение среднего возраста введённых студентов
        //initial data:
        var s1 = restTemplate.postForObject("/student", student("t1", 16), Student.class);
        var s2 = restTemplate.postForObject("/student", student("r2", 17), Student.class);
        var s3 = restTemplate.postForObject("/student", student("r3", 28), Student.class);
        var s4 = restTemplate.postForObject("/student", student("r4", 19), Student.class);
        var s5 = restTemplate.postForObject("/student", student("s5", 38), Student.class);

        var count = (s1.getAge() + s2.getAge() + s3.getAge() + s4.getAge() + s5.getAge()) / 5;
        //test:
        var result = restTemplate.getForObject("/student/average/age", Integer.class);
        System.out.println("result: " +result);
        //check:
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result).isEqualTo(count);
    }

    @Test
    public void getMidlAgeAllStudents_Zero_Test() {  //Тест на получение нулевого среднего возраста
        //test:
        var result = restTemplate.getForObject("/student/average/age", Integer.class);
        System.out.println("result: " +result);
        //check:
        Assertions.assertThat(result).isZero();
        Assertions.assertThat(result).isEqualTo(0);
    }

    @Test
    public void getSumStreamParallel_Test() {
        //initial data:
        Integer summary = Stream.iterate(1, a -> a + 1)
                .limit(1_000_000)
                .reduce(0, Integer::sum);
        //test:
        var result = restTemplate.getForObject("/student/sum-parallel", Integer.class);
        //check:
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result).isEqualTo(summary);
    }

    @Test
    public void printStudentNamesThread_Test() {  ////Тест по выводу студентов в 3-х потоках без синхронизации
        //initial data:
        Student s1 = restTemplate.postForObject("/student", student("st1", 16), Student.class);
        Student s2 = restTemplate.postForObject("/student", student("st2", 17), Student.class);
        Student s3 = restTemplate.postForObject("/student", student("st3", 28), Student.class);
        Student s4 = restTemplate.postForObject("/student", student("st4", 19), Student.class);
        Student s5 = restTemplate.postForObject("/student", student("st5", 38), Student.class);
        Student s6 = restTemplate.postForObject("/student", student("st6", 38), Student.class);
        //test:
        String result = restTemplate.getForObject("/student/print-parallel", String.class);
        System.out.println("result: " +result);
        //check:
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result).isEqualTo("Старт процесса без синхронизатора");
    }

    @Test
    public void printStudentNamesThread_EmptyList_Test() {  //Тест при параллельных потоках без коллекции студентов
        //test:
        ResponseEntity<String> result = restTemplate.exchange("/student/print-parallel",
                HttpMethod.GET, null, String.class);
        //check:
        Assertions.assertThat(result.getBody()).isNull();
        Assertions.assertThat(result.getStatusCode().toString()).isEqualTo("404 NOT_FOUND");
        Assertions.assertThat(result.getStatusCodeValue()).isEqualTo(404); //Можно и так
    }

    @Test
    public void printStudentNamesThreadSynchronization_Test() {  //Тест по синхронизации вывода студентов
        //initial data:
        Student s1 = restTemplate.postForObject("/student", student("st1", 16), Student.class);
        Student s2 = restTemplate.postForObject("/student", student("st2", 17), Student.class);
        Student s3 = restTemplate.postForObject("/student", student("st3", 28), Student.class);
        Student s4 = restTemplate.postForObject("/student", student("st4", 19), Student.class);
        Student s5 = restTemplate.postForObject("/student", student("st5", 38), Student.class);
        Student s6 = restTemplate.postForObject("/student", student("st6", 38), Student.class);
        //test:
        String result = restTemplate.getForObject("/student/print-synchronized", String.class);
        //check:
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result).isEqualTo("Старт процесса с синхронизатором");
    }

    @Test
    public void printStudentNamesThreadSynchronization_EmptyList_Test() {  //Тест при синхронизации без коллекции студентов
        //test:
        ResponseEntity<String> result = restTemplate.exchange("/student/print-synchronized",
                HttpMethod.GET, null, String.class);
        //check:
        Assertions.assertThat(result.getBody()).isNull();
        Assertions.assertThat(result.getStatusCode().toString()).isEqualTo("404 NOT_FOUND");
        Assertions.assertThat(result.getStatusCodeValue()).isEqualTo(404); //Можно и так
    }
}


