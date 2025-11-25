from flask import Blueprint, request, jsonify
from app.models import Todo
from app import db
from flask_jwt_extended import jwt_required, create_access_token, get_jwt_identity


todo_bp = Blueprint('todo_bp', __name__)


@todo_bp.route('/login', methods=['POST'])
def login():
    data = request.get_json()
    username = data.get('username')
    password = data.get('password')
    if username == 'admin' and password == 'password':
        access_token = create_access_token(identity=username)
        return jsonify(access_token=access_token)
    return jsonify({'msg': 'Bad Username or Password!'})


@todo_bp.route('/', methods=['GET'])
@jwt_required()
def get_pending():
    todos = Todo.query.filter(Todo.deleted_at.is_(None)).all()
    return jsonify([todo.to_dict() for todo in todos])


@todo_bp.route('/all', methods=['GET'])
@jwt_required()
def get_todos():
    todos = Todo.query.all()
    return jsonify([todo.to_dict() for todo in todos])


@todo_bp.route('/<int:id>', methods=['GET'])
@jwt_required()
def get_todo_by_id(id):
    todo = Todo.query.get_or_404(id)
    return jsonify(todo.to_dict())


@todo_bp.route('/', methods=['POST'])
@jwt_required()
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
@jwt_required()
def update_todo(id):
    todo = Todo.query.get_or_404(id)
    data = request.get_json()
    todo.name = data.get('name', todo.name)
    todo.description = data.get('description', todo.description)
    todo.status = False if data.get('status', todo.status) == "False" else True
    from datetime import datetime, timezone
    todo.updated_at = datetime.now(timezone.utc)
    db.session.commit()
    return jsonify(todo.to_dict())


@todo_bp.route('/<int:id>', methods=['DELETE'])
@jwt_required()
def delete_todo(id):
    from datetime import datetime, timezone
    todo = Todo.query.get_or_404(id)
    todo.deleted_at = datetime.now(timezone.utc)
    todo.updated_at = datetime.now(timezone.utc)
    db.session.commit()
    return jsonify({'message': 'Deleted successfully!'})
