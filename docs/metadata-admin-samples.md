# Metadata Admin API - Samples

## List all entities

Request:

```bash
curl -s -X GET "http://localhost:8080/api/system/metadata/entities" \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Accept: application/json"
```

Response: 200 OK

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
