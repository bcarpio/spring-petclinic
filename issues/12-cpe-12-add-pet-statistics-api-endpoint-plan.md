# Code Generation Plan

**Issue:** #12 - [CPE-12] Add Pet Statistics API Endpoint
**Branch:** `12-cpe-12-add-pet-statistics-api-endpoint`
**Repository:** bcarpio/spring-petclinic
**Backend:** java
**Created:** 2025-12-04T23:08:19.834062

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

None

### Terraform Standards

# Query: Terraform ECS Fargate Java deployment configuration

## Source: deployment.md - outcome-ops-ai-assist-private (relevance: 0.38)
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

aws ssm put-parameter \
  --name /prd/outcome-ops-ai-assist/github/webhook-secret \
  --value "$WEBHOOK_SECRET" \
  --type SecureString \
  --overwrite
```

**Important**: Use this same secret when configuring the GitHub webhook in your repository settings.

### 4. Configure Terraform Variables

Create `terraform/prd.tfvars`:

```hcl
aws_region   = "us-west-2"
environment  = "prd"
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
terraform plan -var-file=prd.tfvars -out=terraform.prd.out
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
terraform apply terraform.prd.out

# Or apply directly with tfvars
terraform apply -var-file=prd.tfvars
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
  --function-name prd-outcome-ops-ai-assist-ingest-docs \
  --region us-west-2

# Manually trigger ingestion
aws lambda invoke \
  --function-name prd-outcome-ops-ai-assist-ingest-docs \
  /tmp/response.json \
  --region us-west-2

cat /tmp/response.json

# Check CloudWatch logs
aws logs tail /aws/lambda/prd-outcome-ops-ai-assist-ingest-docs \
  --follow \
  --region us-west-2
```

### 5. Verify Data Ingestion

Check if documents were stored:

```bash
# List documents in S3
aws s3 ls s3://prd-outcome-ops-ai-assist-kb/ --recursive

# Scan DynamoDB table
aws dynamodb scan \
  --table-name prd-outcome-ops-ai-assist-code-maps \
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

Update `prd.tfvars`:

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
terraform plan -var-file=prd.tfvars -out=terraform.prd.out
terraform apply terraform.prd.out
```

This updates the SSM parameter without redeploying Lambda.

### Update Lambda Code

Changes to `lambda/ingest-docs/handler.py` or `requirements.txt` trigger Lambda redeploy:

```bash
terraform plan -var-file=prd.tfvars
# Shows: aws_lambda_function will be updated
terraform apply -var-file=prd.tfvars
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
  -var-file=prd.tfvars
```

### Delete Everything

**Warning**: This deletes all data in S3 and DynamoDB!

```bash
# First, empty S3 bucket
aws s3 rm s3://prd-outcome-ops-ai-assist-kb --recursive

# Disable DynamoDB versioning (if enabled)
aws dynamodb update-table \
  --table-name prd-outcome-ops-ai-assist-code-maps \
  --stream-specification StreamEnabled=false

# Then destroy all infrastructure
terraform destroy -var-file=prd.tfvars
```

## Monitoring Deployments

### CloudWatch Logs

```bash
# Real-time logs from Lambda
aws logs tail /aws/lambda/prd-outcome-ops-ai-assist-ingest-docs --follow

# Filter for errors
aws logs tail /aws/lambda/prd-outcome-ops-ai-assist-ingest-docs \
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
  --alarm-name prd-outcome-ops-ingest-errors \
  --alarm-description "Ingest Lambda errors > 1" \
  --metric-name Errors \
  --namespace AWS/Lambda \
  --statistic Sum \
  --period 300 \
  --threshold 1 \
  --comparison-operator GreaterThanOrEqualToThreshold \
  --dimensions Name=FunctionName,Value=prd-outcome-ops-ai-assist-ingest-docs
```

## Troubleshooting

### Lambda fails with "ParameterNotFound"

**Issue**: GitHub token or webhook secret not in SSM

**Fix**:
```bash
# For GitHub token
aws ssm put-parameter \
  --name /prd/outcome-ops-ai-assist/github/token \
  --value "YOUR_TOKEN" \
  --type SecureString \
  --overwrite

# For webhook secret
aws ssm put-parameter \
  --name /prd/outcome-ops-ai-assist/github/webhook-secret \
  --value "YOUR_WEBHOOK_SECRET" \
  --type SecureString \
  --overwrite
```

### Lambda fails with "InvalidAction"

**Issue**: IAM role doesn't have Bedrock permissions

**Fix**: Re-apply Terraform to update role:
```bash
terraform apply -var-file=prd.tfvars -refresh=true
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
  --table-name prd-outcome-ops-ai-assist-code-maps \
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
terraform apply -var-file=prd.tfvars
```

### Rollback Lambda Code

```bash
# Previous Lambda version is in deployment package history
# Fastest fix: revert handler.py in git and redeploy

git revert HEAD~1
terraform apply -var-file=prd.tfvars
```

## Related Documentation

- **Architecture**: See `docs/architecture.md` for system design
- **Lambda Functions**: See `docs/lambda-*.md` for specific function details
- **ADRs**: See `docs/adr/` for architectural decisions
- **Infrastructure Code**: See `terraform/` for all IaC definitions

<!-- Confluence sync -->


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
├── prd.tfvars       # Dev environment values
└── prd.tfvars       # Prd environment values
```

### Variable File Standards

**prd.tfvars:**
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
terraform plan -var-file=prd.tfvars -out=terraform.prd.out

# Step 3: Review the plan output
# Check what resources will be created, modified, or destroyed

# Step 4: Apply the plan (only after review)
terraform apply terraform.prd.out

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

- Dev environment: `terraform.prd.out`
- Prd environment: `terraform.prd.out`
- **Never commit plan files to git** (already in .gitignore)

### Terraform Commands Reference

```bash
cd terraform
terraform workspace list
terraform workspace select dev
terraform fmt -recursive
terraform validate
terraform plan -var-file=prd.tfvars -out=terraform.prd.out
terraform apply terraform.prd.out
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

<!-- Confluence sync -->


## Source: operations.md - ooai-license-server (relevance: 0.34)
# Operations Guide

## Table of Contents
- [Initial Deployment](#initial-deployment)
- [Lambda Layer Publishing](#lambda-layer-publishing)
- [Key Rotation](#key-rotation)
- [Adding Customers](#adding-customers)
- [SES Configuration for Usage Alerts](#ses-configuration-for-usage-alerts)

---

## Initial Deployment

First-time setup for a new environment. The first terraform apply will partially fail because the Lambda layer ZIP doesn't exist in S3 yet. This is expected - it creates enough infrastructure (S3 bucket, SSM Parameter containers) to proceed.

### 1. First Terraform Apply (partial)

```bash
cd terraform
terraform workspace select dev  # or prd
terraform init
terraform plan -var-file=dev.tfvars -out=terraform.dev.out
terraform apply terraform.dev.out
# This WILL FAIL on the Lambda layer - that's expected
```

### 2. Generate Key Pairs (one per environment)

Each environment uses its own RSA key pair for strict separation. **Never share keys between environments.**

```bash
# Generate dev key pair
openssl genrsa -out private_key_dev.pem 2048

# Generate prd key pair (separate key!)
openssl genrsa -out private_key_prd.pem 2048
```

### 3. Store Secrets in SSM Parameter Store

```bash
# Store dev private key
aws ssm put-parameter \
  --name "/dev/ooai-license-server/secrets/license-private-key" \
  --type "SecureString" \
  --value "$(cat private_key_dev.pem)" \
  --region us-west-2

# Store prd private key (different key!)
aws ssm put-parameter \
  --name "/prd/ooai-license-server/secrets/license-private-key" \
  --type "SecureString" \
  --value "$(cat private_key_prd.pem)" \
  --region us-west-2

# Clean up local private keys
rm private_key_dev.pem private_key_prd.pem

# Generate and set admin API key (dev)
aws ssm put-parameter \
  --name "/dev/ooai-license-server/secrets/admin-api-key" \
  --type "SecureString" \
  --value "$(openssl rand -hex 32)" \
  --region us-west-2

# Generate and set admin API key (prd)
aws ssm put-parameter \
  --name "/prd/ooai-license-server/secrets/admin-api-key" \
  --type "SecureString" \
  --value "$(openssl rand -hex 32)" \
  --region us-west-2

```

### 4. Build and Publish Layer

The build script automatically fetches the private key from SSM Parameter Store and extracts the public key. Each environment gets its own layer with the correct public key embedded.

```bash
./scripts/build-license-layer.sh dev --publish    # Builds: dev-outcomeops-license-validator-vX-X-X
./scripts/build-license-layer.sh prd --publish    # Builds: prd-outcomeops-license-validator-vX-X-X
```

**What the script does:**
1. Reads `license_layer_version` from `terraform/{env}.tfvars`
2. Fetches private key from `/{env}/ooai-license-server/secrets/license-private-key` in SSM
3. Extracts public key using `openssl rsa -pubout`
4. Builds ZIP with environment-prefixed name
5. Uploads to `s3://{env}-ooai-license-server-artifacts-{region}/layers/`

### 5. Second Terraform Apply (complete)

```bash
cd terraform
terraform workspace select dev  # or prd
terraform plan -var-file=dev.tfvars -out=terraform.dev.out
terraform apply terraform.dev.out
# This should now succeed
```

---

## Lambda Layer Publishing

When to publish a new layer version:
- Code changes in `lambda/license-layer/python/outcomeops_license/`
- Dependency updates in `requirements.txt`
- Public key rotation (requires environment-specific rebuild)

### Environment Separation

Each environment has its own layer with a distinct public key:
- **dev**: `dev-outcomeops-license-validator-vX-X-X` (uses dev private key from SSM)
- **prd**: `prd-outcomeops-license-validator-vX-X-X` (uses prd private key from SSM)

Licenses issued with a dev key will NOT validate against the prd layer, and vice versa.

### Publishing Process

```bash
# 1. Update version in tfvars (bump for EACH environment you're updating)
#    Edit terraform/dev.tfvars: license_layer_version = "1.1.0"
#    Edit terraform/prd.tfvars: license_layer_version = "1.1.0"

# 2. Build and publish for each environment
./scripts/build-license-layer.sh dev --publish    # Fetches dev key from SSM
./scripts/build-license-layer.sh prd --publish    # Fetches prd key from SSM

# 3. Deploy new layer version for each environment
cd terraform
terraform workspace select dev
terraform plan -var-file=dev.tfvars -out=terraform.dev.out
terraform apply terraform.dev.out

terraform workspace select prd
terraform plan -var-file=prd.tfvars -out=terraform.prd.out
terraform apply terraform.prd.out
```

### Version Strategy

Use semantic versioning:
- **Major** (2.0.0): Breaking changes to validation API
- **Minor** (1.1.0): New features, backward compatible
- **Patch** (1.0.1): Bug fixes

---

## Key Rotation

### Rotation Schedule

| Secret | Rotation Period | Notes |
|--------|-----------------|-------|
| RSA Private Key (dev) | 360 days | Track manually per environment |
| RSA Private Key (prd) | 360 days | Track manually per environment |
| Admin API Key | 360 days | Track manually |

### RSA Key Rotation Process

Key rotation requires coordination because customers have the public key embedded in their Lambda layer. **Rotate each environment independently.**

**Timeline:**
1. **Day 0**: Generate new key pair, publish layer with BOTH old and new public keys
2. **Day 1-30**: Notify customers to update their Lambda layer
3. **Day 31**: Remove old public key from layer, publish final version
4. **Day 31**: Update private key in SSM Parameter Store

**Step-by-step (example for prd):**

```bash
# 1. Generate new key pair for prd environment
openssl genrsa -out private_key_prd_new.pem 2048

# 2. Update validator.py to accept both keys (see code below)
#    Copy old public key to public_key_old.pem

# 3. Bump layer version
#    Edit terraform/prd.tfvars: license_layer_version = "1.2.0"

# 4. Build with old public key still in place
./scripts/build-license-layer.sh prd --publish

# 5. Deploy
cd terraform
terraform workspace select prd
terraform plan -var-file=prd.tfvars -out=terraform.prd.out
terraform apply terraform.prd.out

# 6. Notify customers to update their Lambda layer ARN

# 7. After customers updated, update private key in SSM Parameter Store
aws ssm put-parameter \
  --name "/prd/ooai-license-server/secrets/license-private-key" \
  --type "SecureString" \
  --value "$(cat private_key_prd_new.pem)" \
  --overwrite \
  --region us-west-2

# 8. Clean up and remove old key support from validator.py
rm private_key_prd_new.pem

# 9. Bump version again and publish final layer (script extracts new public key from SSM)
#    Edit terraform/prd.tfvars: license_layer_version = "1.3.0"
./scripts/build-license-layer.sh prd --publish
```

**Multi-key validator code (temporary during rotation):**

```python
# validator.py - during rotation period
PUBLIC_KEYS = [
    Path(__file__).parent / "public_key_old.pem",
    Path(__file__).parent / "public_key.pem",  # new key
]

def _validate_jwt(license_key: str) -> Dict[str, Any]:
    for key_path in PUBLIC_KEYS:
        try:
            public_key = key_path.read_text()
            return jwt.decode(license_key, public_key, algorithms=["RS256"], ...)
        except jwt.InvalidSignatureError:
            continue
    raise LicenseInvalidError("Invalid license signature")
```

### Admin API Key Rotation

Simpler process - only affects internal tools.

```bash
# Generate new key
NEW_KEY=$(openssl rand -hex 32)

# Update secret
aws ssm put-parameter \
  --name "/dev/ooai-license-server/secrets/admin-api-key" \
  --type "SecureString" \
  --value "$NEW_KEY" \
  --overwrite \
  --region us-west-2

# Update any tools/scripts that use the API key
echo "New admin API key: $NEW_KEY"
```

---

## Adding Customers

### 1. Add Customer Account ID

Edit `terraform/dev.tfvars`:

```hcl
customer_aws_account_ids = [
  "111111111111",  # Customer A
  "222222222222",  # Customer B - NEW
]
```

### 2. Deploy

```bash
cd terraform
terraform plan -var-file=dev.tfvars -out=terraform.dev.out
terraform apply terraform.dev.out
```

### 3. Provide Customer with Layer ARN

Get the layer ARN from Terraform output:

```bash
terraform output license_layer_arn
```

Share with customer along with usage instructions.

---

## SES Configuration for Usage Alerts

Usage threshold alerts (ADR-019) are sent via Amazon SES. Before alerts will work, you must:

### 1. Verify Sender Email

```bash
# Request verification for the sender email
aws ses verify-email-identity --email-address alerts@outcomeops.ai --region us-west-2

# Check verification status
aws ses get-identity-verification-attributes \
  --identities alerts@outcomeops.ai \
  --region us-west-2
```

### 2. Configure Variables

Edit `terraform/dev.tfvars` (or `prd.tfvars`):

```hcl
# Internal support email for all alerts
support_email = "support@outcomeops.ai"

# Verified SES sender email
ses_sender_email = "alerts@outcomeops.ai"
```

### 3. Exit SES Sandbox (Production)

By default, SES is in sandbox mode and can only send to verified emails. For production:

1. Go to AWS Console > SES > Account dashboard
2. Click "Request production access"
3. Fill in use case details
4. Wait for approval (usually 24 hours)

### 4. Alert Types

| Threshold | Recipients | Content |
|-----------|------------|---------|
| 80% | Internal only | "Approaching limit" notice |
| 100% | Internal + Customer | "Limit reached" with upgrade info |

Customer contacts resolved in order: billing_contact > technical_contact > company email

---

## Operational Checklist

### Daily
- [ ] Check CloudWatch for license validation errors

### Monthly
- [ ] Review expiring licenses (30-day threshold)
- [ ] Check usage metrics

### Annually (360 days)
- [ ] Rotate RSA private key
- [ ] Rotate admin API key
- [ ] Review and update dependencies



### Testing Standards

None

### Code Style Standards

# Query: code generation verbosity standards

## Source: ADR: ADR-012-code-generation-verbosity (relevance: 0.48)
# ADR-012: Code Generation Verbosity Standards

## Status: Accepted

## Context

AI code generation often produces overly verbose output with excessive comments, examples, and explanations. This causes several problems:

1. **Truncation at 32K token limit**: Simple steps like "Add IAM permissions to terraform/lambda.tf" (expected ~200 lines) can generate 32K tokens of verbose output, hitting the max_tokens limit and failing.

2. **Wasted cost**: Each truncation wastes $0.50+ per attempt (input + output tokens) with no usable code generated.

3. **Double-processing overhead**: Generated code doesn't need inline tutorials when it will be reviewed by developers and tested by CI.

**Real Example:**
- Step 6: "Add IAM permissions for Lambda"
- Expected: ~200 lines of HCL policy definitions
- Actual: 32,000 tokens (hit limit), step failed
- Wasted: $0.51 on unusable output

The root cause is that AI models default to "educational mode" - explaining everything as if teaching a junior developer. This is counterproductive for code generation where:
- Code will be reviewed by humans anyway
- Tests validate correctness
- ADRs document the "why"
- Generated code just needs to work

## Decision

### 1. Concise Code by Default

Generated code should be **production-ready and concise**, not tutorial-style. The code itself should be clear enough that excessive comments aren't needed.

### 2. What to Include

**Docstrings:**
- Function/class docstrings: 1-2 lines describing purpose
- Module docstrings: Brief description of what the file contains

**Comments (only when needed):**
- Non-obvious logic explanations ("Retry 3x because DynamoDB is eventually consistent")
- ADR references ("Follows ADR-004 for error handling pattern")
- Warnings about gotchas ("Note: This must be called before X")

**Code standards:**
- Type hints (for Python)
- Consistent naming
- Clear function signatures

### 3. What to Exclude

**Never generate:**
- Line-by-line explanations of obvious code
- Multiple example scenarios in comments
- "You could also..." alternative approaches
- Tutorial-style explanations
- Redundant docstrings that just restate the function name
- Commented-out code "for reference"
- Explanations of standard library functions
- Block comments explaining what each section does

### 4. Examples

**Python - Too Verbose (Will Truncate):**

```python
def list_recent_docs(limit: int = 10) -> dict:
    """
    List recently ingested knowledge base documents.

    This function queries the DynamoDB table to retrieve recently ingested
    documents. It applies a filter to only return items that have embeddings,
    sorts them by timestamp in descending order (newest first), and limits
    the results based on the provided parameter.

    Args:
        limit (int): Maximum number of documents to return. Must be between
                    1 and 100. Defaults to 10 if not provided.

                    Examples:
                    - limit=5: Returns up to 5 documents
                    - limit=100: Returns up to 100 documents
                    - limit not provided: Returns up to 10 documents

    Returns:
        dict: A dictionary containing:
            - documents (list): List of document metadata dictionaries
            - total_returned (int): Number of documents returned
            - limit (int): The limit that was applied

            Each document dictionary contains:
            - pk (str): Partition key with repo identifier
            - sk (str): Sort key with document type and ID
            ...

    Raises:
        ValidationError: If limit is not between 1 and 100
        ClientError: If DynamoDB scan fails

    Example:
        >>> result = list_recent_docs(limit=5)
        >>> print(result['total_returned'])
        5

    Note:
        This function follows ADR-004 for error handling patterns...
    """
    # Load the DynamoDB table name from environment variable
    # This allows the function to work across different environments
    # (dev, staging, prod) without code changes
    table_name = os.environ.get('CODE_MAPS_TABLE')

    # Create a DynamoDB resource to interact with the table
    # We use resource instead of client for cleaner API
    dynamodb = boto3.resource('dynamodb')

    # ... 50 more lines of explanatory comments ...
```

**Python - Concise (Correct):**

```python
def list_recent_docs(limit: int = 10) -> dict:
    """List recently ingested KB documents, sorted by timestamp descending."""
    table_name = os.environ['CODE_MAPS_TABLE']

    response = dynamodb.scan(
        TableName=table_name,
        FilterExpression='attribute_exists(embedding)',
        Limit=limit
    )

    items = sorted(response['Items'], key=lambda x: x['timestamp'], reverse=True)

    return {
        'documents': [extract_metadata(item) for item in items],
        'total_returned': len(items),
        'limit': limit
    }
```

**Terraform - Too Verbose (Will Truncate):**

```hcl
# IAM Policy for list-recent-docs Lambda
#
# This policy grants the Lambda function the minimum permissions needed
# to perform its intended operations. Following the principle of least
# privilege, we only grant:
#
# 1. DynamoDB Scan - Required to query all documents in the table
#    - We use Scan instead of Query because documents span multiple
#      partition keys (different repos)
#    - The Limit parameter prevents full table scans
#    - Resource ARN is scoped to the specific code-maps table
#
# 2. DynamoDB Query - Required as fallback for single-repo queries
#    - Allows more efficient lookups when filtering by repo
#    - Resource ARN is scoped to the specific code-maps table
#
# 3. SSM GetParameter - Required to load table name from SSM
#    - Allows the Lambda to discover its DynamoDB table dynamically
#    - Resource ARN is scoped to the specific app's parameters
#    - Read-only access (GetParameter only, not PutParameter)
#
# Alternative approaches considered:
# - You could hardcode the table name in env vars instead of SSM
# - You could use Query with GSI instead of Scan for better performance
# - You could grant dynamodb:* but that violates least privilege

module "list_recent_docs_lambda" {
  # Module source and version
  # Using the official Terraform AWS Lambda module from HashiCorp registry
  source  = "terraform-aws-modules/lambda/aws"
  version = "~> 6.0"  # Use version 6.x to ensure compatibility

  # ... continues for hundreds more lines of comments ...
```

**Terraform - Concise (Correct):**

```hcl
module "list_recent_docs_lambda" {
  source  = "terraform-aws-modules/lambda/aws"
  version = "~> 6.0"

  function_name = "${local.prefix}-list-recent-docs"
  handler       = "handler.handler"
  runtime       = "python3.12"

  attach_policy_statements = true
  policy_statements = {
    dynamodb_scan = {
      effect    = "Allow"
      actions   = ["dynamodb:Scan", "dynamodb:Query"]
      resources = [aws_dynamodb_table.code_maps.arn]
    }
    ssm_params = {
      effect    = "Allow"
      actions   = ["ssm:GetParameter"]
      resources = ["arn:aws:ssm:${data.aws_region.current.name}:${data.aws_caller_identity.current.account_id}:parameter/${var.environment}/${var.app_name}/*"]
    }
  }
}
```

### 5. Type-Specific Guidelines

**Lambda Handlers:**
- Brief module docstring
- 1-line function docstrings
- Type hints on all functions
- No inline tutorials about AWS APIs

**Terraform/HCL:**
- No policy explanation comments
- Self-documenting resource names
- ADR references only where relevant (e.g., "# See ADR-004")

**Tests:**
- 1-line docstrings per test
- Test names should be descriptive enough
- No "this test verifies that..." comments
- Fixtures should be minimal

**TypeScript/JavaScript:**
- JSDoc only for public APIs
- No "// This function does X" comments
- Type definitions serve as documentation

## Consequences

### Positive

- **No truncation**: Simple steps won't hit 32K token limit
- **Lower cost**: ~50% fewer output tokens per step
- **Faster generation**: Less time generating verbose comments
- **Better signal-to-noise**: Generated code is easier to review
- **Production-ready**: Code looks like what experienced devs write

### Tradeoffs

- **Less hand-holding**: Junior devs reading generated code may need to look at ADRs
- **Requires good ADRs**: Documentation shifts from inline to ADRs (where it belongs)

## Implementation

### Starting today

1. Update code generation system prompt with verbosity guidelines
2. Add specific guidance for Terraform/IAM steps
3. Test on previously-truncated step types

### Prompt additions

Add to `CODE_GENERATION_SYSTEM_PROMPT`:

```
VERBOSITY: Generate CONCISE, production-ready code.
- Docstrings: 1-2 lines max, describe purpose only
- Comments: Only for non-obvious logic (not "what", but "why")
- No inline tutorials, examples, or alternative approaches
- No line-by-line explanations
- Code should be self-documenting through clear naming
```

Add type-specific guidance:

```
For Terraform/IaC:
- No policy explanation comments
- Resource names should be self-documenting
- Use locals for repeated values

For Tests:
- 1 line docstrings max
- Minimal fixtures
- No "this test verifies..." comments
```

## Related ADRs

- ADR-001: Creating ADRs (documentation belongs in ADRs, not inline)
- ADR-005: Testing Standards (test patterns)

## References

- [Output token limit issue](https://docs.aws.amazon.com/bedrock/latest/userguide/model-parameters-anthropic-claude-messages.html)
- [Clean Code principles](https://www.oreilly.com/library/view/clean-code-a/9780136083238/)

Version History:
- v1.0 (2025-12-02): Initial decision addressing truncation issues

<!-- Confluence sync -->


---STANDARD---
# Query: concise code style guidelines

## Source: ADR: ADR-012-code-generation-verbosity (relevance: 0.40)
# ADR-012: Code Generation Verbosity Standards

## Status: Accepted

## Context

AI code generation often produces overly verbose output with excessive comments, examples, and explanations. This causes several problems:

1. **Truncation at 32K token limit**: Simple steps like "Add IAM permissions to terraform/lambda.tf" (expected ~200 lines) can generate 32K tokens of verbose output, hitting the max_tokens limit and failing.

2. **Wasted cost**: Each truncation wastes $0.50+ per attempt (input + output tokens) with no usable code generated.

3. **Double-processing overhead**: Generated code doesn't need inline tutorials when it will be reviewed by developers and tested by CI.

**Real Example:**
- Step 6: "Add IAM permissions for Lambda"
- Expected: ~200 lines of HCL policy definitions
- Actual: 32,000 tokens (hit limit), step failed
- Wasted: $0.51 on unusable output

The root cause is that AI models default to "educational mode" - explaining everything as if teaching a junior developer. This is counterproductive for code generation where:
- Code will be reviewed by humans anyway
- Tests validate correctness
- ADRs document the "why"
- Generated code just needs to work

## Decision

### 1. Concise Code by Default

Generated code should be **production-ready and concise**, not tutorial-style. The code itself should be clear enough that excessive comments aren't needed.

### 2. What to Include

**Docstrings:**
- Function/class docstrings: 1-2 lines describing purpose
- Module docstrings: Brief description of what the file contains

**Comments (only when needed):**
- Non-obvious logic explanations ("Retry 3x because DynamoDB is eventually consistent")
- ADR references ("Follows ADR-004 for error handling pattern")
- Warnings about gotchas ("Note: This must be called before X")

**Code standards:**
- Type hints (for Python)
- Consistent naming
- Clear function signatures

### 3. What to Exclude

**Never generate:**
- Line-by-line explanations of obvious code
- Multiple example scenarios in comments
- "You could also..." alternative approaches
- Tutorial-style explanations
- Redundant docstrings that just restate the function name
- Commented-out code "for reference"
- Explanations of standard library functions
- Block comments explaining what each section does

### 4. Examples

**Python - Too Verbose (Will Truncate):**

```python
def list_recent_docs(limit: int = 10) -> dict:
    """
    List recently ingested knowledge base documents.

    This function queries the DynamoDB table to retrieve recently ingested
    documents. It applies a filter to only return items that have embeddings,
    sorts them by timestamp in descending order (newest first), and limits
    the results based on the provided parameter.

    Args:
        limit (int): Maximum number of documents to return. Must be between
                    1 and 100. Defaults to 10 if not provided.

                    Examples:
                    - limit=5: Returns up to 5 documents
                    - limit=100: Returns up to 100 documents
                    - limit not provided: Returns up to 10 documents

    Returns:
        dict: A dictionary containing:
            - documents (list): List of document metadata dictionaries
            - total_returned (int): Number of documents returned
            - limit (int): The limit that was applied

            Each document dictionary contains:
            - pk (str): Partition key with repo identifier
            - sk (str): Sort key with document type and ID
            ...

    Raises:
        ValidationError: If limit is not between 1 and 100
        ClientError: If DynamoDB scan fails

    Example:
        >>> result = list_recent_docs(limit=5)
        >>> print(result['total_returned'])
        5

    Note:
        This function follows ADR-004 for error handling patterns...
    """
    # Load the DynamoDB table name from environment variable
    # This allows the function to work across different environments
    # (dev, staging, prod) without code changes
    table_name = os.environ.get('CODE_MAPS_TABLE')

    # Create a DynamoDB resource to interact with the table
    # We use resource instead of client for cleaner API
    dynamodb = boto3.resource('dynamodb')

    # ... 50 more lines of explanatory comments ...
```

**Python - Concise (Correct):**

```python
def list_recent_docs(limit: int = 10) -> dict:
    """List recently ingested KB documents, sorted by timestamp descending."""
    table_name = os.environ['CODE_MAPS_TABLE']

    response = dynamodb.scan(
        TableName=table_name,
        FilterExpression='attribute_exists(embedding)',
        Limit=limit
    )

    items = sorted(response['Items'], key=lambda x: x['timestamp'], reverse=True)

    return {
        'documents': [extract_metadata(item) for item in items],
        'total_returned': len(items),
        'limit': limit
    }
```

**Terraform - Too Verbose (Will Truncate):**

```hcl
# IAM Policy for list-recent-docs Lambda
#
# This policy grants the Lambda function the minimum permissions needed
# to perform its intended operations. Following the principle of least
# privilege, we only grant:
#
# 1. DynamoDB Scan - Required to query all documents in the table
#    - We use Scan instead of Query because documents span multiple
#      partition keys (different repos)
#    - The Limit parameter prevents full table scans
#    - Resource ARN is scoped to the specific code-maps table
#
# 2. DynamoDB Query - Required as fallback for single-repo queries
#    - Allows more efficient lookups when filtering by repo
#    - Resource ARN is scoped to the specific code-maps table
#
# 3. SSM GetParameter - Required to load table name from SSM
#    - Allows the Lambda to discover its DynamoDB table dynamically
#    - Resource ARN is scoped to the specific app's parameters
#    - Read-only access (GetParameter only, not PutParameter)
#
# Alternative approaches considered:
# - You could hardcode the table name in env vars instead of SSM
# - You could use Query with GSI instead of Scan for better performance
# - You could grant dynamodb:* but that violates least privilege

module "list_recent_docs_lambda" {
  # Module source and version
  # Using the official Terraform AWS Lambda module from HashiCorp registry
  source  = "terraform-aws-modules/lambda/aws"
  version = "~> 6.0"  # Use version 6.x to ensure compatibility

  # ... continues for hundreds more lines of comments ...
```

**Terraform - Concise (Correct):**

```hcl
module "list_recent_docs_lambda" {
  source  = "terraform-aws-modules/lambda/aws"
  version = "~> 6.0"

  function_name = "${local.prefix}-list-recent-docs"
  handler       = "handler.handler"
  runtime       = "python3.12"

  attach_policy_statements = true
  policy_statements = {
    dynamodb_scan = {
      effect    = "Allow"
      actions   = ["dynamodb:Scan", "dynamodb:Query"]
      resources = [aws_dynamodb_table.code_maps.arn]
    }
    ssm_params = {
      effect    = "Allow"
      actions   = ["ssm:GetParameter"]
      resources = ["arn:aws:ssm:${data.aws_region.current.name}:${data.aws_caller_identity.current.account_id}:parameter/${var.environment}/${var.app_name}/*"]
    }
  }
}
```

### 5. Type-Specific Guidelines

**Lambda Handlers:**
- Brief module docstring
- 1-line function docstrings
- Type hints on all functions
- No inline tutorials about AWS APIs

**Terraform/HCL:**
- No policy explanation comments
- Self-documenting resource names
- ADR references only where relevant (e.g., "# See ADR-004")

**Tests:**
- 1-line docstrings per test
- Test names should be descriptive enough
- No "this test verifies that..." comments
- Fixtures should be minimal

**TypeScript/JavaScript:**
- JSDoc only for public APIs
- No "// This function does X" comments
- Type definitions serve as documentation

## Consequences

### Positive

- **No truncation**: Simple steps won't hit 32K token limit
- **Lower cost**: ~50% fewer output tokens per step
- **Faster generation**: Less time generating verbose comments
- **Better signal-to-noise**: Generated code is easier to review
- **Production-ready**: Code looks like what experienced devs write

### Tradeoffs

- **Less hand-holding**: Junior devs reading generated code may need to look at ADRs
- **Requires good ADRs**: Documentation shifts from inline to ADRs (where it belongs)

## Implementation

### Starting today

1. Update code generation system prompt with verbosity guidelines
2. Add specific guidance for Terraform/IAM steps
3. Test on previously-truncated step types

### Prompt additions

Add to `CODE_GENERATION_SYSTEM_PROMPT`:

```
VERBOSITY: Generate CONCISE, production-ready code.
- Docstrings: 1-2 lines max, describe purpose only
- Comments: Only for non-obvious logic (not "what", but "why")
- No inline tutorials, examples, or alternative approaches
- No line-by-line explanations
- Code should be self-documenting through clear naming
```

Add type-specific guidance:

```
For Terraform/IaC:
- No policy explanation comments
- Resource names should be self-documenting
- Use locals for repeated values

For Tests:
- 1 line docstrings max
- Minimal fixtures
- No "this test verifies..." comments
```

## Related ADRs

- ADR-001: Creating ADRs (documentation belongs in ADRs, not inline)
- ADR-005: Testing Standards (test patterns)

## References

- [Output token limit issue](https://docs.aws.amazon.com/bedrock/latest/userguide/model-parameters-anthropic-claude-messages.html)
- [Clean Code principles](https://www.oreilly.com/library/view/clean-code-a/9780136083238/)

Version History:
- v1.0 (2025-12-02): Initial decision addressing truncation issues

<!-- Confluence sync -->


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

<!-- Confluence sync -->



<!-- STANDARDS_END -->

## Implementation Steps

### Step 1: Create PetStatistics DTO ⏳

**Status:** pending
**Description:** Create a DTO to represent the pet statistics response with total count, pets by type map, and average visits per pet

**Files:**
- `src/main/java/com/petclinic/dto/PetStatisticsDTO.java`

---

### Step 2: Create PetStatisticsService ⏳

**Status:** pending
**Description:** Create service class with business logic to calculate total pets, group by type, and compute average visits per pet

**Files:**
- `src/main/java/com/petclinic/service/PetStatisticsService.java`

**KB Queries:**
- Spring Data JPA aggregation queries
- JPA repository custom query methods

---

### Step 3: Create PetStatisticsController ⏳

**Status:** pending
**Description:** Create REST controller with GET /api/stats/pets endpoint that delegates to PetStatisticsService and returns 200 with statistics

**Files:**
- `src/main/java/com/petclinic/controller/PetStatisticsController.java`

---

### Step 4: Create unit tests for PetStatisticsService success cases ⏳

**Status:** pending
**Description:** Create unit tests for service layer covering happy path scenarios: calculating total pets, grouping by type, and computing average visits

**Files:**
- `src/test/java/com/petclinic/service/PetStatisticsServiceTest.java`

---

### Step 5: Create unit tests for PetStatisticsService error handling ⏳

**Status:** pending
**Description:** Create unit tests for service layer error scenarios: empty database, database connection failures, and null handling

**Files:**
- `src/test/java/com/petclinic/service/PetStatisticsServiceTest.java`

---

### Step 6: Create unit tests for PetStatisticsController success cases ⏳

**Status:** pending
**Description:** Create controller unit tests using @WebMvcTest for successful GET requests returning 200 with correct statistics JSON

**Files:**
- `src/test/java/com/petclinic/controller/PetStatisticsControllerTest.java`

---

### Step 7: Create unit tests for PetStatisticsController error handling ⏳

**Status:** pending
**Description:** Create controller unit tests for error scenarios: service exceptions returning 500 status code

**Files:**
- `src/test/java/com/petclinic/controller/PetStatisticsControllerTest.java`

---

### Step 8: Create integration tests for pet statistics endpoint ⏳

**Status:** pending
**Description:** Create integration tests using @SpringBootTest to verify end-to-end functionality with real database interactions

**Files:**
- `src/test/java/com/petclinic/integration/PetStatisticsIntegrationTest.java`

**KB Queries:**
- Spring Boot test database configuration with H2

---
