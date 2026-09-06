# US-003: AI-Powered Path Recommendation & Decision Discussion

- **Story ID**: `US-003`
- **Status**: 📋 Backlog
- **Priority**: Medium
- **Target Version**: `v1.3.0`
- **Feature Branch**: `feat/US-003-ai-path-recommendation`
- **Related Components**: [GraphPathResponse.java](file:///e:/dag-engine/src/main/java/com/workflow/dag_engine/models/path/GraphPathResponse.java), [GraphController.java](file:///e:/dag-engine/src/main/java/com/workflow/dag_engine/controller/GraphController.java), [app.js](file:///e:/dag-engine/src/main/resources/static/app.js)

---

## Story Statement
**As a** workflow operations engineer  
**I want to** consult an AI assistant that analyzes computed DAG paths and offers recommendations based on multi-dimensional cost trade-offs  
**So that** I can ask questions, understand the bottlenecks or risks in different routes, and choose the optimal execution path with confidence.

---

## Background
The DAG Engine computes multiple valid paths between start and end nodes, each with a multi-dimensional cost vector (e.g. `latency`, `monetary_cost`, `memory`, `risk_score`). For complex graphs, choosing between Path 1 (cheaper but slower) and Path 2 (faster but memory-intensive) requires nuanced trade-off analysis.

---

## Technical Scope
1. **AI Service Integration**:
   - Create `AiRecommendationService` supporting standard LLM APIs (Google Gemini API / OpenAI API).
   - Configure API key via `application.properties` or environment variable (`AI_API_KEY`).
   - Secure handling: API key is never exposed to the frontend; calls are proxied through backend endpoints.
2. **Backend API Endpoints**:
   - `POST /workflow-engine/graphs/{graphId}/paths/recommend`
     - Input: Path calculation results + User optimization preference (e.g., "minimize latency while keeping budget under $50").
     - Output: Recommended path ID, justification, trade-off analysis.
   - `POST /workflow-engine/graphs/{graphId}/paths/chat`
     - Input: Chat message history, selected paths, user question.
     - Output: AI response explaining specific node/edge costs and alternatives.
3. **UI Integration in Paths Drawer**:
   - In the right-side Paths tab, add an **"Ask AI Advisor"** button.
   - An interactive conversational drawer allowing users to:
     - View AI summary of trade-offs across all paths.
     - Ask follow-up questions (e.g., "Why is Path 2 more expensive than Path 3?").
     - Click "Highlight Recommended Path" to highlight it directly on the SVG canvas.

---

## Acceptance Criteria
- [ ] Backend communicates with AI API using securely configured API key.
- [ ] AI prompt accurately serializes all path sequences and their respective cost vectors.
- [ ] AI returns clear, structured recommendations explaining cost trade-offs.
- [ ] Conversational chat allows interactive Q&A on path decisions.
- [ ] Graceful fallback and user-friendly error handling if API key is missing or quota is exceeded.
