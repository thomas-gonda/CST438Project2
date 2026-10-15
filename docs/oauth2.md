# OAuth 2.0 configuration

The API acts as an OAuth 2.0 resource server using Auth0. When enabled, it validates Auth0 bearer JWTs before protected requests reach the controllers. The existing public game-catalog routes remain public.

Set these environment variables in a deployed environment:

```text
OAUTH2_ENABLED=true
OAUTH2_ISSUER_URI=https://YOUR_AUTH0_TENANT.us.auth0.com/
OAUTH2_AUDIENCE=https://your-api-identifier
OAUTH2_ADMIN_SCOPE=admin
BASIC_AUTH_ENABLED=false
```

In Auth0, create an API and use its Identifier as `OAUTH2_AUDIENCE`. Use the exact tenant issuer URL shown by Auth0, including its trailing slash. Spring Security discovers Auth0's signing keys and validates the token issuer and audience. If discovery is unavailable, configure `OAUTH2_JWK_SET_URI` instead. For local or test environments, `OAUTH2_JWT_SECRET` can validate HS256 tokens without contacting an identity provider.

An OAuth token receives administrator access when its configured admin scope is present, when its `roles`/`role` claim contains `ADMIN`, or when its verified email is listed in `OAUTH2_ADMIN_EMAILS`. Configure the Auth0 API permission used for administrators, such as `admin`, to match `OAUTH2_ADMIN_SCOPE`. The existing endpoint authorization rules continue to use `ROLE_ADMIN`.

Create an Auth0 application for the website and add the website's Render URL to Auth0's Allowed Web Origins and Allowed Callback URLs. The Render URL is not the issuer or audience; it is where the frontend/API is hosted. Auth0 issues the token, while the API validates it.

Keep issuer URLs, JWK URLs, and signing secrets in environment configuration. No provider credentials or signing secrets belong in source control.
