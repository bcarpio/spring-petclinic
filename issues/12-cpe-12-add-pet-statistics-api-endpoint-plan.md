# Code Generation Plan

**Issue:** #12 - [CPE-12] Add Pet Statistics API Endpoint
**Branch:** `12-cpe-12-add-pet-statistics-api-endpoint`
**Repository:** bcarpio/spring-petclinic
**Backend:** java
**Created:** 2025-12-05T01:20:38.229252

## Issue Description

  h2. User Story

  \{noformat}
  As a clinic administrator,
  I want to see statistics about pets in the clinic,
  So that I can understand our patient demographics.
  \{noformat}

  h2. Endpoint

  Path: GET /api/stats/pets

  Response:
  \{code:json}
  {
    "totalPets": 15,
    "petsByType": {
      "dog": 8,
      "cat": 5,
      "bird": 2
    },
    "averageVisitsPerPet": 2.3
  }
  \{code}

  h2. Acceptance Criteria

* Returns total count of all pets
* Returns count of pets grouped by pet type
* Calculates average visits per pet
* Returns 200 on success
* Returns 500 on database error

  h2. Notes

  Follow existing controller and service patterns in the codebase.

## Cached Standards

<!-- Standards retrieved during plan generation (avoid re-querying) -->
<!-- STANDARDS_BEGIN -->

### Lambda Standards

# Query: Spring Boot REST controller standards and patterns

## Source: tests-summary - spring-petclinic (relevance: 0.32)
# Spring PetClinic Unit Tests Summary

## Components Tested

### 1. **Model Layer**
- **ValidatorTests**: Bean validation framework testing
- **VetTests**: Entity serialization testing

### 2. **Controller Layer (Web MVC)**
- **OwnerController**: Owner CRUD operations, search, pagination
- **PetController**: Pet creation/update with validation
- **VisitController**: Visit creation and management
- **VetController**: Vet listing (HTML and JSON responses)
- **CrashController**: Exception handling

### 3. **Service/Repository Layer**
- **ClinicServiceTests**: Integration tests for repositories (Owner, Pet, Vet, Visit operations)

### 4. **Formatters & Validators**
- **PetTypeFormatterTests**: Custom formatter for PetType conversion
- **PetValidatorTests**: Custom validation logic for Pet entities

### 5. **System/Infrastructure**
- **I18nPropertiesSyncTest**: Internationalization completeness checks
- **MysqlTestApplication**: Testcontainers setup for MySQL testing

## Testing Patterns Used

### 1. **Slice Testing**
- `@WebMvcTest`: Isolates web layer testing (controllers only)
- `@DataJpaTest`: Isolates data layer testing (repositories)
- Focused, fast tests with minimal context loading

### 2. **Mocking Strategy**
- `@MockitoBean`: Mocks dependencies in slice tests
- `given().willReturn()`: BDD-style mocking with Mockito
- MockMvc for simulating HTTP requests without full server

### 3. **Test Organization**
- `@Nested` classes: Logical grouping of related test cases (e.g., error scenarios)
- `@BeforeEach`: Setup common test fixtures
- Descriptive test method names following `should...` or `test...` conventions

### 4. **Assertion Libraries**
- **AssertJ**: Fluent assertions (`assertThat()`)
- **Hamcrest**: Matchers for MockMvc (`hasProperty()`, `hasSize()`)
- **JUnit 5**: Modern testing framework with `@Test`, `@Nested`

### 5. **Test Data Builders**
- Helper methods like `george()`, `james()`, `helen()` create test fixtures
- Reusable entity creation patterns

### 6. **Integration Testing**
- `@Transactional`: Automatic rollback for data tests
- `@AutoConfigureTestDatabase`: Real database testing with Testcontainers
- End-to-end repository operations with actual persistence

## Coverage Approach

### 1. **Happy Path Testing**
- Successful form submissions
- Valid data retrieval
- Correct redirects and view rendering

### 2. **Error Scenarios**
- Validation failures (blank fields, invalid dates, duplicate names)
- Missing required fields
- Not found scenarios
- ID mismatch handling

### 3. **Edge Cases**
- Empty search results
- Future birth dates
- Pagination boundaries
- Serialization/deserialization

### 4. **Cross-Cutting Concerns**
- **I18n**: Ensures all strings are internationalized and translated
- **Native Image Compatibility**: Tests disabled in AOT/native mode (`@DisabledInNativeImage`, `@DisabledInAotMode`)
- **Database Profiles**: MySQL-specific test configuration

### 5. **HTTP Layer Testing**
- GET/POST request handling
- Model attribute validation
- View name verification
- Status code assertions
- JSON response validation
- Flash attribute handling

### 6. **Data Layer Testing**
- CRUD operations
- Query methods (findByLastName, pagination)
- Relationship management (Owner-Pet-Visit)
- Transaction boundaries
- ID generation

## Key Characteristics

- **Isolation**: Controllers tested independently from repositories
- **Fast Execution**: Slice tests avoid full application context
- **Maintainability**: Clear test structure with nested classes
- **Realistic**: Integration tests use actual database operations
- **Comprehensive**: Covers validation, formatting, controllers, services, and infrastructure

## Source:  - spring-petclinic (relevance: 0.32)
# Spring PetClinic - Architectural Summary

## 1. Primary Purpose
Spring PetClinic is a **sample application** demonstrating Spring Framework best practices for building a web-based veterinary clinic management system. It manages owners, their pets, veterinarians, specialties, and visit records. This is primarily an educational/reference implementation showcasing Spring Boot capabilities.

## 2. Architecture Pattern
**Monolithic MVC Architecture**
- Traditional three-tier web application
- Server-side rendered HTML using Thymeleaf templates
- Single deployable artifact (JAR/WAR)
- Direct database access through Spring Data JPA

## 3. Key Components

### Domain Modules (Package-by-Feature)
- **`owner`**: Owner management, pet registration, and visit scheduling
  - `OwnerController`, `PetController`, `VisitController`
  - `OwnerRepository`, `PetTypeRepository`
  - Domain entities: `Owner`, `Pet`, `PetType`, `Visit`

- **`vet`**: Veterinarian and specialty management
  - `VetController`, `VetRepository`
  - Domain entities: `Vet`, `Specialty`, `Vets` (collection wrapper)

- **`model`**: Base domain model classes
  - `BaseEntity`, `NamedEntity`, `Person` (abstract base classes)

- **`system`**: Cross-cutting concerns
  - `CacheConfiguration`: Caching setup
  - `WebConfiguration`: MVC configuration
  - `WelcomeController`: Home page
  - `CrashController`: Error handling demonstration

## 4. Technology Stack

### Core Framework
- **Spring Boot** (main application framework)
- **Spring MVC** (web layer)
- **Spring Data JPA** (data access)
- **Hibernate** (ORM implementation)

### View Layer
- **Thymeleaf** (server-side templating)
- **HTML/CSS** with custom SCSS
- **Bootstrap** (implied from CSS structure)

### Build Tools
- **Maven** (primary - `pom.xml`, `mvnw`)
- **Gradle** (alternative - `build.gradle`, `gradlew`)

### Database Support
- **H2** (default in-memory database)
- **HSQLDB** (alternative in-memory)
- **MySQL** (production option)
- **PostgreSQL** (production option)

### Additional Technologies
- **Java** (primary language)
- **Docker** (containerization support)
- **Kubernetes** (orchestration manifests in `k8s/`)

## 5. Infrastructure

### Containerization
- **Docker Compose** (`docker-compose.yml`): Local multi-container setup
- **Dockerfile** (`.devcontainer/Dockerfile`): Development container

### Kubernetes Resources (`k8s/`)
- `db.yml`: Database deployment/service
- `petclinic.yml`: Application deployment/service

### Development Environments
- **DevContainer** configuration for VS Code
- **Gitpod** configuration (`.gitpod.yml`)

### CI/CD Pipelines (`.github/workflows/`)
- `maven-build.yml`: Maven-based build pipeline
- `gradle-build.yml`: Gradle-based build pipeline
- `deploy-and-test-cluster.yml`: Kubernetes deployment testing

## 6. Dependencies

### Key Spring Dependencies
- Spring Boot Starter Web
- Spring Boot Starter Data JPA
- Spring Boot Starter Thymeleaf
- Spring Boot Starter Cache
- Spring Boot Starter Validation

### Database Drivers
- H2 Database
- MySQL Connector
- PostgreSQL Driver

### Testing Libraries
- JUnit (implied from test structure)
- Spring Boot Test
- MockMvc (for controller testing)

### Development Tools
- Spring Boot DevTools
- Checkstyle (with NoHTTP validation)

## 7. Testing Strategy

### Test Organization
```
src/test/java/
├── model/          # Domain validation tests
├── owner/          # Owner module unit tests
├── vet/            # Vet module unit tests
├── service/        # Service layer tests
└── system/         # System/integration tests
```

### Testing Levels

**Unit Tests**
- `OwnerControllerTests`, `PetControllerTests`, `VisitControllerTests`
- `Vet

## Source: docs-summary - spring-petclinic (relevance: 0.30)
# Spring PetClinic Documentation Summary

## Overview
The Spring PetClinic documentation is a comprehensive README for a sample Spring Boot application that serves as a learning and demonstration project. The documentation is well-organized and covers both basic and advanced usage scenarios.

## Topics Covered

### 1. **Getting Started**
- Application overview with visual diagrams and presentation slides
- Quick start instructions for running the application locally
- Support for both Maven and Gradle build tools
- Java version requirements (Java 17+)

### 2. **Running the Application**
- Local execution via command line
- Running directly through Maven/Gradle plugins
- Container deployment (using Spring Boot build plugin, no Dockerfile needed)
- Cloud development environments (Gitpod, GitHub Codespaces)

### 3. **Database Configuration**
- Default H2 in-memory database setup
- H2 console access for database inspection
- MySQL and PostgreSQL configuration options
- Docker commands for running databases locally
- Docker Compose support for database containers
- Spring profiles for switching between databases

### 4. **Development Workflow**
- IDE setup instructions (Eclipse, IntelliJ IDEA, VS Code, Spring Tools Suite)
- Test applications for rapid development feedback
- Testcontainers integration for testing
- CSS compilation from SCSS sources
- Prerequisites and step-by-step IDE configuration

### 5. **Technical Reference**
- Quick reference table for key configuration files
- Main application class location
- Properties files
- Caching configuration

### 6. **Community & Ecosystem**
- Related forks and alternative implementations
- Bug reporting and issue tracking
- Contribution guidelines
- History of contributions to other open-source projects

## Documentation Organization

The documentation follows a **progressive disclosure pattern**, structured as:

1. **Top-level overview** - Badges, quick links, and visual introduction
2. **Quick start** - Immediate "how to run" instructions
3. **Configuration details** - Database and deployment options
4. **Development setup** - IDE-specific instructions
5. **Reference materials** - Links to related projects and resources
6. **Community engagement** - Contributing and issue tracking

The structure moves from simple to complex, allowing users to get started quickly while providing deeper technical details for those who need them.


### Terraform Standards

# Query: Terraform ECS Fargate Java deployment configuration

## Source: config-summary - terraform-serverless (relevance: 0.40)
# Terraform-Serverless Configuration Summary

This is a **serverless booking/reservation system** deployed on AWS using the Serverless Framework v2 with Node.js 12.x runtime in the us-east-1 region.

## Architecture Overview

The system consists of **4 microservices**:

### 1. **API Service** (`api/serverless.yml`)
Main REST API with 5 Lambda functions:
- **register** - POST /users - User registration (writes to DynamoDB users table)
- **login** - POST /login - User authentication (uses JWT, queries users table via email GSI)
- **create_booking** - POST /bookings - Create new booking (requires authorization)
- **list_bookings** - GET /bookings - List bookings (requires authorization)
- **authorizer** - JWT-based custom authorizer for protected endpoints

### 2. **Bookings Consumer** (`bookings-consumer/serverless.yml`)
Event-driven processor:
- **stream_listener** - Listens to DynamoDB Streams from bookings table
- Publishes notifications to SNS topic when bookings are created/updated

### 3. **Email Notification** (`email-notification/serverless.yml`)
Email sender:
- **send_email** - Triggered by SQS queue
- Sends emails via SMTP server

### 4. **SMS Notification** (`sms-notification/serverless.yml`)
SMS sender:
- **send_sms** - Triggered by SQS queue
- Sends SMS via MessageBird API
- Memory: 128MB (dev) / 2048MB (prod)

## Key Patterns

- **Configuration**: All sensitive data/ARNs stored in AWS Systems Manager Parameter Store (SSM)
- **Security**: IAM roles per function, JWT authentication
- **Event Flow**: DynamoDB Streams → SNS → SQS → Notification Lambdas
- **Stage Management**: Supports multiple environments (dev/prod) via custom stage variable

## Source: deployment.md - outcome-ops-ai-assist-outcomeops (relevance: 0.38)
# Deployment Guide

## Prerequisites

1. **Terraform 1.5+**
   ```bash
   brew install terraform
   terraform version
   ```

2. **AWS CLI**
   ```bash
   aws --version
   aws configure  # Set credentials
   ```

3. **GitHub Personal Access Token**
   - Create at: https://github.com/settings/tokens
   - Scopes needed: `repo` (full control)
   - Store securely for next step

4. **Python 3.12+** (for local testing)
   ```bash
   pyenv install 3.12
   pyenv local 3.12
   ```

## Environment Setup

### 1. Clone Repository

```bash
git clone git@github.com:bcarpio/outcome-ops-ai-assist.git
cd outcome-ops-ai-assist
```

### 2. Create GitHub Token in SSM

Store your GitHub token in AWS SSM Parameter Store (encrypted):

```bash
# For dev environment
aws ssm put-parameter \
  --name /dev/outcome-ops-ai-assist/github/token \
  --value "YOUR_GITHUB_TOKEN" \
  --type SecureString \
  --overwrite

# For prod environment
aws ssm put-parameter \
  --name /prd/outcome-ops-ai-assist/github/token \
  --value "YOUR_GITHUB_TOKEN" \
  --type SecureString \
  --overwrite
```

### 3. Create GitHub Webhook Secret in SSM

The webhook secret is used to validate incoming GitHub webhook requests. Generate a random secret and store it:

```bash
# Generate a random secret (or use your own)
WEBHOOK_SECRET=$(openssl rand -hex 32)
echo "Save this secret for GitHub webhook configuration: $WEBHOOK_SECRET"

# For dev environment
aws ssm put-parameter \
  --name /dev/outcome-ops-ai-assist/github/webhook-secret \
  --value "$WEBHOOK_SECRET" \
  --type SecureString \
  --overwrite

# For prod environment
aws ssm put-parameter \
  --name /prd/outcome-ops-ai-assist/github/webhook-secret \
  --value "$WEBHOOK_SECRET" \
  --type SecureString \
  --overwrite
```

**Important**: Use this same secret when configuring the GitHub webhook in your repository settings.

### 4. Configure Terraform Variables

Create `terraform/dev.tfvars`:

```hcl
aws_region   = "us-west-2"
environment  = "dev"
app_name     = "outcome-ops-ai-assist"

repos_to_ingest = [
  {
    name    = "outcome-ops-ai-assist"
    project = "bcarpio/outcome-ops-ai-assist"
    type    = "standards"
  },
  {
    name    = "my-standards-repo"
    project = "myorg/my-standards-repo"
    type    = "standards"
  },
  {
    name    = "my-app"
    project = "myorg/my-app"
    type    = "application"
  }
]
```

Similarly, create `terraform/prd.tfvars` for production.

**Note**: These files are in `.gitignore` (sensitive data). Use `.tfvars.example` as reference.

## Deployment Process

### 1. Initialize Terraform

```bash
cd terraform
terraform init

# If switching between envs, reconfigure backend:
terraform init -reconfigure
```

### 2. Plan Infrastructure

```bash
# Dev environment
terraform plan -var-file=dev.tfvars -out=terraform.dev.out
```

Review the plan output. Key resources:
- S3 bucket for documents
- DynamoDB table for embeddings
- Lambda function for ingestion
- EventBridge schedule
- SSM parameters

### 3. Apply Infrastructure

```bash
# Apply the saved plan (always use plan file, not -auto-approve)
terraform apply terraform.dev.out

# Or apply directly with tfvars
terraform apply -var-file=dev.tfvars
```

This creates:
- S3 knowledge base bucket
- DynamoDB code-maps table
- ingest-docs Lambda function
- EventBridge hourly schedule
- SSM parameter store entries

### 4. Test Deployment

Verify everything is working:

```bash
# Check Lambda function
aws lambda get-function \
  --function-name dev-outcome-ops-ai-assist-ingest-docs \
  --region us-west-2

# Manually trigger ingestion
aws lambda invoke \
  --function-name dev-outcome-ops-ai-assist-ingest-docs \
  /tmp/response.json \
  --region us-west-2

cat /tmp/response.json

# Check CloudWatch logs
aws logs tail /aws/lambda/dev-outcome-ops-ai-assist-ingest-docs \
  --follow \
  --region us-west-2
```

### 5. Verify Data Ingestion

Check if documents were stored:

```bash
# List documents in S3
aws s3 ls s3://dev-outcome-ops-ai-assist-kb/ --recursive

# Scan DynamoDB table
aws dynamodb scan \
  --table-name dev-outcome-ops-ai-assist-code-maps \
  --max-items 5 \
  --region us-west-2
```

## Terraform State Management

### Remote State

State is stored in S3 with locking via DynamoDB. Configuration in `terraform/backend.tf`:

```hcl
backend "s3" {
  bucket         = "terraform-state-bucket"
  key            = "outcome-ops/terraform.tfstate"
  region         = "us-west-2"
  dynamodb_table = "terraform-locks"
  encrypt        = true
}
```

### Viewing State

```bash
# List all resources
terraform state list

# Show specific resource
terraform state show module.ingest_docs_lambda.aws_lambda_function.this[0]
```

### Backup State

```bash
terraform state pull > terraform-backup.json
```

## Updating Infrastructure

### Change Repository Allowlist

Update `dev.tfvars` or `prd.tfvars`:

```hcl
repos_to_ingest = [
  # Add new repo here
  {
    name    = "new-repo"
    project = "bcarpio/new-repo"
    type    = "standards"
  }
]
```

Then:

```bash
terraform plan -var-file=dev.tfvars -out=terraform.dev.out
terraform apply terraform.dev.out
```

This updates the SSM parameter without redeploying Lambda.

### Update Lambda Code

Changes to `lambda/ingest-docs/handler.py` or `requirements.txt` trigger Lambda redeploy:

```bash
terraform plan -var-file=dev.tfvars
# Shows: aws_lambda_function will be updated
terraform apply -var-file=dev.tfvars
```

### Update Lambda Timeout

Change in `terraform/lambda.tf`:

```hcl
module "ingest_docs_lambda" {
  timeout = 600  # Changed from 300
}
```

Then plan and apply.

## Destroying Infrastructure

### Delete Specific Resources

```bash
# Delete only Lambda, keep S3 and DynamoDB
terraform destroy \
  -target=module.ingest_docs_lambda \
  -var-file=dev.tfvars
```

### Delete Everything

**Warning**: This deletes all data in S3 and DynamoDB!

```bash
# First, empty S3 bucket
aws s3 rm s3://dev-outcome-ops-ai-assist-kb --recursive

# Disable DynamoDB versioning (if enabled)
aws dynamodb update-table \
  --table-name dev-outcome-ops-ai-assist-code-maps \
  --stream-specification StreamEnabled=false

# Then destroy all infrastructure
terraform destroy -var-file=dev.tfvars
```

## Monitoring Deployments

### CloudWatch Logs

```bash
# Real-time logs from Lambda
aws logs tail /aws/lambda/dev-outcome-ops-ai-assist-ingest-docs --follow

# Filter for errors
aws logs tail /aws/lambda/dev-outcome-ops-ai-assist-ingest-docs \
  --filter-pattern "ERROR"
```

### Metrics

View Lambda metrics in AWS Console:
- Invocations (hourly ingestion)
- Duration (how long ingestion takes)
- Errors (failed documents)
- Throttles (rate limiting)

### Alarms

Set CloudWatch alarms for:

```bash
# High error rate
aws cloudwatch put-metric-alarm \
  --alarm-name dev-outcome-ops-ingest-errors \
  --alarm-description "Ingest Lambda errors > 1" \
  --metric-name Errors \
  --namespace AWS/Lambda \
  --statistic Sum \
  --period 300 \
  --threshold 1 \
  --comparison-operator GreaterThanOrEqualToThreshold \
  --dimensions Name=FunctionName,Value=dev-outcome-ops-ai-assist-ingest-docs
```

## Troubleshooting

### Lambda fails with "ParameterNotFound"

**Issue**: GitHub token or webhook secret not in SSM

**Fix**:
```bash
# For GitHub token
aws ssm put-parameter \
  --name /dev/outcome-ops-ai-assist/github/token \
  --value "YOUR_TOKEN" \
  --type SecureString \
  --overwrite

# For webhook secret
aws ssm put-parameter \
  --name /dev/outcome-ops-ai-assist/github/webhook-secret \
  --value "YOUR_WEBHOOK_SECRET" \
  --type SecureString \
  --overwrite
```

### Lambda fails with "InvalidAction"

**Issue**: IAM role doesn't have Bedrock permissions

**Fix**: Re-apply Terraform to update role:
```bash
terraform apply -var-file=dev.tfvars -refresh=true
```

### S3 bucket already exists

**Issue**: Bucket name collision (S3 buckets are globally unique)

**Fix**: Change bucket name in Terraform or restore from backup state

### DynamoDB throttling

**Issue**: Many documents or large embeddings causing throttle

**Fix**:
```bash
# Increase on-demand capacity
aws dynamodb update-table \
  --table-name dev-outcome-ops-ai-assist-code-maps \
  --billing-mode PAY_PER_REQUEST
```

## Production Checklist

Before deploying to production (`prd.tfvars`):

- [ ] Test in dev environment completely
- [ ] Review Terraform plan (no surprises)
- [ ] Backup dev infrastructure: `terraform state pull > backup.json`
- [ ] Create prd.tfvars with production settings
- [ ] Store GitHub token in SSM for prd environment
- [ ] Plan production: `terraform plan -var-file=prd.tfvars`
- [ ] Review production plan (different resource names, etc.)
- [ ] Apply production: `terraform apply -var-file=prd.tfvars`
- [ ] Test production Lambda manually
- [ ] Verify production data appears in DynamoDB/S3
- [ ] Set up CloudWatch alarms
- [ ] Document any custom configurations

## Rollback Procedure

If something goes wrong:

### Rollback to Previous Terraform State

```bash
# List previous states
aws s3api list-object-versions \
  --bucket terraform-state-bucket \
  --prefix outcome-ops/

# Restore specific version
aws s3api get-object \
  --bucket terraform-state-bucket \
  --key outcome-ops/terraform.tfstate \
  --version-id YOUR_VERSION_ID \
  terraform-rollback.json

terraform state push terraform-rollback.json

# Re-apply from rolled-back state
terraform apply -var-file=dev.tfvars
```

### Rollback Lambda Code

```bash
# Previous Lambda version is in deployment package history
# Fastest fix: revert handler.py in git and redeploy

git revert HEAD~1
terraform apply -var-file=dev.tfvars
```

## Related Documentation

- **Architecture**: See `docs/architecture.md` for system design
- **Lambda Functions**: See `docs/lambda-*.md` for specific function details
- **ADRs**: See `docs/adr/` for architectural decisions
- **Infrastructure Code**: See `terraform/` for all IaC definitions


## Source: ADR: ADR-004-terraform-workflow (relevance: 0.36)
# ADR-004: Terraform Workflow and Module Standards

## Status: Accepted

## Context

OutcomeOps uses Terraform for infrastructure as code (IaC). We deploy to multiple environments (dev, prd) using workspaces. Consistent infrastructure requires:
- Standardized resource naming across all environments
- Community modules with exact version pinning
- Review of planned changes before applying
- Environment isolation to prevent accidental production changes

## Decision

### Resource Naming Convention

**All resources MUST follow this naming pattern:**

```
${var.environment}-${var.app_name}-{resource-name}
```

**Examples:**
```hcl
# DynamoDB table
name = "${var.environment}-${var.app_name}-licenses"
# Result: dev-outcomeops-licenses, prd-outcomeops-licenses

# Lambda function
function_name = "${var.environment}-${var.app_name}-generate-code"
# Result: dev-outcomeops-generate-code, prd-outcomeops-generate-code

# S3 bucket
bucket = "${var.environment}-${var.app_name}-artifacts"
# Result: dev-outcomeops-artifacts, prd-outcomeops-artifacts

# Secrets Manager
name = "${var.environment}-${var.app_name}/license/private-key"
# Result: dev-outcomeops/license/private-key
```

**Required variables in every Terraform project:**
```hcl
variable "environment" {
  description = "Environment name (dev, staging, prd)"
  type        = string
}

variable "app_name" {
  description = "Application name prefix for resources"
  type        = string
}
```

**Use locals for consistent prefixing:**
```hcl
locals {
  name_prefix = "${var.environment}-${var.app_name}"
}

# Then use throughout:
name = "${local.name_prefix}-licenses"
```

### Community Module Standards

**Always use terraform-aws-modules when available:**

```hcl
# DynamoDB
module "my_table" {
  source  = "terraform-aws-modules/dynamodb-table/aws"
  version = "4.2.0"  # Exact version
}

# Lambda
module "my_lambda" {
  source  = "terraform-aws-modules/lambda/aws"
  version = "8.1.2"  # Exact version
}

# S3
module "my_bucket" {
  source  = "terraform-aws-modules/s3-bucket/aws"
  version = "4.1.0"  # Exact version
}

# API Gateway
module "my_api" {
  source  = "terraform-aws-modules/apigateway-v2/aws"
  version = "5.0.0"  # Exact version
}
```

**Version pinning rules:**
- Pin exact `major.minor.patch` versions (e.g., `version = "4.2.0"`)
- NEVER use pessimistic operators (`~>`, `>=`)
- NEVER use `version = "*"` or omit version
- Update versions explicitly via code review

**Before adding a new module:**
1. Read the existing Terraform file first
2. Check what version is already in use for that module type
3. Use the exact same version as existing modules

### File Organization

```
terraform/
├── versions.tf      # Terraform and provider versions
├── variables.tf     # Input variables
├── main.tf          # Provider config and locals
├── dynamodb.tf      # DynamoDB tables
├── lambda.tf        # Lambda functions
├── s3.tf            # S3 buckets
├── secrets.tf       # Secrets Manager references
├── outputs.tf       # Output values
├── dev.tfvars       # Dev environment values
└── prd.tfvars       # Prd environment values
```

### Variable File Standards

**dev.tfvars:**
```hcl
environment = "dev"
app_name    = "outcomeops"
aws_region  = "us-east-1"
```

**prd.tfvars:**
```hcl
environment = "prd"
app_name    = "outcomeops"
aws_region  = "us-east-1"
```

### Terraform Deployment Workflow

**Always use plan output files for safety and review:**

```bash
cd terraform

# Step 1: Select workspace
terraform workspace select dev

# Step 2: Generate plan for dev environment
terraform plan -var-file=dev.tfvars -out=terraform.dev.out

# Step 3: Review the plan output
# Check what resources will be created, modified, or destroyed

# Step 4: Apply the plan (only after review)
terraform apply terraform.dev.out

# Step 5: Test in dev environment
# Verify features work as expected
# Check CloudWatch logs for errors

# Step 6: Deploy to production (only after dev is stable)
terraform workspace select prd
terraform plan -var-file=prd.tfvars -out=terraform.prd.out
# Review and apply
terraform apply terraform.prd.out
```

### Plan File Naming Convention

- Dev environment: `terraform.dev.out`
- Prd environment: `terraform.prd.out`
- **Never commit plan files to git** (already in .gitignore)

### Terraform Commands Reference

```bash
cd terraform
terraform workspace list
terraform workspace select dev
terraform fmt -recursive
terraform validate
terraform plan -var-file=dev.tfvars -out=terraform.dev.out
terraform apply terraform.dev.out
```

### Local Development vs CI/CD

**Always run locally before committing:**
- `terraform fmt -recursive` - Format code
- `terraform validate` - Validate syntax
- Review plan output

**Never run locally:**
- `terraform apply` to production - Use CI/CD or manual approval only
- Direct Lambda function deployments - Always use Terraform

### Safety Rules

**Never:**
- Apply Terraform without showing the plan first
- Apply to production without testing in dev first
- Force apply without reviewing the plan
- Apply infrastructure changes without a commit in git history
- Use different module versions in the same file

**Always:**
- Use `-out=` flag for terraform plan
- Review plan output before applying
- Test in dev environment first
- Use workspaces for environment isolation
- Prefix all resources with `${var.environment}-${var.app_name}`

## Consequences

### Positive
- Consistent resource naming across environments
- Easy to identify resource environment at a glance
- No version drift with exact pinning
- Plan review prevents infrastructure mistakes
- Output files ensure apply matches reviewed plan
- Workspace isolation prevents accidental production changes

### Tradeoffs
- Must maintain version consistency manually
- Plan review adds minor delay before apply (critical for safety)
- Two-step process (plan then apply) vs direct apply
- Must remember workspace selection for each environment

## Implementation

### Standard DynamoDB Module Usage

```hcl
module "licenses_table" {
  source  = "terraform-aws-modules/dynamodb-table/aws"
  version = "4.2.0"

  name      = "${var.environment}-${var.app_name}-licenses"
  hash_key  = "PK"
  range_key = "SK"

  billing_mode = "PAY_PER_REQUEST"

  attributes = [
    { name = "PK", type = "S" },
    { name = "SK", type = "S" }
  ]

  point_in_time_recovery_enabled = true

  tags = {
    Name        = "${var.environment}-${var.app_name}-licenses"
    Environment = var.environment
  }
}
```

### Standard Lambda Module Usage

```hcl
module "my_lambda" {
  source  = "terraform-aws-modules/lambda/aws"
  version = "8.1.2"

  function_name = "${var.environment}-${var.app_name}-my-function"
  description   = "Description of the function"
  handler       = "handler.handler"
  runtime       = "python3.12"
  timeout       = 30

  source_path = "../lambda/my-function"

  environment_variables = {
    ENV      = var.environment
    APP_NAME = var.app_name
  }

  cloudwatch_logs_retention_in_days = 7

  tags = {
    Name        = "${var.environment}-${var.app_name}-my-function"
    Environment = var.environment
  }
}
```

### AI-Assisted Deployment Protocol

When Claude Code handles deployment:

1. **Claude generates terraform plan files** with `-out=` flag
2. **Claude displays plan output** to developer
3. **Claude explains the infrastructure changes** clearly
4. **Claude waits for developer approval** before applying
5. **Claude applies the reviewed plan** only after approval

## Related ADRs

- ADR-001: Terraform Infrastructure Patterns - Module and version standards
- ADR-002: Development Workflow Standards - Overall development workflow
- ADR-003: Git Commit Standards - Commit format for infrastructure changes

## Version History

- v1.0 (2025-01-06): Initial Terraform workflow standards
- v1.1 (2025-01-25): Added resource naming conventions and module standards



### Testing Standards

# Query: JUnit 5 testing standards and patterns

## Source: tests-summary - spring-petclinic (relevance: 0.40)
# Spring PetClinic Unit Tests Summary

## Components Tested

### 1. **Model Layer**
- **ValidatorTests**: Bean validation framework testing
- **VetTests**: Entity serialization testing

### 2. **Controller Layer (Web MVC)**
- **OwnerController**: Owner CRUD operations, search, pagination
- **PetController**: Pet creation/update with validation
- **VisitController**: Visit creation and management
- **VetController**: Vet listing (HTML and JSON responses)
- **CrashController**: Exception handling

### 3. **Service/Repository Layer**
- **ClinicServiceTests**: Integration tests for repositories (Owner, Pet, Vet, Visit operations)

### 4. **Formatters & Validators**
- **PetTypeFormatterTests**: Custom formatter for PetType conversion
- **PetValidatorTests**: Custom validation logic for Pet entities

### 5. **System/Infrastructure**
- **I18nPropertiesSyncTest**: Internationalization completeness checks
- **MysqlTestApplication**: Testcontainers setup for MySQL testing

## Testing Patterns Used

### 1. **Slice Testing**
- `@WebMvcTest`: Isolates web layer testing (controllers only)
- `@DataJpaTest`: Isolates data layer testing (repositories)
- Focused, fast tests with minimal context loading

### 2. **Mocking Strategy**
- `@MockitoBean`: Mocks dependencies in slice tests
- `given().willReturn()`: BDD-style mocking with Mockito
- MockMvc for simulating HTTP requests without full server

### 3. **Test Organization**
- `@Nested` classes: Logical grouping of related test cases (e.g., error scenarios)
- `@BeforeEach`: Setup common test fixtures
- Descriptive test method names following `should...` or `test...` conventions

### 4. **Assertion Libraries**
- **AssertJ**: Fluent assertions (`assertThat()`)
- **Hamcrest**: Matchers for MockMvc (`hasProperty()`, `hasSize()`)
- **JUnit 5**: Modern testing framework with `@Test`, `@Nested`

### 5. **Test Data Builders**
- Helper methods like `george()`, `james()`, `helen()` create test fixtures
- Reusable entity creation patterns

### 6. **Integration Testing**
- `@Transactional`: Automatic rollback for data tests
- `@AutoConfigureTestDatabase`: Real database testing with Testcontainers
- End-to-end repository operations with actual persistence

## Coverage Approach

### 1. **Happy Path Testing**
- Successful form submissions
- Valid data retrieval
- Correct redirects and view rendering

### 2. **Error Scenarios**
- Validation failures (blank fields, invalid dates, duplicate names)
- Missing required fields
- Not found scenarios
- ID mismatch handling

### 3. **Edge Cases**
- Empty search results
- Future birth dates
- Pagination boundaries
- Serialization/deserialization

### 4. **Cross-Cutting Concerns**
- **I18n**: Ensures all strings are internationalized and translated
- **Native Image Compatibility**: Tests disabled in AOT/native mode (`@DisabledInNativeImage`, `@DisabledInAotMode`)
- **Database Profiles**: MySQL-specific test configuration

### 5. **HTTP Layer Testing**
- GET/POST request handling
- Model attribute validation
- View name verification
- Status code assertions
- JSON response validation
- Flash attribute handling

### 6. **Data Layer Testing**
- CRUD operations
- Query methods (findByLastName, pagination)
- Relationship management (Owner-Pet-Visit)
- Transaction boundaries
- ID generation

## Key Characteristics

- **Isolation**: Controllers tested independently from repositories
- **Fast Execution**: Slice tests avoid full application context
- **Maintainability**: Clear test structure with nested classes
- **Realistic**: Integration tests use actual database operations
- **Comprehensive**: Covers validation, formatting, controllers, services, and infrastructure

## Source: tests-summary - spring-petclinic (relevance: 0.32)
# Spring PetClinic Integration Tests Summary

## Overview
The integration tests validate the Spring PetClinic application's functionality across different database configurations and error handling scenarios using full application context startup.

## Components Tested

### 1. **Database Integration**
- **VetRepository**: Cache functionality tested across all database profiles
- **Owner Details Endpoint**: HTTP endpoint validation (`/owners/1`)
- **Multiple Database Backends**:
  - Default (H2 in-memory)
  - MySQL via Testcontainers
  - PostgreSQL via Docker Compose

### 2. **Error Handling**
- **CrashController**: Exception handling and error page rendering
- Content negotiation (JSON vs HTML responses)
- Custom error page templates

## Testing Patterns Used

### 1. **Full Application Context Testing**
```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
```
- Starts complete Spring Boot application
- Uses random ports to avoid conflicts
- Real HTTP server for end-to-end testing

### 2. **Testcontainers Pattern** (MySqlIntegrationTests)
```java
@Testcontainers(disabledWithoutDocker = true)
@ServiceConnection
@Container
static MySQLContainer container = ...
```
- Automatic Docker container lifecycle management
- Service connection auto-configuration
- Disabled when Docker unavailable

### 3. **Docker Compose Integration** (PostgresIntegrationTests)
```java
properties = { "spring.docker.compose.skip.in-tests=false" }
```
- Uses production-like Docker Compose setup
- Conditional execution based on Docker availability
- Property source introspection for debugging

### 4. **REST Client Testing**
- **RestTemplate** with `RestTemplateBuilder` for HTTP calls
- **TestRestTemplate** for simplified test client setup
- `RequestEntity`/`ResponseEntity` for request/response handling

### 5. **Content Negotiation Testing**
```java
headers.setAccept(List.of(MediaType.TEXT_HTML))
```
- Tests both JSON and HTML responses
- Validates appropriate error formatting per content type

### 6. **Conditional Test Execution**
```java
@DisabledInNativeImage
@DisabledInAotMode
@BeforeAll static void available() { assumeTrue(...) }
```
- Skips tests in native/AOT compilation modes
- Runtime checks for Docker availability

## Coverage Approach

### 1. **Multi-Database Coverage**
- **Default Profile**: H2 in-memory (PetClinicIntegrationTests)
- **MySQL Profile**: Production-like MySQL database
- **PostgreSQL Profile**: Docker Compose orchestration
- Validates database portability and configuration

### 2. **Smoke Testing Strategy**
- Basic repository operations (findAll with caching)
- Critical endpoint availability (owner details)
- Minimal but representative test coverage

### 3. **Error Handling Coverage**
- Exception propagation to error pages
- JSON error responses with proper structure
- HTML error page rendering with custom templates
- HTTP status code validation (500 errors)

### 4. **Configuration Testing**
- Active profiles validation
- Property source resolution (PostgresIntegrationTests)
- Auto-configuration exclusions (CrashControllerIntegrationTests)

### 5. **Isolation Levels**
- **Database tests**: Full stack with real databases
- **Error controller tests**: Minimal context (excludes JPA/DataSource)
- Balances thoroughness with test execution speed

## Key Assertions

1. **HTTP Status Codes**: `assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK)`
2. **Response Structure**: Map key/value validation for error responses
3. **Content Validation**: HTML structure and absence of default error pages
4. **Cache Behavior**: Implicit validation through repeated calls
5. **Property Resolution**: Non-null assertions on configuration properties

## Notable Features

- **Reusable main() methods** for manual testing
- **Property logging utility** for debugging configuration
- **Graceful degradation** when Docker unavailable
- **Minimal test data dependencies** (uses seeded data)
- **Production-like testing** with real databases and HTTP

---STANDARD---
# Query: Mockito mocking patterns for Spring services

## Source: tests-summary - spring-petclinic (relevance: 0.38)
# Spring PetClinic Unit Tests Summary

## Components Tested

### 1. **Model Layer**
- **ValidatorTests**: Bean validation framework testing
- **VetTests**: Entity serialization testing

### 2. **Controller Layer (Web MVC)**
- **OwnerController**: Owner CRUD operations, search, pagination
- **PetController**: Pet creation/update with validation
- **VisitController**: Visit creation and management
- **VetController**: Vet listing (HTML and JSON responses)
- **CrashController**: Exception handling

### 3. **Service/Repository Layer**
- **ClinicServiceTests**: Integration tests for repositories (Owner, Pet, Vet, Visit operations)

### 4. **Formatters & Validators**
- **PetTypeFormatterTests**: Custom formatter for PetType conversion
- **PetValidatorTests**: Custom validation logic for Pet entities

### 5. **System/Infrastructure**
- **I18nPropertiesSyncTest**: Internationalization completeness checks
- **MysqlTestApplication**: Testcontainers setup for MySQL testing

## Testing Patterns Used

### 1. **Slice Testing**
- `@WebMvcTest`: Isolates web layer testing (controllers only)
- `@DataJpaTest`: Isolates data layer testing (repositories)
- Focused, fast tests with minimal context loading

### 2. **Mocking Strategy**
- `@MockitoBean`: Mocks dependencies in slice tests
- `given().willReturn()`: BDD-style mocking with Mockito
- MockMvc for simulating HTTP requests without full server

### 3. **Test Organization**
- `@Nested` classes: Logical grouping of related test cases (e.g., error scenarios)
- `@BeforeEach`: Setup common test fixtures
- Descriptive test method names following `should...` or `test...` conventions

### 4. **Assertion Libraries**
- **AssertJ**: Fluent assertions (`assertThat()`)
- **Hamcrest**: Matchers for MockMvc (`hasProperty()`, `hasSize()`)
- **JUnit 5**: Modern testing framework with `@Test`, `@Nested`

### 5. **Test Data Builders**
- Helper methods like `george()`, `james()`, `helen()` create test fixtures
- Reusable entity creation patterns

### 6. **Integration Testing**
- `@Transactional`: Automatic rollback for data tests
- `@AutoConfigureTestDatabase`: Real database testing with Testcontainers
- End-to-end repository operations with actual persistence

## Coverage Approach

### 1. **Happy Path Testing**
- Successful form submissions
- Valid data retrieval
- Correct redirects and view rendering

### 2. **Error Scenarios**
- Validation failures (blank fields, invalid dates, duplicate names)
- Missing required fields
- Not found scenarios
- ID mismatch handling

### 3. **Edge Cases**
- Empty search results
- Future birth dates
- Pagination boundaries
- Serialization/deserialization

### 4. **Cross-Cutting Concerns**
- **I18n**: Ensures all strings are internationalized and translated
- **Native Image Compatibility**: Tests disabled in AOT/native mode (`@DisabledInNativeImage`, `@DisabledInAotMode`)
- **Database Profiles**: MySQL-specific test configuration

### 5. **HTTP Layer Testing**
- GET/POST request handling
- Model attribute validation
- View name verification
- Status code assertions
- JSON response validation
- Flash attribute handling

### 6. **Data Layer Testing**
- CRUD operations
- Query methods (findByLastName, pagination)
- Relationship management (Owner-Pet-Visit)
- Transaction boundaries
- ID generation

## Key Characteristics

- **Isolation**: Controllers tested independently from repositories
- **Fast Execution**: Slice tests avoid full application context
- **Maintainability**: Clear test structure with nested classes
- **Realistic**: Integration tests use actual database operations
- **Comprehensive**: Covers validation, formatting, controllers, services, and infrastructure

## Source: tests-summary - spring-petclinic (relevance: 0.31)
# Spring PetClinic Integration Tests Summary

## Overview
The integration tests validate the Spring PetClinic application's functionality across different database configurations and error handling scenarios using full application context startup.

## Components Tested

### 1. **Database Integration**
- **VetRepository**: Cache functionality tested across all database profiles
- **Owner Details Endpoint**: HTTP endpoint validation (`/owners/1`)
- **Multiple Database Backends**:
  - Default (H2 in-memory)
  - MySQL via Testcontainers
  - PostgreSQL via Docker Compose

### 2. **Error Handling**
- **CrashController**: Exception handling and error page rendering
- Content negotiation (JSON vs HTML responses)
- Custom error page templates

## Testing Patterns Used

### 1. **Full Application Context Testing**
```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
```
- Starts complete Spring Boot application
- Uses random ports to avoid conflicts
- Real HTTP server for end-to-end testing

### 2. **Testcontainers Pattern** (MySqlIntegrationTests)
```java
@Testcontainers(disabledWithoutDocker = true)
@ServiceConnection
@Container
static MySQLContainer container = ...
```
- Automatic Docker container lifecycle management
- Service connection auto-configuration
- Disabled when Docker unavailable

### 3. **Docker Compose Integration** (PostgresIntegrationTests)
```java
properties = { "spring.docker.compose.skip.in-tests=false" }
```
- Uses production-like Docker Compose setup
- Conditional execution based on Docker availability
- Property source introspection for debugging

### 4. **REST Client Testing**
- **RestTemplate** with `RestTemplateBuilder` for HTTP calls
- **TestRestTemplate** for simplified test client setup
- `RequestEntity`/`ResponseEntity` for request/response handling

### 5. **Content Negotiation Testing**
```java
headers.setAccept(List.of(MediaType.TEXT_HTML))
```
- Tests both JSON and HTML responses
- Validates appropriate error formatting per content type

### 6. **Conditional Test Execution**
```java
@DisabledInNativeImage
@DisabledInAotMode
@BeforeAll static void available() { assumeTrue(...) }
```
- Skips tests in native/AOT compilation modes
- Runtime checks for Docker availability

## Coverage Approach

### 1. **Multi-Database Coverage**
- **Default Profile**: H2 in-memory (PetClinicIntegrationTests)
- **MySQL Profile**: Production-like MySQL database
- **PostgreSQL Profile**: Docker Compose orchestration
- Validates database portability and configuration

### 2. **Smoke Testing Strategy**
- Basic repository operations (findAll with caching)
- Critical endpoint availability (owner details)
- Minimal but representative test coverage

### 3. **Error Handling Coverage**
- Exception propagation to error pages
- JSON error responses with proper structure
- HTML error page rendering with custom templates
- HTTP status code validation (500 errors)

### 4. **Configuration Testing**
- Active profiles validation
- Property source resolution (PostgresIntegrationTests)
- Auto-configuration exclusions (CrashControllerIntegrationTests)

### 5. **Isolation Levels**
- **Database tests**: Full stack with real databases
- **Error controller tests**: Minimal context (excludes JPA/DataSource)
- Balances thoroughness with test execution speed

## Key Assertions

1. **HTTP Status Codes**: `assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK)`
2. **Response Structure**: Map key/value validation for error responses
3. **Content Validation**: HTML structure and absence of default error pages
4. **Cache Behavior**: Implicit validation through repeated calls
5. **Property Resolution**: Non-null assertions on configuration properties

## Notable Features

- **Reusable main() methods** for manual testing
- **Property logging utility** for debugging configuration
- **Graceful degradation** when Docker unavailable
- **Minimal test data dependencies** (uses seeded data)
- **Production-like testing** with real databases and HTTP


### Code Style Standards

# Query: concise code style guidelines

## Source: ADR: ADR-003-git-commit-standards (relevance: 0.32)
# ADR-003: Git Commit Standards

## Status: Accepted

## Context

OutcomeOps AI Assist uses git for version control with a solo developer workflow. Consistent commit message formatting enables:
- Clear project history and changelog generation
- Automated tooling (semantic versioning, release notes)
- Easy searching for specific types of changes
- AI assistant (Claude Code) to follow commit conventions

We need standardized git workflow and commit message format that works for solo development with AI assistance.

## Decision

### Git Workflow (Solo Developer)

**Branch strategy:** None - all work goes directly to main

Since this is solo development with AI assistance, we commit directly to main:

```bash
# Pull latest
git pull origin main

# Make changes locally
# ... edit files, run all pre-commit checks ...

# Stage and commit with conventional commits
git add .
git commit -m "feat(component): clear description of what changed"

# Push to main
git push origin main
```

**Rationale:**
- Solo developer = no merge conflicts
- CI/CD runs on main branch pushes
- Simpler workflow for AI-assisted development
- Feature branches add overhead without benefit

### Commit Message Format

**All commit messages MUST follow the conventional commits format:**

```
<type>(<scope>): <description>

[optional body]

[optional footer]
```

**Required commit types:**
- `feat(scope):` - New features or functionality
- `fix(scope):` - Bug fixes
- `docs(scope):` - Documentation changes only
- `refactor(scope):` - Code improvements without changing functionality
- `test(scope):` - Test additions or updates
- `chore(scope):` - Maintenance tasks, dependency updates, configuration

**Examples:**
```
feat(lambda): add query-kb Lambda function
fix(terraform): correct IAM policy for DynamoDB access
docs(readme): update installation instructions
refactor(handler): improve error handling logic
test(query-kb): add unit tests for vector search
chore(deps): upgrade boto3 to version 1.28.0
feat(cicd): add GitHub Actions workflow for security scans
```

**Rules:**
- **Scope is required** (e.g., lambda, terraform, cli, docs, cicd)
- Description must be clear and concise
- Use lowercase for type and description
- No period at the end of the description
- **No emojis in commit messages**

### Pre-Commit Requirements

Before running `git commit`, ALWAYS execute:

1. **Format code**
   ```bash
   make fmt
   ```

2. **Run validation**
   ```bash
   make validate
   ```

3. **Run tests**
   ```bash
   make test
   # All tests must pass
   ```

4. **Update documentation if needed**
   - Did you add new features or Lambda functions?
   - Did you change how the system works?
   - Update README.md or relevant docs/lambda-*.md files

**Only commit if all checks pass successfully.**

### Git Commands Reference

```bash
git pull origin main
git status
git add .
git commit -m "conventional-format: description"
git push origin main
git log --oneline -10
```

## Consequences

### Positive
- Clear, searchable project history
- Conventional commits enable automated changelog generation
- Scope requirement forces developers to think about impact area
- AI assistants (Claude Code) can follow consistent patterns
- No emojis keeps commits professional and parseable

### Tradeoffs
- Slightly more verbose than free-form commit messages
- Requires discipline to follow format consistently
- No feature branches means no PR reviews (acceptable for solo development)

## Implementation

### Claude Code Protocol

When Claude Code commits changes:
1. Claude runs pre-commit checks (fmt, validate, test)
2. Claude writes commit message in conventional format
3. Claude includes attribution footer:
   ```
   🤖 Generated with [Claude Code](https://claude.com/claude-code)

   Co-Authored-By: Claude <noreply@anthropic.com>
   ```
4. Claude asks for approval before pushing to remote

### Enforcement

- CI/CD pipeline validates commit format (future enhancement)
- Knowledge base ingestion system uses these standards
- Claude Code queries knowledge base for standards before committing

## Related ADRs

- ADR-001: Creating ADRs - How to document architectural decisions
- ADR-002: Development Workflow Standards - Overall development workflow

## Version History

- v1.0 (2025-01-06): Initial git commit standards for outcome-ops-ai-assist



<!-- STANDARDS_END -->

## Implementation Steps

### Step 1: Create PetStatistics DTO ⏳

**Status:** pending
**Description:** Create a DTO to represent the pet statistics response with totalPets, petsByType map, and averageVisitsPerPet fields

**Files:**
- `src/main/java/org/springframework/samples/petclinic/dto/PetStatisticsDTO.java`

---

### Step 2: Create PetStatisticsService interface and implementation ⏳

**Status:** pending
**Description:** Create service layer to calculate pet statistics including total count, grouping by type, and average visits per pet

**Files:**
- `src/main/java/org/springframework/samples/petclinic/service/PetStatisticsService.java`

**KB Queries:**
- Spring Data JPA aggregation queries
- JPA group by and count operations

---

### Step 3: Create PetStatisticsController ⏳

**Status:** pending
**Description:** Create REST controller with GET /api/stats/pets endpoint that returns PetStatisticsDTO

**Files:**
- `src/main/java/org/springframework/samples/petclinic/controller/PetStatisticsController.java`

---

### Step 4: Create custom exception for statistics errors ⏳

**Status:** pending
**Description:** Create StatisticsException for handling database errors during statistics calculation

**Files:**
- `src/main/java/org/springframework/samples/petclinic/exception/StatisticsException.java`

---

### Step 5: Create unit tests for PetStatisticsService - happy path ⏳

**Status:** pending
**Description:** Create unit tests for successful statistics calculation: test total count, pets by type grouping, and average visits calculation (1-3 test methods)

**Files:**
- `src/test/java/org/springframework/samples/petclinic/service/PetStatisticsServiceTest.java`

---

### Step 6: Create unit tests for PetStatisticsService - error handling ⏳

**Status:** pending
**Description:** Create unit tests for database error scenarios: test exception handling when repository throws exceptions (1-3 test methods)

---

### Step 7: Create unit tests for PetStatisticsController - success cases ⏳

**Status:** pending
**Description:** Create controller tests for successful GET /api/stats/pets request: test 200 response, correct JSON structure, and proper service invocation (1-3 test methods)

**Files:**
- `src/test/java/org/springframework/samples/petclinic/controller/PetStatisticsControllerTest.java`

---

### Step 8: Create unit tests for PetStatisticsController - error handling ⏳

**Status:** pending
**Description:** Create controller tests for error scenarios: test 500 response when service throws StatisticsException (1-2 test methods)

---

### Step 9: Create integration tests for pet statistics endpoint ⏳

**Status:** pending
**Description:** Create integration tests with @SpringBootTest: test end-to-end statistics calculation with real database and verify response structure (1-3 test methods)

**Files:**
- `src/test/java/org/springframework/samples/petclinic/controller/PetStatisticsIntegrationTest.java`

---
