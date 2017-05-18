from flask import Flask, render_template, request, Response
from flask_pymongo import PyMongo
from bson import ObjectId, json_util
import json
import os, sys

frontend_dir = os.path.abspath("../Frontend")
static_dir = os.path.abspath("../Frontend/static")

app = Flask(__name__, template_folder=frontend_dir, static_folder=static_dir)
app.config["MONGO_DBNAME"] = "loinnir"
app.config["MONGO_URI"] = "mongodb://localhost:27017/loinnir"
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


def get_json(data):
    return Response(
        json_util.dumps(data),
        mimetype='application/json'
    )


# TODO Backend services
@app.route('/api/v1', methods=["GET"])
def hello_world():
    return get_json({"hello": "world"})


# POST {fb_id: 123456789, ...}
# GET {success: true} / {success:false, reason: "User already exists"}
@app.route('/api/v1/users/create', methods=["POST"])
def create_user():
    users_col = mongo.db.users
    data = request.json
    data["fb_id"] = str(data["fb_id"])
    fb_id = data["fb_id"]

    users_found = users_col.find({"fb_id": str(fb_id)})
    exists = users_found.count() > 0

    if exists:
        return get_json({"success": False, "reason": "User already exists"})
    else:
        users_col.insert(data)
        return get_json({"success": True})


# POST {fb_id: 123456789}
# GET {_id: ..., forename: ..., ...}
# get random person, get me, get individuals for map, get user info for chat, get people in locality
@app.route('/api/v1/users/get', methods=["POST"])
def get_user():
    data = request.json
    fb_id = str(data["fb_id"])

    users_col = mongo.db.users
    user = users_col.find({"fb_id": fb_id})
    is_existing = user.count() > 0

    if is_existing:
        user = list(user)[0]
        return get_json({"success": True, "user": user})
    else:
        return get_json({"success": False, "reason": "fb_id doesn't exist"})


@app.route('/api/v1/users/get-all', methods=["GET"])
def get_all_users():
    return get_json(mongo.db.users.find())


# TODO
@app.route('/api/v1/users/get-nearby', methods=["GET", "POST"])
def get_nearby_users():
    return get_json({"success": True})


# TODO
@app.route('/api/v1/users/get-random', methods=["GET", "POST"])
def get_random_user():
    return get_json({"success": True})


# DELETE {fb_id: 123456789}
# GET {success: true}
@app.route('/api/v1/users/delete', methods=["DELETE"])
def delete_user():
    users_col = mongo.db["users"]
    data = json.loads(request.data)
    fb_id = data["fb_id"]
    users_col.remove({"fb_id": fb_id})
    return get_json({"success": True})


# TODO edit lng, lat, etc
@app.route('/api/v1/users/edit', methods=["PUT"])
def edit_user():
    pass


if __name__ == '__main__':
    env = sys.argv[1]
    if env == "prod":
        app.run(host='0.0.0.0', port=80)
    else:
        app.run(host='0.0.0.0', port=3000)