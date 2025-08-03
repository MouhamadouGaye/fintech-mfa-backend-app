
# Banking Backend Application

A comprehensive, secure banking backend system built with Spring Boot, featuring robust authentication, transaction management, and audit logging.

## 🏗️ Architecture Overview

### Core Features
- **User Management**: Registration, authentication, profile management
- **Account Management**: Multiple account types, balance management, account controls
- **Transaction Processing**: Deposits, withdrawals, transfers with real-time processing
- **Security**: JWT-based authentication, role-based access control, audit logging
- **Admin Panel**: User management, account oversight, audit trail viewing

### Technology Stack
- **Framework**: Spring Boot 3.2.0
- **Security**: Spring Security 6 with JWT
- **Database**: H2 (development), PostgreSQL (production)
- **ORM**: Spring Data JPA with Hibernate
- **Validation**: Bean Validation (Hibernate Validator)
- **Documentation**: OpenAPI 3 (Swagger)
- **Testing**: JUnit 5, Mockito, Spring Boot Test

## 🚀 Quick Start

### Prerequisites
- Java 17 or higher
- Maven 3.6 or higher
- (Optional) PostgreSQL for production

### Installation

1. **Clone the repository**
```bash
git clone <repository-url>
cd banking-backend
```

2. **Build the application**
```bash
mvn clean install
```

3. **Run the application**
```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080/banking-api`

### Default Users
The application creates default users on startup:

**Admin User:**
- Username: `admin`
- Password: `Admin@123`
- Role: ADMIN

**Sample Customer:**
- Username: `john.doe`
- Password: `Customer@123`
- Role: CUSTOMER

## 📊 API Documentation

### Swagger UI
Access the interactive API documentation at:
`http://localhost:8080/banking-api/swagger-ui.html`

### Key Endpoints

#### Authentication
```
POST /api/auth/register    # User registration
POST /api/auth/login       # User login
POST /api/auth/logout      # User logout
```

#### Account Management
```
POST /api/accounts              # Create new account
GET /api/accounts/{id}          # Get account details
GET /api/accounts/my-accounts   # Get current user's accounts
PUT /api/accounts/{id}/freeze   # Freeze account (Admin)
PUT /api/accounts/{id}/unfreeze # Unfreeze account (Admin)
```

#### Transactions
```
POST /api/transactions/deposit   # Deposit money
POST /api/transactions/withdraw  # Withdraw money
GET /api/transactions/{id}       # Get transaction details
GET /api/transactions/my-transactions # Get user transactions
```

#### Transfers
```
POST /api/transfers             # Transfer money between accounts
```

#### Admin Operations
```
GET /api/admin/users           # Get all users
PUT /api/admin/users/{id}/lock # Lock user account
GET /api/admin/audit-logs      # View audit logs
```

## 🔐 Security Features

### Authentication & Authorization
- **JWT Tokens**: Stateless authentication with 5-hour expiration
- **Role-Based Access**: ADMIN, CUSTOMER, MANAGER roles
- **Account Lockout**: Automatic lockout after 5 failed login attempts
- **Password Security**: BCrypt hashing with strength 12

### Security Headers & CORS
- Comprehensive CORS configuration
- Security headers for XSS protection
- CSRF protection disabled for REST API

### Audit Logging
- Complete audit trail for all operations
- IP address and user agent tracking
- Automatic logging of security events

## 💾 Database Schema

### Core Entities

#### Users Table
```sql
- id (Primary Key)
- username (Unique)
- email (Unique)
- password (Encrypted)
- first_name, last_name
- phone_number, address, date_of_birth
- role (CUSTOMER, ADMIN, MANAGER)
- is_active, is_verified
- failed_login_attempts
- account_locked_until
- created_at, updated_at
```

#### Accounts Table
```sql
- id (Primary Key)
- account_number (Unique)
- account_type (SAVINGS, CHECKING, BUSINESS, FIXED_DEPOSIT)
- balance, available_balance
- daily_limit
- is_active, is_frozen
- user_id (Foreign Key)
- created_at, updated_at
```

#### Transactions Table
```sql
- id (Primary Key)
- transaction_reference (Unique)
- transaction_type (DEPOSIT, WITHDRAWAL, TRANSFER, PAYMENT, REFUND)
- amount, description
- status (PENDING, COMPLETED, FAILED, CANCELLED)
- from_account_id, to_account_id (Foreign Keys)
- balance_before, balance_after
- failure_reason
- created_at, processed_at
```

## 🧪 Testing

### Running Tests
```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=UserServiceTest

# Run with coverage
mvn test jacoco:report
```

### Test Coverage
- Unit tests for all service methods
- Integration tests for complete workflows
- Mock-based testing for external dependencies
- JaCoCo coverage reporting

## 🚀 Deployment

### Development Profile
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Production Profile
```bash
# Set environment variables
export DB_USERNAME=your_db_user
export DB_PASSWORD=your_db_password

# Run with production profile
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

### Docker Deployment
```dockerfile
FROM openjdk:17-jdk-slim
COPY target/banking-backend-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

## 🔧 Configuration

### Application Properties
Key configuration options in `application.yml`:

```yaml
banking:
  account:
    default-daily-limit: 5000.00
    max-daily-limit: 50000.00
  transaction:
    max-amount: 1000000.00
    daily-transaction-limit: 100
  security:
    max-failed-attempts: 5
    account-lock-duration: 24
```

### Database Configuration
- **Development**: H2 in-memory database
- **Production**: PostgreSQL with connection pooling
- **Auto-DDL**: Create-drop in dev, validate in production

## 📈 Monitoring & Observability

### Actuator Endpoints
- `/actuator/health` - Application health status
- `/actuator/metrics` - Application metrics
- `/actuator/info` - Application information

### Logging
- Structured logging with configurable levels
- Separate log files for different components
- Audit trail logging for security events

## 🔒 Security Best Practices

### Implemented Security Measures
1. **Input Validation**: Comprehensive validation using Bean Validation
2. **SQL Injection Prevention**: JPA/Hibernate parameterized queries
3. **XSS Protection**: JSON serialization, input sanitization
4. **Authentication**: Strong password policies, account lockout
5. **Authorization**: Method-level security, role-based access
6. **Audit Logging**: Complete audit trail for compliance

### Security Headers
- `X-Content-Type-Options: nosniff`
- `X-Frame-Options: DENY`
- `X-XSS-Protection: 1; mode=block`

## 🤝 Contributing

### Code Style
- Follow Java coding conventions
- Use meaningful variable and method names
- Write comprehensive unit tests
- Document public APIs

### Pull Request Process
1. Create feature branch from main
2. Implement changes with tests
3. Ensure all tests pass
4. Update documentation if needed
5. Submit pull request with description

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 📞 Support

For support and questions:
- Create an issue in the repository
- Contact the development team
- Check the documentation and FAQ

## 🗺️ Roadmap

### Upcoming Features
- [ ] Multi-factor authentication (MFA)
- [ ] Real-time notifications
- [ ] Advanced reporting and analytics
- [ ] Mobile API optimizations
- [ ] Microservices migration
- [ ] Enhanced fraud detection
- [ ] International transfer support
- [ ] Cryptocurrency integration

---

**Note**: This is a demonstration banking application. For production use, additional security measures, compliance checks, and thorough security auditing would be required.