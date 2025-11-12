#
# Etapa de construcción
#
FROM gradle:8.7-jdk21 AS build
USER gradle
WORKDIR /home/gradle/project

# Copiar solo archivos necesarios primero para aprovechar el cache de dependencias
COPY --chown=gradle:gradle build.gradle settings.gradle ./
COPY --chown=gradle:gradle gradle gradle
RUN gradle --no-daemon build -x test || return 0

# Copiar el resto del código fuente
COPY --chown=gradle:gradle . .

# Construir el archivo JAR
RUN gradle --no-daemon bootJar

#
# Etapa de empaquetado
#
FROM eclipse-temurin:21-jre
ENV APP_HOME=/app
WORKDIR ${APP_HOME}

# Argumento opcional para puerto
ARG PORT=8080
ENV PORT=${PORT}

# Copiar el JAR generado desde la etapa de construcción
COPY --from=build /home/gradle/project/build/libs/*.jar app.jar

EXPOSE ${PORT}

# Limitar memoria de JVM para Railway (512MB plan gratuito)
# -Xmx: Memoria máxima del heap
# -Xms: Memoria inicial del heap
# -XX:MaxMetaspaceSize: Memoria máxima para metaspace
# -XX:+UseContainerSupport: Detecta límites del contenedor
ENTRYPOINT ["java", \
    "-Xms256m", \
    "-Xmx384m", \
    "-XX:MaxMetaspaceSize=128m", \
    "-XX:+UseContainerSupport", \
    "-XX:+ExitOnOutOfMemoryError", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]