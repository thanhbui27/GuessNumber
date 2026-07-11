# Guess Number Game

Full-stack demo cho bai test Java Spring Boot: dang ky, dang nhap JWT, doan so 1-5, luot choi, diem, leaderboard, lich su doan va mua them luot.

## Cong nghe

- Backend: Java 17, Spring Boot 3, Spring Web MVC, Spring Data JPA, Hibernate, Spring Security, JWT, Flyway, MySQL 8, Lombok, Springdoc Swagger, JUnit 5, Mockito.
- Frontend: React 18, TypeScript, Vite, Tailwind CSS, Zustand, Axios, TanStack Query, React Hook Form, Zod, lucide-react.
- Infra: Docker Compose, MySQL InnoDB, Nginx cho frontend production.

## Cau truc

```text
guess-number-game/
  backend/
    src/main/java/com/guessgame/
      config/ controller/ dto/ entity/ enums/ exception/ mapper/ repository/ security/ service/
    src/main/resources/db/migration/V1__create_initial_schema.sql
    src/test/java/com/guessgame/
  frontend/
    src/api/ src/components/ src/pages/ src/store/ src/types/ src/utils/
  docker-compose.yml
  .env.example
  README.md
```

## Database schema

- `users`: username/email unique, password BCrypt, score, turns, role, `@Version`, created/updated timestamps.
- `guess_history`: user id, guessed number, server number, WIN/LOSE, score/turns after play, created time.
- `purchase_history`: user id, turns added, amount, provider, transaction code, status, created time.
- Index chinh: `users(score desc, created_at asc, id asc)`, history theo `(user_id, created_at desc)`.

MySQL dung InnoDB de co transaction va row-level lock. Flyway tao schema, backend dung `ddl-auto=validate` trong dev/prod.

## Concurrency /guess

`GameService.guess` chay trong `@Transactional`. Repository lay user bang:

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("select u from User u where u.id = :id")
Optional<User> findByIdForUpdate(Long id);
```

Moi request lock dung row user, tru 1 turn, cap nhat score, ghi history roi commit. Request song song phai cho lock release, doc lai state moi, nen turns khong am va khong mat update. Test concurrency gui 10 luot cho user co 5 turns va assert chi 5 success, turns ve 0, history co 5 ban ghi.

## Ty le thang 5%

Khong random server number roi so sanh, vi nhu vay la 20%. Backend dung `SecureRandom.nextDouble() < game.win-rate`. Neu win thi `serverNumber = guessedNumber`; neu lose thi random so khac guessed number.

## Bao mat JWT

Password duoc BCrypt. JWT chua `userId`, `username`, `role`, issuedAt va expiration. API public chi co register, login va Swagger. Cac API game/user/leaderboard can `Authorization: Bearer <token>`.

## Chay bang Docker Compose

```bash
cp .env.example .env
docker compose up --build
```

- Frontend: http://localhost:5173
- Backend: http://localhost:8080
- Swagger: http://localhost:8080/swagger-ui.html

Dung:

```bash
docker compose down
docker compose down -v
```

## Chay thu cong

Can MySQL 8 va database `guess_number_game`.

Backend:

```bash
cd backend
mvn clean test
mvn spring-boot:run
```

Frontend:

```bash
cd frontend
npm install
npm run dev
```

## Tai khoan demo

Profile `dev` seed 10 user: `alice`, `bob`, `charlie`, `david`, `eva`, `frank`, `grace`, `henry`, `ivy`, `jack`.

Mat khau chung: `Password@123`.

## API

- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `GET /api/v1/users/me`
- `POST /api/v1/game/guess`
- `POST /api/v1/game/buy-turns`
- `GET /api/v1/game/history?page=0&size=10`
- `GET /api/v1/game/purchase-history?page=0&size=10`
- `GET /api/v1/leaderboard`

## Curl mau

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"thanh","email":"thanh@example.com","password":"Password@123"}'

curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail":"thanh","password":"Password@123"}'

curl -X POST http://localhost:8080/api/v1/game/guess \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{"number":3}'

curl http://localhost:8080/api/v1/users/me \
  -H "Authorization: Bearer YOUR_TOKEN"

curl http://localhost:8080/api/v1/leaderboard \
  -H "Authorization: Bearer YOUR_TOKEN"

curl -X POST http://localhost:8080/api/v1/game/buy-turns \
  -H "Authorization: Bearer YOUR_TOKEN"

curl "http://localhost:8080/api/v1/game/history?page=0&size=10" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

Trong Swagger, bam **Authorize** va nhap: `Bearer YOUR_TOKEN`.

## Build va test

Backend:

```bash
cd backend
mvn clean test
mvn clean package
```

Frontend:

```bash
cd frontend
npm run lint
npm run build
```

Test hien co:

- `GameServiceTest`: win tru turn/cong score, het turn khong ghi history.
- `GuessConcurrencyIntegrationTest`: 10 request song song, chi 5 request hop le voi 5 turns.

## Quyet dinh ky thuat

- Controller chi dieu phoi, business logic nam trong service.
- API dung DTO/record, khong tra entity va khong bao gio tra password.
- Demo payment qua `PaymentService` de sau nay them VNPAY/MOMO/PAYPAL.
- Frontend reload se lay token tu localStorage va fetch `/users/me` de dong bo user moi nhat.
- Testcontainers dependency co san; test mac dinh dung H2 profile test vi moi truong Java hien tai khong nhan Docker socket, con runtime dev/prod van dung MySQL/Flyway/InnoDB.

## Huong phat trien

- Them refresh token.
- Them payment gateway that.
- Them admin dashboard quan ly user/history.
- Them E2E test bang Playwright.
