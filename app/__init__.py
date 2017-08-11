from flask import Flask
from flask_pymongo import PyMongo
import os

from app.frontend.frontend import frontend

from app.api.v1.services import services_endpoint
from app.api.v1.messages import messages_endpoint
from app.api.v1.users import user_endpoint
from app.api.v1.debug import debug_endpoint

frontend_dir = os.path.abspath("templates/")
static_dir = os.path.abspath("static/")

app = Flask(__name__, template_folder=frontend_dir, static_folder=static_dir)
app.debug = True

app.register_blueprint(frontend)
app.register_blueprint(debug_endpoint, url_prefix="/api/v1/debug")
app.register_blueprint(user_endpoint, url_prefix="/api/v1/users")
app.register_blueprint(messages_endpoint, url_prefix="/api/v1/messages")
app.register_blueprint(services_endpoint, url_prefix="/api/v1/services")

if "MONGO_USERNAME" in os.environ:
    MONGO_HOST = str(os.environ["MONGO_URL"])
    MONGO_PORT = int(os.environ["MONGO_PORT"])
    MONGO_USERNAME = str(os.environ["MONGO_USERNAME"])
    MONGO_PASSWORD = str(os.environ["MONGO_PASSWORD"])
    MONGO_DBNAME = "loinnir"

    app.config["MONGO_HOST"] = MONGO_HOST
    app.config["MONGO_PORT"] = MONGO_PORT
    app.config["MONGO_USERNAME"] = MONGO_USERNAME
    app.config["MONGO_PASSWORD"] = MONGO_PASSWORD
    app.config["MONGO_DBNAME"] = MONGO_DBNAME

mongo = PyMongo(app)
