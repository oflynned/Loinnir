from flask import Blueprint

messages = Blueprint("messages", __name__)


@messages.route("/")
def index():
    return "messages success!"
