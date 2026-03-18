from flask import Blueprint

bp = Blueprint("dashboard", __name__, template_folder="templates")

from app.dashboard import views, models  # noqa: E402, F401
