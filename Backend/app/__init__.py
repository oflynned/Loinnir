from flask import Flask
from flask_pymongo import PyMongo
import os

from app.frontend.frontend import frontend

from app.api.v1.services import services_endpoint
from app.api.v1.messages import messages_endpoint
from app.api.v1.users import user_endpoint

frontend_dir = os.path.abspath("../Frontend/")
static_dir = os.path.abspath("../Frontend/static/")

app = Flask(__name__, template_folder=frontend_dir, static_folder=static_dir)
app.debug = True

app.register_blueprint(frontend)
app.register_blueprint(user_endpoint, url_prefix="/api/v1/users")
app.register_blueprint(messages_endpoint, url_prefix="/api/v1/messages")
app.register_blueprint(services_endpoint, url_prefix="/api/v1/services")

mongo = PyMongo(app)
