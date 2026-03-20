from datetime import datetime, timezone
from flask import jsonify, render_template, request
from flask_jwt_extended import get_jwt_identity
from app.admin import bp
from app.admin.helpers.auth import admin_required
from app.auth.models import User
from app import db

VALID_ROLES = {"admin", "user"}


@bp.route("/")
def index():
    return render_template("admin/index.html")


# ---------------------------------------------------------------------------
# Users API
# ---------------------------------------------------------------------------

@bp.route("/api/users", methods=["GET"])
@admin_required
def list_users():
    users = User.query.filter_by(deleted_at=None).order_by(User.created_at).all()
    return jsonify([u.to_dict() for u in users])


@bp.route("/api/users", methods=["POST"])
@admin_required
def create_user():
    data = request.get_json()
    if not data or not data.get("username") or not data.get("password"):
        return jsonify({"error": "username and password are required"}), 400

    role = data.get("role", "user")
    if role not in VALID_ROLES:
        return jsonify({"error": f"role must be one of: {', '.join(VALID_ROLES)}"}), 400

    if User.query.filter_by(username=data["username"], deleted_at=None).first():
        return jsonify({"error": "Username already exists"}), 409

    user = User(username=data["username"], role=role)
    user.set_password(data["password"])
    db.session.add(user)
    db.session.commit()
    return jsonify(user.to_dict()), 201


@bp.route("/api/users/<user_id>", methods=["PUT"])
@admin_required
def update_user(user_id):
    data = request.get_json()
    user = User.query.filter_by(id=user_id, deleted_at=None).first()
    if not user:
        return jsonify({"error": "User not found"}), 404

    if "username" in data:
        conflict = User.query.filter(
            User.username == data["username"],
            User.id != user.id,
            User.deleted_at == None,  # noqa: E711
        ).first()
        if conflict:
            return jsonify({"error": "Username already taken"}), 409
        user.username = data["username"]

    if "password" in data and data["password"]:
        user.set_password(data["password"])

    if "role" in data:
        if data["role"] not in VALID_ROLES:
            return jsonify({"error": f"role must be one of: {', '.join(VALID_ROLES)}"}), 400
        # Prevent removing admin role from yourself
        if str(user.id) == get_jwt_identity() and data["role"] != "admin":
            return jsonify({"error": "You cannot remove your own admin role"}), 403
        user.role = data["role"]

    db.session.commit()
    return jsonify(user.to_dict())


@bp.route("/api/users/<user_id>", methods=["DELETE"])
@admin_required
def delete_user(user_id):
    if user_id == get_jwt_identity():
        return jsonify({"error": "You cannot delete your own account"}), 403

    user = User.query.filter_by(id=user_id, deleted_at=None).first()
    if not user:
        return jsonify({"error": "User not found"}), 404

    user.deleted_at = datetime.now(timezone.utc)
    db.session.commit()
    return "", 204
