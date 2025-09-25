import pytest
from fastapi.testclient import TestClient
from src.main import app

client = TestClient(app)

def test_health_check():
    response = client.get("/health")
    assert response.status_code == 200
    assert response.json() == {"status": "ok"}

def test_analyze_media_image():
    with open("path/to/test_image.jpg", "rb") as image_file:
        response = client.post("/analyze_media", files={"file": image_file})
    assert response.status_code == 200
    assert "result" in response.json()

def test_analyze_media_video():
    with open("path/to/test_video.mp4", "rb") as video_file:
        response = client.post("/analyze_media", files={"file": video_file})
    assert response.status_code == 200
    assert "result" in response.json()

def test_analyze_media_unsupported_file():
    with open("path/to/test_file.txt", "rb") as text_file:
        response = client.post("/analyze_media", files={"file": text_file})
    assert response.status_code == 400
    assert response.json()["detail"] == "Unsupported file type"