import os
from pathlib import Path
from dotenv import load_dotenv

load_dotenv()

BASE_DIR = Path(__file__).resolve().parent


class Config:
    SQLALCHEMY_DATABASE_URI = os.environ.get("DATABASE_URL", "postgresql://localhost:5432/tododb")
    SQLALCHEMY_TRACK_MODIFICATIONS = False

    JWT_SECRET_KEY = os.environ.get("JWT_SECRET_KEY", "<SECRET>")

    ANTHROPIC_API_KEY = os.environ.get("ANTHROPIC_API_KEY", "<API KEY>")

    ADMIN_USERNAME = os.environ.get("ADMIN_USERNAME", "username")
    ADMIN_PASSWORD = os.environ.get("ADMIN_PASSWORD", "password")
