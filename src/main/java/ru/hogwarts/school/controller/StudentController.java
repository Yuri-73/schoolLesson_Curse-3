package ru.hogwarts.school.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.hogwarts.school.exception.NoStudentAgeException;
import ru.hogwarts.school.exception.NullAgeException;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.service.StudentService;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("student")
public class StudentController {
    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @PostMapping // POST http://localhost:8090/student
    public ResponseEntity<Student> createStudent(@RequestBody Student student) { //Для записи студентов по телу запроса через свагер(постман)
        Student student1 = studentService.createStudent(student);
        return ResponseEntity.ok(student1); //В свагере увидим созданный объект в JSON
    }

    @GetMapping("{id}") // GET http://localhost:8090/student/1
    public ResponseEntity<Student> findStudent(@PathVariable Long id) { //Для получения студента из по индексу через свагер(постман)
        if (studentService.findStudent(id) == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); //Выводим 404 по варианту 1
        }
        return ResponseEntity.ok(studentService.findStudent(id));  //В свагере увидим выбранный объект в JSON
    }

    @PutMapping // PUT http://localhost:8090/student
    public ResponseEntity<Student> editStudent(@RequestBody Student student) { //Для редактирования студентов через свагер(постман).
        if (studentService.editStudent(student) == null) {
            return ResponseEntity.notFound().build(); //Если студента с этим Id не найдено, то выскочит по умолчанию 404. Вариант 2а
        }
        return ResponseEntity.ok(studentService.editStudent(student)); //В свагере увидим отредактированный объект в JSON
    }

    @DeleteMapping("{id}")  // DELETE http://localhost:8090/student/1
    public ResponseEntity<Student> deleteStudent(@PathVariable Long id) { //Для удаления студента по id через Свагер
        Student student = studentService.deleteStudent(id);
        if (student == null) {
            return ResponseEntity.status(405).build(); //Если студента с этим Id нет, то 405. Вариант 3
        }
        return ResponseEntity.ok(student);
    }

    @GetMapping() // GET http://localhost:8090/student
    public ResponseEntity<Collection<Student>> getAllStudent() { //Для вывода всех студентов через свагер(постман)
        return ResponseEntity.ok(studentService.getAllStudent());
    }

    @GetMapping(path = "/get/by-age")
        //ДЗ-3.2 изначально без репозитория, но теперь работает через getAllStudent()
    public ResponseEntity<Collection<Student>> getStudentByAge(@RequestParam(required = false) Integer age) {
            return ResponseEntity.ok(studentService.getStudentByAge(age));
    }

    // ДЗ-3.4, шаг 1.1
    @GetMapping("/age") // GET http://localhost:8090/student/age?min=22&max=23
    public ResponseEntity<List<Student>> findByAgeBetweenStudent(@RequestParam Integer min, @RequestParam(required = false) Integer max) {
        if (studentService.findByAgeBetween(min, max).isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); //Выводим 404 по варианту 1;
        }
        return ResponseEntity.ok(studentService.findByAgeBetween(min, max)); //Вызов стандартногот метода поиска студентов по отрезку возраста
    }

    //ДЗ-3.4, шаг 4.2*(по имени факультета - по своей инициативе, в условии нет; не через геттер students'а в faculty, а через функционал БД:
    @GetMapping("/faculty") // GET http://localhost:8082/student/faculty?facultyName=АО
    public ResponseEntity<Collection<Student>> findStudentsByFacultyName(String facultyName) {
        return ResponseEntity.ok(studentService.findStudentsByFacultyName(facultyName));
    }

    //ДЗ-3.4 шаг 4.1 (SQL) Получение факультета по Id его студента
    @GetMapping("/{id}/faculty")
    public ResponseEntity<Faculty> getFacultyOfStudent(@PathVariable Long id) {
        return ResponseEntity.ok(studentService.getFacultyOfStudent(id));
    }

    //ДЗ-4.1 шаг 1 Получение общего количества студентов из БД через @Query:
    @GetMapping("/all-count-students")
    // в БД (первый метод шага 1 ДЗ-4.1)
    public Integer getCountAllStudents() {
        return studentService.getCountAllStudentInSchool();
    }

    //ДЗ-4.1 шаг 1 Получение среднего возраста всех студентов из БД через @Query:
    @GetMapping("/midl-age-students")
    public Integer getMidlAgeStudents() {
        return studentService.getMidlAgeStudent();
    }

    //ДЗ-4.1 шаг 1 Получение 5 последних студентов из БД через @Query в обратном порядке:
    @GetMapping("/last-five-students")
    public List<Student> getFiveLastBackStudents() {
        return studentService.getFiveLastBackStudents();
    }


    //ДЗ-4.5: Параллельные стримы
    //Шаг 1. Вывод всех имён студентов, начинающихся с одной и той же буквы,
    // а также отсортированных в алфавитном порядке и находящихся в верхнем регистре:
    @GetMapping("/all-starts-name/{letter}")
    public ResponseEntity<List<String>> getAllNameStartsWithA(@PathVariable String letter) {
        List<String> allNameStartsWithLetter = studentService.getAllNameStartsWithLetter(letter);
        if (allNameStartsWithLetter.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        System.out.println("letter: " + letter);
        return ResponseEntity.ok(allNameStartsWithLetter);
    }

    //Шаг 2. Вывод среднего возраста всех студентов, находящихся в БД студентов:
    @GetMapping("/average/age")
    public Integer getMidlAgeAllStudents() {
        return studentService.getMidlAgeAllStudents();
    }

    //Шаг 4. Вывод целого числа, полученного суммой всех индексов итерации от 1 до 1000000 с помощью параллельного стрима:
    @GetMapping("/sum-parallel")
    public Integer getSumStreamParallel() {
        return studentService.getIntegerParallelStream();
    }

    //ДЗ-4.6: Потоки
    //Шаг 1: Несинхронизированный вывод студентов в 3-х параллельных потоках (вперемешку):
    @GetMapping("/print-parallel")
    public ResponseEntity<String> printStudentNamesThread() {
        String getNameAllStudentsThread = studentService.getNameAllStudentsThread();
        if (getNameAllStudentsThread == null)
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok("Старт процесса без синхронизатора");
    }

    //Шаг 2: Синхронизированный вывод студентов в 3-х параллельных потоках:
    @GetMapping("/print-synchronized")
    public ResponseEntity<String> printStudentNamesThreadSynchronization() {
        String getNameAllStudentsThreadSynchronization = studentService.getNameAllStudentsThreadSynchronization();
        if (getNameAllStudentsThreadSynchronization == null)
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok("Старт процесса с синхронизатором");
    }
}

