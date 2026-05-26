#!/usr/bin/env bash
# Test all users-service endpoints via curl.
# Usage: ./scripts/test-endpoints.sh

set -euo pipefail

BASE_URL="http://localhost:8081"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
CYAN='\033[0;36m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

PASS=0
FAIL=0

check_status() {
    local expected="$1"
    local actual="$2"
    local label="$3"
    if [ "$actual" -eq "$expected" ]; then
        echo -e "  ${GREEN}✓ PASS${NC} ($actual)"
        PASS=$((PASS + 1))
    else
        echo -e "  ${RED}✗ FAIL${NC} (expected $expected, got $actual)"
        FAIL=$((FAIL + 1))
    fi
}

check_body_contains() {
    local haystack="$1"
    local needle="$2"
    local label="$3"
    if echo "$haystack" | grep -q "$needle"; then
        echo -e "  ${GREEN}✓ PASS${NC} — contains '$needle'"
        PASS=$((PASS + 1))
    else
        echo -e "  ${RED}✗ FAIL${NC} — expected to contain '$needle'"
        FAIL=$((FAIL + 1))
    fi
}

echo -e "${CYAN}========================================${NC}"
echo -e "${CYAN}  Users-Service: Endpoint Tests${NC}"
echo -e "${CYAN}  Base URL: $BASE_URL${NC}"
echo -e "${CYAN}========================================${NC}\n"

# ──────────────────────────────────────────────
echo -e "${YELLOW}[1] Health Check${NC}"
HEALTH=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/actuator/health" 2>/dev/null || echo "000")
check_status 200 "$HEALTH" "Health"

# ──────────────────────────────────────────────
echo -e "\n${YELLOW}[2] Create ADMIN${NC}"
ADMIN_RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/users" \
    -H "Content-Type: application/json" \
    -d '{
        "username": "testadmin",
        "password": "Admin123!",
        "email": "testadmin@medical.com",
        "role": "ADMIN"
    }' 2>/dev/null || echo "000")
ADMIN_HTTP=$(echo "$ADMIN_RESP" | tail -1)
ADMIN_BODY=$(echo "$ADMIN_RESP" | sed '$d')
check_status 201 "$ADMIN_HTTP" "Create ADMIN"
ADMIN_ID=$(echo "$ADMIN_BODY" | grep -oP '"id":\K[0-9]+' | head -1)
echo "         → ADMIN ID: $ADMIN_ID"

# ──────────────────────────────────────────────
echo -e "\n${YELLOW}[3] Create PATIENT${NC}"
PATIENT_RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/users" \
    -H "Content-Type: application/json" \
    -d '{
        "username": "testpatient",
        "password": "Clave123!",
        "email": "patient@email.com",
        "role": "PATIENT",
        "firstName": "Carlos",
        "lastName": "Méndez",
        "documentType": "CC",
        "documentNumber": "9876543210",
        "phone": "3001112233",
        "eps": "Sanitas"
    }' 2>/dev/null || echo "000")
PATIENT_HTTP=$(echo "$PATIENT_RESP" | tail -1)
PATIENT_BODY=$(echo "$PATIENT_RESP" | sed '$d')
check_status 201 "$PATIENT_HTTP" "Create PATIENT"
PATIENT_ID=$(echo "$PATIENT_BODY" | grep -oP '"id":\K[0-9]+' | head -1)
echo "         → PATIENT ID: $PATIENT_ID"

# ──────────────────────────────────────────────
echo -e "\n${YELLOW}[4] Create PROFESSIONAL${NC}"
PROF_RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/users" \
    -H "Content-Type: application/json" \
    -d '{
        "username": "testdoctor",
        "password": "Clave123!",
        "email": "doctor@medical.com",
        "role": "PROFESSIONAL",
        "firstName": "Ana",
        "lastName": "Torres",
        "specialty": "Quiropraxia",
        "licenseNumber": "LIC-2025-001",
        "phone": "3105556677"
    }' 2>/dev/null || echo "000")
PROF_HTTP=$(echo "$PROF_RESP" | tail -1)
PROF_BODY=$(echo "$PROF_RESP" | sed '$d')
check_status 201 "$PROF_HTTP" "Create PROFESSIONAL"
PROF_ID=$(echo "$PROF_BODY" | grep -oP '"id":\K[0-9]+' | head -1)
echo "         → PROFESSIONAL ID: $PROF_ID"

# ──────────────────────────────────────────────
echo -e "\n${YELLOW}[5] List All Users${NC}"
LIST_RESP=$(curl -s -w "\n%{http_code}" "$BASE_URL/api/users" 2>/dev/null || echo "000")
LIST_HTTP=$(echo "$LIST_RESP" | tail -1)
LIST_BODY=$(echo "$LIST_RESP" | sed '$d')
check_status 200 "$LIST_HTTP" "List All Users"
COUNT=$(echo "$LIST_BODY" | grep -o '"id"' | wc -l)
echo "         → Users returned: $COUNT"

# ──────────────────────────────────────────────
echo -e "\n${YELLOW}[6] Get User By ID${NC}"
GET_RESP=$(curl -s -w "\n%{http_code}" "$BASE_URL/api/users/$PATIENT_ID" 2>/dev/null || echo "000")
GET_HTTP=$(echo "$GET_RESP" | tail -1)
GET_BODY=$(echo "$GET_RESP" | sed '$d')
check_status 200 "$GET_HTTP" "Get User By ID"
check_body_contains "$GET_BODY" "testpatient" "Username matches"

# ──────────────────────────────────────────────
echo -e "\n${YELLOW}[7] Update User${NC}"
UPDATE_RESP=$(curl -s -w "\n%{http_code}" -X PUT "$BASE_URL/api/users/$PATIENT_ID" \
    -H "Content-Type: application/json" \
    -d '{
        "username": "testpatient_upd",
        "email": "patient_upd@email.com"
    }' 2>/dev/null || echo "000")
UPDATE_HTTP=$(echo "$UPDATE_RESP" | tail -1)
UPDATE_BODY=$(echo "$UPDATE_RESP" | sed '$d')
check_status 200 "$UPDATE_HTTP" "Update User"
check_body_contains "$UPDATE_BODY" "testpatient_upd" "Username updated"

# ──────────────────────────────────────────────
echo -e "\n${YELLOW}[8] Validate Patient By Document${NC}"
VALIDATE_RESP=$(curl -s -w "\n%{http_code}" "$BASE_URL/api/users/patients/validate/9876543210" 2>/dev/null || echo "000")
VALIDATE_HTTP=$(echo "$VALIDATE_RESP" | tail -1)
VALIDATE_BODY=$(echo "$VALIDATE_RESP" | sed '$d')
check_status 200 "$VALIDATE_HTTP" "Validate Patient"
check_body_contains "$VALIDATE_BODY" "true" "Patient exists"

# ──────────────────────────────────────────────
echo -e "\n${YELLOW}[9] Validate Non-existent Patient${NC}"
VALIDATE2_RESP=$(curl -s -w "\n%{http_code}" "$BASE_URL/api/users/patients/validate/0000000000" 2>/dev/null || echo "000")
VALIDATE2_HTTP=$(echo "$VALIDATE2_RESP" | tail -1)
VALIDATE2_BODY=$(echo "$VALIDATE2_RESP" | sed '$d')
check_status 200 "$VALIDATE2_HTTP" "Validate Non-existent"
check_body_contains "$VALIDATE2_BODY" "false" "Patient does not exist"

# ──────────────────────────────────────────────
echo -e "\n${YELLOW}[10] Deactivate User${NC}"
DEACT_RESP=$(curl -s -w "\n%{http_code}" -X PATCH "$BASE_URL/api/users/$PROF_ID/deactivate" 2>/dev/null || echo "000")
DEACT_HTTP=$(echo "$DEACT_RESP" | tail -1)
check_status 204 "$DEACT_HTTP" "Deactivate User"

# ──────────────────────────────────────────────
echo -e "\n${YELLOW}[11] Search by Username${NC}"
SEARCH_USERNAME_RESP=$(curl -s -w "\n%{http_code}" "$BASE_URL/api/users/search/username/testadmin" 2>/dev/null || echo "000")
SEARCH_USERNAME_HTTP=$(echo "$SEARCH_USERNAME_RESP" | tail -1)
SEARCH_USERNAME_BODY=$(echo "$SEARCH_USERNAME_RESP" | sed '$d')
check_status 200 "$SEARCH_USERNAME_HTTP" "Search by Username"
check_body_contains "$SEARCH_USERNAME_BODY" "testadmin" "Found admin"

# ──────────────────────────────────────────────
echo -e "\n${YELLOW}[12] Search by Email${NC}"
SEARCH_EMAIL_RESP=$(curl -s -w "\n%{http_code}" "$BASE_URL/api/users/search/email/doctor@medical.com" 2>/dev/null || echo "000")
SEARCH_EMAIL_HTTP=$(echo "$SEARCH_EMAIL_RESP" | tail -1)
SEARCH_EMAIL_BODY=$(echo "$SEARCH_EMAIL_RESP" | sed '$d')
check_status 200 "$SEARCH_EMAIL_HTTP" "Search by Email"
check_body_contains "$SEARCH_EMAIL_BODY" "doctor@medical.com" "Found by email"

# ──────────────────────────────────────────────
echo -e "\n${YELLOW}[13] Search by Role${NC}"
SEARCH_ROLE_RESP=$(curl -s -w "\n%{http_code}" "$BASE_URL/api/users/search/role/PATIENT" 2>/dev/null || echo "000")
SEARCH_ROLE_HTTP=$(echo "$SEARCH_ROLE_RESP" | tail -1)
SEARCH_ROLE_BODY=$(echo "$SEARCH_ROLE_RESP" | sed '$d')
check_status 200 "$SEARCH_ROLE_HTTP" "Search by Role"
check_body_contains "$SEARCH_ROLE_BODY" "PATIENT" "Found patients"

# ──────────────────────────────────────────────
echo -e "\n${YELLOW}[14] Search by Status (Active)${NC}"
SEARCH_ACTIVE_RESP=$(curl -s -w "\n%{http_code}" "$BASE_URL/api/users/search/status?active=true" 2>/dev/null || echo "000")
SEARCH_ACTIVE_HTTP=$(echo "$SEARCH_ACTIVE_RESP" | tail -1)
SEARCH_ACTIVE_BODY=$(echo "$SEARCH_ACTIVE_RESP" | sed '$d')
check_status 200 "$SEARCH_ACTIVE_HTTP" "Search Active Users"

# ──────────────────────────────────────────────
echo -e "\n${YELLOW}[15] Advanced Search${NC}"
SEARCH_ADV_RESP=$(curl -s -w "\n%{http_code}" "$BASE_URL/api/users/search/advanced?role=ADMIN&active=true" 2>/dev/null || echo "000")
SEARCH_ADV_HTTP=$(echo "$SEARCH_ADV_RESP" | tail -1)
SEARCH_ADV_BODY=$(echo "$SEARCH_ADV_RESP" | sed '$d')
check_status 200 "$SEARCH_ADV_HTTP" "Advanced Search"
check_body_contains "$SEARCH_ADV_BODY" "ADMIN" "Found admins"

# ──────────────────────────────────────────────
echo -e "\n${YELLOW}[16] Create Duplicate Username (should fail)${NC}"
DUP_RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/users" \
    -H "Content-Type: application/json" \
    -d '{
        "username": "testadmin",
        "password": "Admin123!",
        "email": "otro@email.com",
        "role": "ADMIN"
    }' 2>/dev/null || echo "000")
DUP_HTTP=$(echo "$DUP_RESP" | tail -1)
DUP_BODY=$(echo "$DUP_RESP" | sed '$d')
check_status 400 "$DUP_HTTP" "Duplicate username rejected"
check_body_contains "$DUP_BODY" "already exists" "Error message"

# ──────────────────────────────────────────────
echo -e "\n${YELLOW}[17] Weak Password (should fail)${NC}"
WEAK_RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/users" \
    -H "Content-Type: application/json" \
    -d '{
        "username": "weakpwuser",
        "password": "short",
        "email": "weak@email.com",
        "role": "PATIENT"
    }' 2>/dev/null || echo "000")
WEAK_HTTP=$(echo "$WEAK_RESP" | tail -1)
check_status 400 "$WEAK_HTTP" "Weak password rejected"

# ──────────────────────────────────────────────
echo -e "\n${CYAN}========================================${NC}"
echo -e "${CYAN}  Results:${NC}"
echo -e "${CYAN}  ✅ PASS: $PASS${NC}"
echo -e "${CYAN}  ❌ FAIL: $FAIL${NC}"
echo -e "${CYAN}========================================${NC}"

exit $(( FAIL > 0 ? 1 : 0 ))
