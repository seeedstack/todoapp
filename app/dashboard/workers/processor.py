#!/usr/bin/env python3
"""
Autonomous task processor — run via cron every hour.

Cron entry:
    0 * * * * /path/to/venv/bin/python /path/to/app/dashboard/workers/processor.py
"""

import os
import sys
from datetime import datetime, timezone
from pathlib import Path

# Allow running as a standalone script from any working directory
sys.path.insert(0, str(Path(__file__).resolve().parents[4]))

from dotenv import load_dotenv
load_dotenv(Path(__file__).resolve().parents[4] / ".env")

import anthropic
from app.dashboard.helpers.storage import append_log, load_tasks, save_tasks

PRIORITY_ORDER = {"high": 0, "medium": 1, "low": 2}


def log(message: str):
    ts = datetime.now(timezone.utc).strftime("%Y-%m-%d %H:%M:%S")
    entry = f"[{ts}] {message}"
    print(entry)
    append_log(message)


def process_task_with_claude(task: dict) -> str:
    client = anthropic.Anthropic(api_key=os.environ.get("ANTHROPIC_API_KEY"))
    description_part = (
        f"\nDescription: {task['description']}" if task.get("description") else ""
    )
    prompt = (
        f"You are an autonomous task executor. Complete the following task and "
        f"provide a concise summary of what was done or what the result is.\n\n"
        f"Task: {task['title']}{description_part}\n"
        f"Priority: {task['priority']}\n\n"
        f"Execute the task (or describe the steps/actions performed if it requires "
        f"external access) and summarize the outcome in 2-4 sentences."
    )
    message = client.messages.create(
        model="claude-sonnet-4-6",
        max_tokens=1024,
        messages=[{"role": "user", "content": prompt}],
    )
    return message.content[0].text


def main():
    log("=== Processor run started ===")

    tasks = load_tasks()
    pending = [t for t in tasks if t.get("status") == "pending"]

    if not pending:
        log("No pending tasks — nothing to do.")
        log("=== Processor run finished ===")
        return

    pending.sort(key=lambda t: PRIORITY_ORDER.get(t.get("priority", "medium"), 99))
    log(f"Found {len(pending)} pending task(s). Processing highest priority first.")

    task_map = {t["id"]: t for t in tasks}

    for task in pending:
        log(f"Processing: [{task['priority'].upper()}] {task['title']}")
        try:
            result = process_task_with_claude(task)
            task_map[task["id"]]["status"] = "completed"
            task_map[task["id"]]["completed_at"] = datetime.now(timezone.utc).isoformat()
            task_map[task["id"]]["result"] = result
            save_tasks(list(task_map.values()))
            summary = result[:300] + ("..." if len(result) > 300 else "")
            log(f"Completed: {task['title']}")
            log(f"Result: {summary}")
        except Exception as exc:
            log(f"ERROR processing '{task['title']}': {exc}")
            task_map[task["id"]]["status"] = "error"
            save_tasks(list(task_map.values()))

    log("=== Processor run finished ===")


if __name__ == "__main__":
    main()
