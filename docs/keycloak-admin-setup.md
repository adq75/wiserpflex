# Keycloak Admin Role Setup (Quickstart)

This document describes how to create a realm role for administrators and assign it to a user so they can call the admin endpoints (e.g. `POST /api/system/tenants`).

1. Start Keycloak (see docker-compose.yml)
   - Default dev startup: `http://localhost:8080` with admin/admin (as configured in docker-compose)

2. Log in to the Keycloak Admin Console
   - Open: http://localhost:8080
   - Click `Administration Console` and sign in with `admin` / `admin`.

3. Create a Realm (optional)
   - By default you can use the `master` realm for testing, or create a new realm (e.g. `wiseerp`).

4. Create a Client for your API
   - In the realm, go to `Clients` → `Create`.
   - Client ID: `wiseerp-api` (or any name)
   - Client Protocol: `openid-connect`
   - Root URL: `http://localhost:8081` (application port)
   - After creating, set `Access Type` to `confidential` (or `public` for dev) and save.
   - If `confidential`, note the client secret from `Credentials` tab — you'll need it for machine-to-machine flows.

5. Create a Realm Role for Admins
   - Go to `Roles` → `Add Role`.
   - Role Name: `ADMIN` (use uppercase to match Spring security checks).

6. Create a Test User and Assign Role
   - Go to `Users` → `Add user`.
   - Create user (e.g. `sysadmin`) and set `Enabled` = ON.
   - In the created user, go to `Credentials` and set a password (toggle `Temporary` off).
   - In `Role Mappings`, select `ADMIN` from `Available Roles` and `Add selected`.

7. Obtain a Token for Testing
   - For a quick token using password grant (if client is `public` or configured for ROPC):
     - POST to: `http://localhost:8080/realms/<realm>/protocol/openid-connect/token`
     - form data: `client_id=wiseerp-api`, `username=sysadmin`, `password=<password>`, `grant_type=password`
     - Response contains `access_token` (JWT) which you can use in `Authorization: Bearer <token>`.

8. Configure your application
   - Set `spring.security.oauth2.resourceserver.jwt.issuer-uri` to the realm issuer, e.g. `http://localhost:8080/realms/<realm>` in `application.yml` or environment variable `KEYCLOAK_ISSUER`.

Notes and security
- For production, use HTTPS and secure client credentials.
- Use realm roles or client roles based on your authorization model.
- Keep role names consistent with Spring `hasRole("ADMIN")` checks (Keycloak role `ADMIN` maps to authority `ROLE_ADMIN`).
