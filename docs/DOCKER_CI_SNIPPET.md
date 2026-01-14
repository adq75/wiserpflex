# Docker & CI Snippet

This document contains quick commands for building, testing, and running the `wiseerp` service locally, and notes about the included GitHub Actions CI workflow.

- **Build the JAR and Docker image**

```bash
# build the JAR (skip tests during image build if desired)
mvn -B -DskipTests package

# build the docker image
docker build -t wiseerp:local .

# run the image locally
docker run --rm -p 8080:8080 wiseerp:local
```

- **Run tests locally**

```bash
mvn -B test
```

- **CI / Image push**

The repository includes a GitHub Actions workflow at `.github/workflows/ci.yml` that:
- runs `mvn test`,
- packages the app, and
- builds + pushes a Docker image to GitHub Container Registry as `ghcr.io/<owner>/<repo>:latest`.

If you prefer Docker Hub, update the workflow and provide Docker Hub credentials as repository secrets.
