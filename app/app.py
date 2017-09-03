import os

from flask import Flask
from flask_pymongo import PyMongo

frontend_dir = os.path.abspath("../../Frontend/")
static_dir = os.path.abspath("../../Frontend/static/")

app = Flask(__name__, template_folder=frontend_dir, static_folder=static_dir)

if "MONGO_URL" in os.environ:
    MONGO_HOST = str(os.environ["MONGO_URL"])
    MONGO_PORT = int(os.environ["MONGO_PORT"])
    MONGO_USERNAME = str(os.environ["MONGO_USERNAME"])
    MONGO_PASSWORD = str(os.environ["MONGO_PASSWORD"])
    MONGO_DBNAME = "loinnir"

    app.config["MONGO_URI"] = "mongodb://{username}:{password}@{host}:{port}/loinnir" \
        .format(username=MONGO_USERNAME, password=MONGO_PASSWORD, host=MONGO_HOST, port=MONGO_PORT)

    print("using production environment")

else:
    app.config["MONGO_URI"] = "mongodb://localhost:27017/loinnir"
    print("using development environment")

mongo = PyMongo(app)
