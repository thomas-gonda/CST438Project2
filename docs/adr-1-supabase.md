# ADR-001: Use Supabase PostgreSQL for the hosted database

## Decision

Use Supabase PostgreSQL as the shared production database for the GameReview API.

## Reason

The database is hosted separately from the Render application, so the container can be restarted or replaced without losing game, user, or review data. Supabase also gives the team one shared database for the deployed API. Which is easy to navigate and connect.

## Consequence

The database URL and credentials must be supplied as deployment environment variables. They must not be committed to the repository or placed in the Docker image.
