# US-001: Role-Based Access Control (RBAC) via `BaseModel`

- **Story ID**: `US-001`
- **Status**: 📋 Backlog
- **Priority**: High
- **Target Version**: `v1.2.0`
- **Feature Branch**: `feat/US-001-role-based-access`
- **Related Components**: [BaseModel.java](file:///e:/dag-engine/src/main/java/com/workflow/dag_engine/models/validation/BaseModel.java), [User.java](file:///e:/dag-engine/src/main/java/com/workflow/dag_engine/models/userModel/User.java), [UserController.java](file:///e:/dag-engine/src/main/java/com/workflow/dag_engine/controller/UserController.java)

---

## Story Statement
**As a** system user or administrator  
**I want to** log in and select/operate under an assigned role with specific operational privileges  
**So that** privileged actions (such as deleting a user or purging all graphs belonging to a user) are restricted to authorized roles, enforced consistently across all API requests via `BaseModel`.

---

## Background & Architecture
Currently, API requests inherit from [BaseModel.java](file:///e:/dag-engine/src/main/java/com/workflow/dag_engine/models/validation/BaseModel.java), which contains a placeholder for role-based request validation:
```java
public class BaseModel {
    /**
     * A Request Validation class specifically used to validate the input Request
     * based on the role of the user to be defined later and other parameter
     * assigned to the user
     */
}
```
All API request DTOs (e.g. `UserRequest`, `GraphUploadRequest`, `GraphUpdateRequest`) extend `BaseModel`. This story operationalizes `BaseModel` to carry role context and enforce permission verification before reaching business logic.

---

## Roles & Permissions Matrix

| Role | View Graphs & Calculate Paths | Edit / Upload Own Graphs | Delete Own Graph | Delete Any User | Delete All Graphs of a User |
|:---|:---:|:---:|:---:|:---:|:---:|
| **VIEWER** | ✅ | ❌ | ❌ | ❌ | ❌ |
| **OPERATOR / USER** | ✅ | ✅ | ✅ | ❌ | ❌ |
| **ADMIN** | ✅ | ✅ | ✅ | ✅ | ✅ |

---

## Technical Scope
1. **Define Role Enum & User Entity Fields**:
   - Add `RoleEnum` (`ADMIN`, `OPERATOR`, `VIEWER`).
   - Associate roles to `UserEntity` and return available/assigned roles on login.
2. **Update `BaseModel`**:
   - Fields: `userRole`, `requestUserId`, `authToken`.
   - Method: `validatePermission(PermissionRequired permission)`.
3. **Extend Requests**:
   - Ensure all request models extend `BaseModel` and pass role/session context.
4. **Administrative Endpoints**:
   - Endpoint to delete user: `DELETE /workflow-engine/user/{userId}` (Admin only).
   - Endpoint to purge all graphs of a user: `DELETE /workflow-engine/user/{userId}/graphs` (Admin only).
5. **UI Integration**:
   - Allow user to choose their active role during login or in a role-switcher dropdown.
   - Disable/hide destructive admin buttons in the UI for non-admin roles.

---

## Acceptance Criteria
- [ ] User can log in and select from their authorized roles.
- [ ] Every API request inheriting from `BaseModel` validates whether the caller's role has permission for the action.
- [ ] Attempting an unauthorized action (e.g., non-admin calling delete user) returns HTTP 403 Forbidden with appropriate `ErrorDetails`.
- [ ] Admin role can successfully trigger `delete user` and `delete all graphs of user`.
- [ ] Unit and integration tests verify role checks across all controller endpoints.
