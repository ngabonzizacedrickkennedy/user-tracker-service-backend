# User Tracker Service - mTLS

Spring Boot service that tracks user activity using mTLS authentication and broadcasts updates via UDP.

## What It Does

Client connects with certificate → Server extracts email from cert → Updates database (timestamp, IP, port) → Broadcasts via UDP → Listener displays it.

Server: port 8443 (HTTPS), MySQL database, UDP broadcast from 6666 to 6667.

<img width="1064" height="454" alt="image" src="https://github.com/user-attachments/assets/85ad91a6-125b-4858-a1a8-973d1ab64ff9" />

## Tech Stack

Java 21, Spring Boot 3.5.9, MySQL 8.0, Maven, Go (listener), Docker

## Requirements

Install: Java 21, Maven 3.9+, Go 1.25+, Docker

Verify: `java -version`, `mvn -version`, `go version`, `docker --version`

## Project Structure

```
user-tracker-service/
├── certs/          # TLS certs (CA, server, 3 clients)
├── client/         # Client app + JAR
├── listener/       # Go UDP listener
├── src/main/       # Server code
└── docker-compose.yml
```

<img width="548" height="432" alt="image" src="https://github.com/user-attachments/assets/478c5ff1-fd56-4786-a258-872b58dc3af0" />




## Quick Start

### 1. Certificates

Already in `certs/` folder: CA, server cert, 3 client certs (cedrickngabo03@gmail.com, ngabocedkennedy03@gmail.com, novemba42@gmail.com). Password: `cedric`

### 2. Build

```bash
mvn clean package -DskipTests
cd client && mvn clean package && cd ..
cd listener && go build -o listener.exe && cd ..
```

<img width="1220" height="602" alt="image" src="https://github.com/user-attachments/assets/be5db2d5-6bc1-42f9-85dd-57511d88afbd" />


### 3. Run

**MySQL:** `docker-compose up mysql -d` (wait 10 sec)

**Server:** `mvn spring-boot:run` (logs show: Spring Boot, Tomcat 8443, UDP 6666, DB initialized)


**Listener:** `cd listener && ./listener` (shows "Listening on port 6667...")

[<img width="813" height="145" alt="image" src="https://github.com/user-attachments/assets/32dd03e4-3643-4a61-80bc-8153a0cfff1d" />


**Client:** `cd client && java -jar target/user-tracker-client-1.0.0.jar` (shows "Response: 200")

<img width="884" height="274" alt="image" src="https://github.com/user-attachments/assets/e0677fdb-40b8-473b-99a1-923521572d2e" />
<img width="806" height="130" alt="image" src="https://github.com/user-attachments/assets/e672ace9-b833-4e94-9c4b-62763c0bad77" />





## Testing

Check database before/after:
```bash
docker exec -it usertracker-mysql mysql -uroot -pTT4242@mtskr -e "USE UserService_db; SELECT * FROM users;"
```

<img width="1044" height="255" alt="image" src="https://github.com/user-attachments/assets/39fc71d8-e906-4446-968f-51c3c2852da4" />


Test different users: Edit `UserTrackerClient.java`, change keystore to `client2-keystore.p12`, rebuild, run.

## API

`PATCH /track` - Empty request with client cert. Server validates, updates DB, broadcasts. Returns 200 (success), 400 (invalid email), or 403 (user not found).

## UDP Protocol

Binary format: Email (UTF-8) | LastSeen (8 bytes, nanoseconds) | IP (UTF-8) | Port (4 bytes). Big-endian. Broadcast: 255.255.255.255 from 6666 to 6667.

## Troubleshooting

**Port in use:** Change MySQL port in docker-compose.yml (3306→3307)

**Wrong password:** All keystores use `cedric`

**MySQL timeout:** Wait 10-15 seconds after starting MySQL

**UDP not working:** On Windows, run server natively (not in Docker). UDP from Docker doesn't reach host.

**Connection refused:** Server not running - start with `mvn spring-boot:run`

## Design Choices

- TLS in Spring Boot (direct access to certs in code)
- Email in CN field (certificate = authentication)
- Nanoseconds for timestamp (requirement)
- UDP broadcast (no need to know receiver addresses)
- Binary format (efficient, Java DataOutputStream)
- Auto-initialize DB (3 test users on startup)
- Go listener (bonus points for non-mainstream language)
- Server runs natively on Windows (Docker UDP issue)

Notes
Platform: Windows + Git Bash. Server runs natively due to Docker UDP broadcast limitation on Windows.
Technology: Java 21, Maven 3.9.9, Go 1.25.5, MySQL 8.0. Used Go for listener to get bonus points for non-mainstream language.
Security: Self-signed certificates as required by specifications. UDP broadcasts are unencrypted by protocol design.
Development: Hardcoded credentials for local testing. Production would use environment variables.


NGABONZIZA Cedrick Kennedy
