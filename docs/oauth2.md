# OAuth 2.0 configuration

The API acts as an OAuth 2.0 resource server. When enabled, it validates bearer JWTs before protected requests reach the controllers. The existing public game-catalog routes remain public.

Set these environment variables in a deployed environment:

```text
OAUTH2_ENABLED=true
OAUTH2_ISSUER_URI=https://your-identity-provider.example.com/
OAUTH2_ADMIN_SCOPE=admin
BASIC_AUTH_ENABLED=false
```

The issuer URI is the preferred configuration because Spring Security discovers the provider's signing keys and validates the token issuer. If discovery is unavailable, configure `OAUTH2_JWK_SET_URI` instead. For local or test environments, `OAUTH2_JWT_SECRET` can validate HS256 tokens without contacting an identity provider.

An OAuth token receives administrator access when its configured admin scope is present (the default scope is `admin`) or when its `roles`/`role` claim contains `ADMIN`. The existing endpoint authorization rules continue to use `ROLE_ADMIN`.

Keep issuer URLs, JWK URLs, and signing secrets in environment configuration. No provider credentials or signing secrets belong in source control.
