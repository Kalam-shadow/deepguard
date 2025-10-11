# 🛡️ DeepGuard v2.0 - Complete Harassment Detection System

## 🌟 Overview
DeepGuard is a comprehensive real-time harassment detection system with both **Python FastAPI backend** and **Android mobile app**. It provides intelligent threat analysis using enhanced keyword-based detection and AI models.

## ✨ Features
- **🔍 Real-time Harassment Detection**: Advanced multi-level threat analysis (HIGH/MEDIUM/LOW)
- **📱 Android App (ShieldX)**: Complete mobile app with notification monitoring
- **🚀 Fast API Backend**: Two server options - fast keyword-based and AI-powered
- **📊 Accurate Risk Scoring**: Fixed calculation with proper threat percentages
- **🌐 Network Connectivity**: Multiple IP fallback system for Android connectivity
- **🔧 Enhanced Detection**: Multi-severity keyword matching with word boundaries

## 📂 Project Structure
```
deepguard/
├── 🖥️ Backend (Python FastAPI)
│   ├── fast_server.py              # Fast keyword-based detection (recommended)
│   ├── server.py                   # AI-powered detection with transformers
│   ├── src/
│   │   ├── api/
│   │   │   ├── mobile_routes.py    # Mobile API endpoints
│   │   │   └── routes.py           # Web API endpoints
│   │   ├── models/
│   │   │   └── harassment.py       # Enhanced detection algorithms
│   │   └── services/
│   │       └── detection.py        # Detection services
│   └── tests/
│       ├── test_api.py             # API tests
│       └── test_mobile_api.py      # Mobile API tests
│
├── 📱 Android App (ShieldX)
│   ├── app/
│   │   ├── build.gradle.kts        # Android dependencies
│   │   └── src/main/
│   │       ├── java/com/example/shieldx/
│   │       │   ├── api/            # Network communication
│   │       │   │   ├── ShieldXAPI.kt
│   │       │   │   └── ConnectionTester.kt
│   │       │   ├── service/        # Notification monitoring
│   │       │   │   └── ShieldXNotificationListener.kt
│   │       │   └── utils/          # Helper utilities
│   │       └── res/                # UI resources
│   └── gradle/                     # Gradle wrapper
│
└── 📋 Configuration
    ├── requirements.txt            # Python dependencies
    ├── .gitignore                 # Git ignore rules
    └── README.md                  # This file
```

## 🚀 Quick Start

### 1️⃣ Clone the Repository
```bash
git clone https://github.com/Jaisurya3101/deepguard.git
cd deepguard
```

### 2️⃣ Backend Setup (Python)

#### **Option A: Fast Server (Recommended)**
```bash
# Install Python dependencies
pip install -r requirements.txt

# Run the fast server (no AI model downloads)
python fast_server.py
```

#### **Option B: AI-Powered Server**
```bash
# Install Python dependencies with AI models
pip install -r requirements.txt

# Run the AI server (downloads transformers models)
python server.py
```

The server will start on `http://0.0.0.0:8001`

### 3️⃣ Android App Setup

1. **Open Android Studio**
2. **Import Project**: Choose the `ShieldX/` folder
3. **Update IP Address**: In `ShieldXAPI.kt`, update the IP addresses in `BASE_URLS` to match your computer's IP
4. **Build & Run**: Install on your Android device

### 4️⃣ Network Configuration

**Find your computer's IP address:**
```bash
# Windows
ipconfig | findstr "IPv4"

# Linux/Mac
ifconfig | grep "inet "
```

**Update Android app IP addresses** in `ShieldX/app/src/main/java/com/example/shieldx/api/ShieldXAPI.kt`:
```kotlin
private val BASE_URLS = listOf(
    "http://YOUR_COMPUTER_IP:8001",  // Replace with your IP
    "http://192.168.1.100:8001",     // Example IPs
    "http://10.0.2.2:8001",          // Android emulator
)
```

## 🔧 Dependencies

### Python Backend
- **FastAPI**: Web framework for APIs
- **uvicorn**: ASGI server
- **transformers**: AI models (for server.py)
- **torch**: Machine learning framework
- **pydantic**: Data validation

### Android App
- **Kotlin**: Programming language
- **Retrofit**: HTTP client
- **OkHttp**: Networking
- **Material Design**: UI components

## 📊 API Endpoints

### Health Check
```
GET /api/v1/health
```

### Mobile Analysis
```
POST /api/v1/mobile/analyze-notification
{
    "content": "message text",
    "sender": "sender name",
    "source": "com.whatsapp",
    "timestamp": 1234567890
}
```

**Response:**
```json
{
    "harassment": {
        "is_harassment": false,
        "confidence": 0.95,
        "type": "safe",
        "severity": "none",
        "keywords_detected": [],
        "explanation": "✅ SAFE: 95% confidence"
    },
    "risk_score": 5,
    "threat_level": "LOW",
    "detection_method": "keyword_enhanced"
}
```

## 🧪 Testing

### Backend Tests
```bash
# Run API tests
python -m pytest tests/ -v

# Test specific endpoint
python tests/test_mobile_api.py
```

### Android Testing
1. Enable **Notification Access** for ShieldX app
2. Send test WhatsApp messages
3. Check Android logs for analysis results

## 🚨 Threat Detection Examples

| Message | Risk Score | Threat Level | Status |
|---------|------------|--------------|---------|
| "Hello friend" | 0% | LOW | ✅ SAFE |
| "You're stupid" | 35% | LOW | ⚠️ MINOR |
| "I hate you" | 65% | MEDIUM | 🔶 MODERATE |
| "I'll kill you" | 90% | HIGH | 🚨 SEVERE |

## 🔒 Security Features

- **Multi-level Detection**: High/Medium/Low severity classification
- **Word Boundary Matching**: Prevents false positives
- **Real-time Processing**: < 50ms response time
- **Offline Capable**: Keyword detection works without internet

## 🐛 Troubleshooting

### Backend Issues
```bash
# Server won't start
pip install --upgrade fastapi uvicorn

# Port already in use
netstat -ano | findstr :8001
taskkill /F /PID <process_id>
```

### Android Connection Issues
1. **Check IP Address**: Ensure computer IP matches Android app configuration
2. **Firewall**: Allow Python through Windows Firewall
3. **Network**: Both devices must be on same Wi-Fi network
4. **Permissions**: Grant Notification Access to ShieldX app

### Performance Issues
- Use `fast_server.py` for faster response times
- Disable AI models if not needed
- Check network latency between devices

## 📈 Threat Analysis Algorithm

**Keyword Categories:**
- **High Severity (85-95%)**: kill, murder, slaughter, violence, threat
- **Medium Severity (65-80%)**: hate, pathetic, worthless, disgusting  
- **Low Severity (35-55%)**: stupid, idiot, loser, ugly
- **Profanity (25-40%)**: swear words (context dependent)

**Scoring Logic:**
- Base score per category + additional score per extra keyword
- Word boundary matching prevents false positives
- Caps at 95% maximum threat score

## 🤝 Contributing

1. Fork the repository
2. Create feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open Pull Request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🆘 Support

- **GitHub Issues**: Report bugs and request features
- **Documentation**: Check this README for setup help
- **Testing**: Use provided test files for validation

---

**🛡️ DeepGuard v2.0** - Protecting digital communications with intelligent threat detection.