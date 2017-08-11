from flask import Flask
from flask_pymongo import PyMongo
import os

frontend_dir = os.path.abspath("../../Frontend/")
static_dir = os.path.abspath("../../Frontend/static/")

app = Flask(__name__, template_folder=frontend_dir, static_folder=static_dir)
app.config["MONGO_DBNAME"] = "loinnir"

if "MONGO_USERNAME" in os.environ:
    app.config["MONGO_USERNAME"] = os.environ["MONGO_USERNAME"]
    app.config["MONGO_PASSWORD"] = os.environ["MONGO_PASSWORD"]
    app.config["MONGO_HOST"] = os.environ["MONGO_URL"]
    app.config["MONGO_PORT"] = os.environ["MONGO_PORT"]
    app.config["MONGO_DBNAME"] = "loinnir"

    print(app.config)

else:
    app.config["MONGO_URI"] = "mongodb://localhost:27017/loinnir"
    app.debug = True

mongo = PyMongo(app)
