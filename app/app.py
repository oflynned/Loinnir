from flask import Flask
from flask_pymongo import PyMongo
import os

frontend_dir = os.path.abspath("../../Frontend/")
static_dir = os.path.abspath("../../Frontend/static/")

app = Flask(__name__, template_folder=frontend_dir, static_folder=static_dir)
app.config["MONGO_DBNAME"] = "loinnir"

print(os.environ)

if "MONGO_USERNAME" in os.environ:
    print("using production")

    mongo_username = os.environ["MONGO_USERNAME"]
    mongo_password = os.environ["MONGO_PASSWORD"]
    mongo_url = os.environ["MONGO_URL"]
    mongo_port = os.environ["MONGO_PORT"]

    app.config["MONGO_URI"] = "mongodb://{username}:{password}@{url}:{port}/loinnir".format(
        username=mongo_username, password=mongo_password, url=mongo_url, port=mongo_port)
else:
    print("using development")

    app.config["MONGO_URI"] = "mongodb://localhost:27017/loinnir"
    app.debug = True

mongo = PyMongo(app)
