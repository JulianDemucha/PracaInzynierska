# 🎵 SoundSpace

**Usługa strumieniowania muzyki z personalizowanymi rekomendacjami.**
*Projekt inżynierski (Politechnika Lubelska 2025).*

---

## 🚀 Szybki Start (Docker)

Aplikacja jest w pełni skonteneryzowana. Aby uruchomić środowisko deweloperskie:

1.  **Klonowanie repozytorium:**
    ```bash
    git clone https://github.com/JulianDemucha/PracaInzynierska.git
    cd soundspace
    ```

2.  **Konfiguracja:**
    Zmiana nazwy pliku konfiguracyjnego:
    ```bash
    mv .env.example .env
    ```

3.  **Uruchomienie:**
    ```bash
    docker-compose up --build
    ```

### 🔗 Dostępne usługi:
* **Aplikacja (Frontend):** [http://localhost](http://localhost) (Domyślny port 80)
* **API (Backend):** Dostępne wewnętrznie lub przez proxy na porcie 80.
* **Baza Danych:** Dostępna dla kontenerów (host: `db`, port: `5432`).

---

## ℹ️ O projekcie

**SoundSpace** to system realizujący funkcjonalności serwisu streamingowego w architekturze Monolitu Modułowego. Głównym celem projektu było stworzenie wydajnego mechanizmu dystrybucji treści audio oraz implementacja autorskich algorytmów dobierania treści (rekomendacji).

## 🔥 Kluczowe Funkcjonalności

### 🎧 Wydajny Streaming Audio
* **HTTP Range (Chunking):** Przesyłanie plików we fragmentach – płynne przewijanie bez pobierania całości.
* **Optymalizacja pamięci:** Strumieniowanie bezpośrednie (ResourceRegion) minimalizujące zużycie RAM.

### 🧠 Hybrydowy System Rekomendacji
* **Scoring Algorithm:** Algorytm ważący preferencje gatunkowe, autorów i historię.
* **ViewCap Logic:** Logarytmiczne skalowanie wyświetleń (anty-wiral).
* **Backfill & Cache:** Obsługa "zimnego startu" i buforowanie wyników (**Spring Cache**).

### 🔍 Ważona Wyszukiwarka
* **Native Queries:** Obliczanie *Relevance Score* w SQL.
* **Smart Sort:** *Dokładne dopasowanie > Zaczyna się od > Zawiera*.

### 🛡️ Bezpieczeństwo
* **Bezstanowa autoryzacja:** JWT + Refresh Token (rotacja).
* **HttpOnly Cookies:** Ochrona tokenów przed XSS.
* **Apache Tika:** Walidacja plików binarnych (Magic Bytes).

---

## 🛠️ Tech Stack

* **Backend:** Java 21, Spring Boot 3, Hibernate, PostgreSQL, Apache Tika.
* **Frontend:** React 18, Axios, HTML/CSS, Vite, Nginx.
* **DevOps:** Docker, Docker Compose, JUnit 5, Mockito.

---

## 🧪 Testy

Projekt posiada pokrycie testami integracyjnymi (H2 Database):
```bash
# Uruchomienie testów backendu:
./mvnw test
```

## Autorzy:
* Julian Demucha - Backend
* Hubert Świątek - Frontend
