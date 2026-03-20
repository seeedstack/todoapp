import pytest


BASE = "/api"


class TestListTasks:
    def test_empty(self, client, auth_headers):
        resp = client.get(f"{BASE}/tasks", headers=auth_headers)
        assert resp.status_code == 200
        assert resp.get_json() == []

    def test_returns_created_tasks(self, client, auth_headers):
        client.post(f"{BASE}/tasks", json={"title": "Task A"}, headers=auth_headers)
        client.post(f"{BASE}/tasks", json={"title": "Task B"}, headers=auth_headers)
        resp = client.get(f"{BASE}/tasks", headers=auth_headers)
        assert resp.status_code == 200
        titles = [t["title"] for t in resp.get_json()]
        assert "Task A" in titles
        assert "Task B" in titles

    def test_unauthenticated(self, client):
        resp = client.get(f"{BASE}/tasks")
        assert resp.status_code in (401, 422)


class TestCreateTask:
    def test_missing_title(self, client, auth_headers):
        resp = client.post(f"{BASE}/tasks", json={"description": "no title"}, headers=auth_headers)
        assert resp.status_code == 400
        assert "error" in resp.get_json()

    def test_minimal(self, client, auth_headers):
        resp = client.post(f"{BASE}/tasks", json={"title": "Buy milk"}, headers=auth_headers)
        assert resp.status_code == 201
        data = resp.get_json()
        assert data["title"] == "Buy milk"
        assert data["status"] == "pending"
        assert data["priority"] == "medium"
        assert data["id"] is not None

    def test_full_fields(self, client, auth_headers):
        resp = client.post(
            f"{BASE}/tasks",
            json={"title": "Deploy", "description": "Push to prod", "priority": "high"},
            headers=auth_headers,
        )
        assert resp.status_code == 201
        data = resp.get_json()
        assert data["description"] == "Push to prod"
        assert data["priority"] == "high"

    def test_persisted(self, client, auth_headers):
        client.post(f"{BASE}/tasks", json={"title": "Persisted"}, headers=auth_headers)
        tasks = client.get(f"{BASE}/tasks", headers=auth_headers).get_json()
        assert any(t["title"] == "Persisted" for t in tasks)

    def test_unauthenticated(self, client):
        resp = client.post(f"{BASE}/tasks", json={"title": "X"})
        assert resp.status_code in (401, 422)


class TestUpdateTask:
    def test_update_title(self, client, auth_headers):
        created = client.post(f"{BASE}/tasks", json={"title": "Old"}, headers=auth_headers).get_json()
        resp = client.put(f"{BASE}/tasks/{created['id']}", json={"title": "New"}, headers=auth_headers)
        assert resp.status_code == 200
        assert resp.get_json()["title"] == "New"

    def test_update_status(self, client, auth_headers):
        created = client.post(f"{BASE}/tasks", json={"title": "Task"}, headers=auth_headers).get_json()
        resp = client.put(f"{BASE}/tasks/{created['id']}", json={"status": "completed"}, headers=auth_headers)
        assert resp.status_code == 200
        assert resp.get_json()["status"] == "completed"

    def test_update_priority(self, client, auth_headers):
        created = client.post(f"{BASE}/tasks", json={"title": "Task"}, headers=auth_headers).get_json()
        resp = client.put(f"{BASE}/tasks/{created['id']}", json={"priority": "low"}, headers=auth_headers)
        assert resp.status_code == 200
        assert resp.get_json()["priority"] == "low"

    def test_not_found(self, client, auth_headers):
        resp = client.put(f"{BASE}/tasks/nonexistent-id", json={"title": "X"}, headers=auth_headers)
        assert resp.status_code == 404

    def test_unauthenticated(self, client):
        resp = client.put(f"{BASE}/tasks/some-id", json={"title": "X"})
        assert resp.status_code in (401, 422)


class TestDeleteTask:
    def test_delete(self, client, auth_headers):
        created = client.post(f"{BASE}/tasks", json={"title": "Gone"}, headers=auth_headers).get_json()
        resp = client.delete(f"{BASE}/tasks/{created['id']}", headers=auth_headers)
        assert resp.status_code == 204
        tasks = client.get(f"{BASE}/tasks", headers=auth_headers).get_json()
        assert not any(t["id"] == created["id"] for t in tasks)

    def test_not_found(self, client, auth_headers):
        resp = client.delete(f"{BASE}/tasks/nonexistent-id", headers=auth_headers)
        assert resp.status_code == 404

    def test_unauthenticated(self, client):
        resp = client.delete(f"{BASE}/tasks/some-id")
        assert resp.status_code in (401, 422)


class TestLogs:
    def test_empty(self, client, auth_headers):
        resp = client.get(f"{BASE}/logs", headers=auth_headers)
        assert resp.status_code == 200
        assert resp.get_json() == []

    def test_create_appends_log(self, client, auth_headers):
        client.post(f"{BASE}/tasks", json={"title": "Logged task"}, headers=auth_headers)
        logs = client.get(f"{BASE}/logs", headers=auth_headers).get_json()
        assert len(logs) >= 1
        assert any("Logged task" in line for line in logs)

    def test_delete_appends_log(self, client, auth_headers):
        created = client.post(f"{BASE}/tasks", json={"title": "To delete"}, headers=auth_headers).get_json()
        client.delete(f"{BASE}/tasks/{created['id']}", headers=auth_headers)
        logs = client.get(f"{BASE}/logs", headers=auth_headers).get_json()
        assert any("DELETED" in line for line in logs)

    def test_logs_newest_first(self, client, auth_headers):
        client.post(f"{BASE}/tasks", json={"title": "First"}, headers=auth_headers)
        client.post(f"{BASE}/tasks", json={"title": "Second"}, headers=auth_headers)
        logs = client.get(f"{BASE}/logs", headers=auth_headers).get_json()
        assert len(logs) >= 2
        assert logs[0] > logs[-1]

    def test_unauthenticated(self, client):
        resp = client.get(f"{BASE}/logs")
        assert resp.status_code in (401, 422)
