import os
import click
from flask import Flask
from flask_sqlalchemy import SQLAlchemy
from flask_migrate import Migrate
from flask_jwt_extended import JWTManager
from config import Config

db = SQLAlchemy()
migrate = Migrate()
jwt = JWTManager()


def create_app(config=None):
    app = Flask(__name__)
    app.config.from_object(Config)
    if config:
        app.config.from_object(config)

    db.init_app(app)

    with app.app_context():
        from app.auth.models import User                    # noqa: F401
        from app.dashboard.models import Task, TaskLog      # noqa: F401

    migrate.init_app(app, db)
    jwt.init_app(app)

    from app.auth import bp as auth_bp
    app.register_blueprint(auth_bp, url_prefix='/api/auth')

    from app.dashboard import bp as dashboard_bp
    app.register_blueprint(dashboard_bp, url_prefix='/')

    from app.admin import bp as admin_bp
    app.register_blueprint(admin_bp, url_prefix='/admin')

    @app.cli.command('seed-admin')
    def seed_admin():
        """Create the admin user from ADMIN_USERNAME / ADMIN_PASSWORD env vars."""
        from app.auth.models import User

        username = os.environ.get('ADMIN_USERNAME')
        password = os.environ.get('ADMIN_PASSWORD')

        if not username or not password:
            click.echo('Error: ADMIN_USERNAME and ADMIN_PASSWORD must be set in .env', err=True)
            return

        existing = User.query.filter_by(username=username).first()
        if existing:
            click.echo(f'User "{username}" already exists.')
            return

        user = User(username=username)
        user.set_password(password)
        db.session.add(user)
        db.session.commit()
        click.echo(f'Admin user "{username}" created.')

    return app


app = create_app()
