import pytest
from fastapi.testclient import TestClient
from datetime import datetime
from src.main import app

client = TestClient(app)

def test_mobile_analyze_notification():
    """Test the mobile notification analysis endpoint"""
    
    # First, get a token (using existing auth system)
    login_response = client.post("/api/v1/token", data={
        "username": "johndoe",
        "password": "secret"
    })
    assert login_response.status_code == 200
    token = login_response.json()["access_token"]
    
    headers = {"Authorization": f"Bearer {token}"}
    
    # Test harassment detection
    notification_data = {
        "sender": "test_user",
        "message_text": "You're so stupid and worthless!",
        "app_name": "WhatsApp",
        "timestamp": datetime.now().isoformat(),
        "device_id": "test_device_123"
    }
    
    response = client.post(
        "/api/v1/mobile/analyze-notification",
        json=notification_data,
        headers=headers
    )
    
    assert response.status_code == 200
    result = response.json()
    
    # Check response structure
    assert "is_harassment" in result
    assert "confidence_score" in result
    assert "severity_level" in result
    assert "alert_id" in result
    assert "recommendation" in result

def test_mobile_analyze_notification_safe_message():
    """Test the mobile notification analysis with a safe message"""
    
    # Get token
    login_response = client.post("/api/v1/token", data={
        "username": "johndoe",
        "password": "secret"
    })
    token = login_response.json()["access_token"]
    headers = {"Authorization": f"Bearer {token}"}
    
    # Test safe message
    notification_data = {
        "sender": "friend",
        "message_text": "Hey! How are you doing today?",
        "app_name": "Messenger",
        "timestamp": datetime.now().isoformat(),
        "device_id": "test_device_123"
    }
    
    response = client.post(
        "/api/v1/mobile/analyze-notification",
        json=notification_data,
        headers=headers
    )
    
    assert response.status_code == 200
    result = response.json()
    
    # For a safe message, harassment should be False or low confidence
    assert result["is_harassment"] == False or result["confidence_score"] < 0.5

def test_device_status():
    """Test device status endpoint"""
    
    # Get token
    login_response = client.post("/api/v1/token", data={
        "username": "johndoe",
        "password": "secret"
    })
    token = login_response.json()["access_token"]
    headers = {"Authorization": f"Bearer {token}"}
    
    response = client.get(
        "/api/v1/mobile/device-status/test_device_123",
        headers=headers
    )
    
    assert response.status_code == 200
    result = response.json()
    
    assert "device_id" in result
    assert "monitoring_active" in result
    assert "apps_monitored" in result

def test_batch_notifications():
    """Test batch notification analysis"""
    
    # Get token
    login_response = client.post("/api/v1/token", data={
        "username": "johndoe",
        "password": "secret"
    })
    token = login_response.json()["access_token"]
    headers = {"Authorization": f"Bearer {token}"}
    
    batch_data = {
        "notifications": [
            {
                "sender": "user1",
                "message_text": "Hello there!",
                "app_name": "WhatsApp",
                "timestamp": datetime.now().isoformat(),
                "device_id": "test_device_123"
            },
            {
                "sender": "user2",
                "message_text": "You're pathetic and should disappear!",
                "app_name": "Instagram",
                "timestamp": datetime.now().isoformat(),
                "device_id": "test_device_123"
            }
        ],
        "device_id": "test_device_123"
    }
    
    response = client.post(
        "/api/v1/mobile/analyze-batch-notifications",
        json=batch_data,
        headers=headers
    )
    
    assert response.status_code == 200
    results = response.json()
    
    assert len(results) == 2
    assert all("is_harassment" in result for result in results)