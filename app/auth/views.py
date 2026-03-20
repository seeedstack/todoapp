from flask import request, jsonify
from flask_jwt_extended import create_access_token
from app.auth import bp
from app.auth.models import User


@bp.route('/login', methods=['POST'])
def login():
    data = request.get_json()
    if not data or not data.get('username') or not data.get('password'):
        return jsonify({'error': 'Username and password are required'}), 400

    user = User.query.filter_by(username=data['username'], deleted_at=None).first()
    if not user or not user.check_password(data['password']):
        return jsonify({'error': 'Invalid credentials'}), 401

    token = create_access_token(identity=str(user.id), additional_claims={"role": user.role})
    return jsonify({'access_token': token}), 200
