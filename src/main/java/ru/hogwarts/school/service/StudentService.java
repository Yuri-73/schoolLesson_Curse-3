package ru.hogwarts.school.service;

import net.bytebuddy.implementation.bind.annotation.Empty;
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
import java.util.stream.Stream;

@Service
public class StudentService {
    private final StudentRepository studentRepository;

    private final Logger logger = LoggerFactory.getLogger(FacultyService.class); //ДЗ-4.6 Включение логирования результатов для студента
    private final Object flag = new Object(); //ДЗ-4.6 Потоки

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

    //ДЗ-4.5
    //Шаг 1. Вывод всех имён студентов, начинающихся с одной и той же буквы,
    // а также отсортированных в алфавитном порядке и находящихся в верхнем регистре:
    public List<String> getAllNameStartsWithLetter(String letter) {
        System.out.println("letter: " + letter);
        return studentRepository.findAll()
                .stream()  //Переводим нашу коллекцию студентов в Stream, чтобы через него
                // добавлять специальные функциональные методы
                .map(Student::getName) //Мап-метод создания коллекции String из коллекции Student
                .filter(e -> e.startsWith(letter)) //Фильтруем по первой букве слова в коллекции
                .map(String::toUpperCase)//Переводим отфильтрованные на одинаковую букву слова в верхний регистр
                .sorted()//Сортируем имена по алфавиту (наверное, по второй букве, т.к. первая буква одинаковая)
                .collect(Collectors.toList()); //Терминальная функция собирания имён в коллекцию
    }

    //Шаг 2. Вывод среднего возраста всех студентов, находящихся в БД студентов:
    public Integer getMidlAgeAllStudents() {
        double average = studentRepository.findAll()
                .stream()  //Переводим нашу коллекцию студентов в Stream, чтобы через него
                // добавлять специальные функциональные методы
                .mapToInt(Student::getAge)  //Мап-метод создания коллекции Integer из коллекции Student
                .average() //Ищем среднее значение коллекции с типом данных Integer
                .orElse(0.0);  //Если в созданной коллекция Integer пустая, то возвращается 0.0
        return (int) average;
    }

    //Шаг 4. Вывод целого числа, полученного суммой всех значений от 1 до 1000 с помощью параллельного стрима:
    public Integer getIntegerParallelStream() {
        //Проверка времени сумирования с использованием метода parallel():
        long start = System.currentTimeMillis();
        Integer result = Stream.iterate(1, a -> a + 1) //Объявляем итератор стрима
                .limit(1000000)  //Ограничиваем число до 1000 включительно. Начиная с 1 прибавляем по 1.
                .parallel() //Распараллеливаем потоки в пулле
                .reduce(0, Integer::sum); //Суммируем собранные потоки в конце
        System.out.println("Время суммирования с использованием параллельных стримов составило: " + (System.currentTimeMillis() - start) + " мс");

        //Проверка времени сумирования без использования метода parallel(), но со стримом:
        start = System.currentTimeMillis();
        Integer result2 = Stream.iterate(1, a -> a + 1) //Объявляем итератор стрима
                .limit(1000000)  //Ограничиваем число до 1000 включительно. Начиная с 1 прибавляем по 1.
                .reduce(0, Integer::sum); //Суммируем собранные потоки в конце
        System.out.println("Время суммирования без использования параллельных стримов составило: " + (System.currentTimeMillis() - start) + " мс");

        //Проверка времени сумирования без использования стрима вообще:
        start = System.currentTimeMillis();
        int totalCount = 0;
        for (int i = 0; i <= 1000000; i++) {
            totalCount += i;
        }
        System.out.println("Время суммирования без использования стрима составило: " + (System.currentTimeMillis() - start) + " мс");

        return result;
    }

    //ДЗ-4.6 Потоки
    //Шаг 1: Несинхронизированный вывод студентов в 3-х параллельных потоках (вперемешку):
    public String getNameAllStudentsThread() {
        List<Student> students = studentRepository.findAll();
        if (students.isEmpty()) {
            return null;
        }
        List<String> studentName = students.stream()
                .map(Student::getName)
                .collect(Collectors.toList());
        studentName.forEach(i -> System.out.println(i));
        System.out.println("Запуск метода несинхронизированных потоков getNameAllStudentsThread():");

        System.out.println("Основной поток:");
        studentName.stream().limit(2).forEach(System.out::println);

        new Thread(() -> {
            System.out.println("Первый дочерний поток:");
            studentName.stream().skip(2).limit(2).forEach(System.out::println);
        }).start();

        new Thread(() -> {
            System.out.println("Второй дочерний поток:");
            studentName.stream().skip(4).limit(2).forEach(System.out::println);
        }).start();

        return students.toString();
    }

    //Шаг 2: Синхронизированный вывод студентов в 3-х параллельных потоках (но в соответствии с очерёдностью вывода через getAllStudent()):
    public String getNameAllStudentsThreadSynchronization() {
        List<Student> students = studentRepository.findAll();
        if (students.isEmpty()) {
            return null;
        }
        System.out.println("Запуск метода синхронизированных потоков getNameAllStudentsThreadSynchronization():");

        System.out.println("Основной поток:");
        synchronizePart(students, 0);
        synchronizePart(students, 1);

        new Thread(() -> {
            System.out.println("Первый дочерний поток:");
            synchronizePart(students, 2);
            synchronizePart(students, 3);
        }).start();

        new Thread(() -> {
            System.out.println("Второй дочерний поток:");
            synchronizePart(students, 4);
            synchronizePart(students, 5);
        }).start();

        return students.toString();
    }

    //Метод к ДЗ-4.6 с блоком синхронизации для getNameAllStudentsThreadSynchronization():
    private void synchronizePart(List<Student> students, int index) {
        synchronized (flag) {
            if (index >= 0 && index < students.size()) {
                System.out.println(students.get(index).getName());
            }
        }
    }
}

