.SILENT:


all:
	./mvnw spring-boot:run

sync:
	./mvnw clean install

purge:
	./mvnw dependency:purge-local-repository
