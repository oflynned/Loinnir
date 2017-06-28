import math
import os
import sys
import time
import urllib.parse

from bson import json_util
from flask import Flask, request, Response
from flask_pymongo import PyMongo
from pyfcm import FCMNotification

from app.helpers.helper import Helper

frontend_dir = os.path.abspath("../Frontend")
static_dir = os.path.abspath("../Frontend/static")

app = Flask(__name__, template_folder=frontend_dir, static_folder=static_dir)
app.config["MONGO_DBNAME"] = "loinnir"
app.config["MONGO_URI"] = "mongodb://localhost:27017/loinnir"
app.debug = True

mode = "dev"

# persistence
mongo = PyMongo(app)

# TODO
"""
    unread/read messages
    pagination
    auto generate password to protect public api by tokens
    
    refactor by moving to blueprints, this is getting too long to comfortably code
"""