import os

from flask import Flask
from flask_pymongo import PyMongo

frontend_dir = os.path.abspath("../../Frontend/")
static_dir = os.path.abspath("../../Frontend/static/")

app = Flask(__name__, template_folder=frontend_dir, static_folder=static_dir)

if "MONGODB_URI" in os.environ:
    app.config["MONGO_URI"] = os.environ["MONGODB_URI"]
    print("using production environment")
else:
    app.config["MONGO_URI"] = "mongodb://localhost:27017/loinnir"
    print("using development environment")

mongo = PyMongo(app)
