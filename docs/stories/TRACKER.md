# User Story Tracker

Welcome to the Project Nexus / DAG Engine User Story Tracker. This document tracks the status, priority, and implementation progress of active user stories.

## Story Dashboard

| Story ID | Title | Status | Priority | Target Milestone | Assignee | Spec Document |
|:---|:---|:---:|:---:|:---:|:---:|:---|
| **[US-001](file:///e:/dag-engine/docs/stories/US-001-role-based-access-control.md)** | Role-Based Access Control (RBAC) via `BaseModel` | 📋 Backlog | High | v1.2.0 | TBD | [US-001 Spec](file:///e:/dag-engine/docs/stories/US-001-role-based-access-control.md) |
| **[US-002](file:///e:/dag-engine/docs/stories/US-002-user-authenticator-request-validation.md)** | User Authenticator & API Request Validation in `BaseModel` | 📋 Backlog | High | v1.2.0 | TBD | [US-002 Spec](file:///e:/dag-engine/docs/stories/US-002-user-authenticator-request-validation.md) |
| **[US-003](file:///e:/dag-engine/docs/stories/US-003-ai-path-recommendation-and-discussion.md)** | AI-Powered Path Recommendation & Decision Discussion | 📋 Backlog | Medium | v1.3.0 | TBD | [US-003 Spec](file:///e:/dag-engine/docs/stories/US-003-ai-path-recommendation-and-discussion.md) |
| **[US-004](file:///e:/dag-engine/docs/stories/US-004-cost-range-path-pruning.md)** | Cost Parameter Range Thresholds & Path Trimming | 📋 Backlog | High | v1.2.0 | TBD | [US-004 Spec](file:///e:/dag-engine/docs/stories/US-004-cost-range-path-pruning.md) |

---

## Status Definitions
- 📋 **Backlog**: Story defined, acceptance criteria set, ready for refinement.
- 🟡 **In Progress**: Active development on dedicated feature branch (`feat/US-xxx-*`).
- 🔍 **In Review**: Pull Request submitted, automated tests and peer reviews running.
- ✅ **Done**: Merged into `main`, verified with integration tests.

---

## Git Workflow Conventions

### Branch Naming
```bash
feat/US-001-role-based-access
feat/US-002-user-auth-validation
feat/US-003-ai-path-recommendation
feat/US-004-cost-range-path-pruning
```

### Commit Message Format
Follow [Conventional Commits](https://www.conventionalcommits.org/):
```bash
git commit -m "feat(auth): [US-001] introduce Role enum and BaseModel role validation"
git commit -m "feat(paths): [US-004] add cost range pruning filter to path calculation"
```

---

## Summary of Stories

### 1. [US-001: Role-Based Access Control](file:///e:/dag-engine/docs/stories/US-001-role-based-access-control.md)
- **Problem**: Any user currently has uniform access without role-based administrative boundaries.
- **Solution**: Users select or are assigned roles (`ADMIN`, `OPERATOR`, `VIEWER`). Administrative actions (delete user, delete all graphs of a user) require role authorization validated through [BaseModel.java](file:///e:/dag-engine/src/main/java/com/workflow/dag_engine/models/validation/BaseModel.java).

### 2. [US-002: User Authenticator & API Request Validation](file:///e:/dag-engine/docs/stories/US-002-user-authenticator-request-validation.md)
- **Problem**: API endpoints need unified session/token validation and payload sanitization across all controllers.
- **Solution**: Implement centralized authentication and request integrity validation inside [BaseModel.java](file:///e:/dag-engine/src/main/java/com/workflow/dag_engine/models/validation/BaseModel.java) extended by all request DTOs.

### 3. [US-003: AI-Powered Path Recommendation](file:///e:/dag-engine/docs/stories/US-003-ai-path-recommendation-and-discussion.md)
- **Problem**: Users with complex multi-criteria paths need intelligent guidance to trade off competing dimensions (e.g. latency vs cost).
- **Solution**: Integrate an LLM provider (Gemini / OpenAI) via API key to analyze computed Pareto-optimal paths and power an interactive path discussion panel in the UI.

### 4. [US-004: Cost Parameter Range Thresholds & Path Trimming](file:///e:/dag-engine/docs/stories/US-004-cost-range-path-pruning.md)
- **Problem**: Path calculation returns all possible routes even if individual step costs or total costs exceed operational thresholds.
- **Solution**: Add configurable min/max cost range constraints per dimension to filter and prune paths during calculation (at any step or cumulatively).
