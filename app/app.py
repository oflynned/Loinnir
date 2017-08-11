from flask import Flask
from flask_pymongo import PyMongo
import os

frontend_dir = os.path.abspath("../../Frontend/")
static_dir = os.path.abspath("../../Frontend/static/")

app = Flask(__name__, template_folder=frontend_dir, static_folder=static_dir)

if "MONGO_USERNAME" in os.environ:
    app.config["MONGO_URI"] = "mongodb://{username}:{password}@{host}:{port}/loinnir"\
        .format(username=os.environ["MONGO_USERNAME"], password=os.environ["MONGO_PASSWORD"],
                host=os.environ["MONGO_URL"], port=os.environ["MONGO_PORT"])

    print(app.config)

else:
    app.config["MONGO_URI"] = "mongodb://localhost:27017/loinnir"
    app.debug = True

mongo = PyMongo(app)
