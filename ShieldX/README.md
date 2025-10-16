# 🛡️ ShieldX - Ready to Load in Android Studio

## 📱 **READY TO USE - Just Open in Android Studio!**

### **🚀 Quick Setup (3 Steps)**

#### **1. Open in Android Studio**
```
1. Open Android Studio
2. File > Open > Navigate to: c:\deepguard\deepgaurd\deepguard\ShieldX
3. Click "Open" and wait for Gradle sync to complete
```

#### **2. Start DeepGuard Backend**
```powershell
# In PowerShell (as Administrator):
cd c:\deepguard\deepgaurd\deepguard
python -m uvicorn src.main:app --host 0.0.0.0 --port 8001 --reload
```

#### **3. Build and Run**
```
1. In Android Studio: Build > Clean Project
2. Build > Rebuild Project  
3. Run > Run 'app' (or press Shift+F10)
```

---

## 📂 **Complete Project Structure**
```
ShieldX/                                    # ← Open this folder in Android Studio
├── 📄 build.gradle.kts                    # ✅ Root build configuration
├── 📄 settings.gradle.kts                 # ✅ Project settings
├── 📁 gradle/wrapper/                     # ✅ Gradle wrapper
└── 📁 app/                                # ✅ Main app module
    ├── 📄 build.gradle.kts                # ✅ App dependencies & config
    ├── 📄 proguard-rules.pro              # ✅ ProGuard rules
    └── 📁 src/main/                       # ✅ Source code
        ├── 📄 AndroidManifest.xml         # ✅ App permissions & services
        ├── 📁 java/com/example/shieldx/   # ✅ Kotlin source files
        │   ├── 📄 MainActivity.kt         # ✅ Main UI
        │   ├── 📁 service/                # ✅ Notification listener
        │   ├── 📁 api/                    # ✅ Backend integration
        │   ├── 📁 data/                   # ✅ Data models
        │   └── 📁 utils/                  # ✅ Utilities
        └── 📁 res/                        # ✅ Resources
            ├── 📁 drawable/               # ✅ Icons
            ├── 📁 mipmap-*/               # ✅ App icons
            ├── 📁 values/                 # ✅ Strings, colors, themes
            └── 📁 xml/                    # ✅ Backup rules
```

---

## ✅ **What's Already Configured**

### **🔧 Dependencies**
- ✅ Compose UI with Material Design 3
- ✅ Retrofit for API calls  
- ✅ Coroutines for async operations
- ✅ All required permissions

### **🌐 Network Setup**  
- ✅ Your IP addresses: `192.168.56.1`, `192.168.137.1`, `172.16.125.114`
- ✅ HTTP cleartext traffic enabled
- ✅ Auto-fallback to different IPs

### **📱 App Features**
- ✅ Beautiful Material Design 3 UI
- ✅ Notification listener service
- ✅ Real-time harassment detection
- ✅ Connection testing
- ✅ Permission handling

---

## 🎯 **After Installation**

### **First Run:**
1. **Grant Permissions**: App will guide you through setup
2. **Enable Notification Access**: Tap "Enable Protection" 
3. **Test Connection**: Use "Test Backend Connection" button
4. **Monitor Logs**: Check Logcat for "ShieldX" tags

### **Testing Harassment Detection:**
1. Send test messages in WhatsApp with words like: "hate", "stupid", "ugly"
2. Watch for ShieldX alert notifications
3. Check Logcat for detection results

---

## 🐛 **Troubleshooting**

### **"Cannot resolve symbol" errors:**
```
File > Invalidate Caches and Restart > Invalidate and Restart
```

### **"Backend Disconnected" in app:**
```powershell
# 1. Ensure backend is running:
cd c:\deepguard\deepgaurd\deepguard
python -m uvicorn src.main:app --host 0.0.0.0 --port 8001 --reload

# 2. Test API directly:
curl http://192.168.56.1:8001/api/v1/mobile/analyze-notification -X POST -H "Content-Type: application/json" -d '{"content":"test","source":"test","sender":"test","timestamp":1234567890}'
```

### **Gradle sync issues:**
1. File > Settings > Build > Gradle > Use Gradle from: 'gradle-wrapper.properties file'
2. Build > Clean Project
3. File > Sync Project with Gradle Files

### **OutOfMemoryError (Java heap space):**
```
✅ ALREADY FIXED! But if you still get memory errors:

1. In Android Studio:
   File > Settings > Build > Gradle
   Gradle JVM: Select Java 11 or higher
   
2. Or run the build script:
   Double-click: build_shieldx.bat
   
3. Or manually set environment variables:
   set GRADLE_OPTS=-Xmx4096m -XX:MaxPermSize=512m
   ./gradlew clean assembleDebug
```

---

## 📊 **Features Ready to Use**

- 🛡️ **Real-time Protection**: Monitors WhatsApp, Telegram, Instagram, etc.
- 🤖 **AI Detection**: Connects to your DeepGuard backend
- ⚡ **Instant Alerts**: Shows harassment notifications immediately  
- 🔄 **Smart Fallback**: Local keyword detection if backend fails
- 🎨 **Modern UI**: Material Design 3 with beautiful animations
- 📱 **Easy Setup**: Guided permission setup process

---

**🚀 The project is 100% ready - just open `ShieldX` folder in Android Studio and run!** 🛡️