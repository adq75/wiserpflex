# Kubernetes Readiness & Liveness Probe Guidance

Use Spring Boot Actuator endpoints for Kubernetes probes. The project exposes actuator health endpoints; enable them in `application.yml` and configure Kubernetes to hit `/actuator/health/readiness` for readiness and `/actuator/health/liveness` for liveness.

Example Deployment snippet (Kubernetes):

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: wiseerp
spec:
  replicas: 1
  selector:
    matchLabels:
      app: wiseerp
  template:
    metadata:
      labels:
        app: wiseerp
    spec:
      containers:
        - name: wiseerp
          image: <your-image>
          ports:
            - containerPort: 8081
          readinessProbe:
            httpGet:
              path: /actuator/health/readiness
              port: 8081
            initialDelaySeconds: 10
            periodSeconds: 10
            failureThreshold: 6
          livenessProbe:
            httpGet:
              path: /actuator/health/liveness
              port: 8081
            initialDelaySeconds: 30
            periodSeconds: 30
            failureThreshold: 3
          env:
            - name: DB_URL
              value: jdbc:postgresql://postgres:5432/wiseerp
            - name: DB_USER
              value: wise
            - name: DB_PASS
              value: wisepass
            - name: KEYCLOAK_ISSUER
              value: http://keycloak:8080/realms/master

```

Recommendations
- Use `/actuator/health/readiness` so the pod only becomes READY after the `db` and `tenant` health checks pass.
- Tune `initialDelaySeconds` and `failureThreshold` to allow DB and migrations time to complete during startup.
- For production, secure actuator endpoints (don't expose them publicly) and require authentication for details.
- Consider using a separate readiness check that confirms migrations and tenant setup have completed if startup takes longer.
 
Configuration
- The set of required tables that the readiness check verifies is configurable in `application.yml` under `wiseerp.health.required-tables` (default: `audit_log`).
- If your tenant schemas require additional base tables on startup, add them to this list so the readiness probe waits until those tables are present.
