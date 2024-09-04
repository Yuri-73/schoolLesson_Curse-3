package ru.hogwarts.school.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.hogwarts.school.exception.NoStudentAgeException;
import ru.hogwarts.school.exception.NullAgeException;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.service.StudentService;

import java.util.Collection;

@RestController
@RequestMapping("student")
public class StudentController {
    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @PostMapping // POST http://localhost:8090/student
    public ResponseEntity createStudent(@RequestBody Student student) { //Для записи студентов по телу через свагер(постман)
        Student student1 = studentService.createStudent(student);
        return ResponseEntity.ok(student1); //В свагере увидим созданный объект в JSON
    }

    @GetMapping("{id}") // GET http://localhost:8090/student/1
    public ResponseEntity <Student> findStudent(@PathVariable Long id) { //Для получения студента из Мапы по индексу через свагер(постман)
        if (studentService.findStudent(id) == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); //Выводим 404 по варианту 1
        }
        return ResponseEntity.ok(studentService.findStudent(id));  //В свагере увидим выбранный объект в JSON
    }

    @PutMapping // PUT http://localhost:8090/student
    public ResponseEntity<Student> editStudent(@RequestBody Student student) { //Для редактирования студентов в Мапе через свагер(постман).
        // Если такого студента в Мапе нет, то выйдет 404
        if (studentService.editStudent(student) == null) {
            return ResponseEntity.notFound().build(); //Если студента с этим Id не найдено, то выскочит по умолчанию 404. Вариант 2а
        }
        return ResponseEntity.ok(studentService.editStudent(student)); //В свагере увидим отредактированный объект в JSON
    }
//Другой вариант PUT-запроса для демонстрации реакции приложения на ошибку 404. Пришлось возвращать строку:
//    @PutMapping // PUT http://localhost:8090/student
//    public ResponseEntity<String> editStudent(@RequestBody Student student) { //Для редактирования студентов в Мапе через свагер(постман).
//        // Если такого студента в Мапе нет, то выйдет 404
//        if (studentService.editStudent(student) == null) {
//            return ResponseEntity.badRequest().body("В списках студентов не значится"); //Если студента с этим Id не найдено, то выскочит строка "В списках студентов не значится". Вариант 2б
//        }
//        studentService.editStudent(student);
//        return ResponseEntity.ok().body("Студент по указанному id успешно отредактирован"); //В свагере увидим отредактированный объект в JSON
//    }

    @DeleteMapping("{id}")  // DELETE http://localhost:8090/student/1
    public ResponseEntity deleteStudent(@PathVariable Long id) { //Для удаления студента по id из Мапы через Свагер
        if (studentService.deleteStudent(id) == null) {
            return ResponseEntity.status(405).build(); //Если студента с этим Id нет, то выскочит 405. Вариант 3
        }
        return ResponseEntity.ok(studentService.deleteStudent(id)); //При удалении студента по выбранному Id по умолчанию пропишется 404.
    }

    @GetMapping() // GET http://localhost:8090/student
    public ResponseEntity<Collection<Student>> getAllStudent() { //Для вывода всех студентов Мапы через свагер(постман)
        return ResponseEntity.ok(studentService.getAllStudent());
    }

    @GetMapping(path = "/searchAge")
        //localhost:8090/student/searchAge?age=22
    String getStudentByAge(@RequestParam(required = false) Integer age) { //Для вывода студентов из Мапы, чей возраст совпадает с параметром входа через свагер(постман)
        try {
            return "Студенты с таким возрастом: " + studentService.getStudentByAge(age);
        } catch (NullAgeException exc) {
            return "Параметр адреса не задан";
        } catch (NoStudentAgeException exception) {
            return "Студентов с таким возрастом в коллекции нет";
        }
    }
}

