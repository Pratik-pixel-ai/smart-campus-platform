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
