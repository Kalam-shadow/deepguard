#!/usr/bin/env python3
"""
Fast DeepGuard Server - Keyword-based harassment detection only
"""

import sys
import os
sys.path.append(os.path.dirname(os.path.abspath(__file__)))

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
import uvicorn
import re

print("🚀 Starting DeepGuard Fast Server...")

app = FastAPI(title="DeepGuard API", version="1.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

def enhanced_harassment_check(text: str) -> dict:
    """Enhanced keyword-based harassment detection"""
    
    # High severity threats (immediate danger)
    high_severity_keywords = [
        'kill', 'murder', 'slaughter', 'assassinate', 'eliminate', 'execute',
        'destroy', 'annihilate', 'harm', 'hurt', 'attack', 'assault', 'beat',
        'violence', 'violent', 'threat', 'threaten', 'revenge', 'payback'
    ]
    
    # Medium severity harassment  
    medium_severity_keywords = [
        'hate', 'despise', 'loathe', 'disgust', 'sick', 'pathetic', 'worthless',
        'useless', 'failure', 'reject', 'trash', 'garbage', 'waste', 'scum'
    ]
    
    # Low severity offensive language
    low_severity_keywords = [
        'stupid', 'idiot', 'moron', 'dumb', 'fool', 'loser', 'freak', 'weirdo',
        'ugly', 'fat', 'gross', 'disgusting', 'annoying', 'irritating'
    ]
    
    # Profanity (context-dependent)
    profanity_keywords = [
        'fuck', 'shit', 'bitch', 'ass', 'damn', 'hell', 'bastard', 'crap'
    ]
    
    text_lower = text.lower().strip()
    high_matches = []
    medium_matches = []
    low_matches = []
    profanity_matches = []
    
    # Check each category
    for keyword in high_severity_keywords:
        pattern = r'\b' + re.escape(keyword) + r'\b'
        if re.search(pattern, text_lower):
            high_matches.append(keyword)
            
    for keyword in medium_severity_keywords:
        pattern = r'\b' + re.escape(keyword) + r'\b'
        if re.search(pattern, text_lower):
            medium_matches.append(keyword)
            
    for keyword in low_severity_keywords:
        pattern = r'\b' + re.escape(keyword) + r'\b'
        if re.search(pattern, text_lower):
            low_matches.append(keyword)
            
    for keyword in profanity_keywords:
        pattern = r'\b' + re.escape(keyword) + r'\b'
        if re.search(pattern, text_lower):
            profanity_matches.append(keyword)
    
    # Calculate threat score based on severity
    toxic_score = 0.0
    all_matches = []
    
    if high_matches:
        toxic_score = 0.85 + len(high_matches) * 0.05  # 85-95% for high threats
        all_matches.extend(high_matches)
    elif medium_matches:
        toxic_score = 0.65 + len(medium_matches) * 0.05  # 65-80% for medium threats  
        all_matches.extend(medium_matches)
    elif low_matches:
        toxic_score = 0.35 + len(low_matches) * 0.05   # 35-55% for low threats
        all_matches.extend(low_matches)
    elif profanity_matches:
        toxic_score = 0.25 + len(profanity_matches) * 0.03  # 25-40% for profanity only
        all_matches.extend(profanity_matches)
        
    # Cap the maximum score
    toxic_score = min(0.95, toxic_score)
    
    return {
        'toxic_score': toxic_score,
        'matches': len(all_matches),
        'found_keywords': all_matches
    }

@app.get("/")
async def root():
    return {"message": "DeepGuard Fast API is running!", "status": "healthy", "method": "keyword_detection"}

@app.get("/api/v1/health")
async def health():
    return {"status": "healthy", "message": "API is working", "method": "keyword_detection"}

@app.post("/api/v1/mobile/analyze-notification")
async def analyze_notification(data: dict):
    content = data.get("content", "")
    sender = data.get("sender", "unknown")
    
    print(f"🔍 ANALYZING: '{content}' from {sender}")
    
    # Use enhanced keyword detection
    analysis = enhanced_harassment_check(content)
    toxic_score = analysis['toxic_score']
    keywords = analysis['found_keywords']
    
    is_harassment = toxic_score > 0.3
    confidence = toxic_score if is_harassment else (1.0 - toxic_score)
    
    # Risk percentage should reflect harassment probability
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
        "processing_time_ms": 5,
        "risk_score": risk_percentage,
        "threat_level": threat_level,
        "detection_method": "keyword_enhanced"
    }
    
    print(f"🎯 RESULT: {risk_percentage}% RISK SCORE - {threat_level} THREAT - Method: keyword_enhanced")
    if keywords:
        print(f"🔍 Keywords detected: {keywords}")
    
    return result

if __name__ == "__main__":
    print("📱 Ready to receive Android app requests!")
    uvicorn.run(app, host="0.0.0.0", port=8001)