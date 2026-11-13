package cr.ac.ucr.edumentor.config;

import cr.ac.ucr.edumentor.entity.Materia;
import cr.ac.ucr.edumentor.repository.MateriaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MateriasIniciales implements CommandLineRunner {

    private final MateriaRepository materiaRepository;

    public MateriasIniciales(MateriaRepository materiaRepository) {
        this.materiaRepository = materiaRepository;
    }

    @Override
    public void run(String... args) {
        // La base es de archivo: si ya hay materias no se vuelven a insertar
        if (materiaRepository.count() > 0) {
            return;
        }
        materiaRepository.saveAll(List.of(
                new Materia("IF1300", "Introducción a la Computación", "Informática Empresarial", 4),
                new Materia("IF2000", "Programación I", "Informática Empresarial", 4),
                new Materia("IF3000", "Programación II", "Informática Empresarial", 4),
                new Materia("IF4100", "Fundamentos de Bases de Datos", "Informática Empresarial", 4),
                new Materia("IF0009", "Desarrollo de Software IV", "Informática Empresarial", 4),
                new Materia("MA0320", "Estructuras Discretas", "Informática Empresarial", 4),
                new Materia("MA1001", "Cálculo I", "Ingeniería", 4),
                new Materia("FS0210", "Física General I", "Ingeniería", 3)
        ));
    }
}
