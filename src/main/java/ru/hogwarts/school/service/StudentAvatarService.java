package ru.hogwarts.school.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.hogwarts.school.exception.StudentNotFoundException;
import ru.hogwarts.school.model.Avatar;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.StudentAvatarRepository;

import javax.imageio.ImageIO;
import javax.persistence.Column;
import javax.transaction.Transactional;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static java.nio.file.StandardOpenOption.CREATE_NEW;

@Service
@Transactional
public class StudentAvatarService {

    //    @Value("${students.avatar.dir.path}") //Путь, где будет храниться наша картинка.
    private String avatarsDir;

    private final StudentService studentService;
    private final StudentAvatarRepository studentAvatarRepository;

    private final Logger logger = LoggerFactory.getLogger(StudentAvatarService.class); //ДЗ-4.6 Включение логирования результатов

    public StudentAvatarService(StudentService studentService, StudentAvatarRepository studentAvatarRepository, @Value("${students.avatar.dir.path}") String avatarsDir) {
        logger.info("The constructor of the StudentAvatarService class is launched");
        this.studentService = studentService;
        this.studentAvatarRepository = studentAvatarRepository;
        this.avatarsDir = avatarsDir;
    }

    public void uploadAvatar(Long id, MultipartFile file) throws IOException { // Метод входного потока для загрузки файла картинки
        logger.info("Was invoked method for uploadAvatar");
        // на указанное место на диске и в БД одновременно
        Student student = studentService.findStudent(id); // Находим объект студента по id.

        Path filePath = Path.of(avatarsDir, id + "." + getExtension(file.getOriginalFilename()));  // Путь к файлу сохраняем в переменной filePath
        System.out.println("id1 = " + id);
        Files.createDirectories(filePath.getParent()); // Создаем директорию для хранения файла
        Files.deleteIfExists(filePath);  // Удаляем из созданной папки предыдущий файл, если он там был
        try (InputStream is = file.getInputStream(); // Открываем входной поток для приёма файла file
             OutputStream os = Files.newOutputStream(filePath, CREATE_NEW); // Создаём выходной поток и в пустой файл
             //на ЖД ('id') по переменной filePath по байту перекачиваем содержимое файла file
             BufferedInputStream bis = new BufferedInputStream(is, 1024); // Буферные вход и далее - выход потока (собираем байты группами по 1024 байта каждая)
             BufferedOutputStream bos = new BufferedOutputStream(os, 1024)) {
            bis.transferTo(bos); // Запуск передачи данных из входного в выходной поток
        }
        Avatar avatar = findAvatar(id); //Поиск нашей картинки по id студента. Если её там не было, то создаём новый объект avatar, а если была, то редактируем (см. этот метод ниже)
        //Инициализация объекта avatar параметрами от входного файла:
        avatar.setStudent(student);
        avatar.setFilePath(filePath.toString()); // Указываем путь к файлу
        avatar.setFileSize(file.getSize()); // Указываем его размер
        avatar.setMediaType(file.getContentType()); // Указываем его контент
        avatar.setData(generateImagePreview(filePath)); // Создаём маленькую картинку, который с помощью метода generateImagePreview() уменьшает размер картинки,
//        avatar.setData(file.getBytes()); //Вот если вместо верхней строки вставить эту, тест на данный метод пройдёт, но почему-то пропадут картинки в файловой системе!
        //и ложим в массив байтов для БД.

        studentAvatarRepository.save(avatar); // Сохраняем этот объект в БД. Фактически будет сохранен массив байтов, а все остальные переменные объекта создавались для размещения на диске.
    }

    public Avatar findAvatar(Long id) {
        logger.info("Was invoked method for findAvatar");
        Student student = studentService.findStudent(id); //Чтобы не выдавал 500, если нет в базе такого студента
        logger.debug("student found in the Database");
        return studentAvatarRepository.findByStudentId(id).orElse(new Avatar()); // Этот метод в репозитории следует писать
    }

    public byte[] generateImagePreview(Path filePath) throws IOException { //Метод уменьшения картинки для БД
        logger.info("Was invoked method for generate Image Preview");
        try (InputStream is = Files.newInputStream(filePath);
            BufferedInputStream bis = new BufferedInputStream(is, 1024);
            ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            BufferedImage image = ImageIO.read(bis);
            int height = image.getHeight() / (image.getHeight() / 100);
            BufferedImage date = new BufferedImage(100, height, image.getType());
            Graphics2D graphics = date.createGraphics();
            graphics.drawImage(image, 0, 0, 100, height, null);
            graphics.dispose();
            ImageIO.write(date, getExtension(filePath.getFileName().toString()), baos);

            return baos.toByteArray();
        }
    }

    private String getExtension(String filename) {
        logger.info("Was invoked method for getExtension avatar");
        return filename.substring(filename.lastIndexOf(".") + 1);
    } //Определение формата расширения файла

    // Пагинация шаг 2 ДЗ-4.1 всего 1 метод: постраничный вывод аватарок:
    public List<Avatar> getAllAvatarsPage(Integer pageNumber, Integer pageSize) {
        logger.info("Was invoked method for getAllavatar avatar");
        PageRequest pageRequest = PageRequest.of(pageNumber - 1, pageSize);
        return studentAvatarRepository.findAll(pageRequest).getContent();
    }
}
