# AI-Powered University Academic Advisor

A full-stack academic management system with AI capabilities — providing student management, course enrollment, academic performance tracking, and five AI-powered modules (Chatbot, Risk Prediction, Research Assistant, Exam Generator, Career Recommendation).

## Tech Stack

| Layer     | Technology                                                      |
|-----------|-----------------------------------------------------------------|
| Backend   | Java 21, Spring Boot 3.5.13, Spring AI 1.0.9, Spring Security  |
| Frontend  | React 18, Vite 5, MUI 6, Axios, React Router v6                |
| Database  | MySQL 8                                                        |
| AI        | OpenAI (GPT-4o-mini), Gemini, Hugging Face                     |
| Testing   | JUnit 5, Mockito, Spring Test                                  |
| Infra     | Docker, Docker Compose, GitHub Actions                         |

## Architecture

```
┌─────────────┐     ┌──────────────┐     ┌─────────┐
│  React App  │────▶│  Spring Boot │────▶│  MySQL  │
│  (port 3000)│     │  (port 8080) │     │(port 3306)│
└─────────────┘     └──────┬───────┘     └─────────┘
                           │
                    ┌──────┴───────┐
                    │   AI APIs    │
                    │ OpenAI/Gemini│
                    └──────────────┘
```

## AI Modules

1. **Academic Chatbot** — Answers general and student-specific questions using LLM with grounded student data
2. **Risk Prediction** — Deterministic risk scoring (attendance/exam weights) + LLM-generated explanations
3. **Research Assistant** — Upload PDF → text extraction (PDFBox) → LLM analysis (summary, findings, gaps)
4. **Exam Generator** — Generates MCQs, theory, practical, and case study questions via LLM
5. **Career Recommendation** — Matches student profile against career paths using LLM

## Quick Start

### Prerequisites

- Java 21
- Node 18
- MySQL 8
- Docker & Docker Compose (optional)

### Docker (recommended)

```bash
docker compose up
```

This starts MySQL, the backend (port 8080), and the frontend (port 3000).

### Manual

**1. Database**

```sql
CREATE DATABASE academic_advisor_db;
```

**2. Backend**

```bash
cp .env.example .env   # configure DB credentials and API keys
mvn spring-boot:run
```

**3. Frontend**

```bash
cd frontend
cp .env.example .env.local
npm install
npm run dev
```

### Environment Variables

| Variable           | Description              | Default               |
|--------------------|--------------------------|-----------------------|
| `DB_USERNAME`      | MySQL username           | `root`                |
| `DB_PASSWORD`      | MySQL password           | `password`            |
| `JWT_SECRET`       | JWT signing key          | *(auto-generated)*    |
| `OPENAI_API_KEY`   | OpenAI API key           | —                     |
| `GEMINI_API_KEY`   | Google Gemini API key    | —                     |
| `SEED_ENABLED`     | Seed demo data on startup| `true`                |

## Testing

```bash
# All tests
mvn test

# Specific test class
mvn test -Dtest=GradeServiceIntegrationTest
```

## Roles

- **Admin** — Manage students, courses, lecturers, departments; view class rankings
- **Lecturer** — Record grades, generate exams, access research assistant
- **Student** — View dashboard, enrolled courses, results; use chatbot and career recommender

## License

MIT
