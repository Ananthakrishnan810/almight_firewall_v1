from fastapi import FastAPI
import pandas as pd
import re
import urllib.parse

app = FastAPI()

df = pd.read_csv("rules.xlsx")
rules = df.to_dict(orient="records")

def normalize(text):
    if not text:
        return ""
    return urllib.parse.unquote(text).lower()

def match_rule(rule, payload):
    field = rule["field"]
    pattern = rule["pattern"]
    pattern_type = rule["pattern_type"]

    text = normalize(payload.get(field, ""))

    if pattern_type == "KEYWORD":
        return pattern.lower() in text

    elif pattern_type == "REGEX":
        return re.search(pattern, text, re.IGNORECASE)

    return False


@app.post("/detect")
def detect(payload: dict):
    print("Incoming Payload:", payload)
    for rule in rules:
        if match_rule(rule, payload):
            return {"threat_type": rule["threat_type"]}

    return {"threat_type": "NONE"}