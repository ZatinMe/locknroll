#!/bin/bash

# Redis Learning Project - Test Scenarios Script
# This script runs various test scenarios to demonstrate Redis functionality

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
NC='\033[0m' # No Color

# Function to print colored output
print_header() {
    echo -e "\n${PURPLE}========================================${NC}"
    echo -e "${PURPLE}$1${NC}"
    echo -e "${PURPLE}========================================${NC}\n"
}

print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to wait for application to be ready
wait_for_app() {
    local max_attempts=30
    local attempt=1
    
    print_status "Waiting for application to be ready..."
    
    while [ $attempt -le $max_attempts ]; do
        if curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
            print_success "Application is ready!"
            return 0
        fi
        
        echo -n "."
        sleep 2
        attempt=$((attempt + 1))
    done
    
    print_error "Application failed to start within timeout"
    return 1
}

# Function to run a test scenario
run_scenario() {
    local scenario_name=$1
    local scenario_function=$2
    
    print_header "Running Scenario: $scenario_name"
    
    if $scenario_function; then
        print_success "Scenario '$scenario_name' completed successfully"
    else
        print_error "Scenario '$scenario_name' failed"
        return 1
    fi
}

# Scenario 1: Basic Cache Test
scenario_basic_cache() {
    print_status "Testing basic cache functionality..."
    
    # First request (cache miss)
    print_status "Making first request (should be cache miss)..."
    time curl -s http://localhost:8080/api/fruits/1 > /dev/null
    
    # Second request (cache hit)
    print_status "Making second request (should be cache hit)..."
    time curl -s http://localhost:8080/api/fruits/1 > /dev/null
    
    # Third request (cache hit)
    print_status "Making third request (should be cache hit)..."
    time curl -s http://localhost:8080/api/fruits/1 > /dev/null
    
    return 0
}

# Scenario 2: Cache Invalidation Test
scenario_cache_invalidation() {
    print_status "Testing cache invalidation..."
    
    # Read fruit to populate cache
    print_status "Reading fruit to populate cache..."
    curl -s http://localhost:8080/api/fruits/1 > /dev/null
    
    # Update fruit (should invalidate cache)
    print_status "Updating fruit (should invalidate cache)..."
    curl -s -X PUT http://localhost:8080/api/fruits/1 \
        -H "Content-Type: application/json" \
        -d '{"name":"Updated Apple","price":3.00,"quantity":90,"description":"Updated description","category":"Fruit"}' > /dev/null
    
    # Read again (should fetch fresh data)
    print_status "Reading fruit again (should fetch fresh data)..."
    curl -s http://localhost:8080/api/fruits/1 > /dev/null
    
    return 0
}

# Scenario 3: Concurrent Purchase Test
scenario_concurrent_purchases() {
    print_status "Testing concurrent purchases..."
    
    # Start multiple concurrent purchases
    print_status "Starting 10 concurrent purchase requests..."
    
    # Yes, this for loop creates 10 background processes (threads) by using & at the end of each iteration
    # Each process runs a curl command concurrently
    for i in {1..10}; do
        (
            echo "Thread $i starting purchase..."
            curl -s -X POST "http://localhost:8080/api/fruits/1/purchase?quantity=1" > /dev/null
            echo "Thread $i completed purchase"
        ) &
    done
    
    # Wait for all requests to complete
    wait
    
    print_status "All concurrent purchases completed"
    
    # Check final state
    print_status "Checking final fruit state..."
    curl -s http://localhost:8080/api/fruits/1 | jq '.quantity' 2>/dev/null || echo "Final quantity check completed"
    
    return 0
}

# Scenario 4: Mixed Operations Test
scenario_mixed_operations() {
    print_status "Testing mixed operations (purchases and restocks)..."
    
    # Start mixed operations
    print_status "Starting mixed operations..."
    
    # 5 purchase operations
    for i in {1..5}; do
        (
            echo "Purchase thread $i starting..."
            curl -s -X POST "http://localhost:8080/api/fruits/1/purchase?quantity=2" > /dev/null
            echo "Purchase thread $i completed"
        ) &
    done
    
    # 3 restock operations
    for i in {1..3}; do
        (
            echo "Restock thread $i starting..."
            curl -s -X POST "http://localhost:8080/api/fruits/1/restock?quantity=5" > /dev/null
            echo "Restock thread $i completed"
        ) &
    done
    
    # Wait for all operations to complete
    wait
    
    print_status "All mixed operations completed"
    
    return 0
}

# Scenario 5: Stress Test
scenario_stress_test() {
    print_status "Running stress test..."
    
    # Create a new fruit for stress testing
    print_status "Creating a new fruit for stress testing..."
    curl -s -X POST http://localhost:8080/api/fruits \
        -H "Content-Type: application/json" \
        -d '{"name":"Stress Test Apple","price":2.00,"quantity":1000,"description":"Apple for stress testing","category":"Fruit"}' > /dev/null
    
    # Get the ID of the created fruit (assuming it's the last one)
    print_status "Starting stress test with 50 concurrent operations..."
    
    # Start 50 concurrent operations
    for i in {1..50}; do
        (
            echo "Stress thread $i starting..."
            curl -s -X POST "http://localhost:8080/api/fruits/1/purchase?quantity=1" > /dev/null
            echo "Stress thread $i completed"
        ) &
    done
    
    # Wait for all operations to complete
    wait
    
    print_status "Stress test completed"
    
    return 0
}

# Scenario 6: API Endpoints Test
scenario_api_endpoints() {
    print_status "Testing all API endpoints..."
    
    # Get all fruits
    print_status "Testing GET /api/fruits..."
    curl -s http://localhost:8080/api/fruits > /dev/null
    
    # Get fruit by ID
    print_status "Testing GET /api/fruits/1..."
    curl -s http://localhost:8080/api/fruits/1 > /dev/null
    
    # Get fruit by name
    print_status "Testing GET /api/fruits/name/Apple..."
    curl -s http://localhost:8080/api/fruits/name/Apple > /dev/null
    
    # Get fruits by category
    print_status "Testing GET /api/fruits/category/Fruit..."
    curl -s http://localhost:8080/api/fruits/category/Fruit > /dev/null
    
    # Get low stock fruits
    print_status "Testing GET /api/fruits/low-stock..."
    curl -s http://localhost:8080/api/fruits/low-stock > /dev/null
    
    return 0
}

# Scenario 7: Health Check Test
scenario_health_check() {
    print_status "Testing health endpoints..."
    
    # Check application health
    print_status "Checking application health..."
    curl -s http://localhost:8080/actuator/health | jq '.' 2>/dev/null || echo "Health check completed"
    
    return 0
}

# Main function
main() {
    print_header "Redis Learning Project - Test Scenarios"
    
    # Check if application is running
    if ! wait_for_app; then
        print_error "Please start the application first with: ./run.sh run"
        exit 1
    fi
    
    # Run all scenarios
    run_scenario "Basic Cache Test" scenario_basic_cache
    run_scenario "Cache Invalidation Test" scenario_cache_invalidation
    run_scenario "Concurrent Purchase Test" scenario_concurrent_purchases
    run_scenario "Mixed Operations Test" scenario_mixed_operations
    run_scenario "API Endpoints Test" scenario_api_endpoints
    run_scenario "Health Check Test" scenario_health_check
    
    # Optional stress test (can be enabled/disabled)
    if [ "${1:-}" = "--stress" ]; then
        run_scenario "Stress Test" scenario_stress_test
    fi
    
    print_header "All Test Scenarios Completed!"
    print_success "Check the application logs for detailed information about Redis operations"
    print_status "You can also check the databases:"
    echo "  - PostgreSQL: psql -U postgres -d locknroll_db"
    echo "  - MongoDB: mongosh locknroll_mongo"
    echo "  - Redis: redis-cli"
}

# Check if jq is available for JSON formatting
if ! command -v jq >/dev/null 2>&1; then
    print_warning "jq is not installed. Some JSON output will not be formatted."
    print_status "Install jq with: brew install jq (macOS) or apt install jq (Ubuntu)"
fi

# Run main function
main "$@"
