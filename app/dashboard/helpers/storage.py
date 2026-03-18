import json
import os
from datetime import datetime, timezone
from pathlib import Path

DATA_DIR = Path(__file__).resolve().parents[3] / "data"
TASKS_FILE = DATA_DIR / "tasks.json"
LOG_FILE = DATA_DIR / "tasks.log"


def _ensure_data_dir():
    DATA_DIR.mkdir(exist_ok=True)


def load_tasks() -> list:
    if not TASKS_FILE.exists():
        return []
    with open(TASKS_FILE) as f:
        return json.load(f)


def save_tasks(tasks: list):
    _ensure_data_dir()
    with open(TASKS_FILE, "w") as f:
        json.dump(tasks, f, indent=2, default=str)


def append_log(message: str):
    _ensure_data_dir()
    ts = datetime.now(timezone.utc).strftime("%Y-%m-%d %H:%M:%S")
    with open(LOG_FILE, "a") as f:
        f.write(f"[{ts}] {message}\n")


def read_logs(limit: int = 200) -> list:
    if not LOG_FILE.exists():
        return []
    with open(LOG_FILE) as f:
        lines = [line.rstrip("\n") for line in f if line.strip()]
    return lines[-limit:][::-1]  # newest first
