# Metadata Admin API - Samples

## List all entities

Request:

```bash
curl -s -X GET "http://localhost:8080/api/system/metadata/entities" \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Accept: application/json"
```

Response: 200 OK

Example response:

```json
[
  {
    "id": "62a0d833-697d-4610-9a18-12b1734209f2",
    "tenantId": "8b5d548c-fef5-4aac-a165-ef99dc75c420",
    "name": "Product",
    "displayName": "Product",
    "category": "MASTER",
    "schemaDefinition": {"columns": []},
    "fields": [],
    "version": "v1",
    "isActive": true
  }
]
```

## List entities for a tenant

```bash
curl -s -X GET "http://localhost:8080/api/system/metadata/entities?tenantId=11111111-2222-3333-4444-555555555555" \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Accept: application/json"
```

## List metadata change logs for a tenant

```bash
curl -s -X GET "http://localhost:8080/api/system/metadata/changes?tenantId=11111111-2222-3333-4444-555555555555" \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Accept: application/json"
```

Example response (200):

```json
[
  {
    "id": "b1f3d5a4-1a2b-4c3d-9e0f-abcdef012345",
    "tenantId": "11111111-2222-3333-4444-555555555555",
    "entityDefinitionId": "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee",
    "changeType": "CREATE",
    "changePayload": {"name":"Product","fields":[]},
    "changedBy": "admin@example.com",
    "createdAt": "2026-01-14T03:36:57.733Z"
  }
]
```

## Create a metadata change log

```bash
curl -s -X POST "http://localhost:8080/api/system/metadata/changes" \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "tenantId": "11111111-2222-3333-4444-555555555555",
    "entityDefinitionId": "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee",
    "changeType": "CREATE",
    "changePayload": {"name":"Product","fields":[]},
    "changedBy": "admin@example.com"
  }'
```

Example response (201 Created):

```json
{
  "id": "b1f3d5a4-1a2b-4c3d-9e0f-abcdef012345",
  "tenantId": "11111111-2222-3333-4444-555555555555",
  "entityDefinitionId": "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee",
  "changeType": "CREATE",
  "changePayload": {"name":"Product","fields":[]},
  "changedBy": "admin@example.com",
  "createdAt": "2026-01-14T03:36:57.733Z"
}
```
