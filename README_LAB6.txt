Лаба 6 — HTTPS, цепочка сертификатов и CI

Что в пакете:
1. certs/generate-chain.ps1 — генерация цепочки Root CA -> Intermediate CA -> Server на Windows через keytool.
2. src/main/resources/application-lab6-example.yaml — пример настройки HTTPS для Spring Boot.
3. .github/workflows/ci.yml — CI для GitHub Actions: compile + test + package + upload artifact.
4. gitignore.lab6.additions.txt — что добавить в .gitignore.
5. LAB6_CHECK_STEPS.txt — как проверить по пунктам.

Важно:
- Обязательно подставь свой номер студенческого билета вместо REPLACE_WITH_STUDENT_ID.
- Не коммить .jks, .csr и пароли в репозиторий.
- Для GitHub Actions используй Secrets и Variables:
  - Secret: SSL_KEYSTORE_BASE64
  - Secret: SSL_KEYSTORE_PASSWORD
  - Secret: JWT_SECRET
  - Variable: SSL_KEY_ALIAS
