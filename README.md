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

2. W drugim terminalu uruchom API. API tworzy schemat bazy przy starcie. Stan bazy i API sprawdzisz na [porcie 8080](http://127.0.0.1:8080/actuator/health).

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

Baza używa lokalnego portu 5455. Jeśli jest zajęty, przed startem ustaw np. `$env:POSTGRES_PORT = '5456'` w terminalu bazy i w terminalu API. Jeśli uruchamiasz API pod innym adresem, w terminalu frontendu ustaw `API_BASE_URL` na ten adres.

## Lokalny schemat bazy na gałęzi login-register

Baza PostgreSQL `agile` zawiera tabele `roles`, `app_users`, `statuses`, `projects`, `tasks` i `kanban` z polami oraz relacjami z przekazanego schematu. Definicja znajduje się w `project-rest-api/src/main/resources/schema.sql` i jest wykonywana przez API. Identyfikatory projektów, zadań i kart kanban są UUID; role i statusy mają liczbowe identyfikatory. Daty utworzenia i UUID mają wartości domyślne w bazie.

Lokalne Compose ma nazwę `agile-login-register-local` i osobny wolumen `agile-schema-data`. Dotychczasowy wolumen `postgres-data` i jego dane pozostają zachowane. Nie podłączaj starej bazy z tabelą `projects(id BIGINT, name)` do nowej wersji API: zmiana nie migruje dotychczasowych danych.

Odwołanie `app_users.id → auth.users.id` działa dzięki lokalnej tabeli `auth.users`, zawierającej wyłącznie UUID. To tabela pomocnicza, bez usługi uwierzytelniania. Schemat zachowuje pole `app_users.haslo`; można przechowywać w nim wyłącznie skrót hasła. Konto techniczne ma w tym polu `NULL`.

Inicjalizacja dodaje role „Administrator” i „Użytkownik”, statusy „Do zrobienia”, „W trakcie”, „Zrobione” i użytkownika technicznego `00000000-0000-0000-0000-000000000001` (`local@example.invalid`). Obecny formularz tworzy projekty przypisane do tego użytkownika, zapisując nazwę w `projects.tytul`. API nadal przyjmuje `name`, ale zwraca `id` jako tekst UUID. Formularze logowania i rejestracji pozostają widokami; podłączenie uwierzytelniania jest osobnym zadaniem.

Uruchomienie całego projektu: `docker compose up --build --wait`. Po zatrzymaniu usług dane są zachowane w wolumenie.

### Testy schematu na PostgreSQL

Testy MockMvc uruchamiają się podczas zwykłego `build`. Dodatkowe testy `DatabaseSchemaTest` wymagają osobnej, pustej bazy testowej. Tworzą schemat w transakcji i wycofują dane po każdym teście:

```powershell
docker compose exec -T db createdb -U projekty_app agile_schema_test
$env:AGILE_TEST_DATABASE_URL = 'jdbc:postgresql://127.0.0.1:5455/agile_schema_test'
$env:AGILE_TEST_DATABASE_USER = 'projekty_app'
$env:AGILE_TEST_DATABASE_PASSWORD = 'projekty_local' # lub ustawione POSTGRES_PASSWORD
java -jar gradle/wrapper/gradle-wrapper.jar build
```

Bazę testową tworzysz tylko raz. Testy sprawdzają ponowną inicjalizację, zapis projektu → zadania → kanban, generowanie UUID i dat oraz odrzucanie błędnych powiązań i projektu bez właściciela.

## Pokaz na zajęciach

Na pustej bazie strona wyświetla „Nie ma jeszcze projektów”. Kliknij „Zapisz” bez wpisywania nazwy, a potem wpisz same spacje i powtórz. W obu przypadkach pojawi się „Wpisz nazwę projektu”. Zapisz „Projekt Alfa” i „Projekt Beta”. Pojawią się na liście, a po odświeżeniu nadal będą widoczne.

Na już używanej bazie do pokazu wybierz nową nazwę. Jeśli chcesz pokazać pustą listę, zatrzymaj bieżący zestaw usług i uruchom `docker compose -p agile-sprint-demo up --build --wait`. Ten zestaw używa osobnego wolumenu. Po pokazie zatrzymaj go poleceniem `docker compose -p agile-sprint-demo stop`.

## Moduły i sprawdzenie

- `project-web-app` obsługuje stronę pod `/` i formularz pod `/projects`.
- `project-rest-api` zwraca listę w `GET /api/projects` i zapisuje `{"name":"Projekt Alfa"}` przez `POST /api/projects`. Poprawny zapis zwraca HTTP 201 z identyfikatorem i nazwą. Pusta nazwa, same spacje lub nazwa przekraczająca 120 znaków zwracają HTTP 400.
- `java -jar gradle/wrapper/gradle-wrapper.jar build` buduje oba moduły i uruchamia testy MockMvc z Mockito. Testy nie wymagają działającej bazy.

Plan prac jest w [dokumentach zespołu](docs). Instrukcje frameworków: [RestClient](https://docs.spring.io/spring-framework/reference/integration/rest-clients.html), [inicjalizacja bazy](https://docs.spring.io/spring-boot/how-to/data-initialization.html) i [Thymeleaf](https://www.thymeleaf.org/doc/tutorials/3.1/usingthymeleaf.html).
