package ru.hogwarts.school.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import ru.hogwarts.school.exception.NoFacultyColorException;
import ru.hogwarts.school.exception.NullEmptyColorException;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.FacultyRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FacultyService {

    private final FacultyRepository facultyRepository;

    private final Logger logger = LoggerFactory.getLogger(FacultyService.class); //ДЗ-4.6 Включение логирования результатов для факультета

    public FacultyService(FacultyRepository facultyRepository) {
        logger.info("The constructor of the FacultyService class is launched");
        this.facultyRepository = facultyRepository;
    }

    public Faculty createFaculty(Faculty faculty) {
        logger.info("Was invoked method for create faculty");
        return facultyRepository.save(faculty);
    }

    public Faculty findFaculty(Long id) {
       logger.info("a method for searching a faculty by its id has been launched: " + id);
       Faculty faculty = facultyRepository.findById(id).orElse(null);
       return faculty;
    }

    public Faculty editFaculty(Faculty faculty) {
        logger.info("Faculty editing method started");
        return facultyRepository.findById(faculty.getId())
                .map(e -> facultyRepository.save(faculty))
                .orElse(null);
    }

    public Faculty deleteFaculty(Long id) {
        logger.info("Method deleteFaculty started");
        var entity = facultyRepository.findById(id).orElse(null);
        if (entity != null) {
            facultyRepository.delete(entity);
        }
        return entity;
    }

    public Collection<Faculty> getAllFaculty() {
        logger.info("Method getAllFaculty started");
        return facultyRepository.findAll();
    }

    //ДЗ-3.3:
    public Collection<String> getFacultyByColor(String color) {
        logger.info("Method getFacultyByColor started");
        if (color == null || color.isEmpty()) {
            logger.error("There is no such color = " + color);
            throw new NullEmptyColorException();
        }
        Collection<String> facultyListByColor = getAllFaculty()
                .stream()
                .filter(e -> e.getColor().equals(color))
                .map(e -> e.getName())
                .collect(Collectors.toList());
        if (facultyListByColor.isEmpty()) {
            logger.error("There is not faculty with color = " + color);
            throw new NoFacultyColorException();
        }
        return facultyListByColor;
    }

    //ДЗ-3.4 шаг 1.2:
    public Faculty findByColorIgnoreCase(String color) {
        logger.info("Method findByColorIgnoreCase started");
        return facultyRepository.findByColorIgnoreCase(color);
    }

    //ДЗ-3.4 шаг 1.2(доп.):
    public Faculty findByNameAndColor(String name, String color) {
        logger.info("Method findByNameAndColor started");
        return facultyRepository.findByNameAndColor(name, color);
    }

    public Faculty findByName(String name) {
        logger.info("Method findByName started");
        return facultyRepository.findByName(name);
    }

    public Collection<Student> getStudentsOfFaculty(Long id) {
        logger.info("Method getStudentsOfFaculty started");
        return facultyRepository.findById(id)
                .map(Faculty::getStudents)
                .orElse(Collections.emptyList());
    }
}

