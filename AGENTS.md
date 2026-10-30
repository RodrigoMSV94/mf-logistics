# Permanent engineering standards

Apply these standards to every code change in this repository, in every session:

- Use Java 21 and Spring Boot 4 as the target platform and follow SOLID, Clean Code, and Clean Architecture.
- Keep domain and application logic independent of Spring, JPA, web, and other infrastructure details; communicate across layers through appropriate ports and DTOs.
- Add or update unit tests and integration tests for every behavior change. Verify the exact requested behavior, including the relevant rendered view, persistence migration, or API flow, rather than relying only on compilation.
- Run the smallest relevant test set while iterating, then run the full Maven test suite before completing a change when feasible. Report any test that could not be run.
- Preserve existing user data and unrelated work. Database changes must use safe, versioned Liquibase migrations; never add destructive startup behavior for remote or production databases.
- The project currently declares Spring Boot 3.3.5 in `pom.xml`. Do not claim it is running Boot 4 or silently perform a major framework migration as part of an unrelated task; make the target and current version explicit when compatibility matters.
