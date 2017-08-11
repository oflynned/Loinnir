from flask import Flask
from flask_pymongo import PyMongo
import os

frontend_dir = os.path.abspath("../../Frontend/")
static_dir = os.path.abspath("../../Frontend/static/")

app = Flask(__name__, template_folder=frontend_dir, static_folder=static_dir)
app.config["MONGO_DBNAME"] = "loinnir"

if "MONGO_USERNAME" in os.environ:
    mongo_username = os.environ["MONGO_USERNAME"]
    mongo_password = os.environ["MONGO_PASSWORD"]
    mongo_url = os.environ["MONGO_URL"]

    app.config["MONGO_URI"] = "mongodb://" + mongo_username + ":" + mongo_password + "@" + mongo_url + "/loinnir"
else:
    app.config["MONGO_URI"] = "mongodb://localhost:27017/loinnir"
    app.debug = True

mongo = PyMongo(app)
