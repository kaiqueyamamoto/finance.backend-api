#!/bin/bash

# Test script for authentication endpoints
BASE_URL="http://localhost:8080"

echo "🔐 Testing Finance API Authentication"
echo "====================================="

# Test 1: Login with default user
echo -e "\n1. Testing login with default user..."
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "kaiqueyamamoto",
    "password": "B4aHwdyVX5RXal08eHzzGzydfAFHUAMhW7s61bktGU8="
  }')

echo "Login Response: $LOGIN_RESPONSE"

# Extract token from response
TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

if [ -n "$TOKEN" ]; then
    echo "✅ Token extracted: ${TOKEN:0:20}..."
    
    # Test 2: Test /auth/me endpoint
    echo -e "\n2. Testing /auth/me endpoint..."
    ME_RESPONSE=$(curl -s -X GET "$BASE_URL/auth/me" \
      -H "Authorization: Bearer $TOKEN")
    
    echo "Me Response: $ME_RESPONSE"
    
    # Test 3: Test /auth/validate endpoint
    echo -e "\n3. Testing /auth/validate endpoint..."
    VALIDATE_RESPONSE=$(curl -s -X GET "$BASE_URL/auth/validate" \
      -H "Authorization: Bearer $TOKEN")
    
    echo "Validate Response: $VALIDATE_RESPONSE"
    
else
    echo "❌ Failed to extract token from login response"
fi

# Test 4: Test without token (should fail)
echo -e "\n4. Testing /auth/me without token (should fail)..."
NO_TOKEN_RESPONSE=$(curl -s -X GET "$BASE_URL/auth/me")
echo "No Token Response: $NO_TOKEN_RESPONSE"

echo -e "\n====================================="
echo "Test completed!"
