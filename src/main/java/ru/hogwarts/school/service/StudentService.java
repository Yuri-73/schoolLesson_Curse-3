package ru.hogwarts.school.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import ru.hogwarts.school.exception.NoStudentAgeException;
import ru.hogwarts.school.exception.NullAgeException;
import ru.hogwarts.school.exception.StudentNotFoundException;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.FacultyRepository;
import ru.hogwarts.school.repository.StudentRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class StudentService {
    private final StudentRepository studentRepository;

    private final Logger logger = LoggerFactory.getLogger(FacultyService.class); //ДЗ-4.6 Включение логирования результатов для студента

    public StudentService(StudentRepository studentRepository) {
        logger.info("The constructor of the StudentService class is launched");
        this.studentRepository = studentRepository;
    }

    public Student createStudent(Student student) {
        logger.info("Was invoked method for create student");
        return studentRepository.save(student);
    }

    public Student findStudent(Long id) {
        logger.info("a method for searching a student by its id has been launched: " + id);
        Student student = studentRepository.findById(id).orElseThrow(() -> new StudentNotFoundException(id));
        return student;
    }

    public Student editStudent(Student student) {
        logger.info("Student editing method started");
        return studentRepository.findById(student.getId())
                .map(e -> studentRepository.save(student))
                .orElse(null);
    }

    public Student deleteStudent(Long id) {
        logger.info("Method deleteStudent started");
        var entity = studentRepository.findById(id).orElse(null);
        if (entity != null) {
            studentRepository.delete(entity);
        }
        return entity;
    }

    public Collection<Student> getAllStudent() {
        logger.info("Method getAllStudent started");
        return studentRepository.findAll();
    }

    //ДЗ-3.3:
    public Collection<Student> getStudentByAge(Integer age) {
        logger.info("Method getStudentByAge started");
        if (age <= 0) {
            logger.error("Attention! age = " + age + " <= 0");
            throw new NullAgeException(age);
        }
        Collection<Student> studentListByAge = getAllStudent()
                .stream()
                .filter(e -> e.getAge() == age)
                .collect(Collectors.toList());
        if (studentListByAge.isEmpty()) {
            logger.error("There are no students of this age on the list");
            throw new NoStudentAgeException(age);
        }
        logger.debug("Age variable is valid");
        return studentListByAge;
    }

    //ДЗ-3.4 шаг 1.1:
    public List<Student> findByAgeBetween(Integer min, Integer max) {
        logger.info("Method findByAgeBetween started");
        return studentRepository.findByAgeBetween(min, max);
    }

    //ДЗ-3.4 шаг 4.2* (по имени факультета):
    public Collection<Student> findStudentsByFacultyName(String facultyName) {
        logger.info("Method findStudentsByFacultyName started");
        return studentRepository.findStudentsByFacultyName(facultyName);
    }

    //ДЗ-3.4 шаг 4.2 (по Id студента):
    public Faculty getFacultyOfStudent(Long id) {
        logger.info("Method getFacultyOfStudent started");
        return studentRepository.findById(id)
                .map(Student::getFaculty)
                .orElse(null);
    }

    //Методы для SQL-запросов из БД (3 шт.): шаг 1 ДЗ-4.1:
    public Integer getCountAllStudentInSchool() {
        logger.info("Method getCountAllStudentInSchool started");
        return studentRepository.getAllStudentInSchool();
    }

    public Integer getMidlAgeStudent() {
        logger.info("Method getMidlAgeStudent started");
        return studentRepository.getMidlAgeStudent();
    }

    public List<Student> getFiveLastBackStudents() {
        logger.info("Method getFiveLastBackStudents started");
        // в БД в обратном порядке(третий метод шага 1 ДЗ-4.1)
        return studentRepository.getFiveLastBackStudents();
    }
}

