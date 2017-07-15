from flask import Blueprint, request
from bson import ObjectId

from Loinnir import mode
from app.api.v1.users import User
from app.app import mongo
from app.helpers.fcm import FCM
from app.helpers.helper import Helper

messages_endpoint = Blueprint("messages", __name__)


# POST { from_id: <string>, to_id: <string>, message: <string> }
# RETURN { success: <boolean> }
@messages_endpoint.route('/send-partner-message', methods=["POST"])
def send_partner_message():
    data = request.json

    message = {
        "from_id": str(data["from_id"]),
        "to_id": str(data["to_id"]),
        "time": Helper.get_current_time_in_millis(),
        "message": str(data["message"]),
        "was_seen": False
    }

    mongo.db.partner_conversations.insert(message)
    FCM.notify_partner_chat_update(data["from_id"], data["to_id"], mode)

    return Helper.get_json({"success": True})


# POST { fb_id: <string>, message: <string> }
# RETURN { success: <boolean> }
@messages_endpoint.route('/send-locality-message', methods=["POST"])
def send_locality_message():
    data = request.json

    fb_id = str(data["fb_id"])
    users_col = mongo.db.users
    user = users_col.find({"fb_id": fb_id})
    user = list(user)[0]
    locality = user["locality"]

    message = {
        "fb_id": str(data["fb_id"]),
        "locality": str(locality),
        "time": Helper.get_current_time_in_millis(),
        "message": str(data["message"])
    }

    mongo.db.locality_conversations.insert(message)
    FCM.notify_locality_chat_update(fb_id, mode)

    return Helper.get_json({"success": True})


# used to determine whether to match and subscribe the users or not
# POST { my_id: <string>, partner_id: <string> }
# RETURN { count: <int> }
@messages_endpoint.route("/get-partner-messages-count", methods=["POST"])
def get_partner_messages_count():
    data = request.json
    my_id = str(data["my_id"])
    partner_id = str(data["partner_id"])

    messages = list(mongo.db.partner_conversations.find({"participants": [my_id, partner_id]}))
    return Helper.get_json({"count": len(messages)})


# get messages between partners that have matched via roulette
# POST { my_id: <string>, partner_id: <string> }
# RETURN [ <message>, ... ]
@messages_endpoint.route("/get-partner-messages", methods=["POST"])
def get_partner_messages():
    data = request.json
    my_id = str(data["my_id"])
    partner_id = str(data["partner_id"])

    participants = [my_id, partner_id]
    query = {"from_id": {"$in": participants}, "to_id": {"$in": participants}}
    messages = list(mongo.db.partner_conversations.find(query).sort("time", -1))

    returned_messages = []

    for message in messages:
        returned_messages.append({"message": message, "user": User.get_user(message["from_id"])})

    # sort by descending time
    sorted_list = sorted(returned_messages, key=lambda k: k["message"]["time"], reverse=False)

    return Helper.get_json(sorted_list)


# POST { my_id: <string>, partner_id: <string>, oldest_message_id: <string> }
# RETURN [ <message>, ... ]
@messages_endpoint.route("/get-paginated-partner-messages", methods=["POST"])
def get_paginated_partner_messages():
    data = request.json
    my_id = str(data["my_id"])
    partner_id = str(data["partner_id"])
    oldest_message_id = str(data["oldest_message_id"])

    participants = [my_id, partner_id]
    query = {
        "from_id": {"$in": participants},
        "to_id": {"$in": participants},
        "_id": {"$lt": ObjectId(oldest_message_id)}
    }
    total_messages = list(mongo.db.partner_conversations.find(query).sort("_id", -1).limit(25))

    returned_messages = []
    for message in total_messages:
        returned_messages.append({"message": message, "user": User.get_user(message["from_id"])})

    sorted_list = sorted(returned_messages, key=lambda k: k["message"]["time"], reverse=False)

    return Helper.get_json(sorted_list)


# POST { fb_id: <string>, last_message_id: <string> }
# RETURN [ <message>, ... ]
@messages_endpoint.route("/get-paginated-locality-messages", methods=["POST"])
def get_paginated_locality_messages():
    data = request.json
    my_id = str(data["fb_id"])
    oldest_message_id = str(data["oldest_message_id"])

    me = User.get_user(my_id)

    query = {
        "locality": me["locality"],
        "fb_id": {"$nin": me["blocked"]},
        "_id": {"$lt": ObjectId(oldest_message_id)}
    }
    total_messages = list(mongo.db.partner_conversations.find(query).sort("_id", -1).limit(25))

    returned_messages = []
    for message in total_messages:
        returned_messages.append({"message": message, "user": User.get_user(message["from_id"])})

    sorted_list = sorted(returned_messages, key=lambda k: k["message"]["time"], reverse=False)

    return Helper.get_json(sorted_list)


# get all messages residing within the locality for the user's record provided
# comes with initial pagination of 25 messages
# POST { fb_id: <string> }
# RETURN [ <message>, ... ]
@messages_endpoint.route("/get-locality-messages", methods=["POST"])
def get_locality_messages():
    data = request.json
    fb_id = str(data["fb_id"])

    user = User.get_user(fb_id)
    locality = str(user["locality"])
    blocked_users = user["blocked"]

    # aggregate over the messages to get the fb user details
    messages = list(mongo.db.locality_conversations.find({
        "locality": locality,
        "fb_id": {"$nin": blocked_users}
    }).sort("time", -1).limit(25))

    for i, message in enumerate(messages):
        fb_id = message["fb_id"]
        user = User.get_user(fb_id)
        messages[i]["user"] = user

    sorted_list = sorted(list(messages), key=lambda k: k["time"], reverse=False)
    return Helper.get_json(sorted_list)


# POST { my_id: <string>, partner_id: <string> }
# RETURN { success: <boolean> }
@messages_endpoint.route("/mark-seen", methods=["POST"])
def mark_message_seen():
    data = request.json
    my_id = data["my_id"]
    partner_id = data["partner_id"]
    curr_time = Helper.get_current_time_in_millis()

    query = {"from_id": partner_id, "to_id": my_id, "time": {"$lte": curr_time}}
    messages = list(mongo.db.partner_conversations.find(query).sort("time", -1))

    for message in messages:
        message["was_seen"] = True
        mongo.db.partner_conversations.save(message)

    return Helper.get_json({"success": True})


# POST { fb_id: <string> }
# RETURN [ <fb id>, ... ]
@messages_endpoint.route("/get-partner-ids", methods=["POST"])
def get_conversations():
    data = request.json
    fb_id = str(data["fb_id"])
    partner_ids = User.get_user(fb_id)["partners"]
    return Helper.get_json(partner_ids)


# POST { my_id: <string>, partner_id: <string> }
# RETURN { success: <boolean> }
@messages_endpoint.route("/subscribe-partner", methods=["POST"])
def subscribe_conversations():
    data = request.json
    my_id = str(data["my_id"])
    partner_id = str(data["partner_id"])

    if partner_id not in User.get_user(my_id)["partners"]:
        mongo.db.users.update({"fb_id": my_id}, {"$push": {"partners": partner_id}})

    if my_id not in User.get_user(partner_id)["partners"]:
        mongo.db.users.update({"fb_id": partner_id}, {"$push": {"partners": my_id}})

    return Helper.get_json({"success": True})


# POST { my_id: <string>, partner_id: <string> }
# RETURN { success: <boolean>, partners: [ <fb id>, ... ] }
@messages_endpoint.route("/unsubscribe-partner", methods=["POST"])
def unsubscribe_user():
    data = request.json
    my_id = str(data["my_id"])
    partner_id = str(data["partner_id"])

    mongo.db.users.update({"fb_id": my_id}, {"$pull": {"partners": partner_id}})
    mongo.db.users.update({"fb_id": partner_id}, {"$pull": {"partners": my_id}})

    new_partners_list = User.get_user(my_id)["partners"]
    return Helper.get_json({"success": True, "partners": new_partners_list})


# POST { fb_id: <string> }
# RETURN [ { <message>, <user> }, ... ]
@messages_endpoint.route("/get-past-conversation-previews", methods=["POST"])
def get_conversations_previews():
    data = request.json
    fb_id = str(data["fb_id"])

    partners = User.get_user(fb_id)["partners"]
    messages_preview = []

    for partner in partners:
        # check if only one message exists in the conversation
        my_messages_query = {"$and": [{"from_id": {"$in": [fb_id]}}, {"to_id": {"$in": [partner]}}]}
        partner_messages_query = {"$and": [{"from_id": {"$in": [partner]}}, {"to_id": {"$in": [fb_id]}}]}

        messages_from_me = mongo.db.partner_conversations.find(my_messages_query)
        messages_from_partner = mongo.db.partner_conversations.find(partner_messages_query)

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
            last_message_in_chat = list(mongo.db.partner_conversations.find(query).sort("time", -1).limit(1))[0]

        # now get the count of unread messages
        unread_messages = list(
            mongo.db.partner_conversations.find({"from_id": partner, "to_id": fb_id, "was_seen": False}))
        messages_preview.append(
            {"count": len(unread_messages), "message": last_message_in_chat, "user": User.get_user(partner)})

    # sort list by last sent time of the message fragments
    sorted_list = sorted(messages_preview, key=lambda k: k["message"]["time"], reverse=True)

    return Helper.get_json(sorted_list)


# POST { my_id: <string>, partner_id: <string> }
# RETURN { <message>, <user> }
@messages_endpoint.route("/get-partner-conversation-preview", methods=["POST"])
def get_partner_conversation_preview():
    data = request.json
    my_id = str(data["my_id"])
    partner_id = str(data["partner_id"])  # check if only one message exists in the conversation

    my_messages_query = {"$and": [{"from_id": {"$in": [my_id]}}, {"to_id": {"$in": [partner_id]}}]}
    partner_messages_query = {"$and": [{"from_id": {"$in": [partner_id]}}, {"to_id": {"$in": [my_id]}}]}

    messages_from_me = mongo.db.partner_conversations.find(my_messages_query)
    messages_from_partner = mongo.db.partner_conversations.find(partner_messages_query)

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
        query = {"$and": [{"to_id": {"$in": [my_id, partner_id]}}, {"from_id": {"$in": [my_id, partner_id]}}]}
        last_message_in_chat = list(mongo.db.partner_conversations.find(query).sort("time", -1).limit(1))[0]

    return Helper.get_json({"message": last_message_in_chat, "user": User.get_user(partner_id)})
