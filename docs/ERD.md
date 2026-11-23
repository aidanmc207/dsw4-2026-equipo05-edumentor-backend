# Modelo Entidad-Relación

Base de datos H2 en modo archivo. Los enums se guardan como texto (`@Enumerated(EnumType.STRING)`).

```mermaid
erDiagram
    USUARIO ||--o{ DISPONIBILIDAD_TUTOR : "publica"
    USUARIO ||--o{ SESION_TUTORIA : "reserva (estudiante)"
    USUARIO ||--o{ SESION_TUTORIA : "imparte (tutor)"
    MATERIA ||--o{ SESION_TUTORIA : "se trata en"
    SESION_TUTORIA ||--o{ RECURSO_ESTUDIO : "tiene"
    SESION_TUTORIA ||--o| EVALUACION_TUTORIA : "recibe"

    USUARIO {
        BIGINT id PK
        VARCHAR(20) carne UK "NOT NULL"
        VARCHAR(120) email UK "NOT NULL"
        VARCHAR(255) password_hash "NOT NULL"
        VARCHAR(60) nombre "NOT NULL"
        VARCHAR(60) apellido "NOT NULL"
        VARCHAR(20) rol "NOT NULL: ESTUDIANTE, TUTOR, COORDINADOR"
        DOUBLE promedio_calificacion "NOT NULL, default 0"
        INT total_evaluaciones "NOT NULL, default 0"
    }

    MATERIA {
        BIGINT id PK
        VARCHAR(10) codigo_materia UK "NOT NULL"
        VARCHAR(100) nombre_materia "NOT NULL"
        VARCHAR(100) carrera "NOT NULL"
        INT creditos "NOT NULL"
    }

    DISPONIBILIDAD_TUTOR {
        BIGINT id PK
        BIGINT tutor_id FK "NOT NULL"
        VARCHAR(10) dia_semana "NOT NULL: MONDAY ... SUNDAY"
        TIME hora_inicio "NOT NULL"
        TIME hora_fin "NOT NULL, mayor que hora_inicio"
        BOOLEAN esta_activo "NOT NULL, default true"
    }

    SESION_TUTORIA {
        BIGINT id PK
        BIGINT estudiante_id FK "NOT NULL"
        BIGINT tutor_id FK "NOT NULL"
        BIGINT materia_id FK "NOT NULL"
        TIMESTAMP fecha_hora "NOT NULL"
        INT duracion_minutos "NOT NULL"
        VARCHAR(20) estado "NOT NULL"
        VARCHAR(20) modalidad "NOT NULL: VIRTUAL, PRESENCIAL"
    }

    RECURSO_ESTUDIO {
        BIGINT id PK
        BIGINT sesion_id FK "NOT NULL"
        VARCHAR(150) titulo "NOT NULL"
        VARCHAR(500) url_recurso "NOT NULL"
        VARCHAR(30) tipo_archivo "NULL"
    }

    EVALUACION_TUTORIA {
        BIGINT id PK
        BIGINT sesion_id FK,UK "NOT NULL"
        INT calificacion_estrellas "NOT NULL, entre 1 y 5"
        VARCHAR(500) comentario "NULL"
        TIMESTAMP fecha_evaluacion "NOT NULL"
    }
```

## Relaciones

| Relación | Cardinalidad | FK |
|---|---|---|
| Usuario (tutor) – DisponibilidadTutor | 1 : N | `disponibilidad_tutor.tutor_id` |
| Usuario (estudiante) – SesionTutoria | 1 : N | `sesion_tutoria.estudiante_id` |
| Usuario (tutor) – SesionTutoria | 1 : N | `sesion_tutoria.tutor_id` |
| Materia – SesionTutoria | 1 : N | `sesion_tutoria.materia_id` |
| SesionTutoria – RecursoEstudio | 1 : N | `recurso_estudio.sesion_id` |
| SesionTutoria – EvaluacionTutoria | 1 : 0..1 | `evaluacion_tutoria.sesion_id` (única) |

Usuario y Materia quedan en relación N:M a través de `sesion_tutoria`: un estudiante puede recibir tutorías de muchas materias y una materia la reciben muchos estudiantes.

## Restricciones

- `usuario.carne`, `usuario.email` y `materia.codigo_materia` son únicos.
- `evaluacion_tutoria.sesion_id` es único: una sesión se evalúa una sola vez.
- `estado` toma los valores `REQUESTED`, `CONFIRMED`, `REJECTED`, `CANCELLED`, `IN_SESSION`, `COMPLETED`, `REVIEWED`.
- `disponibilidad_tutor.tutor_id` y `sesion_tutoria.tutor_id` deben apuntar a un usuario con rol `TUTOR`. Esto se valida en la capa de servicio, no en la base de datos.
- Los traslapes de sesiones y la regla de cancelación con 2 horas también se validan en el servicio.
