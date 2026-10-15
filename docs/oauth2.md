# OAuth 2.0 configuration

The API acts as an OAuth 2.0 resource server. This project is configured for Google OpenID Connect by default. When enabled, it validates bearer JWTs before protected requests reach the controllers. The existing public game-catalog routes remain public.

Set these environment variables in a deployed environment:

```text
OAUTH2_ENABLED=true
OAUTH2_ISSUER_URI=https://accounts.google.com
OAUTH2_ADMIN_EMAILS=admin@example.com
BASIC_AUTH_ENABLED=false
```

The issuer URI is the preferred configuration because Spring Security discovers Google's signing keys and validates the token issuer. If discovery is unavailable, configure `OAUTH2_JWK_SET_URI` instead. For local or test environments, `OAUTH2_JWT_SECRET` can validate HS256 tokens without contacting an identity provider.

An OAuth token receives administrator access when its configured admin scope is present, when its `roles`/`role` claim contains `ADMIN`, or when its verified email is listed in `OAUTH2_ADMIN_EMAILS`. The existing endpoint authorization rules continue to use `ROLE_ADMIN`.

Create the Google OAuth client in Google Cloud and add the website's Render URL to the authorized origins or redirect URIs for that client. The Render URL is not the issuer; it is where the frontend/API is hosted.

Keep issuer URLs, JWK URLs, and signing secrets in environment configuration. No provider credentials or signing secrets belong in source control.
