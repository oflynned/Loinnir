from flask import Flask, jsonify, render_template
from flask_pymongo import PyMongo
import os

frontend_dir = os.path.abspath("../Frontend")
static_dir = os.path.abspath("../Frontend/static")

app = Flask(__name__, template_folder=frontend_dir, static_folder=static_dir)
app.config["MONGO_DBNAME"] = "anseo"
app.config["MONGO_URI"] = "mongodb://localhost:27017/anseo"
app.debug = True
mongo = PyMongo(app)


# TODO static serving of frontend -- move to blueprint soon
@app.route("/", methods=["GET"])
def index():
    return render_template("index.html")


@app.route("/tacaiocht", methods=["GET"])
def tacaiocht():
    return render_template("tacaiocht.html")


@app.route("/priobhaideacht", methods=["GET"])
def priobhaideacht():
    return render_template("priobhaideacht.html")


@app.route("/faq", methods=["GET"])
def faq():
    return render_template("faq.html")


# TODO Backend services

@app.route('/api/v1', methods=["GET"])
def hello_world():
    return jsonify({"success": True})


@app.route('/api/v1/users', methods=["POST"])
def create_user():
    pass


@app.route('/api/v1/users', methods=["GET"])
def get_user(uid):
    user_db = mongo.db.users
    user = user_db.find_one({"_id": uid})

    if user:
        output = user
    else:
        output = {"success": False}

    return jsonify(output)


@app.route('/api/v1/users', methods=["DELETE"])
def delete_user():
    pass


@app.route('/api/v1/users', methods=["PUT"])
def edit_user():
    pass


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=3000)
