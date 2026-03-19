import os
import uuid
from datetime import datetime, timezone
from flask import jsonify, render_template, request
from flask_jwt_extended import jwt_required
from app.dashboard import bp
from app.dashboard.models import Task, TaskLog
from app import db


def _append_log(message: str):
    ts = datetime.now(timezone.utc).strftime("%Y-%m-%d %H:%M:%S")
    entry = f"[{ts}] {message}"
    log = TaskLog(message=entry)
    db.session.add(log)


# ---------------------------------------------------------------------------
# Pages
# ---------------------------------------------------------------------------

@bp.route("/")
def index():
    return render_template("dashboard/index.html")


@bp.route("/login")
def login_page():
    return render_template("dashboard/login.html")


# ---------------------------------------------------------------------------
# Tasks API
# ---------------------------------------------------------------------------

@bp.route("/api/tasks", methods=["GET"])
@jwt_required()
def list_tasks():
    tasks = Task.query.filter_by(deleted_at=None).order_by(Task.created_at.desc()).all()
    return jsonify([t.to_dict() for t in tasks])


@bp.route("/api/tasks", methods=["POST"])
@jwt_required()
def create_task():
    data = request.get_json(force=True)
    if not data or not data.get("title"):
        return jsonify({"error": "title is required"}), 400

    task = Task(
        title=data["title"],
        description=data.get("description", ""),
        priority=data.get("priority", "medium"),
    )
    db.session.add(task)
    _append_log(f"CREATED [{task.priority.upper()}] {task.title}")
    db.session.commit()
    return jsonify(task.to_dict()), 201


@bp.route("/api/tasks/<task_id>", methods=["PUT"])
@jwt_required()
def update_task(task_id):
    try:
        task_uuid = uuid.UUID(task_id)
    except (ValueError, AttributeError):
        return jsonify({"error": "Not found"}), 404
    task = Task.query.filter_by(id=task_uuid, deleted_at=None).first()
    if not task:
        return jsonify({"error": "Not found"}), 404

    data = request.get_json(force=True)
    for key in ("title", "description", "priority", "status"):
        if key in data:
            setattr(task, key, data[key])

    _append_log(f"UPDATED [{task.priority.upper()}] {task.title}")
    db.session.commit()
    return jsonify(task.to_dict())


@bp.route("/api/tasks/<task_id>", methods=["DELETE"])
@jwt_required()
def delete_task(task_id):
    try:
        task_uuid = uuid.UUID(task_id)
    except (ValueError, AttributeError):
        return jsonify({"error": "Not found"}), 404
    task = Task.query.filter_by(id=task_uuid, deleted_at=None).first()
    if not task:
        return jsonify({"error": "Not found"}), 404

    task.deleted_at = datetime.now(timezone.utc)
    _append_log(f"DELETED {task.title}")
    db.session.commit()
    return "", 204


# ---------------------------------------------------------------------------
# Logs API
# ---------------------------------------------------------------------------

@bp.route("/api/logs", methods=["GET"])
@jwt_required()
def get_logs():
    logs = TaskLog.query.filter_by(deleted_at=None).order_by(TaskLog.created_at.desc()).limit(200).all()
    return jsonify([log.message for log in logs])


# ---------------------------------------------------------------------------
# CLI: process-tasks
# ---------------------------------------------------------------------------

PRIORITY_ORDER = {"high": 0, "medium": 1, "low": 2}


@bp.cli.command("process-tasks")
def process_tasks_command():
    """Process all pending tasks using Claude AI."""
    import anthropic

    def log(message: str):
        ts = datetime.now(timezone.utc).strftime("%Y-%m-%d %H:%M:%S")
        entry = f"[{ts}] {message}"
        print(entry)
        db.session.add(TaskLog(message=entry))
        db.session.commit()

    log("=== Processor run started ===")

    pending = (
        Task.query.filter_by(status="pending", deleted_at=None).all()
    )
    pending.sort(key=lambda t: PRIORITY_ORDER.get(t.priority, 99))

    if not pending:
        log("No pending tasks — nothing to do.")
        log("=== Processor run finished ===")
        return

    log(f"Found {len(pending)} pending task(s). Processing highest priority first.")

    client = anthropic.Anthropic(api_key=os.environ.get("ANTHROPIC_API_KEY"))

    for task in pending:
        log(f"Processing: [{task.priority.upper()}] {task.title}")
        try:
            description_part = f"\nDescription: {task.description}" if task.description else ""
            prompt = (
                f"You are an autonomous task executor. Complete the following task and "
                f"provide a concise summary of what was done or what the result is.\n\n"
                f"Task: {task.title}{description_part}\n"
                f"Priority: {task.priority}\n\n"
                f"Execute the task (or describe the steps/actions performed if it requires "
                f"external access) and summarize the outcome in 2-4 sentences."
            )
            message = client.messages.create(
                model="claude-sonnet-4-6",
                max_tokens=1024,
                messages=[{"role": "user", "content": prompt}],
            )
            result = message.content[0].text
            task.status = "completed"
            task.completed_at = datetime.now(timezone.utc)
            task.result = result
            db.session.commit()
            summary = result[:300] + ("..." if len(result) > 300 else "")
            log(f"Completed: {task.title}")
            log(f"Result: {summary}")
        except Exception as exc:
            task.status = "error"
            db.session.commit()
            log(f"ERROR processing '{task.title}': {exc}")

    log("=== Processor run finished ===")
