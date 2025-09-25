from pydantic_settings import BaseSettings
from typing import List

class Settings(BaseSettings):
    """
    Centralized application settings. Pydantic will automatically
    load values from a .env file to override these defaults.
    """

    # API Settings
    API_PREFIX: str = "/api/v1"
    HOST: str = "0.0.0.0"
    PORT: int = 8000
    DEBUG: bool = False
    WORKERS: int = 4

    # Security (SECRET_KEY must be set in the .env file)
    SECRET_KEY: str
    ALGORITHM: str = "HS256"
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 30
    
    # 👇 Add these lines to define the user credentials
    API_USERNAME: str
    API_PASSWORD: str

    # CORS
    ALLOWED_ORIGINS: List[str] = ["http://localhost:3000"]

    # Model Settings
    DEEPFAKE_MODEL_PATH: str = "models/deepfake.pt"
    HARASSMENT_MODEL_PATH: str = "models/harassment.pt"

    class Config:
        env_file = ".env"
        case_sensitive = False

# Create a single, importable instance of the Settings class
settings = Settings()