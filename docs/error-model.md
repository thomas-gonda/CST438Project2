# API Error Model

The API uses RFC 9457 Problem Details for errors. Clients should use the HTTP status code and display the `detail` message when it is available.

```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "size must be between 1 and 100",
  "instance": "/api/v1/games"
}
```

| Status | Meaning |
|---|---|
| 400 | The request data or query parameters are invalid. |
| 401 | The request needs a valid sign-in token. |
| 403 | The signed-in user does not have the required role. |
| 404 | The requested resource does not exist. |
| 500 | The server could not complete the request. |

Error responses should not expose database details or secret configuration values.
