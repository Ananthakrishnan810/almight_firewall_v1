import json
import urllib.request
import urllib.error
import sys

BASE_URL = "http://127.0.0.1:5000"

def make_request(method, path, data=None):
    url = f"{BASE_URL}{path}"
    headers = {"Content-Type": "application/json"} if data else {}
    body = json.dumps(data).encode("utf-8") if data else None
    req = urllib.request.Request(url, data=body, headers=headers, method=method)
    with urllib.request.urlopen(req) as resp:
        return resp.status, json.loads(resp.read().decode("utf-8"))

def run_tests():
    print(f"Connecting to Threat Detection Service at {BASE_URL}...")
    
    # Test 1: Health check
    status, res = make_request("GET", "/health")
    assert status == 200, f"Expected 200, got {status}"
    assert res.get("status") == "UP", f"Expected UP, got {res.get('status')}"
    print(f"✔ GET /health passed: status={res.get('status')}, rules_loaded={res.get('rules_loaded')}")
    
    # Test 2: Rules endpoint
    status, res = make_request("GET", "/rules")
    assert status == 200, f"Expected 200, got {status}"
    assert res.get("count", 0) > 0, "Expected non-zero rules"
    print(f"✔ GET /rules passed: {res.get('count')} active rules listed")
    
    # Test 3: Reload rules
    status, res = make_request("POST", "/reload-rules")
    assert status == 200, f"Expected 200, got {status}"
    assert res.get("status") == "success"
    print(f"✔ POST /reload-rules passed: {res.get('rules_loaded')} rules reloaded")
    
    # Test 4: SQL Injection Detection
    payload = {"query": "id=1 UNION SELECT 1,2,3", "uri": "/products", "body": ""}
    status, res = make_request("POST", "/detect", payload)
    assert status == 200
    assert res.get("threat_type") == "SQL_INJECTION", f"Expected SQL_INJECTION, got {res}"
    print(f"✔ POST /detect (SQL Injection) passed: {res}")
    
    # Test 5: Path Traversal Detection
    payload = {"query": "..%2F..%2Fetc%2Fpasswd", "uri": "/files", "body": ""}
    status, res = make_request("POST", "/detect", payload)
    assert status == 200
    assert res.get("threat_type") == "PATH_TRAVERSAL", f"Expected PATH_TRAVERSAL, got {res}"
    print(f"✔ POST /detect (Path Traversal) passed: {res}")
    
    # Test 6: XSS Detection
    payload = {"query": "%3Cscript%3Ealert(1)%3C/script%3E", "uri": "/search", "body": ""}
    status, res = make_request("POST", "/detect", payload)
    assert status == 200
    assert res.get("threat_type") == "XSS", f"Expected XSS, got {res}"
    print(f"✔ POST /detect (XSS) passed: {res}")
    
    # Test 7: Clean Request
    payload = {"query": "q=spring+boot&page=1", "uri": "/search", "body": "filter=active"}
    status, res = make_request("POST", "/detect", payload)
    assert status == 200
    assert res.get("threat_type") == "NONE", f"Expected NONE, got {res}"
    print(f"✔ POST /detect (Clean Request) passed: {res}")

    print("\nALL 7 THREAT DETECTION SERVICE TESTS PASSED SUCCESSFULLY!")

if __name__ == "__main__":
    try:
        run_tests()
    except Exception as e:
        print(f"Test failed with error: {e}", file=sys.stderr)
        sys.exit(1)
