from flask import jsonify, render_template, request
from app.dashboard import bp
from app.dashboard.helpers.storage import (
    append_log,
    load_tasks,
    read_logs,
    save_tasks,
)
from app.dashboard.models import Task


@bp.route("/")
def index():
    return render_template("dashboard/index.html")


# ---------------------------------------------------------------------------
# Tasks API
# ---------------------------------------------------------------------------

@bp.route("/api/tasks", methods=["GET"])
def list_tasks():
    return jsonify(load_tasks())


@bp.route("/api/tasks", methods=["POST"])
def create_task():
    data = request.get_json(force=True)
    if not data or not data.get("title"):
        return jsonify({"error": "title is required"}), 400

    task = Task(
        title=data["title"],
        description=data.get("description", ""),
        priority=data.get("priority", "medium"),
    )
    tasks = load_tasks()
    tasks.append(task.to_dict())
    save_tasks(tasks)
    append_log(f"CREATED [{task.priority.upper()}] {task.title}")
    return jsonify(task.to_dict()), 201


@bp.route("/api/tasks/<task_id>", methods=["PUT"])
def update_task(task_id):
    data = request.get_json(force=True)
    tasks = load_tasks()
    for task in tasks:
        if task["id"] == task_id:
            for key in ("title", "description", "priority", "status"):
                if key in data:
                    task[key] = data[key]
            save_tasks(tasks)
            append_log(f"UPDATED [{task['priority'].upper()}] {task['title']}")
            return jsonify(task)
    return jsonify({"error": "Not found"}), 404


@bp.route("/api/tasks/<task_id>", methods=["DELETE"])
def delete_task(task_id):
    tasks = load_tasks()
    task = next((t for t in tasks if t["id"] == task_id), None)
    if not task:
        return jsonify({"error": "Not found"}), 404
    save_tasks([t for t in tasks if t["id"] != task_id])
    append_log(f"DELETED {task['title']}")
    return "", 204


# ---------------------------------------------------------------------------
# Logs API
# ---------------------------------------------------------------------------

@bp.route("/api/logs", methods=["GET"])
def get_logs():
    return jsonify(read_logs())
