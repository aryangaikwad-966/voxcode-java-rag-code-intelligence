# VoxCode — Agentic Java RAG Intelligence

VoxCode is an Agentic AI system for Java/Spring repository intelligence that uses AST analysis, dependency analysis, and RAG to investigate engineering problems, gather and validate evidence, create engineering plans, perform human-approved bounded remediation, and verify or roll back changes.

**Core Principle:**
Understand → Retrieve → Investigate → Prove → Plan → Act → Verify

**Core Thesis:**
"Understand before changing, prove before acting, and verify after acting."

## Architecture

VoxCode follows a unified architecture:

```
Repository → Repository Intelligence (AST + Dependency Graph + Repository Index) → Hybrid Repository-Aware RAG → One Adaptive Primary Agent → Evidence Engine → Finding Validation → Engineering Plan → Human Approval → Bounded Remediation → MCP/Tool Interface → Isolated Docker Workspace → Build + Tests + Static Analysis + Diff/Scope Validation → PASS (Accept) / FAIL (Bounded Repair) / FAIL after retry budget (Rollback) → Engineering Report → Evaluation
```

## Technology Stack

**Backend:**
- Java 21
- Spring Boot
- Maven
- Spring AI

**Repository Intelligence:**
- JavaParser for AST/program structure
- JGraphT for dependency relationships
- Repository Index for unified querying

**RAG:**
- Embeddings
- Qdrant
- Hybrid retrieval (vector + lexical + metadata/symbol + dependency-aware)
- Reranking
- Context assembly
- RAG evaluation and ablation

**Agentic AI:**
- One adaptive primary agent
- Tool calling
- Structured agent state
- Persisted decision/engineering traces

**MCP:**
- Focused MCP server/interface for tool interoperability

**Remediation:**
- OpenRewrite for deterministic Java/Spring transformations where appropriate
- AI-generated targeted patches only when deterministic transformation is insufficient

**Execution:**
- Docker sandbox
- Controlled build/test execution
- Resource limits
- Restricted filesystem/network access

**Database:**
- MySQL for application state, findings, plans, approvals, traces, reports

**Security:**
- Spring Security
- Authentication/authorization
- Repository isolation
- Path traversal protection
- Secret filtering
- Prompt-injection defenses
- Unsafe command protection

**Testing:**
- JUnit 5
- Mockito
- Testcontainers

**Frontend:**
- React

**CI/CD:**
- Git
- GitHub
- GitHub Actions
- Pull Requests
- Branch protection
- Automated quality gates

## Intelligence Boundaries

- **AST = Structural Truth**: JavaParser provides deterministic structural analysis
- **JGraphT = Relationship Truth**: Dependency graph provides deterministic relationship analysis
- **RAG = Semantic/Contextual Information**: Hybrid retrieval provides semantic context
- **Tools = Operational Truth**: Agent tools provide operational capabilities
- **Build/Tests = Behavioral Truth**: Execution verification provides behavioral validation

## Local Setup

### Prerequisites
- Docker & Docker Compose
- Java 21 (install via Homebrew: `brew install openjdk@21`)
- Maven 3.9+
- Node.js 20+

### Java 21 Setup
If you have multiple Java versions installed, set Java 21 as the default:
```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@21
export PATH="$JAVA_HOME/bin:$PATH"
```

Or use the provided setup script:
```bash
source setup-env.sh
```

### Starting Infrastructure
1. Make sure Docker is running.
2. In the root directory, run:
   ```bash
   docker-compose up -d
   ```
   This starts the MySQL (port 3306) and Qdrant (port 6333) containers.

### Backend Development
1. Navigate to `Backend/`
2. Set up Java 21 environment:
   ```bash
   export JAVA_HOME=/opt/homebrew/opt/openjdk@21
   export PATH="$JAVA_HOME/bin:$PATH"
   ```
3. Verify Java version:
   ```bash
   java -version
   ```
4. Run the Spring Boot application:
   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
   ```

### Frontend Development
1. Navigate to `Frontend/`
2. Install dependencies:
   ```bash
   npm install
   ```
3. Start the dev server:
   ```bash
   npm run dev
   ```

## Project Scope

VoxCode is a one-year solo project focused on building a deep, measurable, and defensible Agentic AI system for Java/Spring repositories.

**Primary Capabilities:**
- Investigation: Repository intelligence and evidence-backed findings
- Remediation: Bounded, human-approved code modifications

**Supported Remediation Scope:**
- Localized bug fixes
- Security fixes using existing infrastructure
- Incorrect annotations/configuration
- Deprecated Java/Spring API migrations
- Incorrect method calls
- Transaction-boundary fixes
- Dependency/configuration corrections
- Other small, single-concern, few-file fixes

**Unsupported Scope:**
- Entire application feature generation
- Authentication infrastructure from scratch
- New payment systems
- Large modules
- Arbitrary application generation
- Unlimited autonomous coding

## Development Workflow

VoxCode follows a professional Git workflow:

**Branching Strategy:**
- `main` → stable/release-ready code
- `develop` → integration branch for upcoming releases
- `feature/*` → new feature/task implementation branches
- `fix/*` → bug fix branches
- `hotfix/*` → critical fixes

**Developer Workflow:**
1. Start from latest `develop`
2. Pull latest remote changes
3. Create feature/fix branch
4. Implement only assigned task and dependencies
5. Write/update tests
6. Run local validation
7. Run Maven validation
8. Review git status and diff
9. Commit logical changes
10. Push branch
11. Open PR against `develop`
12. Run GitHub Actions
13. Fix CI failures
14. Review PR
15. Merge only after required checks pass
16. Delete branch after successful merge

## Documentation

- **TODO.md** - Single source of truth for VoxCode specification and implementation tasks
- **Docs/PRD.md** - Product Requirements Document (locked)
- **Docs/Design doc.md** - UI/UX Design Document (locked)
- **Docs/Tech doc.md** - Technical Design Document (locked)

## Technology Protection

VoxCode does NOT include:
- Kafka
- Kubernetes
- Redis
- Microservices
- GraphRAG
- Multi-agent systems
- Custom LLM
- Foundation model training
- Full IDE replacement
- Autonomous production deployment
- Unlimited repair loops
- Huge MCP ecosystem
- Generic multi-language support
- General-purpose coding assistant functionality

Every technology has a direct role in: Retrieve → Investigate → Decide → Act → Verify
