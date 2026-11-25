# Almighty Threat Detection Microservice (FastAPI)

A dedicated Python FastAPI microservice that performs real-time inspection and pattern-matching of HTTP requests (URI, Query string, Body) to detect security threats such as SQL Injection (SQLi), Cross-Site Scripting (XSS), and Path Traversal.

---

## 1. Overview & Architecture

This microservice runs independently on **port 5000** and communicates with the **`almight_firewall_v1`** Spring Boot service via asynchronous HTTP REST calls using Spring `WebClient`.

```
Incoming Request ──► [almight_firewall_v1] (Port 7080)
                             │
                      (WebClient POST /detect)
                             ▼
              [threat_detection] (FastAPI Port 5000)
                             │
                      (Inspects rules from rules.xlsx)
                             ▼
              Returns: {"threat_type": "SQL_INJECTION" | "XSS" | "PATH_TRAVERSAL" | "NONE"}
```

---

## 2. API Endpoints

Base URL: `http://localhost:5000`

| Method | Endpoint | Description | Request Body | Response Example |
|---|---|---|---|---|
| `GET` | `/health` | Health and rule status check | None | `{"status": "UP", "rules_loaded": 6}` |
| `GET` | `/rules` | View all active detection rules | None | `{"count": 6, "rules": [...]}` |
| `POST` | `/reload-rules` | Hot reload rules from `rules.xlsx` | None | `{"status": "success", "rules_loaded": 6}` |
| `POST` | `/detect` | Classify threat from HTTP payload | `{"uri": "...", "query": "...", "body": "..."}` | `{"threat_type": "SQL_INJECTION"}` |

---

## 3. How to Run

### Using the start script:
```bash
chmod +x start.sh
./start.sh
```

### Or using python / uvicorn directly:
```bash
# Using the bundled virtual environment
./firewall-env/bin/python -m uvicorn main:app --host 0.0.0.0 --port 5000 --reload

# Or using system python
python3 -m uvicorn main:app --host 0.0.0.0 --port 5000 --reload
```
