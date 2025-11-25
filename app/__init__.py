from flask import Flask
from flask_sqlalchemy import SQLAlchemy
from flask_migrate import Migrate
from flask_jwt_extended import JWTManager
import config

app = Flask(__name__)
app.config.from_object('config.Config')

db = SQLAlchemy(app)
migrate = Migrate(app, db)

app.config['JWT_SECRET_KEY'] = "sdkfnasafduyabdofadbfoadfboaiybysbouyvouYVOUyvUOYvouyvuyVOUyvouyvUYVyUVouyqverqweqefd"
jwt = JWTManager(app)

migrate.init_app(app, db, render_as_batch=True)

from app.routes import todo_bp
app.register_blueprint(todo_bp, url_prefix='/api/todo')
