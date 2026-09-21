# AI assistant flow

## The pipeline

```mermaid
sequenceDiagram
    actor U as User
    participant UI as Assistant page
    participant C as AiController
    participant G as GroqAiService
    participant CX as CampusContextService
    participant SV as Attendance / Timetable /<br/>Assignment / Academic services
    participant DB as PostgreSQL
    participant API as api.groq.com

    U->>UI: "Am I below 75% in any subject?"
    UI->>C: POST /api/ai/chat { question }
    Note over C: the JWT identifies the user;<br/>the browser sends no ids

    C->>G: ask(request)

    alt GROQ_API_KEY is empty
        G-->>C: { answer: "AI assistant is not configured...", configured: false }
        C-->>UI: 200 — the page shows a banner, nothing breaks
    else configured
        G->>CX: buildContext()
        CX->>CX: role from the SecurityContext
        CX->>SV: attendance summary, today's classes,<br/>pending assignments, marks, announcements
        SV->>DB: scoped queries
        DB-->>SV: rows
        SV-->>CX: DTOs
        CX-->>G: compact text snapshot (a few hundred tokens)

        G->>API: POST /v1/chat/completions<br/>system prompt + context + question<br/>Authorization: Bearer GROQ_API_KEY
        API-->>G: choices[0].message.content
        G->>G: extract the text
        G-->>C: { answer, configured: true, model }
        C-->>UI: 200
        UI->>U: renders the answer
    end
```

## The context snapshot

For a **student**:

```
User: Aary Ghadage (student)
Today: 2026-03-02 (MONDAY)

ATTENDANCE
Overall: 82.5% (33 present of 40 lectures). Lectures that can still be missed while staying at 75%: 4.
- Database Management Systems (IT701): 83.33% (5/6)
- Machine Learning (IT702): 66.67% (4/6)
...

TODAY'S CLASSES
- 09:00-10:00 Database Management Systems in A-101 with Dr. Vilas Gaikwad

UPCOMING ASSIGNMENTS
- Normalisation case study (Database Management Systems), due 2026-03-08T23:59, status PENDING

ACADEMIC RECORD
Overall: 74.5%
- Semester 7: 74.5% across 4 subjects

RECENT ANNOUNCEMENTS
- [HIGH] Mid-semester examination timetable: ...
```

For **faculty and admin** the snapshot carries their lectures, the assignments they set with submission
counts, campus attendance and recent notices instead.

## Design choices worth defending

**Called from Java, never from the browser.** The key lives in the backend environment. A browser call
would ship the key to every user in the network tab.

**The model has no database access.** It receives text the backend assembled and returns text. There is no
tool use, no SQL generation, nothing that could read a table it was not given. The worst case for a prompt
injection is a wrong sentence, not a data leak.

**The context comes from the JWT.** `CampusContextService` asks `CurrentUser` who is signed in. No id from
the request body is used, so a crafted payload cannot pull another student's marks into the prompt.

**Small context on purpose.** Summaries rather than raw rows: faster, cheaper, and easier for the model to
answer from accurately.

**Temperature 0.2 and a strict system prompt** — answer only from the context, never invent attendance,
marks or deadlines, say plainly when the information is not there.

**Failure is graceful.** A missing key produces a clear message; a network failure or a timeout produces
*"The assistant could not be reached right now"*. Neither returns a 500, and the rest of the platform is
unaffected.

## Configuration

| Property | Environment variable | Default |
|---|---|---|
| `groq.api-key` | `GROQ_API_KEY` | *(empty)* |
| `groq.model` | `GROQ_MODEL` | `llama-3.3-70b-versatile` |
| `groq.base-url` | — | `https://api.groq.com/openai/v1/chat/completions` |
| `groq.timeout-ms` | — | `20000` |

The endpoint is OpenAI-compatible, so switching provider means changing the base URL and the key; the
request and response handling stay as they are.
