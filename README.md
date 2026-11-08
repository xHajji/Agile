# Agile Sprint 1 · Thymeleaf

Sprint 1 pozwala dodać projekt i odczytać zapisaną listę. Formularz wysyła nazwę do aplikacji Spring MVC. Ta przekazuje ją przez RestClient do osobnego API Spring Boot, które zapisuje projekt w PostgreSQL. Thymeleaf generuje widok listy na serwerze.

## Uruchomienie całego projektu

Włącz Docker Desktop i w terminalu w katalogu z plikiem `compose.yaml` wpisz:

```powershell
docker compose up --build --wait
```

Docker buduje API i frontend z kodu źródłowego, uruchamia PostgreSQL i czeka na gotowość wszystkich usług. Przy pierwszym uruchomieniu pobiera obrazy bazowe i zależności Gradle. Java na komputerze nie jest wymagana.

Otwórz [stronę Projekty](http://127.0.0.1:8081/). Stan API i bazy sprawdzisz pod adresem [Actuator Health](http://127.0.0.1:8080/actuator/health). Polecenie `docker compose stop` zatrzymuje wszystkie trzy usługi i zachowuje dane w wolumenie PostgreSQL.

## Uruchomienie modułów Javy z IDE lub terminala

Potrzebujesz JDK 21 oraz działającego Docker Desktop z Compose. Polecenia wykonuj w katalogu z plikiem `compose.yaml`. Na Windows zalecana jest ścieżka bez polskich znaków. Jeśli projekt jest już w takiej ścieżce jak `D:\SZKOŁA`, użyj polecenia `java -jar` poniżej oraz katalogu `GRADLE_USER_HOME` bez polskich znaków. Jeśli ten katalog jest ustawiony, stosuj to samo ustawienie w każdym terminalu Gradle.

1. Uruchom bazę. Terminal możesz później zamknąć.

   ```powershell
   docker compose up -d --wait db
   ```

2. W drugim terminalu uruchom API. API tworzy tabelę `projects` przy pierwszym starcie. Stan bazy i API sprawdzisz na [porcie 8080](http://127.0.0.1:8080/actuator/health).

   ```powershell
   $env:GRADLE_USER_HOME = 'D:\Agile-Sprint1-Gradle'
   java -jar gradle/wrapper/gradle-wrapper.jar :project-rest-api:bootRun
   ```

3. W trzecim terminalu uruchom widok i otwórz [stronę Projekty](http://127.0.0.1:8081/).

   ```powershell
   $env:GRADLE_USER_HOME = 'D:\Agile-Sprint1-Gradle'
   java -jar gradle/wrapper/gradle-wrapper.jar :project-web-app:bootRun
   ```

   Na macOS lub Linuksie użyj `./gradlew :project-rest-api:bootRun` i `./gradlew :project-web-app:bootRun`.

Jeśli port 5432 jest zajęty, przed startem ustaw np. `$env:POSTGRES_PORT = '5433'` w terminalu bazy i w terminalu API. Jeśli uruchamiasz API pod innym adresem, w terminalu frontendu ustaw `API_BASE_URL` na ten adres.

## Pokaz na zajęciach

Na pustej bazie strona wyświetla „Nie ma jeszcze projektów”. Kliknij „Zapisz” bez wpisywania nazwy, a potem wpisz same spacje i powtórz. W obu przypadkach pojawi się „Wpisz nazwę projektu”. Zapisz „Projekt Alfa” i „Projekt Beta”. Pojawią się na liście, a po odświeżeniu nadal będą widoczne.

Na już używanej bazie do pokazu wybierz nową nazwę. Jeśli chcesz pokazać pustą listę, zatrzymaj bieżący zestaw usług i uruchom `docker compose -p agile-sprint-demo up --build --wait`. Ten zestaw używa osobnego wolumenu. Po pokazie zatrzymaj go poleceniem `docker compose -p agile-sprint-demo stop`.

## Moduły i sprawdzenie

- `project-web-app` obsługuje stronę pod `/` i formularz pod `/projects`.
- `project-rest-api` zwraca listę w `GET /api/projects` i zapisuje `{"name":"Projekt Alfa"}` przez `POST /api/projects`. Poprawny zapis zwraca HTTP 201 z identyfikatorem i nazwą. Pusta nazwa, same spacje lub nazwa przekraczająca 120 znaków zwracają HTTP 400.
- `java -jar gradle/wrapper/gradle-wrapper.jar build` buduje oba moduły i uruchamia testy MockMvc z Mockito. Testy nie wymagają działającej bazy.

Plan prac jest w [dokumentach zespołu](docs). Instrukcje frameworków: [RestClient](https://docs.spring.io/spring-framework/reference/integration/rest-clients.html), [inicjalizacja bazy](https://docs.spring.io/spring-boot/how-to/data-initialization.html) i [Thymeleaf](https://www.thymeleaf.org/doc/tutorials/3.1/usingthymeleaf.html).
