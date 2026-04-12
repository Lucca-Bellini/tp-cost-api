# --- Fase 1: Construção ---
# Usa uma imagem oficial do Maven com Java 21 para compilar o projeto
FROM maven:3.9.9-eclipse-temurin-21 AS build

# Define o diretório de trabalho dentro do contêiner
WORKDIR /app

# Copia o arquivo de configuração do Maven e o código-fonte
COPY pom.xml .
COPY src ./src

# Executa o comando para baixar as dependências e gerar o arquivo .jar
RUN mvn clean package -DskipTests

# --- Fase 2: Execução ---
# Usa uma imagem menor, com apenas o Java 21, para rodar a aplicação
FROM eclipse-temurin:21-jre-alpine

# Define o diretório de trabalho
WORKDIR /app

# Copia o arquivo .jar gerado na fase de construção para a imagem final
COPY --from=build /app/target/*.jar app.jar

# Declara a porta que a aplicação vai usar (importante para o Render)
EXPOSE 8080

# Comando para iniciar a aplicação quando o contêiner for executado
ENTRYPOINT ["java", "-jar", "app.jar"]