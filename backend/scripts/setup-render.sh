#!/bin/bash

# ============================================
# Render.com Deployment Script
# ============================================
# Quick setup script for deploying to Render

echo "🚀 Render.com Setup for Booking Platform"
echo "=========================================="
echo ""

# Check if render CLI is installed
if ! command -v render &> /dev/null; then
    echo "⚠️  Render CLI not found. Install with:"
    echo "   npm install -g render-cli"
    echo ""
    echo "📝 Manual setup instead:"
    echo "   1. Go to https://render.com"
    echo "   2. Sign up with GitHub"
    echo "   3. New Web Service → Select your repo"
    echo "   4. Configure:"
    echo "      - Branch: dev"
    echo "      - Root Directory: backend"
    echo "      - Environment: Docker"
    echo "      - Region: Frankfurt"
    echo "      - Plan: Free"
    echo ""
    echo "   5. Add environment variables (see DEPLOYMENT-GUIDE.md)"
    echo ""
    exit 0
fi

echo "✅ Render CLI found"
echo ""

# Render Blueprint deployment
echo "📦 Using render.yaml blueprint..."
echo ""
echo "Run this command to deploy:"
echo ""
echo "  cd backend"
echo "  render blueprint deploy"
echo ""
echo "Then set environment variables in Render dashboard:"
echo ""
echo "  1. Go to your service in Render"
echo "  2. Environment → Add variables"
echo "  3. Copy from .env.dev.example"
echo ""
echo "🔗 Quick links:"
echo "   Dashboard: https://dashboard.render.com"
echo "   Docs: https://render.com/docs"
