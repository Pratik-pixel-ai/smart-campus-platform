# Registration fix (latest)

## What was wrong
- `DataSeeder` created departments only when the `users` table was empty. A database that already
  had accounts but no departments showed **"No departments available"** on the register form forever.
- `AuthService.register` checked for a duplicate email using the address as typed, but saved it
  lower-cased, so `Foo@x.com` passed the check and then failed on the unique constraint as a
  generic 500. It also created the user row before validating roll number / employee code.
- The register form let a disabled, empty department select submit `departmentId: 0`.
- Any response without a server message was shown as the same vague text ("Could not create the
  account"), which hid whether the backend was down, returning 5xx, or rejecting the input.
- No test ever called `/api/auth/register` or `/api/departments/public`, so the earlier
  "17 tests pass" result did not cover registration at all.

## What changed
- `DataSeeder`: creates the default departments whenever none exist; find-or-create in the seed.
- `AuthService.register`: one normalised (trimmed, lower-case) email for check and insert; role
  fields validated before writing; invalid department is a clear 400.
- `GlobalExceptionHandler`: constraint violations -> 409, malformed body/invalid enum -> 400.
- `api.js` / `Register.jsx`: unreachable server and 5xx are reported as such; department load
  failures show the real reason with a Retry button; submit is blocked until a department is chosen.
- New tests: public department list, registration + case-insensitive duplicate email (H2 suite,
  runs on every `mvn test`) and the same registration flow against PostgreSQL (opt-in suite).
- Removed a stray nested copy of the project and a zip from `frontend/src/pages/auth/`.

## Verification status of THIS change
NOT executed. The environment used to prepare it had no Maven, PostgreSQL or network. Java and JSX
were syntax-checked only. Run `mvn test` (and the PostgreSQL command below) and `npm run build`
before relying on it.

## If the register form still fails
1. `curl -i http://localhost:8080/api/departments/public`
   - connection refused -> backend is not running; read its console for the startup error.
   - `[]` -> restart the backend once (it now creates default departments), or add one as admin.
   - 500 -> send the stack trace from the backend console.
2. Docker users: rebuild, otherwise the old image is served: `docker compose up --build`.

---

# Updated source release

This archive packages the existing patched working tree, not a fresh upstream checkout.

## Included fixes
- Explicit Hibernate string casts for nullable search parameters in Student, Faculty,
  Subject and Classroom repositories, addressing PostgreSQL lower(bytea) failures.
- Same-origin frontend /api routing and proxy configuration.
- PostgreSQL regression tests, browser smoke script and frontend dependency lockfile.

## Recorded verification
The saved Maven report shows 17 tests, 0 failures, 0 errors, 0 skipped:
- PostgresSearchTest: 3
- SmartCampusApiTest: 8
- AttendanceSummaryTest: 3
- GradeCalculatorTest: 3
The saved frontend production build succeeded (with a bundle-size warning).
The saved browser smoke result records 30 role-specific pages and no detected failures.
These are results from the earlier run, not tests rerun during ZIP packaging.
Evidence is included in verification/.

## Repeat backend tests
Install Java 17 and Maven. Create a DISPOSABLE PostgreSQL database and grant the
configured user schema permissions. Never use a live database: the PostgreSQL
regression profile uses create-drop and deletes its schema at shutdown.

    cd backend
    RUN_POSTGRES_TESTS=true TEST_DATABASE_URL=jdbc:postgresql://localhost:5432/smartcampus_test TEST_DATABASE_USERNAME=smartcampus TEST_DATABASE_PASSWORD=smartcampus mvn test

Without RUN_POSTGRES_TESTS=true, the three PostgreSQL tests are skipped.

## Build frontend
    cd frontend
    npm ci
    npm run build

See README.md for application setup and scripts/browser_smoke.py for browser checks.
Dependencies, build outputs, local environment files and Git history are excluded.
Install dependencies and build locally before running the application.

## Scope limits
Docker deployment and live Groq/BLE integrations remain UNTESTED by request.
The included Docker/proxy configuration changes have not been deployment-verified.
Page smoke checks do not establish every workflow or integration is error-free.
This is not a guarantee of a bug-free or security-audited production deployment.
Replace demo credentials and configure production secrets before public use.
