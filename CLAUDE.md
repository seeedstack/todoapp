# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Setup

```bash
python -m venv venv
source venv/bin/activate
pip install -r requirements.txt
```

Requires PostgreSQL running at `localhost:5432` with a database named `tododb`.

## Commands

```bash
# Run the app
python run.py

# Database migrations
flask db migrate -m "description"
flask db upgrade
flask db downgrade
```

## Architecture

Flask REST API with JWT authentication and PostgreSQL via SQLAlchemy.

- `config.py` — DB URI (`postgresql://localhost:5432/tododb`) and JWT secret
- `app/__init__.py` — App factory: initializes SQLAlchemy, Flask-Migrate, JWTManager, and registers the todo blueprint at `/api/todo`
- `app/models.py` — Single `Todo` model with soft-delete (uses `deleted_at` timestamp)
- `app/routes.py` — All endpoints; login is hardcoded (`admin`/`password`), all other routes are JWT-protected
- `migrations/` — Alembic migrations managed via Flask-Migrate

## API

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/api/todo/login` | No | Returns JWT token |
| GET | `/api/todo/` | JWT | List non-deleted todos |
| GET | `/api/todo/all` | JWT | List all todos (including deleted) |
| GET | `/api/todo/<id>` | JWT | Get single todo |
| POST | `/api/todo/` | JWT | Create todo |
| PUT | `/api/todo/<id>` | JWT | Update todo |
| DELETE | `/api/todo/<id>` | JWT | Soft-delete todo |

## Key Patterns

- **Soft delete**: `DELETE` sets `deleted_at`; `GET /` excludes these; `GET /all` includes them
- **Status field**: Boolean, but routes handle string `"False"` explicitly when parsing request data

### Module Structure

Each feature module follows this layout (using `app/auth/` as the reference):

```
app/<module>/
├── __init__.py     # Blueprint definition only — defines `bp`, then imports views and models
├── models.py       # SQLAlchemy models for this module
├── views.py        # Route handlers registered on `bp`
├── helpers/        # Pure helper functions (no Flask context dependencies)
└── workers/        # Background task workers
```

**`__init__.py`** — define the blueprint, then import views and models to trigger route/model registration:
```python
from flask import Blueprint
bp = Blueprint("auth", __name__)
from app.auth import views, models
```

**`models.py`** — SQLAlchemy models. Use `UUID(as_uuid=True)` PKs with `default=uuid.uuid4`. Include `deleted_at` / `deleted_by` columns for soft-delete. Place a `to_dict()` method on each model for serialization.

**`views.py`** — import `bp` from the module's `__init__`, use `AppMessages` for all response strings, and `bad_request` / `error_response` for all error returns. Always filter active records with `deleted_at=None`.

**Registering a new module** — add the blueprint in `app/__init__.py`:
```python
from app.<module> import bp as <module>_bp
app.register_blueprint(<module>_bp, url_prefix='/api/<module>')
```

## Rules
ALWAYS before making any changes. Search on the web for the newest documentation. And only implement if you are 100% sure it will work.
