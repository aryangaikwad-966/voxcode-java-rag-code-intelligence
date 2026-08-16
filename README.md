# VoxCode — Agentic Java/Spring Code Review & Remediation Agent

VoxCode is a dual-capability platform prioritizing deterministic evidence, bounded remediation, and strict safety verifications.

## Local Setup

### Prerequisites
- Docker & Docker Compose
- Java 21
- Maven 3.9+
- Node.js 20+

### Starting Infrastructure
1. Make sure Docker is running.
2. In the root directory, run:
   ```bash
   docker-compose up -d
   ```
   This starts the MySQL (port 3306) and Qdrant (port 6333) containers.

### Backend Development
1. Navigate to `Backend/`
2. Ensure you are using Java 21:
   ```bash
   java -version
   ```
3. Run the Spring Boot application:
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
