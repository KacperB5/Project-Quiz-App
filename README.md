<div align="center">

# 📱 QuizApp

### Natywna Aplikacja Quizowa na Android

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9-7F52FF?style=flat-square&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Android](https://img.shields.io/badge/Android-API%2033+-3DDC84?style=flat-square&logo=android&logoColor=white)](https://developer.android.com)
[![PHP](https://img.shields.io/badge/PHP-8.x-777BB4?style=flat-square&logo=php&logoColor=white)](https://www.php.net)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=flat-square&logo=mysql&logoColor=white)](https://www.mysql.com)
[![License: MIT](https://img.shields.io/badge/License-MIT-03DAC5?style=flat-square)](LICENSE)

**QuizApp** to nowoczesna, w pełni natywna aplikacja mobilna na platformę Android, łącząca elementy gry edukacyjnej, rywalizacji sieciowej i panelu analitycznego. Komunikuje się asynchronicznie z zewnętrzną bazą danych MySQL przez dedykowane API napisane w PHP.

[Funkcjonalności](#-funkcjonalności) · [Architektura](#-architektura) · [Uruchomienie](#-uruchomienie) · [API](#-endpointy-api) · [Baza danych](#-baza-danych) · [Autorzy](#-autorzy)

</div>

---

## Funkcjonalności

### Tryb Jednoosobowy (Singleplayer)
- **Losowe pytania** — do 10 losowo wybranych pytań na sesję
- **Presja czasu** — 10-sekundowy pasek odliczający czas odpowiedzi (`CountDownTimer` + `ProgressBar`)
- **Multimedia** — pytania mogą zawierać obrazy (ładowane przez Glide) lub wideo `.mp4` (odtwarzane w `WebView` z HTML5 `<video>`)
- **Ekran wyników** — podsumowanie z liczbą punktów, procentową celnością, całkowitym czasem rozgrywki i średnim czasem na pytanie

### Tryb Wieloosobowy (Multiplayer)
- **Pokoje gier** — tworzenie pokoju dla 2 lub 3 graczy z unikalnym 4-cyfrowym PIN-em
- **Synchronizacja w czasie rzeczywistym** — polling co 1 s przez `sync_question.php`; automatyczny timeout po 15 s
- **Wizualizacja rywali** — odpowiedzi przeciwników pokazane ikonką 👤 na przyciskach odpowiedzi
- **Kolejne rundy** — host może wybrać nowy quiz bez opuszczania pokoju
- **Ekran podium** — system wykrywa zwycięzcę, remis i przegraną

### Kreator Quizów
- Tworzenie nowych kategorii (quizów) z poziomu aplikacji
- Formularz pytania: treść, URL obrazu/wideo, 4 warianty odpowiedzi (A–D), wskazanie poprawnej odpowiedzi
- Pytania trafiają do **kolejki moderacji** przed publikacją

### Panel Moderacji (Admin)
- Rola `admin` pobierana z bazy danych przy logowaniu
- Przeglądanie kolejki oczekujących pytań
- Akcje: **ZATWIERDŹ** (pytanie aktywne) lub **ODRZUĆ** (pytanie usuwane)

### Statystyki i Ranking
- **Profil gracza**: liczba rozegranych gier, najlepszy wynik %, średnia celność, średni czas odpowiedzi, ulubiony tryb
- **Globalny ranking** — lista graczy posortowana wg sumarycznej liczby punktów

### Wielojęzyczność (PL / EN)
- Przełącznik flag 🇵🇱 / 🇬🇧 na ekranie powitalnym
- Zmiana języka **bez restartu aplikacji** — `attachBaseContext()` + `AppCompatDelegate.setApplicationLocales()` + `recreate()`
- Wszystkie zasoby tekstowe w `strings.xml` (domyślny EN) i `strings.xml` w `values-pl/` (PL)

---

## Architektura

```
┌─────────────────────────────────────────────┐
│              Android App (Kotlin)            │
│                                             │
│  MainActivity ──► WelcomeActivity           │
│       │                  │                  │
│  RegisterActivity   ┌────┴────┐             │
│                     │Fragments│             │
│                     │ Quizzes │             │
│                     │ Ranking │             │
│                     │  Stats  │             │
│                     │  About  │             │
│                     └────┬────┘             │
│                          │                  │
│   ChooseQuizActivity ◄───┤                  │
│   PlayQuizActivity       │                  │
│   MultiplayerActivity    │                  │
│   LobbyActivity          │                  │
│   MultiplayerScoreActivity                  │
│   CreateQuizActivity                        │
└──────────────┬──────────────────────────────┘
               │  HTTP (Volley / OkHttp)
               ▼
┌─────────────────────────────────────────────┐
│         Backend PHP — alwaysdata.net        │
│              /api/*.php                     │
└──────────────┬──────────────────────────────┘
               │  PDO / MySQLi
               ▼
┌─────────────────────────────────────────────┐
│               MySQL Database                │
│  users · quizzes · questions · results      │
│  rooms · room_players · room_scores         │
└─────────────────────────────────────────────┘
```

### Stack technologiczny

| Warstwa | Technologia |
|---------|-------------|
| Język aplikacji | Kotlin |
| UI | XML Layouts + Material Design 3 (ciemny motyw `#121212`, akcent `#03DAC5`) |
| Komunikacja sieciowa | [Volley 1.2.1](https://github.com/google/volley) (GET/POST JSON), [OkHttp 4.12.0](https://square.github.io/okhttp/) (rejestracja) |
| Ładowanie obrazów | [Glide 4.16.0](https://github.com/bumptech/glide) |
| Backend | PHP 8.x z PDO + Prepared Statements |
| Baza danych | MySQL 8.0 |
| Hosting | [alwaysdata.net](https://www.alwaysdata.com) |
| Min SDK | 33 (Android 13) |
| Target SDK | 36 |

---

## Uruchomienie

### Wymagania
- Android Studio **Hedgehog** (2023.1.1) lub nowszy
- JDK 11+
- Urządzenie / emulator z Android **API 33+** (rekomendowane API 34+)
- Dostęp do internetu

### Kroki

1. **Klonuj repozytorium**
   ```bash
   git clone https://github.com/KacperB5/Project-Quiz-App.git
   cd Project-Quiz-App
   ```

2. **Otwórz w Android Studio**
   ```
   File → New → Import Project → wskaż folder główny
   ```
   Poczekaj na zakończenie synchronizacji Gradle.

3. **Sprawdź zasoby językowe**
   - Angielski (domyślny): `app/src/main/res/values/strings.xml`
   - Polski: `app/src/main/res/values-pl/strings.xml`

4. **Uruchom aplikację**
   - Podłącz telefon z włączonym **USB Debugging** lub uruchom emulator
   - Kliknij **Run app** w Android Studio

5. **Czyszczenie przy problemach z zasobami**
   ```
   Build → Clean Project → Build → Rebuild Project
   ```

> **Uwaga:** Aplikacja korzysta z backendu hostowanego pod adresem `https://quiz-app.alwaysdata.net/api/`. Nie jest wymagana żadna lokalna konfiguracja serwera.

---

## Endpointy API

Wszystkie endpointy dostępne pod: `https://quiz-app.alwaysdata.net/api/`

### Autoryzacja

| Endpoint | Metoda | Opis |
|----------|--------|------|
| `login.php` | POST | Logowanie — zwraca `username` i `role` |
| `register.php` | POST | Rejestracja nowego użytkownika |

### Quizy i pytania

| Endpoint | Metoda | Opis |
|----------|--------|------|
| `get_quiz_list.php` | GET | Lista wszystkich kategorii quizów |
| `quizzes.php?quiz_id=N` | GET | Pytania dla danego quizu |
| `add_quiz_category.php` | POST | Tworzenie nowej kategorii |
| `add_question.php` | POST | Dodanie pytania do kolejki moderacji |

### Moderacja

| Endpoint | Metoda | Opis |
|----------|--------|------|
| `moderate_content.php` | GET/POST | Pobieranie kolejki i zatwierdzanie/odrzucanie pytań |

### Wyniki i statystyki

| Endpoint | Metoda | Opis |
|----------|--------|------|
| `save_result.php` | POST | Zapis wyniku ukończonego quizu |
| `get_user_stats.php` | GET | Statystyki profilu użytkownika |
| `get_ranking.php` | GET | Globalny ranking graczy |

### Multiplayer

| Endpoint | Metoda | Opis |
|----------|--------|------|
| `create_room.php` | POST | Tworzenie pokoju gry (generuje PIN) |
| `join_room.php` | POST | Dołączanie do pokoju przez PIN |
| `check_room_status.php` | GET | Status pokoju (waiting / playing) |
| `sync_question.php` | POST | Synchronizacja odpowiedzi graczy |
| `get_room_results.php` | GET | Wyniki końcowe pokoju |
| `check_next_round.php` | GET | Sprawdzenie czy host wybrał nową rundę |
| `next_round.php` | POST | Ustawienie nowego quizu przez hosta |
| `close_room.php` | POST | Zamknięcie pokoju przez hosta |

---

## Baza danych

### Tabela `results`

| Kolumna | Typ | Opis |
|---------|-----|------|
| `id` | INT AUTO_INCREMENT | Klucz główny |
| `username` | VARCHAR(100) | Identyfikator gracza |
| `quiz_id` | INT | ID powiązanego quizu |
| `score` | INT | Punkty zdobyte przez gracza |
| `max_score` | INT | Maksymalna możliwa liczba punktów |
| `average_time` | DOUBLE | Średni czas odpowiedzi (sekundy) |

> Backend stosuje **Prepared Statements** (PDO) we wszystkich zapytaniach, co chroni przed atakami SQL Injection.

---

## Struktura projektu

```
app/src/main/
├── java/
│   ├── MainActivity.kt          # Ekran logowania
│   ├── RegisterActivity.kt      # Rejestracja (OkHttp)
│   ├── WelcomeActivity.kt       # Ekran główny + nawigacja dolna + zmiana języka
│   ├── MenuActivity.kt          # Menu trybu singleplayer
│   ├── ChooseQuizActivity.kt    # Wybór quizu (solo lub multiplayer)
│   ├── PlayQuizActivity.kt      # Rozgrywka (timer, multimedia, sync)
│   ├── MultiplayerActivity.kt   # Tworzenie / dołączanie do pokoju
│   ├── LobbyActivity.kt         # Oczekiwanie na start (polling)
│   ├── MultiplayerScoreActivity.kt  # Ekran wyników multiplayer
│   ├── CreateQuizActivity.kt    # Kreator pytań + panel moderacji
│   ├── RankingActivity.kt       # Ranking (Activity)
│   ├── QuizzesFragment.kt       # Fragment: wybór trybu gry
│   ├── RankingFragment.kt       # Fragment: globalny ranking
│   ├── StatsFragment.kt         # Fragment: statystyki użytkownika
│   └── AboutFragment.kt         # Fragment: samouczek krok-po-kroku
├── res/
│   ├── layout/                  # Pliki XML layoutów
│   ├── values/strings.xml       # Zasoby tekstowe (EN, domyślne)
│   ├── values-pl/strings.xml    # Zasoby tekstowe (PL)
│   ├── anim/                    # Animacje (fade_in, slide_in_right…)
│   └── drawable/                # Ikony i zasoby graficzne
└── AndroidManifest.xml
```

---

## Zrzuty ekranu

> *(Dodaj zrzuty ekranu do folderu `/screenshots` i zaktualizuj ścieżki poniżej)*

| Logowanie | Wybór quizu | Rozgrywka | Multiplayer |
|-----------|-------------|-----------|-------------|
| ![login](screenshots/login.png) | ![quiz](screenshots/choose_quiz.png) | ![play](screenshots/play.png) | ![multi](screenshots/multiplayer.png) |

---

## Autorzy

| Imię i nazwisko | Rola |
|----------------|------|
| **Kacper Bałabuch** | Android (Kotlin), Backend PHP |
| **Daniel Wilk** | Android (Kotlin), Backend PHP |
| **Aleksy Pietrzniak** | Android (Kotlin), Backend PHP |

---

## Licencja

Projekt udostępniony na licencji **MIT**. Szczegóły w pliku [LICENSE](LICENSE).

---

<div align="center">

Made in Kotlin · PHP · MySQL

</div>
