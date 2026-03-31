# Spring Security + JWT — How It Works in This Project

## The Big Picture

Before JWT, every API call was open — anyone could hit any endpoint.
After JWT, every API call must carry a **token** that proves who you are and what role you have.

```
Without JWT:   Frontend → API → Response  ✅ (no check)
With JWT:      Frontend → API → "Who are you?" → Token valid? → Response  ✅
                                                → No token?   → 401 ❌
                                                → Wrong role? → 403 ❌
```

---

## What is JWT?

JWT = **JSON Web Token**

It's a string that looks like this:
```
eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJoci5hZG1pbkBjb21wYW55LmNvbSJ9.abc123
```

It has 3 parts separated by dots:
1. **Header** — algorithm used (HS256)
2. **Payload** — who you are (email, expiry time)
3. **Signature** — proof it hasn't been tampered with

The server signs it with a secret key. If anyone changes the payload, the signature breaks and the server rejects it.

---

## The Files We Created

```
security/
  JwtUtil.java              ← Creates and validates tokens
  JwtAuthFilter.java        ← Intercepts every request, checks the token
  UserDetailsServiceImpl.java ← Loads user from DB by email

config/
  SecurityConfig.java       ← The master config — who can access what

services/
  AuthService.java          ← Interface
  impl/AuthServiceImpl.java ← Login logic

controller/
  AuthController.java       ← POST /api/auth/login endpoint

dtos/
  LoginRequest.java         ← { email, password }
  AuthResponse.java         ← { token, userId, fullName, role, ... }
```

---

## Flow 1 — Login (Getting a Token)

```
User types email + password
        ↓
POST /api/auth/login
        ↓
AuthController receives LoginRequest
        ↓
AuthServiceImpl.login()
        ↓
AuthenticationManager.authenticate()
  → Calls UserDetailsServiceImpl.loadUserByUsername(email)
  → Loads user from DB
  → Compares BCrypt hashed password
  → If wrong password → throws BadCredentialsException → 401
        ↓
Password correct → JwtUtil.generateToken(userDetails)
  → Creates JWT signed with our secret key
  → Token expires in 24 hours (86400000 ms)
        ↓
Returns AuthResponse { token, userId, fullName, role, ... }
        ↓
Frontend stores token in localStorage as "psi_token"
```

**Code — JwtUtil.generateToken():**
```java
return Jwts.builder()
    .subject(userDetails.getUsername())   // email goes in the token
    .issuedAt(new Date())
    .expiration(new Date(now + 86400000)) // 24 hours
    .signWith(getSigningKey())            // sign with secret
    .compact();
```

---

## Flow 2 — Every API Request After Login

```
Frontend makes any API call
  → axios interceptor adds header: Authorization: Bearer eyJhbGci...
        ↓
Request hits Spring Boot
        ↓
JwtAuthFilter runs BEFORE the controller (OncePerRequestFilter)
        ↓
  1. Read the Authorization header
  2. Extract the token (remove "Bearer ")
  3. Call JwtUtil.extractEmail(token) → get email from payload
  4. Load user from DB: UserDetailsServiceImpl.loadUserByUsername(email)
  5. Call JwtUtil.isTokenValid(token, userDetails)
     → checks email matches
     → checks token not expired
  6. If valid → set authentication in SecurityContext
     (Spring now knows who this user is for this request)
        ↓
SecurityConfig checks: does this user's role allow this endpoint?
  → Yes → request reaches the Controller ✅
  → No  → 403 Forbidden ❌
```

**Code — JwtAuthFilter:**
```java
String token = authHeader.substring(7); // remove "Bearer "
String email = jwtUtil.extractEmail(token);
UserDetails userDetails = userDetailsService.loadUserByUsername(email);

if (jwtUtil.isTokenValid(token, userDetails)) {
    // Tell Spring: this user is authenticated
    SecurityContextHolder.getContext().setAuthentication(authToken);
}
```

---

## Flow 3 — Role-Based Access Control

In `SecurityConfig.java` we define who can access what:

```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/api/auth/**").permitAll()          // login = public

    .requestMatchers(POST, "/api/users").hasRole("HR")    // only HR creates users
    .requestMatchers(POST, "/api/appraisals").hasRole("HR")

    .requestMatchers(PUT, "/api/appraisals/*/manager-review/**").hasRole("MANAGER")
    .requestMatchers(PUT, "/api/appraisals/*/self-assessment/**").hasRole("EMPLOYEE")

    .anyRequest().authenticated()                         // everything else = logged in
)
```

The role comes from `UserDetailsServiceImpl`:
```java
List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
// e.g. ROLE_HR, ROLE_MANAGER, ROLE_EMPLOYEE
```

Spring Security automatically checks `hasRole("HR")` against `ROLE_HR`.

---

## Password Hashing

Passwords are never stored as plain text.

```
User creates account with password "password123"
        ↓
BCryptPasswordEncoder.encode("password123")
        ↓
Stored in DB: "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"
        ↓
At login: BCrypt.matches("password123", storedHash) → true ✅
```

BCrypt is a one-way hash — you can never reverse it back to the original password.

---

## What Happens Without a Token

```
GET /api/users  (no Authorization header)
        ↓
JwtAuthFilter: no header → skip, don't set authentication
        ↓
SecurityConfig: .anyRequest().authenticated()
        ↓
Spring returns 401 Unauthorized
```

---

## What Happens With Wrong Role

```
Employee calls POST /api/appraisals (HR only)
        ↓
JwtAuthFilter: token valid, user authenticated as ROLE_EMPLOYEE
        ↓
SecurityConfig: .hasRole("HR") → EMPLOYEE doesn't have HR role
        ↓
Spring returns 403 Forbidden
```

---

## Summary — The 5 Key Classes

| Class | What it does |
|-------|-------------|
| `JwtUtil` | Creates tokens on login, validates tokens on each request |
| `JwtAuthFilter` | Runs before every request, reads the token, sets who the user is |
| `UserDetailsServiceImpl` | Loads user from DB by email, maps role to Spring authority |
| `SecurityConfig` | Defines which endpoints need which roles |
| `AuthServiceImpl` | Handles login — verifies password, returns token |

---

## The Token Lifecycle

```
Login → Token created (valid 24h)
         ↓
Every request → Token sent in header → Validated
         ↓
Token expires → Next request returns 401
         ↓
Frontend redirects to /login (axios interceptor handles this)
         ↓
User logs in again → New token
```

---

## One-Line Answers for Presentation

**Q: What is Spring Security?**
A framework that protects your API — handles authentication (who are you) and authorization (what can you do).

**Q: What is JWT?**
A signed token the server gives you after login. You send it with every request to prove your identity.

**Q: Why not sessions?**
Sessions store state on the server. JWT is stateless — the server doesn't remember anything, the token carries all the info. Better for REST APIs.

**Q: How does the server trust the token?**
It signed the token with a secret key. If anyone tampers with the payload, the signature breaks and the server rejects it.

**Q: What happens if the token is stolen?**
It works until it expires (24h). That's why HTTPS is important — it prevents the token from being intercepted in transit.
