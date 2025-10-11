#!/bin/bash
# Setup script for DeepGuard on new devices

echo "🛡️ Setting up DeepGuard v2.0..."

# Check Python version
python_version=$(python --version 2>&1)
echo "📍 Python version: $python_version"

# Create virtual environment
echo "🔧 Creating virtual environment..."
python -m venv venv

# Activate virtual environment
if [[ "$OSTYPE" == "msys" || "$OSTYPE" == "win32" ]]; then
    echo "🪟 Windows detected - activating venv..."
    source venv/Scripts/activate
else
    echo "🐧 Unix/Linux detected - activating venv..."
    source venv/bin/activate
fi

# Install dependencies
echo "📦 Installing Python dependencies..."
pip install --upgrade pip
pip install -r requirements.txt

# Test fast server
echo "🚀 Testing fast server..."
python -c "
import sys
sys.path.append('.')
from fast_server import app
print('✅ Fast server setup complete!')
"

echo ""
echo "🎉 DeepGuard setup complete!"
echo ""
echo "🚀 To start the server:"
echo "   python fast_server.py"
echo ""
echo "📱 For Android app:"
echo "   1. Open ShieldX/ in Android Studio"
echo "   2. Update IP addresses in ShieldXAPI.kt"
echo "   3. Build and install on device"
echo ""
echo "🌐 Find your IP address:"
if [[ "$OSTYPE" == "msys" || "$OSTYPE" == "win32" ]]; then
    ipconfig | findstr "IPv4"
else
    ifconfig | grep "inet "
fi