# US-002: User Authenticator System & API Request Validation via `BaseModel`

- **Story ID**: `US-002`
- **Status**: 📋 Backlog
- **Priority**: High
- **Target Version**: `v1.2.0`
- **Feature Branch**: `feat/US-002-user-auth-validation`
- **Related Components**: [BaseModel.java](file:///e:/dag-engine/src/main/java/com/workflow/dag_engine/models/validation/BaseModel.java), [UserController.java](file:///e:/dag-engine/src/main/java/com/workflow/dag_engine/controller/UserController.java), [GraphController.java](file:///e:/dag-engine/src/main/java/com/workflow/dag_engine/controller/GraphController.java)

---

## Story Statement
**As a** backend service  
**I want to** authenticate and validate incoming API requests centrally through `BaseModel`  
**So that** invalid payloads, expired sessions, tampered credentials, or unauthorized user impersonation are caught uniformly before executing controller handlers.

---

## Technical Scope
1. **Unified Request Context in `BaseModel`**:
   - `correlationId`: For tracing end-to-end request lifecycle across logs.
   - `authToken` or `sessionToken`: Authenticator token generated upon successful login.
   - `timestamp`: Request generation epoch for replay attack prevention.
   - `clientIp`: Optional audit origin IP.
2. **Authenticator Validation Engine**:
   - `AuthenticatorService`: Verifies token signature, expiration, and active user session.
   - Self-validating method on `BaseModel`:
     ```java
     public ValidationResult validateRequest(AuthenticatorService authService) {
         // 1. Verify correlation ID exists
         // 2. Authenticate token and matching userId
         // 3. Check structural validity
     }
     ```
3. **Controller / Interceptor Integration**:
   - Spring interceptor or filter (`RequestValidationFilter`) that invokes `BaseModel` validation on incoming `@RequestBody`.
   - Reject unauthenticated or corrupted requests with standard `ApplicationException` (HTTP 401 Unauthorized / HTTP 400 Bad Request).
4. **UI Session Management**:
   - Store session token in `sessionStorage` or secure cookie upon login.
   - Automatically inject token and `correlationId` in `fetch()` network headers and request bodies.

---

## Acceptance Criteria
- [ ] Every request extending `BaseModel` is verified against the `AuthenticatorService`.
- [ ] Missing, invalid, or expired authentication tokens are rejected with HTTP 401.
- [ ] A request claiming `userId = X` cannot be executed using a token issued to `userId = Y`.
- [ ] Correlation ID is present in all requests and logs for auditability.
- [ ] Automated integration tests cover valid token, expired token, mismatched user, and malformed payload scenarios.
