# Disabling Authentication for Local Development

The register/login system was included because the tutorial added it, but **it's not necessary for local use**. Here are your options:

## Option 1: Disable Authentication (Easiest for Local Testing)

Update `SecurityConfig.java` to allow all endpoints without authentication:

```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/**").permitAll() // Allow everything
)
```

## Option 2: Keep Simple Default User

Create a default user that's always available - no registration needed.

## Option 3: Keep It (Current Setup)

The current setup uses in-memory user storage (not a real database), so it's already simple:
- Just register once
- Credentials are stored in memory (lost on restart)
- No database needed

## Recommendation for Local Use

**Disable authentication** - it's just extra steps for local testing. Authentication makes sense for:
- Production deployments
- Multi-user scenarios
- When you want to track who generated what

But for local testing/documentation generation, it's unnecessary overhead.

