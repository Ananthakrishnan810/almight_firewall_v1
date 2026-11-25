#!/bin/bash
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$DIR"

echo "Starting Almighty Threat Detection Microservice on port 5000..."

if [ -f "$DIR/firewall-env/bin/python" ]; then
    "$DIR/firewall-env/bin/python" -m uvicorn main:app --host 0.0.0.0 --port 5000 --reload
else
    python3 -m uvicorn main:app --host 0.0.0.0 --port 5000 --reload
fi
