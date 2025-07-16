# LessionPRM Android App - Backend API Integration

## Overview
This document outlines the complete backend API integration implementation for the LessionPRM Android application. The app now has comprehensive connectivity with the LessionPRM-Backend server.

## ✅ Implemented Features

### 1. Authentication System
- **LoginActivity**: Complete JWT-based authentication with token storage
- **RegisterActivity**: User registration with validation and error handling  
- **SplashActivity**: Auto-login functionality using stored JWT tokens
- **JWT Refresh**: Automatic token refresh on expiration (401 errors)

### 2. Course Management
- **CourseListFragment**: Course browsing with pagination, search, and infinite scroll
- **CourseDetailActivity**: Course details with enrollment functionality
- **Enrollment**: Automatic enrollment for free courses, payment flow for paid courses
- **MyCoursesFragment**: User's enrolled courses (UI ready for backend integration)

### 3. Payment Integration
- **PaymentActivity**: Complete MoMo payment integration
  - Creates payment requests via backend API
  - Opens MoMo app/web for payment
  - Real-time payment status monitoring
  - Automatic enrollment upon successful payment

### 4. User Profile Management
- **ProfileFragment**: Complete user profile functionality
  - Load profile data from backend
  - Update profile information (name, phone)
  - Offline fallback to cached data
  - Logout functionality

### 5. Admin Dashboard
- **AdminDashboardFragment**: Admin analytics and statistics
  - Total users, courses, enrollments statistics
  - Revenue charts with monthly data
  - Admin role verification
  - Interactive charts using MPAndroidChart

### 6. Network Layer Enhancements
- **ConfigManager**: Environment-specific configuration
  - Development, Staging, Production environments
  - Automatic environment detection based on build type
  - Configurable timeouts and retry counts
- **NetworkMonitor**: Real-time network connectivity monitoring
  - LiveData observables for network status
  - Network type detection (WiFi, Cellular, Ethernet)
- **RetrofitClient**: Enhanced with:
  - Automatic JWT token refresh
  - Request retry mechanism with exponential backoff
  - Environment-based logging
  - Proper error handling

## 🔧 Technical Implementation

### API Endpoints Integrated
```
Authentication:
- POST /auth/login
- POST /auth/register  
- POST /auth/refresh

Courses:
- GET /courses (with pagination and search)
- GET /courses/{id}
- POST /courses/{id}/enroll
- GET /courses/my-courses

Payment:
- POST /payment/momo/create
- GET /payment/momo/status/{orderId}

User Profile:
- GET /users/profile
- PUT /users/profile

Admin:
- GET /statistics/overview
- GET /revenue/monthly
```

### Configuration
```java
// Development (Emulator)
BASE_URL = "http://10.0.2.2:8080/api/"

// Staging  
BASE_URL = "https://staging-api.lessionprm.com/api/"

// Production
BASE_URL = "https://api.lessionprm.com/api/"
```

### Security Features
- JWT token automatic refresh
- Secure token storage in SharedPreferences
- SSL/TLS validation for production
- Request/response logging only in debug builds

### Error Handling
- Network connectivity checks
- Retry mechanism with exponential backoff
- User-friendly error messages in Vietnamese
- Offline data fallback where applicable

## 🚀 Usage Instructions

### 1. Backend Configuration
Update the base URLs in `ConfigManager.java` to point to your backend server:
```java
private static final String DEV_BASE_URL = "your-dev-server-url/api/";
private static final String STAGING_BASE_URL = "your-staging-server-url/api/";
private static final String PROD_BASE_URL = "your-production-server-url/api/";
```

### 2. Testing Authentication
1. Launch the app
2. Register a new account or login with existing credentials
3. Verify JWT token is stored and used for subsequent requests
4. Test auto-login by restarting the app

### 3. Testing Course Management
1. Browse courses in the course list
2. Search for specific courses
3. View course details
4. Enroll in free courses
5. Test payment flow for paid courses

### 4. Testing Payment Integration
1. Select a paid course
2. Proceed to payment
3. Test MoMo payment flow
4. Verify enrollment upon successful payment

### 5. Admin Features
1. Login with an admin account
2. Access admin dashboard
3. View statistics and revenue charts
4. Verify admin-only access control

## 📝 Notes for Developers

### Environment Switching
To switch environments, update the `ConfigManager.setEnvironment()` call or use build variants.

### Adding New Endpoints
1. Add method to `ApiService.java`
2. Create request/response models in `data/model/`
3. Implement UI logic in appropriate Activity/Fragment

### Network Monitoring
Access network status anywhere in the app:
```java
NetworkMonitor.getInstance(context).getNetworkAvailable().observe(this, isConnected -> {
    // Handle network state changes
});
```

### Error Handling Best Practices
- Always check network connectivity before API calls
- Provide user-friendly error messages
- Implement offline fallback where possible
- Use the retry mechanism for temporary failures

## 🔍 Testing Checklist

- [ ] User registration with valid/invalid data
- [ ] User login with correct/incorrect credentials
- [ ] Auto-login on app restart
- [ ] JWT token refresh on expiration
- [ ] Course list loading and pagination
- [ ] Course search functionality
- [ ] Course enrollment (free courses)
- [ ] Payment flow (paid courses)
- [ ] Profile data loading and updating
- [ ] Admin dashboard access control
- [ ] Network connectivity handling
- [ ] Offline scenario handling

## 🐛 Known Limitations

1. Profile picture upload not implemented (UI placeholder ready)
2. Push notifications not integrated
3. Offline course content not cached
4. Email verification not implemented

These features can be added in future iterations as needed.

## 📞 Support

For any issues or questions regarding the backend integration, please refer to the code comments and this documentation. The implementation follows Android best practices and is ready for production use with proper backend server configuration.