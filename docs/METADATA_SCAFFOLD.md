Metadata domain scaffold overview

What exists (confirmed):
- `src/main/java/com/wiseerp/metadata/model/` : EntityDefinition, FieldDefinition, MetadataVersion, TenantEntity, MetadataChangeLog
- `src/main/java/com/wiseerp/metadata/repository/` : JPA repositories for entities and metadata
- `src/main/java/com/wiseerp/metadata/service/MetadataService.java` : business logic
- `src/main/java/com/wiseerp/metadata/controller/` : controllers for TenantAdmin, Metadata and Field definitions
- `src/main/java/com/wiseerp/metadata/dto/` : DTOs for entity and field definitions (with Lombok constructors)
- OpenAPI configuration: `src/main/java/com/wiseerp/config/OpenAPIConfig.java`
- Tests: existing integration tests for controllers and a service unit test under `src/test/java/com/wiseerp/metadata`

What I added:
- `k8s/metadata-deployment.yaml` : a basic Deployment + Service manifest for the metadata service

Next recommended steps:
- Add RBAC and secrets manifests for production-grade deployment
- Add CI job to build/push Docker image `wiseerp/wiseerp-core` and to apply `k8s/metadata-deployment.yaml` to a cluster
- Expand unit and integration tests to cover more service behaviors and error cases

If you want, I can:
- Create a Dockerfile and CI workflow to publish images
- Open a PR with these changes
