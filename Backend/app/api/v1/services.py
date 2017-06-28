from flask import Blueprint

services = Blueprint("services", __name__)


@services.route("/")
def index():
    return "services success!"
