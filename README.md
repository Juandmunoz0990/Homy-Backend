# 🏡 Proyecto de Alojamientos

Este proyecto consiste en el desarrollo de una plataforma para la gestión de alojamientos.  
Su objetivo principal es permitir a los usuarios **buscar, reservar y administrar alojamientos** de manera sencilla y rápida, al mismo tiempo que los anfitriones pueden **publicar, editar y controlar sus propiedades**.  

La aplicación busca ofrecer una experiencia similar a la de plataformas reconocidas (como Airbnb), pero adaptada a nuestras necesidades específicas.  

---

## 🛠️ Tecnologías utilizadas

- **Frontend:** React / Next.js, TailwindCSS  
- **Backend:** Spring Boot, JPA, MySQL  
- **Autenticación:** Spring Security con JWT  
- **Infraestructura:** Monorepo con integración frontend-backend

---

## 👥 Integrantes del equipo

- Juan Pablo Rodríguez  
- Miguel Angel Vasquez
- Juan David Muñoz
- Juan Felipe Hurtado (Ingeniería de Software II)
- Sebastián Agudelo (Ingeniería de Software II)

## Observaciones:
- Hacer exception handler de todas las excepciones.
- throws excpetion en controllers?
- records for DTOs
- validaciones for dtos requests
- MethodArgumentNotValidException: Excepción que se lanza cuando no se cumple alguna validación puesta en los DTO (@NotNull, @Email, @Max, etc..). Para este caso debe crear un nuevo DTO con el nombre: ValidationDTO, este record debe debe tener dos String: field y message.
- 
