# US-004: Cost Parameter Range Thresholds & Path Trimming

- **Story ID**: `US-004`
- **Status**: 📋 Backlog
- **Priority**: High
- **Target Version**: `v1.2.0`
- **Feature Branch**: `feat/US-004-cost-range-path-pruning`
- **Related Components**: [PathComponentManagerImpl.java](file:///e:/dag-engine/src/main/java/com/workflow/dag_engine/componentManager/paths/component_manager/PathComponentManagerImpl.java), [GraphPathResponse.java](file:///e:/dag-engine/src/main/java/com/workflow/dag_engine/models/path/GraphPathResponse.java), [app.js](file:///e:/dag-engine/src/main/resources/static/app.js)

---

## Story Statement
**As a** workflow designer or pipeline scheduler  
**I want to** specify permissible cost value ranges (min/max thresholds) for specific cost dimensions (either at any intermediate step or as an aggregate total)  
**So that** paths that violate SLA bounds or budget constraints are pruned during traversal or trimmed from the final calculated path set.

---

## Background
Currently, the path calculation engine finds all possible directed paths from the start node to all destination nodes. In large DAGs with heavy branching, this can generate hundreds of paths, many of which are infeasible due to operational constraints (e.g. `latency > 500ms` or `memory > 4GB` or single edge `step_cost > 100`).

---

## Technical Scope
1. **Cost Constraint Specification Model**:
   - `CostFilterCriteria`:
     ```java
     public class CostFilterCriteria {
         String costName;        // e.g. "time", "memory", "cost"
         Double minVal;          // Optional lower threshold
         Double maxVal;          // Optional upper threshold
         Scope scope;            // AGGREGATE (total path cost) or STEP (any single node/edge)
     }
     ```
2. **Path Engine Algorithm Enhancement**:
   - In [PathComponentManagerImpl.java](file:///e:/dag-engine/src/main/java/com/workflow/dag_engine/componentManager/paths/component_manager/PathComponentManagerImpl.java):
     - **Early Pruning (Branch & Bound)**: If monotonic cost dimensions (strictly non-negative) exceed `maxVal` during DFS/traversal, prune that search branch early to save CPU and memory.
     - **Post-Traversal Filtering**: Discard any completed path whose aggregate or step cost violates specified ranges.
3. **API Enhancements**:
   - Support `POST /workflow-engine/graphs/{graphId}/paths` with optional `costConstraints` in request body.
   - Return metadata in `GraphPathResponse`: `totalPathsFound`, `pathsPrunedCount`, and remaining `filteredPaths`.
4. **UI Integration**:
   - In the Path Calculation panel or View screen, provide a **"Filter Thresholds"** button.
   - Users can set sliders or min/max number inputs for each active cost parameter (e.g. `Max Latency: 250`, `Max Cost: $50`).
   - Clicking "Apply Filter" re-queries or re-filters the displayed path cards and canvas highlights dynamically.

---

## Acceptance Criteria
- [ ] Users can define min and max limits for one or multiple cost parameters.
- [ ] Both cumulative path cost limits and step-by-step limits are supported.
- [ ] Paths with cost values exceeding the allowed range are omitted from the returned path list.
- [ ] For monotonic costs, branches are pruned early during path traversal.
- [ ] The UI displays how many paths were filtered out and presents only qualifying paths.
- [ ] Unit tests verify single-dimension, multi-dimension, and boundary condition trimming.
