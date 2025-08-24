# Spring Boot Backend Setup for Supabase Authentication

## Overview

Your Spring Boot backend is now configured to work with Supabase JWT authentication. Here's what has been implemented:

## ✅ What's Been Added

### 1. **Dependencies** (pom.xml)
- JWT parsing libraries (Auth0 and JJWT)
- Spring Boot WebFlux for HTTP client
- Spring Boot Security for authentication support

### 2. **JWT Authentication Filter**
- `JwtAuthenticationFilter.java` - Validates Supabase JWT tokens
- Extracts user information from tokens
- Sets authentication context for requests

### 3. **User Management**
- Updated `Users.java` model with proper JPA annotations
- Created `UserRole.java` and `UserStatus.java` enums
- `UsersRepository.java` for database operations
- `UserService.java` for user sync and management

### 4. **Authentication Endpoints**
- `/api/auth/me` - Get current user information
- `/api/auth/profile` - Update user profile
- `/api/auth/test` - Test backend connection

### 5. **Configuration Updates**
- Enhanced CORS configuration
- JWT secret configuration in properties
- Filter registration for JWT validation

## 🔧 Setup Steps

### Step 1: Get Your Supabase JWT Secret

1. Go to your Supabase Dashboard
2. Navigate to `Settings` > `API`
3. Copy the **JWT Secret** (not the anon key)
4. Update `application.properties`:

```properties
# Replace with your actual Supabase JWT secret
supabase.jwt.secret=your-actual-jwt-secret-here
```

### Step 2: Database Configuration

Your database should automatically create the users table with proper columns. If using a different database than the current setup, update these properties:

```properties
spring.datasource.url=your-database-url
spring.datasource.username=your-username
spring.datasource.password=your-password
```

### Step 3: Test the Setup

1. **Start your Spring Boot application:**
   ```bash
   ./mvnw spring-boot:run
   ```

2. **Test the connection:**
   ```bash
   curl http://localhost:8080/api/auth/test
   ```

3. **Test authenticated endpoint (you'll need a valid JWT token):**
   ```bash
   curl -H "Authorization: Bearer YOUR_JWT_TOKEN" http://localhost:8080/api/auth/me
   ```

## 🔄 How Authentication Works

### Frontend to Backend Flow:

1. **User logs in** through your React app with Supabase
2. **Supabase returns JWT token** to frontend
3. **Frontend includes token** in API requests: `Authorization: Bearer TOKEN`
4. **Backend validates token** using Supabase JWT secret
5. **Backend extracts user info** and syncs with local database
6. **Backend processes request** with authenticated user context

### JWT Token Structure:

The Supabase JWT contains:
- `sub` (subject) - User ID
- `email` - User email
- `user_metadata` - Additional user data (name, etc.)
- `exp` - Expiration time
- Other claims...

## 📝 Available API Endpoints

### Public Endpoints:
- `GET /api/auth/test` - Test connection

### Protected Endpoints (require JWT token):
- `GET /api/auth/me` - Get current user info
- `PUT /api/auth/profile` - Update user profile

### Example API Usage:

**Get current user:**
```javascript
// From your React app using the api utility
import { api } from '../lib/api';

const { data } = await api.get('/api/auth/me');
console.log(data); // User information
```

**Update profile:**
```javascript
const { data } = await api.put('/api/auth/profile', {
  firstName: 'John',
  lastName: 'Doe',
  bio: 'Software Developer'
});
```

## 🛡️ Security Features

### JWT Validation:
- Verifies token signature using Supabase secret
- Checks token expiration
- Extracts user claims safely

### CORS Configuration:
- Allows requests from your React app (localhost:5173)
- Supports all necessary HTTP methods
- Includes Authorization header support

### Database Sync:
- Automatically creates user records from JWT
- Updates existing users with new information
- Maintains user roles and status

## 🔧 Customization Options

### Adding New Protected Endpoints:

```java
@GetMapping("/api/protected-endpoint")
public ResponseEntity<?> protectedEndpoint(HttpServletRequest request) {
    Boolean authenticated = (Boolean) request.getAttribute("authenticated");
    
    if (authenticated == null || !authenticated) {
        return ResponseEntity.status(401).body("Not authenticated");
    }
    
    String userId = (String) request.getAttribute("userId");
    String email = (String) request.getAttribute("userEmail");
    
    // Your protected logic here
    return ResponseEntity.ok("Protected data");
}
```

### Accessing User Information in Controllers:

```java
// Get authentication status
Boolean authenticated = (Boolean) request.getAttribute("authenticated");

// Get user ID from JWT
String userId = (String) request.getAttribute("userId");

// Get user email
String email = (String) request.getAttribute("userEmail");

// Get full JWT claims
Claims claims = (Claims) request.getAttribute("userClaims");
```

## 🚀 Next Steps

1. **Configure your Supabase JWT secret** in application.properties
2. **Test the authentication** with your React frontend
3. **Add protected endpoints** for your specific business logic
4. **Implement role-based access control** using the UserRole enum
5. **Add additional user profile fields** as needed

## 🐛 Troubleshooting

### Common Issues:

1. **"Invalid JWT token" errors:**
   - Check that your JWT secret matches Supabase
   - Verify token is being sent with "Bearer " prefix

2. **CORS errors:**
   - Ensure your React app URL is in allowedOrigins
   - Check that all required headers are allowed

3. **Database connection issues:**
   - Verify your database configuration
   - Check that the users table exists

4. **User not found:**
   - The system automatically creates users on first login
   - Check that JWT contains required user information

The backend is now fully configured to work with your Supabase authentication system! 🎉
