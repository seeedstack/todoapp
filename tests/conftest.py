import pytest
from app import create_app, db as _db
from app.auth.models import User


class TestConfig:
    TESTING = True
    SQLALCHEMY_DATABASE_URI = "sqlite:///:memory:"
    SQLALCHEMY_TRACK_MODIFICATIONS = False
    JWT_SECRET_KEY = "test-secret"
    JWT_ACCESS_TOKEN_EXPIRES = False


@pytest.fixture()
def app():
    app = create_app(TestConfig)
    with app.app_context():
        _db.create_all()
        yield app
        _db.drop_all()


@pytest.fixture()
def client(app):
    return app.test_client()


@pytest.fixture()
def admin_user(app):
    user = User(username="testadmin")
    user.set_password("testpass")
    _db.session.add(user)
    _db.session.commit()
    return user


@pytest.fixture()
def auth_headers(client, admin_user):
    resp = client.post(
        "/api/auth/login",
        json={"username": "testadmin", "password": "testpass"},
    )
    token = resp.get_json()["access_token"]
    return {"Authorization": f"Bearer {token}"}
