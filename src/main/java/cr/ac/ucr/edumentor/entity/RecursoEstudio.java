package cr.ac.ucr.edumentor.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "recurso_estudio")
public class RecursoEstudio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sesion_id", nullable = false)
    private SesionTutoria sesion;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(name = "url_recurso", nullable = false, length = 500)
    private String urlRecurso;

    public RecursoEstudio() {
    }

    public RecursoEstudio(SesionTutoria sesion, String titulo, String urlRecurso) {
        this.sesion = sesion;
        this.titulo = titulo;
        this.urlRecurso = urlRecurso;
    }

    public Long getId() {
        return id;
    }

    public SesionTutoria getSesion() {
        return sesion;
    }

    public void setSesion(SesionTutoria sesion) {
        this.sesion = sesion;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getUrlRecurso() {
        return urlRecurso;
    }

    public void setUrlRecurso(String urlRecurso) {
        this.urlRecurso = urlRecurso;
    }
}
