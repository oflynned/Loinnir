from flask import Flask, render_template, request, Response
from flask_pymongo import PyMongo
from bson import json_util
from random import randint
from flask_pyfcm import FCM
import json, os, sys, time, math

from Helper import Helper

frontend_dir = os.path.abspath("../Frontend")
static_dir = os.path.abspath("../Frontend/static")

app = Flask(__name__, template_folder=frontend_dir, static_folder=static_dir)
app.config["MONGO_DBNAME"] = "loinnir"
app.config["MONGO_URI"] = "mongodb://localhost:27017/loinnir"
app.debug = True

# persistence
mongo = PyMongo(app)

# real time messaging
app.config["FCM_API_KEY"] = Helper.get_fcm_api_key()
fcm = FCM()
fcm.init_app(app)

# TODO
"""
    unread/read messages
    dispatch notifications
    past conversations preview
    notify individuals via fcm
    implement fcm server
    account deletion
    reporting? when should an auto ban occur?
    auto generate password to protect public api by tokens
    
    refactor by moving to blueprints, this is getting too long to comfortably code
"""


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


@app.route('/api/v1', methods=["GET", "POST"])
def hello_world():
    return get_json({"response": "hello world!"})


@app.route('/api/v1/get-array', methods=["GET", "POST"])
def hello_world_array():
    response = []
    for i in range(0, 10):
        response.append({"response": i})

    return get_json(response)


# POST {fb_id: 123456789, ...}
# GET {success: true} / {success:false, reason: "User already exists"}
@app.route('/api/v1/users/create', methods=["POST"])
def create_user():
    users_col = mongo.db.users
    data = request.json
    data["fb_id"] = str(data["fb_id"])
    data["locality"] = get_locality(float(data["lat"]), float(data["lng"]))
    fb_id = data["fb_id"]

    users_found = users_col.find({"fb_id": str(fb_id)})
    exists = users_found.count() > 0

    if exists:
        return get_json({"success": False, "reason": "User already exists"})
    else:
        users_col.insert(data)
        return get_json({"success": True})


# POST {fb_id: ..., ...}
# GET {success:true}
@app.route("/api/v1/users/edit", methods=["POST"])
def edit_user():
    users_col = mongo.db.users
    data = request.json
    fb_id = str(data["fb_id"])

    user = users_col.find({"fb_id": fb_id})
    user = list(user)[0]

    for key in dict(data):
        user[key] = data[key]

    users_col.save(user)

    return get_json({"success": True, "updated_data": user})


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
    return get_json(mongo.db.users.find({"fb_id": {"$ne": fb_id}, "show_location": True}))


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
@app.route('/api/v1/users/get-nearby-count', methods=["POST"])
def get_nearby_users_count():
    data = request.json
    fb_id = str(data["fb_id"])

    # find local users and exclude self from lookup
    users_col = mongo.db.users
    this_user = list(users_col.find({"fb_id": fb_id}))[0]
    locality = this_user["locality"]
    nearby_users = users_col.find({"fb_id": {"$ne": fb_id}, "locality": locality})
    return get_json({"count": nearby_users.count(), "locality": locality})


# POST {fb_id:123456789}
@app.route('/api/v1/users/get-random', methods=["POST"])
def get_random_user():
    data = request.json
    fb_id = str(data["fb_id"])

    # exclude self and others chatted to already in partners list
    users_col = mongo.db.users
    partners_met = list(mongo.db.conversations.find({"fb_id": fb_id}))

    if len(partners_met) == 0:
        users = users_col.find({"fb_id": {"$ne": fb_id}})
        count = mongo.db.users.count() - 2
        user = users[randint(0, count)]
        return get_json(user)

    else:
        # append self too to exclude self matching
        blocked_users = list(partners_met)
        if len(blocked_users) > 0:
            blocked_users = blocked_users[0]["blocked"]

        partners_met = list(partners_met)
        if len(partners_met) > 0:
            partners_met = partners_met[0]["partners"]

        partners_met.append(fb_id)
        users = users_col.find({"$and": [{"fb_id": {"$nin": partners_met}}, {"fb_id": {"$nin": blocked_users}}]})

        if users.count() == 0:
            return get_json({"success": False, "reason": "Out of new users"})
        elif users.count() == 1:
            return get_json(list(users)[0])
        else:
            user = users[randint(0, users.count() - 1)]
            return get_json(user)


# POST {fb_id:...}
# GET {count:...}
@app.route("/api/v1/users/get-unmatched-count", methods=["POST"])
def get_unmatched_user_count():
    data = request.json
    fb_id = str(data["fb_id"])
    users_col = mongo.db.users
    partners_met = list(mongo.db.conversations.find({"fb_id": fb_id}))
    blocked_users = []

    if len(partners_met) > 0:
        blocked_users = partners_met[0]["blocked"]
        partners_met = partners_met[0]["partners"]

    partners_met.append(fb_id)
    users = users_col.find({"$and": [{"fb_id": {"$nin": partners_met}}, {"fb_id": {"$nin": blocked_users}}]})
    return get_json({"count": users.count()})


# POST {fb_id:...}
# GET {count:...}
@app.route("/api/v1/users/get-matched-count", methods=["POST"])
def get_matched_user_count():
    data = request.json
    fb_id = str(data["fb_id"])
    partners_met = list(mongo.db.conversations.find({"fb_id": fb_id}))
    if len(partners_met) > 0:
        partners_met = partners_met[0]["partners"]

    return get_json({"count": len(partners_met)})


# DELETE {fb_id: 123456789}
@app.route('/api/v1/users/delete', methods=["DELETE"])
def delete_user():
    users_col = mongo.db["users"]
    data = json.loads(request.data)
    fb_id = str(data["fb_id"])
    users_col.remove({"fb_id": fb_id})
    return get_json({"success": True})


# POST {lat: ..., lng: ...}
# GET {locality: ...}
@app.route("/api/v1/services/get-nearest-town", methods=["POST"])
def get_nearest_town():
    data = request.json
    lat = data["lat"]
    lng = data["lng"]

    return get_json({"locality": get_locality(lat, lng)})


def groom_population_dataset():
    dataset = Helper.get_populated_areas()
    groomed_set = []

    for item in dataset:
        town = item["properties"]["NAMN1"]
        town_lng = item["geometry"]["coordinates"][0]
        town_lat = item["geometry"]["coordinates"][1]
        groomed_set.append({"town": town, "lat": town_lat, "lng": town_lng})

    return groomed_set


def get_distance(my_lat, my_lng, town_lat, town_lng):
    return math.fabs(math.sqrt((town_lat - my_lat) ** 2 + (town_lng - my_lng) ** 2))


def get_locality(lat, lng):
    dataset = Helper.get_groomed_populated_areas()

    nearest_town = 0
    shortest_distance = 0

    for i, town in enumerate(dataset):
        town_lat = town["lat"]
        town_lng = town["lng"]
        distance = get_distance(lat, lng, town_lat, town_lng)

        if i == 0:
            shortest_distance = distance
            nearest_town = town["town"]

        if distance < shortest_distance:
            shortest_distance = distance
            nearest_town = town["town"]

    return nearest_town


# POST {fb_id: 123456789, lat: ..., lng: ...}
@app.route('/api/v1/users/update-location', methods=["POST"])
def update_location():
    data = request.json
    fb_id = str(data["fb_id"])

    users_col = mongo.db.users
    user = list(users_col.find({"fb_id": fb_id}))[0]
    user["lat"] = data["lat"]
    user["lng"] = data["lng"]
    user["locality"] = get_locality(data["lat"], data["lng"])

    users_col.save(user)
    return get_json({"success": True, "user": user})


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
    return get_json({"success": True, "message": message})


# POST {fb_id: ..., message: "..."}
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


# get messages between partners that have matched via roulette
# POST {my_id: ..., partner_id: ...}
# GET [{...},{...}]
@app.route("/api/v1/messages/get-partner-messages", methods=["POST"])
def get_partner_messages():
    data = request.json
    my_id = str(data["my_id"])
    partner_id = str(data["partner_id"])

    partner_col = mongo.db.partner_conversations
    participants = [my_id, partner_id]
    query = {"from_id": {"$in": participants}, "to_id": {"$in": participants}}
    messages = list(partner_col.find(query).sort("time", -1))

    returned_messages = []

    for message in messages:
        user = list(mongo.db.users.find({"fb_id": message["from_id"]}))[0]
        returned_messages.append({"message": message, "user": user})

    # sort by descending time
    sorted_list = sorted(returned_messages, key=lambda k: k["message"]["time"], reverse=False)

    return get_json(sorted_list)


# POST {my_id: ..., partner_id: ...}
# GET [{...},{...}]
@app.route("/api/v1/messages/get-partner-messages-count", methods=["POST"])
def get_partner_messages_count():
    data = request.json
    my_id = str(data["my_id"])
    partner_id = str(data["partner_id"])

    partner_col = mongo.db.partner_conversations
    messages = list(partner_col.find({"participants": [my_id, partner_id]}))

    return get_json({"count": len(messages)})


# TODO dev
@app.route("/api/v1/messages/get-messages", methods=["GET"])
def get_all_messages():
    messages = list(mongo.db.conversations.find())
    return get_json(messages)


# get all messages residing within the locality for the user's record provided
# POST {fb_id: ...}
# GET [{...},{...}]
@app.route("/api/v1/messages/get-locality-messages", methods=["POST"])
def get_locality_messages():
    data = request.json
    fb_id = str(data["fb_id"])
    user = list(mongo.db.users.find({"fb_id": fb_id}))[0]
    locality = str(user["locality"])

    locality_col = mongo.db.locality_conversations

    # get user doc under conversations to see if it's been generated from sending a message yet
    blocked_users = list(mongo.db.conversations.find({"fb_id": fb_id}))

    # don't show blocked users' messages
    if len(blocked_users) > 0:
        blocked_users = blocked_users[0]["blocked"]

    # aggregate over the messages to get the fb user details
    messages = locality_col.find({"locality": locality, "fb_id": {"$nin": blocked_users}})
    messages = list(messages)

    for i, message in enumerate(messages):
        fb_id = message["fb_id"]
        user = mongo.db.users.find({"fb_id": fb_id})
        user_details = list(user)[0]
        messages[i]["user"] = user_details

        # temp disable this as I'm not sure how much info I need to provide for messages

        # messages[i]["user"] = dict()
        # messages[i]["user"]["name"] = user_details["name"]
        # messages[i]["user"]["profile_pic"] = user_details["profile_pic"]

    return get_json(list(messages))


# POST {fb_id: ...}
# GET [partner_id_1, partner_id_2, ...]
@app.route("/api/v1/messages/get-partner-ids", methods=["POST"])
def get_conversations():
    data = request.json
    fb_id = str(data["fb_id"])

    conversations_col = mongo.db.conversations
    conversations = conversations_col.find({"fb_id": fb_id})

    return get_json(list(conversations)[0]["partners"])


# POST {my_id: ..., partner_id: ...}
@app.route("/api/v1/messages/subscribe-partner", methods=["POST"])
def subscribe_conversations():
    data = request.json
    my_id = str(data["my_id"])
    partner_id = str(data["partner_id"])

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
            conversations_col.update({"fb_id": partner_id}, {"$push": {"partners": my_id}})
            return get_json({"success": True, "action": "added new partner to list"})
    else:
        # first conversation ever, update both fb_id and partner list
        conversation = dict()
        conversation["fb_id"] = my_id
        conversation["partners"] = [partner_id]
        conversations_col.insert(conversation)

        conversation = dict()
        conversation["fb_id"] = partner_id
        conversation["partners"] = [my_id]
        conversations_col.insert(conversation)

        return get_json({"success": True, "action": "created first ever conversation and subscribed partner"})


# POST {"my_id":..., "partner_id":...}
@app.route("/api/v1/messages/unsubscribe-partner", methods=["POST"])
def unsubscribe_user():
    data = request.json
    my_id = str(data["my_id"])
    partner_id = str(data["partner_id"])

    conversations_col = mongo.db.conversations
    conversations_col.update({"fb_id": my_id}, {"$pull": {"partners": partner_id}})
    conversations_col.update({"fb_id": partner_id}, {"$pull": {"partners": my_id}})

    new_subscription_list = conversations_col.find({"fb_id": my_id})

    return get_json({"success": True, "subscriptions": new_subscription_list})


# POST {"my_id":..., "partner_id":...}
@app.route("/api/v1/users/block-user", methods=["POST"])
def block_user():
    data = request.json
    my_id = str(data["my_id"])
    partner_id = str(data["partner_id"])

    conversations_col = mongo.db.conversations

    # first unsubscribe both users
    conversations_col.update({"fb_id": my_id}, {"$pull": {"partners": partner_id}})
    conversations_col.update({"fb_id": partner_id}, {"$pull": {"partners": my_id}})

    # now add the partner's id to this user's blocked list
    conversations_col.update({"fb_id": my_id}, {"$push": {"blocked": partner_id}})
    my_profile = list(conversations_col.find({"fb_id": my_id}))[0]

    return get_json({"success": True, "user": my_profile})


# POST {fb_id: ...}
@app.route("/api/v1/users/get-blocked-users", methods=["POST"])
def get_blocked_users():
    data = request.json
    fb_id = str(data["fb_id"])

    conversations_col = mongo.db.conversations
    users = conversations_col.find({"fb_id": fb_id})

    if users.count() == 0:
        return get_json([])

    users = list(users)[0]["blocked"]
    return get_json(users)


# POST {"my_id":..., "block_id":...}
@app.route("/api/v1/users/unblock-user", methods=["POST"])
def unblock_user():
    data = request.json
    my_id = str(data["my_id"])
    partner_id = str(data["partner_id"])

    conversations_col = mongo.db.conversations

    # now add the partner's id to this user's blocked list
    conversations_col.update({"fb_id": my_id}, {"$pull": {"blocked": partner_id}})
    my_profile = list(conversations_col.find({"fb_id": my_id}))[0]

    return get_json({"success": True, "user": my_profile})


# POST {fb_id: ...}
# GET [{fb_id:..., last_message_time:..., last_message_content:...}]
@app.route("/api/v1/messages/get-past-conversation-previews", methods=["POST"])
def get_conversations_previews():
    data = request.json
    fb_id = str(data["fb_id"])

    conversations_col = mongo.db.conversations
    partners = list(conversations_col.find({"fb_id": fb_id}))
    if len(partners) > 0:
        partners = partners[0]["partners"]
    messages_preview = []

    for partner in partners:
        # check if only one message exists in the conversation
        messages_col = mongo.db.partner_conversations
        my_messages_query = {"$and": [{"from_id": {"$in": [fb_id]}}, {"to_id": {"$in": [partner]}}]}
        partner_messages_query = {"$and": [{"from_id": {"$in": [partner]}}, {"to_id": {"$in": [fb_id]}}]}

        messages_from_me = messages_col.find(my_messages_query)
        messages_from_partner = messages_col.find(partner_messages_query)

        my_messages_count = messages_from_me.count()
        partner_messages_count = messages_from_partner.count()

        # remember that a connection is only made on sending a message
        # both being 0 shouldn't be possible if they're partners

        if my_messages_count > 0 and partner_messages_count == 0:
            # I sent messages but no replies were sent back
            last_message_in_chat = list(messages_from_me.sort("time", -1).limit(1))[0]
        elif my_messages_count == 0 and partner_messages_count > 0:
            # partner sent me messages and I haven't replied
            last_message_in_chat = list(messages_from_partner.sort("time", -1).limit(1))[0]
        else:
            # both parties have communicated with each other
            query = {"$and": [{"to_id": {"$in": [fb_id, partner]}}, {"from_id": {"$in": [fb_id, partner]}}]}
            last_message_in_chat = list(messages_col.find(query).sort("time", -1).limit(1))[0]

        user_details = list(mongo.db.users.find({"fb_id": partner}))[0]
        messages_preview.append({"message": last_message_in_chat, "user": user_details})

    # sort list by last sent time of the message fragments
    sorted_list = sorted(messages_preview, key=lambda k: k["message"]["time"], reverse=False)

    return get_json(sorted_list)


@app.route("/api/v1/services/create-individual-notification", methods=["POST"])
def generate_notification():
    data = request.json


@app.route("/api/v1/services/notify-chat-update", methods=["POST"])
def notify_chat_update():
    pass


if __name__ == '__main__':
    if len(sys.argv) > 1:
        env = sys.argv[1]
        if env == "prod":
            app.run(host='0.0.0.0', port=80)
    else:
        app.run(host='0.0.0.0', port=3000)
