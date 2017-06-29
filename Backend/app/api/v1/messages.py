from flask import Blueprint, request
import time

from app.app import mongo
from app.helpers.helper import Helper

messages_endpoint = Blueprint("messages", __name__)


# POST {from_id: ..., to_id: ..., message: "..."}
# GET {success: true}
@messages_endpoint.route('/send-partner-message', methods=["POST"])
def send_partner_message():
    data = request.json

    message = dict()
    message["from_id"] = str(data["from_id"])
    message["to_id"] = str(data["to_id"])
    message["time"] = int(round(time.time() * 1000))
    message["message"] = str(data["message"])

    partner_col = mongo.db.partner_conversations
    partner_col.insert(message)

    Helper.notify_partner_chat_update(data["from_id"], data["to_id"])

    return Helper.get_json({"success": True, "message": message})


# POST {fb_id: ..., message: "..."}
@messages_endpoint.route('/send-locality-message', methods=["POST"])
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

    Helper.notify_locality_chat_update(fb_id)

    return Helper.get_json({"success": True})


# get messages between partners that have matched via roulette
# POST {my_id: ..., partner_id: ...}
# GET [{...},{...}]
@messages_endpoint.route("/get-partner-messages", methods=["POST"])
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

    return Helper.get_json(sorted_list)


# POST {my_id: ..., partner_id: ...}
# GET [{...},{...}]
@messages_endpoint.route("/get-partner-messages-count", methods=["POST"])
def get_partner_messages_count():
    data = request.json
    my_id = str(data["my_id"])
    partner_id = str(data["partner_id"])

    partner_col = mongo.db.partner_conversations
    messages = list(partner_col.find({"participants": [my_id, partner_id]}))

    return Helper.get_json({"count": len(messages)})


# TODO dev
@messages_endpoint.route("/get-messages", methods=["GET"])
def get_all_messages():
    messages = list(mongo.db.conversations.find())
    return Helper.get_json(messages)


@messages_endpoint.route("/get-all-locality-messages", methods=["GET"])
def get_all_locality_messages():
    messages = mongo.db.locality_conversations.find()
    return Helper.get_json(list(messages))


@messages_endpoint.route("/get-all-partner-messages", methods=["GET"])
def get_all_partner_messages():
    messages = mongo.db.partner_conversations.find()
    return Helper.get_json(list(messages))


# get all messages residing within the locality for the user's record provided
# POST {fb_id: ...}
# GET [{...},{...}]
@messages_endpoint.route("/get-locality-messages", methods=["POST"])
def get_locality_messages():
    data = request.json
    fb_id = str(data["fb_id"])
    user = list(mongo.db.users.find({"fb_id": fb_id}))[0]
    locality = str(user["locality"])

    locality_col = mongo.db.locality_conversations

    # get user doc under conversations to see if it's been generated from sending a message yet
    user_querying_blocked_users = list(mongo.db.conversations.find({"fb_id": fb_id}))[0]

    # don't show blocked users' messages
    if "blocked" in user_querying_blocked_users:
        blocked_users = user_querying_blocked_users["blocked"]
    else:
        blocked_users = []

    # aggregate over the messages to get the fb user details
    messages = locality_col.find({"locality": locality, "fb_id": {"$nin": blocked_users}})
    messages = list(messages)

    for i, message in enumerate(messages):
        fb_id = message["fb_id"]
        user = mongo.db.users.find({"fb_id": fb_id})
        user_details = list(user)[0]
        messages[i]["user"] = user_details

    return Helper.get_json(list(messages))


# POST {fb_id: ...}
# GET [partner_id_1, partner_id_2, ...]
@messages_endpoint.route("/get-partner-ids", methods=["POST"])
def get_conversations():
    data = request.json
    fb_id = str(data["fb_id"])

    conversations_col = mongo.db.conversations
    conversations = conversations_col.find({"fb_id": fb_id})

    return Helper.get_json(list(conversations)[0]["partners"])


# POST {my_id: ..., partner_id: ...}
@messages_endpoint.route("/subscribe-partner", methods=["POST"])
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
            return Helper.get_json({"success": True, "action": "already subscribed to partner"})
        else:
            # never talked to you before, just add the id to the list
            conversations_col.update({"fb_id": my_id}, {"$push": {"partners": partner_id}})
            conversations_col.update({"fb_id": partner_id}, {"$push": {"partners": my_id}})
            return Helper.get_json({"success": True, "action": "added new partner to list"})
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

        return Helper.get_json({"success": True, "action": "created first ever conversation and subscribed partner"})


# POST {"my_id":..., "partner_id":...}
@messages_endpoint.route("/unsubscribe-partner", methods=["POST"])
def unsubscribe_user():
    data = request.json
    my_id = str(data["my_id"])
    partner_id = str(data["partner_id"])

    conversations_col = mongo.db.conversations
    conversations_col.update({"fb_id": my_id}, {"$pull": {"partners": partner_id}})
    conversations_col.update({"fb_id": partner_id}, {"$pull": {"partners": my_id}})

    new_subscription_list = conversations_col.find({"fb_id": my_id})

    return Helper.get_json({"success": True, "subscriptions": new_subscription_list})


# POST {fb_id: ...}
# GET [{fb_id:..., last_message_time:..., last_message_content:...}, {...}, ...]
@messages_endpoint.route("/get-past-conversation-previews", methods=["POST"])
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

    return Helper.get_json(sorted_list)
