#!/bin/bash

# ============================================
# Connection String Parser
# ============================================
# Helper script to parse your Neon/Upstash connection strings
# into individual environment variables

echo "🔧 Connection String Parser"
echo "================================"
echo ""

# ============================================
# Parse Redis URL
# ============================================
echo "📦 REDIS CONNECTION"
echo "-------------------"
REDIS_URL="redis://default:AZLEAAIncDIwMDcwMDE1YWE5YmQ0NDdiOTNmMWIxOTA0N2QzNGJjN3AyMzc1NzI@composed-catfish-37572.upstash.io:6379"

# Extract host
REDIS_HOST=$(echo $REDIS_URL | sed -n 's/.*@\(.*\):[0-9]*/\1/p')
# Extract port
REDIS_PORT=$(echo $REDIS_URL | sed -n 's/.*:\([0-9]*\)$/\1/p')
# Extract password (between : and @)
REDIS_PASSWORD=$(echo $REDIS_URL | sed -n 's/.*:\(.*\)@.*/\1/p' | sed 's/default://')

echo "REDIS_HOST=$REDIS_HOST"
echo "REDIS_PORT=$REDIS_PORT"
echo "REDIS_PASSWORD=$REDIS_PASSWORD"
echo "REDIS_SSL=true"
echo ""

# ============================================
# Parse PostgreSQL JDBC URL
# ============================================
echo "🗄️  DATABASE CONNECTION"
echo "-------------------"
JDBC_URL="jdbc:postgresql://ep-weathered-hill-agqo5f2k-pooler.c-2.eu-central-1.aws.neon.tech/neondb?user=neondb_owner&password=npg_G62BDQMlkbKH&sslmode=require&channelBinding=require"

echo "DATABASE_URL=$JDBC_URL"
echo ""

# ============================================
# Generate JWT Secret
# ============================================
echo "🔐 JWT SECRET (Generate New)"
echo "-------------------"
JWT_SECRET=$(openssl rand -base64 32)
echo "JWT_SECRET=$JWT_SECRET"
echo ""

# ============================================
# Output for easy copy-paste
# ============================================
echo "================================"
echo "📋 COPY THESE TO YOUR DEPLOYMENT PLATFORM:"
echo "================================"
echo ""
cat <<EOF
SPRING_PROFILES_ACTIVE=dev
DATABASE_URL=$JDBC_URL
REDIS_HOST=$REDIS_HOST
REDIS_PORT=$REDIS_PORT
REDIS_PASSWORD=$REDIS_PASSWORD
REDIS_SSL=true
JWT_SECRET=$JWT_SECRET
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-gmail-app-password
MAIL_FROM=noreply@booking-dev.com
MAIL_FROM_NAME=Booking Platform Dev
APP_BASE_URL=https://your-app.onrender.com
EOF

echo ""
echo "✅ Done! Update MAIL_* and APP_BASE_URL with your values"
