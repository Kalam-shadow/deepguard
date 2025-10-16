﻿#!/usr/bin/env python3
"""DeepGuard v3.0 - Complete Integrated Server"""

import sys, os
sys.path.append(os.path.dirname(os.path.abspath(__file__)))

from fastapi import FastAPI, HTTPException, Header
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from typing import Optional, List, Dict
from datetime import datetime
import uvicorn, uuid

from src.core.security import create_access_token, verify_password, get_password_hash
from src.utils.logging import logger

app = FastAPI(title="DeepGuard API v3.0", version="3.0.0")
app.add_middleware(CORSMiddleware, allow_origins=["*"], allow_credentials=True, allow_methods=["*"], allow_headers=["*"])

users_db = {}
analytics_db = {"total_scans": 0, "threats_detected": 0, "recent_scans": []}

@app.on_event("startup")
def initialize_users_db():
    users_db["testuser"] = {
        "user_id": "1",
        "username": "testuser",
        "email": "test@deepguard.com",
        "full_name": "Test User",
        "hashed_password": get_password_hash("password123")
    }

class SignupRequest(BaseModel):
    username: str; email: str; password: str; full_name: Optional[str] = None

class LoginRequest(BaseModel):
    username: str; password: str

class ScanRequest(BaseModel):
    text: str

class NotificationPayload(BaseModel):
    content: str; sender: Optional[str] = "unknown"; timestamp: Optional[int] = None

def enhanced_harassment_check(text: str) -> dict:
    high = ['kill', 'murder', 'die', 'threat', 'hurt', 'harm']
    medium = ['hate', 'stupid', 'idiot', 'loser', 'pathetic']
    low = ['annoying', 'weird', 'dumb', 'ugly']
    profanity = ['fuck', 'shit', 'bitch', 'ass', 'damn']
    text_lower = text.lower()
    high_m = [k for k in high if k in text_lower]
    medium_m = [k for k in medium if k in text_lower]
    low_m = [k for k in low if k in text_lower]
    prof_m = [k for k in profanity if k in text_lower]
    all_m = high_m + medium_m + low_m + prof_m
    if high_m: toxic, level, sev = 0.85, "HIGH", "critical"
    elif medium_m: toxic, level, sev = 0.65, "MEDIUM", "high"
    elif low_m: toxic, level, sev = 0.40, "LOW", "medium"
    elif prof_m: toxic, level, sev = 0.30, "LOW", "low"
    else: toxic, level, sev = 0.05, "NONE", "none"
    is_har = toxic > 0.3
    return {'toxic_score': toxic, 'is_harassment': is_har, 'threat_level': level, 'severity': sev, 'keywords': all_m, 'confidence': toxic if is_har else (1.0 - toxic)}

@app.get("/")
async def root():
    return {"message": "DeepGuard API v3.0", "status": "healthy", "version": "3.0.0", "port": 8002}

@app.get("/api/v1/health")
async def health():
    return {"status": "healthy", "message": "API operational"}

@app.post("/api/v1/auth/signup")
async def signup(r: SignupRequest):
    if r.username in users_db: raise HTTPException(400, "Username exists")
    users_db[r.username] = {"user_id": str(len(users_db)+1), "username": r.username, "email": r.email, "full_name": r.full_name or r.username, "hashed_password": get_password_hash(r.password)}
    token = create_access_token({"sub": r.username})
    user = {k:v for k,v in users_db[r.username].items() if k!="hashed_password"}
    return {"success": True, "message": "Registered", "data": {"access_token": token, "token_type": "bearer", "user": user}}

@app.post("/api/v1/auth/login")
async def login(r: LoginRequest):
    u = users_db.get(r.username)
    if not u or not verify_password(r.password, u["hashed_password"]): raise HTTPException(401, "Invalid credentials")
    token = create_access_token({"sub": r.username})
    user = {k:v for k,v in u.items() if k!="hashed_password"}
    return {"success": True, "message": "Login OK", "data": {"access_token": token, "token_type": "bearer", "user": user}}

@app.get("/api/v1/auth/me")
async def get_me(authorization: Optional[str] = Header(None)):
    if not authorization: raise HTTPException(401, "Not authenticated")
    user = {k:v for k,v in users_db["testuser"].items() if k!="hashed_password"}
    return {"success": True, "data": user}

@app.post("/api/v1/scan_text")
async def scan_text(r: ScanRequest):
    a = enhanced_harassment_check(r.text)
    analytics_db["total_scans"] += 1
    if a["is_harassment"]: analytics_db["threats_detected"] += 1
    res = {"is_harassment": a["is_harassment"], "confidence": round(a["confidence"], 3), "severity": a["severity"], "threat_level": a["threat_level"], "keywords_detected": a["keywords"], "risk_score": int(a["toxic_score"]*100), "explanation": f"Threat: {a['threat_level']}" if a["is_harassment"] else "Safe"}
    return {"success": True, "message": "Scan complete", "data": res}

@app.post("/api/v1/scan_image")
async def scan_image():
    return {"success": True, "message": "Coming soon", "data": {"is_deepfake": False, "confidence": 0.0}}

@app.post("/api/v1/mobile/analyze-notification")
async def analyze_notification(p: NotificationPayload):
    a = enhanced_harassment_check(p.content)
    analytics_db["total_scans"] += 1
    if a["is_harassment"]: analytics_db["threats_detected"] += 1
    risk = int(a["toxic_score"]*100)
    return {"harassment": {"is_harassment": a["is_harassment"], "confidence": round(a["confidence"], 3), "type": "threat" if a["is_harassment"] else "safe", "severity": a["severity"], "keywords_detected": a["keywords"], "explanation": f"HARASSMENT: {risk}% risk" if a["is_harassment"] else "SAFE"}, "analysis_id": str(uuid.uuid4()), "timestamp": p.timestamp or int(datetime.now().timestamp()*1000), "risk_score": risk, "threat_level": a["threat_level"], "detection_method": "keyword_enhanced"}

@app.get("/api/v1/analytics/overview")
async def get_analytics():
    safe = analytics_db["total_scans"] - analytics_db["threats_detected"]
    return {"success": True, "data": {"total_scans": analytics_db["total_scans"], "threats_detected": analytics_db["threats_detected"], "threats_blocked": analytics_db["threats_detected"], "safe_messages": safe, "recent_activity": analytics_db["recent_scans"][:10]}}

if __name__ == "__main__":
    print("\n" + "="*60)
    print("DeepGuard v3.0 - Complete Integrated Server")
    print("="*60)
    print("Port: 8002")
    print("URL: http://0.0.0.0:8002")
    print("Docs: http://localhost:8002/docs")
    print("="*60 + "\n")
    uvicorn.run(app, host="0.0.0.0", port=8002)
