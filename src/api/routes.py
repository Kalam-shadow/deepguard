from fastapi import APIRouter, HTTPException, File, UploadFile, Depends
from typing import List, Optional

# 👇 use relative imports since routes.py and schemas.py are in the same module
from .schemas import (
    DeepfakeRequest,
    HarassmentRequest,
    AnalysisResponse,
    BatchAnalysisRequest,
    HealthCheckResponse
)

# absolute imports for other project modules
from src.services.detection import detect_deepfake, detect_harassment
from src.core.security import get_current_user
from src.utils.logging import logger

router = APIRouter(prefix="/api/v1")


@router.get("/health", response_model=HealthCheckResponse)
async def health_check():
    """Check if the API is operational"""
    return HealthCheckResponse(status="healthy")


@router.post("/analyze/deepfake", response_model=AnalysisResponse)
async def analyze_deepfake(
    request: DeepfakeRequest,
    current_user: str = Depends(get_current_user)
):
    """Analyze media file for potential deepfake content"""
    try:
        logger.info("Processing deepfake analysis request")
        result = await detect_deepfake(request.file)
        return AnalysisResponse(
            success=True,
            result=result,
            message="Analysis completed successfully"
        )
    except Exception as e:
        logger.error(f"Deepfake analysis failed: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))


@router.post("/analyze/harassment", response_model=AnalysisResponse)
async def analyze_harassment(
    request: HarassmentRequest,
    current_user: str = Depends(get_current_user)
):
    """Analyze text for harassment content"""
    try:
        logger.info("Processing harassment analysis request")
        result = await detect_harassment(request.text)
        return AnalysisResponse(
            success=True,
            result=result,
            message="Analysis completed successfully"
        )
    except Exception as e:
        logger.error(f"Harassment analysis failed: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))


@router.post("/analyze/batch", response_model=List[AnalysisResponse])
async def analyze_batch(
    request: BatchAnalysisRequest,
    current_user: str = Depends(get_current_user)
):
    """Process multiple items for analysis"""
    try:
        results = []
        for item in request.items:
            if item.type == "deepfake":
                result = await detect_deepfake(item.content)
            elif item.type == "harassment":
                result = await detect_harassment(item.content)
            else:
                raise HTTPException(
                    status_code=400,
                    detail=f"Unsupported analysis type: {item.type}"
                )
            results.append(AnalysisResponse(
                success=True,
                result=result,
                message=f"{item.type} analysis completed"
            ))
        return results
    except Exception as e:
        logger.error(f"Batch analysis failed: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))


@router.post("/upload", response_model=AnalysisResponse)
async def upload_file(
    file: UploadFile = File(...),
    analysis_type: Optional[str] = "deepfake",
    current_user: str = Depends(get_current_user)
):
    """Upload and analyze a file"""
    try:
        contents = await file.read()
        if analysis_type == "deepfake":
            result = await detect_deepfake(contents)
        else:
            raise HTTPException(
                status_code=400,
                detail=f"Unsupported analysis type: {analysis_type}"
            )

        return AnalysisResponse(
            success=True,
            result=result,
            message="File analysis completed successfully"
        )
    except Exception as e:
        logger.error(f"File upload and analysis failed: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))
