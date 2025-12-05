#!/bin/bash

# ============================================
# Deployment Verification Script
# ============================================
# Test your deployed backend to ensure everything works

if [ -z "$1" ]; then
    echo "Usage: ./verify-deployment.sh <your-app-url>"
    echo "Example: ./verify-deployment.sh https://booking-api-dev.onrender.com"
    exit 1
fi

BASE_URL=$1

echo "🧪 Testing Backend Deployment"
echo "================================"
echo "URL: $BASE_URL"
echo ""

# Test 1: Health Check
echo "1️⃣  Testing health endpoint..."
HEALTH_RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/actuator/health")
if [ "$HEALTH_RESPONSE" == "200" ]; then
    echo "   ✅ Health check passed"
else
    echo "   ❌ Health check failed (HTTP $HEALTH_RESPONSE)"
fi
echo ""

# Test 2: API Root
echo "2️⃣  Testing API root..."
API_RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/api")
if [ "$API_RESPONSE" == "200" ] || [ "$API_RESPONSE" == "404" ]; then
    echo "   ✅ API endpoint responding"
else
    echo "   ❌ API not responding (HTTP $API_RESPONSE)"
fi
echo ""

# Test 3: Check Redis Connection
echo "3️⃣  Checking for Redis errors in logs..."
echo "   (Check your deployment platform dashboard for Redis connection logs)"
echo ""

# Test 4: Check Database Connection
echo "4️⃣  Checking database connection..."
echo "   (If health check passed, database is connected)"
echo ""

# Test 5: Memory Usage
echo "5️⃣  Memory usage check..."
echo "   Expected: 150-180MB"
echo "   Check metrics in your deployment platform dashboard"
echo ""

echo "================================"
echo "📊 Summary"
echo "================================"
echo ""
echo "If all checks passed:"
echo "  ✅ Backend is deployed successfully!"
echo ""
echo "Next steps:"
echo "  1. Test a public endpoint (e.g., get business by slug)"
echo "  2. Monitor logs for any errors"
echo "  3. Check Redis cache hit rate in Upstash dashboard"
echo "  4. Monitor memory usage (should be <200MB)"
echo ""
echo "Useful commands:"
echo "  curl $BASE_URL/actuator/health"
echo "  curl $BASE_URL/api/public/business/YOUR-SLUG"
