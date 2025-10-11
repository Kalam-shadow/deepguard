@echo off
REM Setup script for DeepGuard on Windows

echo 🛡️ Setting up DeepGuard v2.0...

REM Check Python version
python --version
if %errorlevel% neq 0 (
    echo ❌ Python not found! Please install Python 3.8+ first
    pause
    exit /b 1
)

REM Create virtual environment
echo 🔧 Creating virtual environment...
python -m venv venv

REM Activate virtual environment
echo 🪟 Activating virtual environment...
call venv\Scripts\activate

REM Install dependencies
echo 📦 Installing Python dependencies...
python -m pip install --upgrade pip
pip install -r requirements.txt

REM Test fast server
echo 🚀 Testing fast server...
python -c "import sys; sys.path.append('.'); from fast_server import app; print('✅ Fast server setup complete!')"

echo.
echo 🎉 DeepGuard setup complete!
echo.
echo 🚀 To start the server:
echo    python fast_server.py
echo.
echo 📱 For Android app:
echo    1. Open ShieldX\ in Android Studio
echo    2. Update IP addresses in ShieldXAPI.kt
echo    3. Build and install on device
echo.
echo 🌐 Your IP addresses:
ipconfig | findstr "IPv4"
echo.
pause