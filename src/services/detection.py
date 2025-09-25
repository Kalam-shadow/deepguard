# src/services/detection.py

from fastapi import HTTPException
from typing import Dict, Any, Union
from PIL import Image
from src.models.deepfake import DeepfakeModel
from src.models.harassment import HarassmentDetector
import io

# You would need a library like OpenCV to handle video frames
# import cv2
# import numpy as np

class DetectionService:
    def __init__(self):
        # The model name is now updated to a valid one.
        # Change this line for testing purposes
        self.deepfake_model = DeepfakeModel(model_name="google/vit-base-patch16-224")

        self.harassment_model = HarassmentDetector()

    def analyze_image(self, image: Image.Image) -> Dict[str, Any]:
        try:
            # Corrected method call
            deepfake_result = self.deepfake_model.analyze_image(image)
            return {"deepfake": deepfake_result, "harassment": None}
        except Exception as e:
            raise HTTPException(status_code=500, detail=f"Error analyzing image: {str(e)}")

    def analyze_video(self, video_bytes: bytes) -> Dict[str, Any]:
        # NOTE: Real video analysis requires extracting frames first.
        # This is a placeholder showing how it would work if you had frames.
        # For a real implementation, you'd use a library like OpenCV here.
        try:
            deepfake_result = {"status": "Video analysis not fully implemented."}
            return {"deepfake": deepfake_result, "harassment": None}
        except Exception as e:
            raise HTTPException(status_code=500, detail=f"Error analyzing video: {str(e)}")

    def analyze_text(self, text: str) -> Dict[str, Any]:
        try:
            harassment_result = self.harassment_model.detect_harassment([text])
            return {"deepfake": None, "harassment": harassment_result}
        except Exception as e:
            raise HTTPException(status_code=500, detail=f"Error analyzing text: {str(e)}")


# Instantiate service once
_service = DetectionService()


# Async wrappers for your routes.py
async def detect_deepfake(file: Union[str, bytes]) -> Dict[str, Any]:
    try:
        if isinstance(file, bytes):
            image = Image.open(io.BytesIO(file))
            return _service.analyze_image(image)
        elif isinstance(file, str):
            image = Image.open(file)
            return _service.analyze_image(image)
        else:
            raise HTTPException(status_code=400, detail="Unsupported file format")
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Deepfake detection error: {str(e)}")


async def detect_harassment(text: str) -> Dict[str, Any]:
    try:
        return _service.analyze_text(text)
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Harassment detection error: {str(e)}")