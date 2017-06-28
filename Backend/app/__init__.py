from flask import Flask, render_template
import os

from app.frontend.frontend import frontend

from app.api.v1.services import services
from app.api.v1.messages import messages
from app.api.v1.user import user


frontend_dir = os.path.abspath("../Frontend/")
static_dir = os.path.abspath("../Frontend/static/")

app = Flask(__name__, template_folder=frontend_dir, static_folder=static_dir)
app.debug = True

app.register_blueprint(frontend)
app.register_blueprint(user, url_prefix="/api/v1/user")
app.register_blueprint(messages, url_prefix="/api/v1/messages")
app.register_blueprint(services, url_prefix="/api/v1/services")
