from flask import Flask
from flask_sqlalchemy import SQLAlchemy
from flask_migrate import Migrate
import config

app = Flask(__name__)
app.config.from_object('config.Config')

db = SQLAlchemy(app)
migrate = Migrate(app, db)

migrate.init_app(app, db, render_as_batch=True)

from app.routes import todo_bp
app.register_blueprint(todo_bp, url_prefix='/api/todo')
