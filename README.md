# Social Media Campaign Agent — Spring Boot Case Study

A research prototype implementing a **glass-box, human-governed multi-agent workflow** for context-aware social-media campaign generation.

This project was developed as the implementation artifact for the M.Sc. thesis:

> **A Glass-Box Architecture for Inspectable Multi-Agent Workflows: Human-in-the-Loop Governance and Hallucination Mitigation**

The system uses a centralized orchestration approach to coordinate specialized AI agents, persist and expose intermediate workflow state, and require human review at defined control boundaries before campaign content is finalized.

---

## Research Purpose

Large-language-model workflows can be difficult to inspect when intermediate agent outputs, state transitions, and approval decisions remain hidden inside transient prompts or runtime memory.

This prototype investigates a different approach: a **glass-box architecture** in which important intermediate results are captured in explicit application-layer state and exposed for inspection. The marketing domain is used as a case study because campaign generation requires the coordination of constraints such as campaign objectives, platform requirements, content quality, brand voice, and approval decisions.

The prototype supports investigation of three architectural concerns:

- **State recovery and traceability:** Persisting structured workflow state makes campaign execution easier to inspect, debug, and recover.
- **Hallucination containment:** Worker agents receive task-scoped information rather than unrestricted peer-to-peer conversational context.
- **Human-in-the-Loop governance:** A user reviews the relevant workflow payload through the application interface before an external or consequential action is accepted.

---

## Architecture

The application follows a **centralized Orchestrator-Worker topology**.

```text
Browser User
    |
    | Submit campaign goal
    v
Spring Boot / Thymeleaf Interface
    |
    v
Campaign Controller
    |
    v
Central Orchestration Layer
    |
    +--> Specialized Agent(s)
    |       |
    |       +--> Structured agent output
    |
    v
State Contracts and Audit Trail
    |
    v
Human-in-the-Loop Review Boundary
    |
    +--> Approve / revise / reject
```

### Core Design Principles

- **Centralized orchestration:** Agent execution and handoffs are mediated by the application orchestration layer rather than uncontrolled worker-to-worker communication.
- **Role-bounded agents:** Each agent has a defined task and goal within the campaign-generation workflow.
- **Structured state contracts:** Intermediate outputs are represented as typed application objects instead of being retained only in conversational memory.
- **Inspectable workflow state:** Important campaign data, transitions, and generated artifacts can be reviewed through the application.
- **Append-only audit trail:** Workflow events and state changes are retained to support traceability and forensic inspection.
- **Scoped context:** Agents operate on task-relevant context to reduce accidental contamination of downstream steps.
- **Human approval boundary:** The workflow pauses at a defined checkpoint, allowing a human operator to review the raw structured payload before proceeding.

---

## User Workflow

1. Start the Spring Boot application.
2. Open the local web interface in a browser.
3. Enter a campaign goal in the campaign submission form.
4. Submit the goal to initiate the multi-agent workflow.
5. The orchestration layer delegates defined tasks to the specialized agents.
6. Intermediate results are captured as structured workflow state.
7. The user inspects the generated campaign data at the Human-in-the-Loop checkpoint.
8. The user can approve, revise, or reject the workflow according to the interface controls.

The interface is intentionally part of the research artifact: it operationalizes human oversight as a technical control boundary rather than treating approval as a purely symbolic confirmation step.

---

## Technology Stack

| Technology | Purpose |
|---|---|
| Java | Core implementation language |
| Spring Boot | Application backend, dependency management, orchestration, and web layer |
| Maven | Build and dependency management |
| Thymeleaf | Server-side rendered user interface |
| HTML/CSS | Browser-based campaign input and review interface |
| LangChain4j | Java integration layer for LLM-enabled agent interactions |
| Google Vertex AI | Configured LLM provider for agent execution |
| JSON | Serialization format for inspectable state and persisted workflow artifacts |

---

## Project Structure

```text
src/main/java/com/example/socialmediacampaignagentsprintboot/
├── agent/        # Specialized agent implementations and agent responsibilities
├── config/        # Application, model-provider, and framework configuration
├── controller/    # HTTP endpoints and user-interface request handling
├── dto/           # Request, response, and transfer objects
├── model/         # Domain models, workflow state, and state contracts
├── service/       # Orchestration, persistence, audit, and business logic
└── CampaignAgentApplication.java
```

The agent classes in the `agent` package define the individual tasks and goals used during campaign generation. The controller layer receives browser requests, while the service and model layers coordinate state transitions, structured outputs, and auditability.

---

## Running Locally

### Prerequisites

- Java Development Kit (JDK) compatible with the version defined in `pom.xml`
- Maven, or the included Maven Wrapper
- Access to the configured LLM provider
- Required provider credentials and project configuration, where applicable

### Configure environment variables

Do not commit credentials to the repository. Configure required model-provider values through environment variables or a local configuration file that is excluded from version control.

Typical configuration may include:

```properties
# Example only — use the variables and property names configured in this project.
GOOGLE_CLOUD_PROJECT=your-gcp-project-id
GOOGLE_CLOUD_LOCATION=your-gcp-region
GOOGLE_APPLICATION_CREDENTIALS=/path/to/service-account-key.json
```

### Start the application

On macOS or Linux:

```bash
./mvnw spring-boot:run
```

On Windows:

```bat
mvnw.cmd spring-boot:run
```

Alternatively, with a locally installed Maven version:

```bash
mvn spring-boot:run
```

After startup, open the application in a browser:

```text
http://localhost:8080
```

If a different server port is configured in the application properties, replace `8080` with that port.

---

## Thesis Mapping

| Thesis concern | Prototype mechanism |
|---|---|
| Workflow state evaporation | Structured state contracts and persisted workflow artifacts |
| Opaque agent handoffs | Centralized orchestration and inspectable intermediate outputs |
| Hallucination snowballing | Scoped context and controlled agent handoffs |
| Automation bias in approval | Raw payload inspection at a Human-in-the-Loop checkpoint |
| Weak traceability | Append-only audit trail and explicit state transitions |
| Intervention integrity | Deterministic application-layer approval and override boundaries |

This implementation does not claim to make LLM output fully deterministic or eliminate hallucinations. Instead, it demonstrates how application-level architecture can improve visibility, traceability, containment, and human control over probabilistic multi-agent workflows.

---

## Limitations

This project is a research prototype and is not intended as a production-ready social-media publishing system.

Its scope is limited to application-layer orchestration and governance mechanisms. It does not include:

- Autonomous publication to external social-media platforms
- Production-scale authentication, authorization, or tenant isolation
- Full enterprise observability infrastructure
- Training or fine-tuning of foundation models
- A guarantee that generated content is factually correct or legally compliant

Human review remains necessary before using generated content in real marketing activity.

---

## Academic Use and Citation

This repository supports the implementation and evaluation of the associated M.Sc. thesis. If referring to the architecture or implementation in academic work, cite the thesis rather than presenting this prototype as an independently validated production system.

---
## AI-Assisted Development Disclosure

This research prototype was developed by the author with the assistance of AI-enabled development tools, including IntelliJ IDEA code-completion functionality and Google Gemini.

Google Gemini was used extensively to assist with the development of the server-side HTML/Thymeleaf templates located in `src/main/resources/templates/`. It was also used as support for implementation exploration, debugging, refactoring suggestions, and documentation drafting.

The author designed the research artifact, defined the architectural requirements and agent responsibilities, integrated the generated or suggested code into the Spring Boot application, reviewed and adapted the implementation, executed tests, and retained responsibility for all final source code, results, and claims in the associated thesis.

AI-generated or AI-assisted suggestions were not included automatically; they were evaluated and modified where necessary. This disclosure applies to the repository as a whole. Individual source files are not separately attributed unless a file was substantially generated with minimal author modification.

---

## License

This repository is provided for academic and research purposes. Please contact the author for reuse, collaboration, or licensing questions.