from flask import Flask, render_template, request, Response
from flask_pymongo import PyMongo
from bson import json_util
import json
import os, sys
from random import randint
import time

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
    return get_json({"hey there": "developer ;)"})


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
        return get_json(list(user)[0])
    else:
        return get_json({"success": False, "reason": "fb_id doesn't exist"})


# POST {fb_id:123456789}
# GET [{}]
@app.route('/api/v1/users/get-all', methods=["GET"])
def get_all_users():
    return get_json(mongo.db.users.find())


# POST {fb_id:123456789}
# GET [{}]
@app.route('/api/v1/users/get-others', methods=["POST"])
def get_other_users():
    data = request.json
    fb_id = str(data["fb_id"])
    return get_json(mongo.db.users.find({"fb_id": {"$ne": fb_id}}))


# POST {fb_id:123456789}
# GET [{...}]
@app.route('/api/v1/users/get-nearby', methods=["POST"])
def get_nearby_users():
    data = request.json
    fb_id = str(data["fb_id"])

    # find local users and exclude self from lookup
    users_col = mongo.db.users
    this_user = list(users_col.find({"fb_id": fb_id}))[0]
    nearby_users = users_col.find({"fb_id": {"$ne": fb_id}, "locality": this_user["locality"]})
    return get_json(nearby_users)


# POST {fb_id:123456789}
# GET [{}]
@app.route('/api/v1/users/get-random', methods=["GET", "POST"])
def get_random_user():
    data = request.json
    fb_id = str(data["fb_id"])

    # find random user and exclude self from lookup
    # TODO need to exclude users already chatted with!
    users_col = mongo.db.users
    # exclude self
    count = mongo.db.users.count() - 2
    users = users_col.find({"fb_id": {"$ne": fb_id}})
    user = users[randint(0, count)]
    return get_json(user)


# DELETE {fb_id: 123456789}
# GET {success: true}
@app.route('/api/v1/users/delete', methods=["DELETE"])
def delete_user():
    users_col = mongo.db["users"]
    data = json.loads(request.data)
    fb_id = str(data["fb_id"])
    users_col.remove({"fb_id": fb_id})
    return get_json({"success": True})


# TODO get list of some sort and classify by nearest town from web service or spreadsheet
def get_nearest_town(lng, lat):
    pass


# POST {fb_id:123456789,lat:0,lng:0,locality:"Place"}
# TODO POST {fb_id:123456789,lat:0,lng:0} and automatically generate locality
# GET {...}
@app.route('/api/v1/users/update-location', methods=["POST"])
def update_location():
    data = request.json
    fb_id = str(data["fb_id"])

    users_col = mongo.db.users
    user = list(users_col.find({"fb_id": fb_id}))[0]
    user["lat"] = data["lat"]
    user["lng"] = data["lng"]
    user["locality"] = data["locality"]

    users_col.save(user)
    return get_json({"success": True})


# POST {from_id: ..., to_id: ..., message: "..."}
# GET {success: true}
@app.route('/api/v1/messages/send-partner-message', methods=["POST"])
def send_partner_message():
    data = request.json

    message = dict()
    message["from_id"] = str(data["from_id"])
    message["to_id"] = str(data["to_id"])
    message["time"] = int(round(time.time() * 1000))
    message["message"] = str(data["message"])

    partner_col = mongo.db.partner_conversations
    partner_col.insert(message)
    return get_json({"success": True})


# POST {fb_id: ..., message: "..."}
# GET {success: true}
@app.route('/api/v1/messages/send-locality-message', methods=["POST"])
def send_locality_message():
    data = request.json

    fb_id = str(data["fb_id"])
    users_col = mongo.db.users
    user = users_col.find({"fb_id": fb_id})
    user = list(user)[0]
    locality = user["locality"]

    message = dict()
    message["fb_id"] = str(data["fb_id"])
    message["locality"] = str(locality)
    message["time"] = int(round(time.time() * 1000))
    message["message"] = str(data["message"])

    locality_col = mongo.db.locality_conversations
    locality_col.insert(message)

    return get_json({"success": True})


# TODO order by time
# get messages pertaining to chatting with an individual
# POST {my_id: ..., partner_id: ...}
# GET [{...},{...}]
@app.route("/api/v1/messages/get-partner-messages", methods=["POST"])
def get_partner_messages():
    data = request.json
    my_id = data["my_id"]
    partner_id = data["partner_id"]

    partner_col = mongo.db.partner_conversations
    messages = partner_col.find({"participants": [my_id, partner_id]})

    return get_json(list(messages))


# TODO order by time
# get all messages residing within the locality for the user's record provided
# POST {fb_id: ...}
# GET [{...},{...}]
@app.route("/api/v1/messages/get-locality-messages", methods=["POST"])
def get_locality_messages():
    data = request.json
    fb_id = data["fb_id"]
    user = list(mongo.db.users.find({"fb_id": fb_id}))[0]
    locality = user["locality"]

    locality_col = mongo.db.locality_conversations
    messages = locality_col.find({"locality": locality})

    return get_json(list(messages))


# POST {my_id: ..., partner_id: ...}
@app.route("/api/v1/messages/subscribe-partner-conversation", methods=["POST"])
def subscribe_conversations():
    data = request.json
    my_id = data["my_id"]
    partner_id = data["partner_id"]

    conversations_col = mongo.db.conversations
    # have I ever talked to anyone before?
    conversation = conversations_col.find({"fb_id": my_id})
    if conversation.count() > 0:
        # okay cool, I've talked to someone before, I just have to check the partner id now
        conversation = conversations_col.find({"fb_id": my_id, "partners": partner_id})
        if conversation.count() > 0:
            # I've talked to you before, nothing else to do?
            return get_json({"success": True, "action": "already subscribed to partner"})
        else:
            # never talked to you before, just add the id to the list
            conversations_col.update({"fb_id": my_id}, {"$push": {"partners": partner_id}})
            return get_json({"success": True, "action": "added new partner to list"})
    else:
        # first conversation ever, update both fb_id and partner list
        conversation = dict()
        conversation["fb_id"] = my_id
        conversation["partners"] = [partner_id]
        conversations_col.insert(conversation)
        return get_json({"success": True, "action": "created first ever conversation and subscribed partner"})


# POST {fb_id: ...}
# GET [partner_id_1, partner_id_2, ...]
@app.route("/api/v1/messages/get-partner-conversations", methods=["POST"])
def get_conversations():
    data = request.json
    fb_id = data["fb_id"]

    conversations_col = mongo.db.conversations
    conversations = conversations_col.find({"fb_id": fb_id})
    return get_json(list(conversations)[0]["partners"])



if __name__ == '__main__':
    if len(sys.argv) > 1:
        env = sys.argv[1]
        if env == "prod":
            app.run(host='0.0.0.0', port=80)
    else:
        app.run(host='0.0.0.0', port=3000)
