from flask import Blueprint, request, jsonify
from app.models import Todo
from app import db


todo_bp = Blueprint('todo_bp', __name__)


@todo_bp.route('/', methods=['GET'])
def get_pending():
    todos = Todo.query.filter(Todo.deleted_at.is_(None)).all()
    return jsonify([todo.to_dict() for todo in todos])


@todo_bp.route('/all', methods=['GET'])
def get_todos():
    todos = Todo.query.all()
    return jsonify([todo.to_dict() for todo in todos])


@todo_bp.route('/<int:id>', methods=['GET'])
def get_todo_by_id(id):
    todo = Todo.query.get_or_404(id)
    return jsonify(todo.to_dict())


@todo_bp.route('/', methods=['POST'])
def create_todo():
    data = request.get_json()
    name = data.get('name') or data.get('title')
    description = data.get('description')
    status = False if data.get('status') == "False" else True
    if not name:
        return jsonify({"error": "Name required"}), 400

    todo = Todo(name=name, description=description, status=status)
    db.session.add(todo)
    db.session.commit()
    return jsonify(todo.to_dict()), 201


@todo_bp.route('/<int:id>', methods=['PUT'])
def update_todo(id):
    todo = Todo.query.get_or_404(id)
    data = request.get_json()
    todo.name = data.get('name', todo.name)
    todo.description = data.get('description', todo.description)
    todo.status = data.get('status', todo.status)
    db.session.commit()
    return jsonify(todo.to_dict())


@todo_bp.route('/<int:id>', methods=['DELETE'])
def delete_todo(id):
    from datetime import datetime, timezone
    todo = Todo.query.get_or_404(id)
    todo.deleted_at = datetime.now(timezone.utc)
    db.session.commit()
    return jsonify({'message': 'Deleted successfully!'})
