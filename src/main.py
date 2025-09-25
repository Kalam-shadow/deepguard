# src/main.py

from fastapi import FastAPI, Depends
from fastapi.middleware.cors import CORSMiddleware

# Import your centralized settings and logger
from src.core.config import Settings, settings
from src.utils.logging import logger

# Import the router that contains all your endpoints
from src.api.routes import router as api_router

# Metadata for API documentation tags
tags_metadata = [
    {
        "name": "Health",
        "description": "API health check.",
    },
    {
        "name": "Authentication",
        "description": "User authentication and token management.",
    },
    {
        "name": "Analysis",
        "description": "Endpoints for deepfake and harassment analysis.",
    },
]

# Create the main FastAPI application instance
app = FastAPI(
    title="DeepGuard API",
    description="AI-powered Deepfake & Harassment Detection",
    version="1.0.0",
    openapi_tags=tags_metadata,
    # Hide the default /docs and /redoc URLs if debug mode is off
    docs_url="/docs" if settings.DEBUG else None,
    redoc_url="/redoc" if settings.DEBUG else None,
)

# --- Application Lifecycle Events ---
@app.on_event("startup")
async def startup_event():
    """
    Actions to perform on application startup.
    (e.g., loading ML models, connecting to a database)
    """
    logger.info("Application startup...")
    # In a real app, you might load models here:
    # service.load_models()

@app.on_event("shutdown")
async def shutdown_event():
    """
    Actions to perform on application shutdown.
    (e.g., cleaning up resources, closing database connections)
    """
    logger.info("Application shutdown.")


# --- Add CORS Middleware ---
if settings.ALLOWED_ORIGINS:
    app.add_middleware(
        CORSMiddleware,
        allow_origins=[str(origin) for origin in settings.ALLOWED_ORIGINS],
        allow_credentials=True,
        allow_methods=["*"],
        allow_headers=["*"],
    )

# --- Include Your API Router ---
app.include_router(api_router, prefix=settings.API_PREFIX)


@app.get("/", tags=["Health"])
async def read_root(app_settings: Settings = Depends(lambda: settings)):
    """
    A simple root endpoint to confirm the API is running and show current settings.
    """
    return {
        "message": "Welcome to the DeepGuard API!",
        "debug_mode": app_settings.DEBUG,
        "api_prefix": app_settings.API_PREFIX
    }