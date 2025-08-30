.SILENT:


all:
	./mvnw spring-boot:run

build:
	./mvnw clean verify package

purge:
	./mvnw dependency:purge-local-repository
