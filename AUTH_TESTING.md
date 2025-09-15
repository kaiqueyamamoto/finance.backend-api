# Authentication Testing Guide

## Problem Fixed
The 403 errors on `/auth/me` and `/auth/validate` endpoints were caused by a critical bug in the JWT configuration where a new random secret key was generated for each request, making token validation impossible.

## Changes Made
1. **Fixed JwtConfig.java**: Updated `getSigningKey()` method to use the configured secret from `application.properties` instead of generating random keys
2. **Updated SecurityConfig.java**: Explicitly configured authentication requirements for `/auth/me` and `/auth/validate` endpoints

## Testing the Fix

### Method 1: Using the Test Script
```bash
# Make sure the application is running on port 8080
./test-auth.sh
```

### Method 2: Manual Testing with curl

1. **Login to get a token:**
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "kaiqueyamamoto",
    "password": "B4aHwdyVX5RXal08eHzzGzydfAFHUAMhW7s61bktGU8="
  }'
```

2. **Test /auth/me with the token:**
```bash
curl -X GET http://localhost:8080/auth/me \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

3. **Test /auth/validate with the token:**
```bash
curl -X GET http://localhost:8080/auth/validate \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

### Method 3: Using Swagger UI
1. Open http://localhost:8080/swagger-ui.html
2. Click on the "Authentication" section
3. Use the "Authorize" button to set your JWT token
4. Test the `/auth/me` and `/auth/validate` endpoints

## Expected Results
- ✅ Login should return a JWT token
- ✅ `/auth/me` should return user information when authenticated
- ✅ `/auth/validate` should return token validation status when authenticated
- ❌ Both endpoints should return 401/403 when not authenticated

## Default User Credentials
- **Username**: `kaiqueyamamoto`
- **Password**: `B4aHwdyVX5RXal08eHzzGzydfAFHUAMhW7s61bktGU8=`
- **Roles**: `USER,ADMIN`
