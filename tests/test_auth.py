import pytest


class TestLogin:
    def test_missing_body(self, client):
        resp = client.post("/api/auth/login", json={})
        assert resp.status_code == 400
        assert "error" in resp.get_json()

    def test_missing_password(self, client):
        resp = client.post("/api/auth/login", json={"username": "admin"})
        assert resp.status_code == 400

    def test_missing_username(self, client):
        resp = client.post("/api/auth/login", json={"password": "pass"})
        assert resp.status_code == 400

    def test_wrong_password(self, client, admin_user):
        resp = client.post(
            "/api/auth/login",
            json={"username": "testadmin", "password": "wrong"},
        )
        assert resp.status_code == 401
        assert resp.get_json()["error"] == "Invalid credentials"

    def test_unknown_user(self, client):
        resp = client.post(
            "/api/auth/login",
            json={"username": "nobody", "password": "pass"},
        )
        assert resp.status_code == 401

    def test_success(self, client, admin_user):
        resp = client.post(
            "/api/auth/login",
            json={"username": "testadmin", "password": "testpass"},
        )
        assert resp.status_code == 200
        data = resp.get_json()
        assert "access_token" in data
        assert isinstance(data["access_token"], str)
