#!/usr/bin/env python3
"""
Simple DeepGuard API Server for harassment detection
"""

import sys
import os
sys.path.append(os.path.dirname(os.path.abspath(__file__)))

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
import uvicorn
from src.models.harassment import HarassmentDetector

print("🤖 Initializing DeepGuard AI Harassment Detection...")
try:
    detector = HarassmentDetector()
    print("✅ AI Model loaded successfully!")
except Exception as e:
    print(f"⚠️ AI Model failed to load, using keyword detection: {e}")
    detector = None

app = FastAPI(title="DeepGuard API", version="1.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

@app.get("/")
async def root():
    return {"message": "DeepGuard API is running!", "status": "healthy", "ai_enabled": detector is not None}

@app.get("/api/v1/health")
async def health():
    return {"status": "healthy", "message": "API is working", "ai_enabled": detector is not None}

@app.post("/api/v1/mobile/analyze-notification")
async def analyze_notification(data: dict):
    content = data.get("content", "")
    sender = data.get("sender", "unknown")
    
    print(f"🔍 ANALYZING: '{content}' from {sender}")
    
    if detector:
        # Use AI model
        try:
            analysis_result = detector.analyze_text(content)
            toxic_score = analysis_result.get('TOXIC', 0.0)
            method = analysis_result.get('method', 'AI_model')
            keywords = analysis_result.get('found_keywords', [])
        except Exception as e:
            print(f"⚠️ AI analysis failed: {e}")
            toxic_score = simple_keyword_check(content)
            method = 'keyword_fallback'
            keywords = []
    else:
        # Use simple keyword detection
        toxic_score = simple_keyword_check(content)
        method = 'keyword_only'
        keywords = []
    
    is_harassment = toxic_score > 0.3
    confidence = toxic_score if is_harassment else (1.0 - toxic_score)
    
    # Risk percentage should reflect harassment probability, not confidence
    risk_percentage = round(toxic_score * 100)
    
    # Determine threat level based on harassment probability
    if toxic_score > 0.8:
        threat_level = "HIGH"
    elif toxic_score > 0.6:
        threat_level = "MEDIUM" 
    else:
        threat_level = "LOW"
    
    # Android app compatible response format
    result = {
        "harassment": {
            "is_harassment": is_harassment,
            "confidence": round(confidence, 3),
            "type": "threat" if is_harassment else "safe",
            "severity": threat_level.lower() if is_harassment else "none",
            "keywords_detected": keywords,
            "explanation": f"🎯 HARASSMENT: {risk_percentage}% risk | {threat_level} threat" if is_harassment else f"✅ SAFE: {round(confidence*100)}% confidence"
        },
        "analysis_id": f"analysis_{data.get('timestamp', '0')}",
        "timestamp": data.get("timestamp", 0),
        "processing_time_ms": 30,
        "risk_score": risk_percentage,
        "threat_level": threat_level,
        "detection_method": method
    }
    
    print(f"🎯 RESULT: {risk_percentage}% RISK SCORE - {threat_level} THREAT - Method: {method}")
    if keywords:
        print(f"🔍 Keywords detected: {keywords}")
    
    return result

def simple_keyword_check(text):
    """Simple keyword-based harassment detection"""
    harassment_keywords = [
        'kill', 'die', 'hate', 'stupid', 'idiot', 'fuck', 'shit', 'bitch',
        'ass', 'damn', 'hell', 'loser', 'worthless', 'pathetic', 'ugly',
        'murder', 'destroy', 'hurt', 'pain', 'suffer', 'threat', 'attack'
    ]
    
    text_lower = text.lower()
    matches = sum(1 for keyword in harassment_keywords if keyword in text_lower)
    
    if matches > 0:
        return min(0.95, 0.6 + matches * 0.15)  # 60% base + 15% per match
    return 0.1  # Low confidence for safe messages

if __name__ == "__main__":
    print("🚀 Starting DeepGuard API Server...")
    print("📱 Ready to receive Android app requests!")
    uvicorn.run(app, host="0.0.0.0", port=8001)