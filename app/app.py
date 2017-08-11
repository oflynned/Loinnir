from flask import Flask
from flask_pymongo import PyMongo
import os

frontend_dir = os.path.abspath("../../Frontend/")
static_dir = os.path.abspath("../../Frontend/static/")

app = Flask(__name__, template_folder=frontend_dir, static_folder=static_dir)
app.config["MONGO_DBNAME"] = "loinnir"

if "MONGO_USERNAME" in os.environ and "MONGO_PASSWORD" in os.environ:
    username = os.environ["MONGO_USERNAME"]
    password = os.environ["MONGO_PASSWORD"]
    url = "52.19.192.229"
    port = 27017
    app.config["MONGO_URI"] = "mongodb://{username}:{password}@{url}:{port}/loinnir".format(
        username=username, password=password, url=url, port=port)
else:
    app.config["MONGO_URI"] = "mongodb://localhost:27017/loinnir"
    app.debug = True

mongo = PyMongo(app)
