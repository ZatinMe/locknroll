#!/bin/bash

# Redis Learning Project - Run Script
# This script helps you run the application with proper setup

set -e

echo "🚀 Redis Learning Project - LockN'Roll"
echo "======================================"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
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

# Function to check if a command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Function to check if a service is running
check_service() {
    local service=$1
    local port=$2
    
    if command_exists nc; then
        if nc -z localhost $port 2>/dev/null; then
            return 0
        else
            return 1
        fi
    else
        print_warning "netcat not found, skipping port check for $service"
        return 0
    fi
}

# Check prerequisites
print_status "Checking prerequisites..."

if ! command_exists java; then
    print_error "Java is not installed. Please install Java 17 or higher."
    exit 1
fi

if ! command_exists mvn; then
    print_error "Maven is not installed. Please install Maven 3.6 or higher."
    exit 1
fi

print_success "Prerequisites check passed"

# Check services
print_status "Checking required services..."

# Check PostgreSQL
if check_service "PostgreSQL" 5432; then
    print_success "PostgreSQL is running on port 5432"
else
    print_warning "PostgreSQL is not running on port 5432"
    print_status "You can start PostgreSQL with: brew services start postgresql@13"
fi

# Check MongoDB
if check_service "MongoDB" 27017; then
    print_success "MongoDB is running on port 27017"
else
    print_warning "MongoDB is not running on port 27017"
    print_status "You can start MongoDB with: brew services start mongodb/brew/mongodb-community"
fi

# Check Redis
if check_service "Redis" 6379; then
    print_success "Redis is running on port 6379"
else
    print_warning "Redis is not running on port 6379"
    print_status "You can start Redis with: brew services start redis"
fi

# Parse command line arguments
COMMAND=${1:-"run"}

case $COMMAND in
    "run")
        print_status "Building and running the application..."
        mvn clean compile
        mvn spring-boot:run
        ;;
    "test")
        print_status "Running tests..."
        mvn test
        ;;
    "build")
        print_status "Building the application..."
        mvn clean compile
        ;;
    "docker")
        print_status "Starting services with Docker Compose..."
        if command_exists docker-compose; then
            docker-compose up -d
            print_success "Services started with Docker Compose"
            print_status "Access points:"
            echo "  - Application: http://localhost:8080"
            echo "  - Redis Commander: http://localhost:8081"
            echo "  - MongoDB Express: http://localhost:8082"
            echo "  - pgAdmin: http://localhost:8083"
        else
            print_error "Docker Compose is not installed"
            exit 1
        fi
        ;;
    "stop-docker")
        print_status "Stopping Docker services..."
        if command_exists docker-compose; then
            docker-compose down
            print_success "Docker services stopped"
        else
            print_error "Docker Compose is not installed"
            exit 1
        fi
        ;;
    "clean")
        print_status "Cleaning project..."
        mvn clean
        print_success "Project cleaned"
        ;;
    "help")
        echo "Usage: $0 [COMMAND]"
        echo ""
        echo "Commands:"
        echo "  run         Build and run the application (default)"
        echo "  test        Run all tests"
        echo "  build       Build the application only"
        echo "  docker      Start all services with Docker Compose"
        echo "  stop-docker Stop Docker services"
        echo "  clean       Clean the project"
        echo "  help        Show this help message"
        echo ""
        echo "Examples:"
        echo "  $0 run      # Build and run the application"
        echo "  $0 test     # Run tests"
        echo "  $0 docker   # Start services with Docker"
        ;;
    *)
        print_error "Unknown command: $COMMAND"
        echo "Use '$0 help' to see available commands"
        exit 1
        ;;
esac
