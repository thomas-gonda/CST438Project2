# ADR-002: Use Auth0 OAuth2 with PKCE

## Decision

Use Auth0 for user sign-in. The Android app uses the OAuth2 Authorization Code flow with PKCE, and the Spring Boot API acts as an OAuth2 Resource Server that validates bearer access tokens.

## Reason

The Android app cannot safely store a client secret, so PKCE protects the authorization code flow without requiring one. Auth0 handles sign-in, while the API checks the access token and the user's permissions. Google OAuth was looked over do to needed financial info.

## Consequence

The Android app sends the access token in the `Authorization: Bearer` header. The API rejects missing, invalid, or insufficiently privileged tokens with the appropriate HTTP status code. Auth0 configuration values and client credentials stay outside the repository.
