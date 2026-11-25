import os
import re
import urllib.parse
from typing import Dict, Any, List
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
import pandas as pd

app = FastAPI(
    title="Almighty Threat Detection Service",
    description="Microservice for inspecting HTTP traffic and classifying security threats (SQLi, XSS, Path Traversal)",
    version="1.0.0"
)

# Enable CORS for cross-service calls
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

RULES_FILE = os.environ.get(
    "RULES_FILE",
    os.path.join(os.path.dirname(os.path.abspath(__file__)), "rules.xlsx")
)

rules: List[Dict[str, Any]] = []

def load_rules():
    global rules
    try:
        if os.path.exists(RULES_FILE):
            try:
                df = pd.read_csv(RULES_FILE)
            except Exception:
                df = pd.read_excel(RULES_FILE)
            rules = df.to_dict(orient="records")
            print(f"[Threat Detection] Successfully loaded {len(rules)} rules from {RULES_FILE}")
        else:
            print(f"[Threat Detection] Warning: Rules file not found at {RULES_FILE}")
            rules = []
    except Exception as e:
        print(f"[Threat Detection] Error reading rules from {RULES_FILE}: {e}")
        rules = []

# Initial load on module startup
load_rules()

def normalize(text: str) -> str:
    if not text:
        return ""
    return urllib.parse.unquote(str(text)).lower()

def match_rule(rule: Dict[str, Any], payload: Dict[str, Any]) -> bool:
    field = str(rule.get("field", "")).strip().lower()
    pattern = str(rule.get("pattern", ""))
    pattern_type = str(rule.get("pattern_type", "")).strip().upper()

    # If specific field is targeted and present
    if field and field in payload and payload.get(field):
        text = normalize(payload.get(field, ""))
    else:
        # Search across all payload fields (uri, query, body)
        text = " ".join(normalize(v) for v in payload.values() if v)

    if pattern_type == "KEYWORD":
        return pattern.lower() in text
    elif pattern_type == "REGEX":
        return bool(re.search(pattern, text, re.IGNORECASE))

    return False
<<<<<<< HEAD
#It will detect the thread from the .xlsx sheet and retuen back it
=======

@app.get("/health")
def health():
    return {
        "status": "UP",
        "service": "almighty-threat-detection",
        "rules_loaded": len(rules),
        "rules_file": RULES_FILE
    }

@app.get("/rules")
def get_rules():
    return {
        "count": len(rules),
        "rules": rules
    }

@app.post("/reload-rules")
def reload_rules():
    load_rules()
    return {
        "status": "success",
        "rules_loaded": len(rules)
    }
>>>>>>> a90d9a3 (Restructure project folders and add new services with cassabdra log micro service and the threat detection microservice with the fast api)

@app.post("/detect")
def detect(payload: dict):
    print("Incoming Payload for detection:", payload)
    for rule in rules:
        if match_rule(rule, payload):
            threat = rule.get("threat_type", "UNKNOWN")
            print(f"Matched threat: {threat} on pattern: {rule.get('pattern')}")
            return {"threat_type": threat}

    return {"threat_type": "NONE"}
