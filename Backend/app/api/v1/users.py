import json
from random import randint

from flask import Blueprint, request

from app.app import mongo
from app.helpers.helper import Helper

user_endpoint = Blueprint("users", __name__)


# POST {fb_id: 123456789, ...}
# GET {success: true} / {success:false, reason: "User already exists"}
@user_endpoint.route('/create', methods=["POST"])
def create_user():
    users_col = mongo.db.users
    data = request.json
    data["fb_id"] = str(data["fb_id"])
    data["locality"] = Helper.get_locality(float(data["lat"]), float(data["lng"]))
    data["blocked"] = []
    data["partners"] = []
    fb_id = data["fb_id"]

    users_found = users_col.find({"fb_id": str(fb_id)})
    exists = users_found.count() > 0

    if exists:
        return Helper.get_json({"success": False, "reason": "User already exists"})
    else:
        users_col.insert(data)
        return Helper.get_json({"success": True})


# POST {fb_id: ..., ...}
# GET {success:true}
@user_endpoint.route("/edit", methods=["POST"])
def edit_user():
    users_col = mongo.db.users
    data = request.json
    fb_id = str(data["fb_id"])

    user = users_col.find({"fb_id": fb_id})
    user = list(user)[0]

    for key in dict(data):
        user[key] = data[key]

    users_col.save(user)

    return Helper.get_json({"success": True, "updated_data": user})


# POST {fb_id: 123456789}
# GET {_id: ..., forename: ..., ...}
# get random person, get me, get individuals for map, get user info for chat, get people in locality
@user_endpoint.route('/get', methods=["POST"])
def get_user():
    data = request.json
    fb_id = str(data["fb_id"])

    users_col = mongo.db.users
    user = users_col.find({"fb_id": fb_id})
    is_existing = user.count() > 0

    if is_existing:
        return Helper.get_json(list(user)[0])
    else:
        return Helper.get_json({"success": False, "reason": "fb_id doesn't exist"})


# POST {fb_id:123456789}
# GET [{}]
@user_endpoint.route('/get-all', methods=["GET"])
def get_all_users():
    users = mongo.db.users.find()
    return Helper.get_json(users)


# POST {fb_id:123456789}
# GET [{}]
@user_endpoint.route('/get-others', methods=["POST"])
def get_other_users():
    data = request.json
    fb_id = str(data["fb_id"])
    return Helper.get_json(mongo.db.users.find({"fb_id": {"$ne": fb_id}, "show_location": True}))


# POST {fb_id:123456789}
# GET [{...}]
@user_endpoint.route('/get-nearby', methods=["POST"])
def get_nearby_users():
    data = request.json
    fb_id = str(data["fb_id"])

    # find local users and exclude self from lookup
    users_col = mongo.db.users
    this_user = list(users_col.find({"fb_id": fb_id}))[0]
    nearby_users = users_col.find({"fb_id": {"$ne": fb_id}, "locality": this_user["locality"]})
    return Helper.get_json(nearby_users)


# POST {fb_id:123456789}
@user_endpoint.route('/get-nearby-count', methods=["POST"])
def get_nearby_users_count():
    data = request.json
    fb_id = str(data["fb_id"])

    # find local users and exclude self from lookup
    users_col = mongo.db.users
    this_user = list(users_col.find({"fb_id": fb_id}))[0]
    locality = this_user["locality"]
    nearby_users = users_col.find({"fb_id": {"$ne": fb_id}, "locality": locality})
    return Helper.get_json({"count": nearby_users.count(), "locality": locality})


# POST {fb_id:123456789}
@user_endpoint.route('/get-random', methods=["POST"])
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
        return Helper.get_json(user)

    else:
        # append self too to exclude self matching
        blocked_users = list(partners_met)[0]
        if "blocked" in blocked_users:
            blocked_users = blocked_users["blocked"]
        else:
            blocked_users = []

        partners_met = list(partners_met)[0]
        if "partners" in partners_met:
            partners_met = partners_met["partners"]
        else:
            partners_met = []

        partners_met.append(fb_id)
        users = users_col.find({"$and": [{"fb_id": {"$nin": partners_met}}, {"fb_id": {"$nin": blocked_users}}]})

        if users.count() == 0:
            return Helper.get_json({"success": False, "reason": "Out of new users"})
        elif users.count() == 1:
            return Helper.get_json(list(users)[0])
        else:
            user = users[randint(0, users.count() - 1)]
            return Helper.get_json(user)


# POST {fb_id:...}
# GET {count:...}
@user_endpoint.route("/get-unmatched-count", methods=["POST"])
def get_unmatched_user_count():
    data = request.json
    fb_id = str(data["fb_id"])
    users_col = mongo.db.users
    partners_met = list(mongo.db.conversations.find({"fb_id": fb_id}))
    blocked_users = []

    if len(partners_met) > 0:
        if "blocked" in partners_met[0]:
            blocked_users = partners_met[0]["blocked"]
        else:
            blocked_users = []

        if "partners" in partners_met[0]:
            partners_met = partners_met[0]["partners"]
        else:
            partners_met = []

    partners_met.append(fb_id)
    users = users_col.find({"$and": [{"fb_id": {"$nin": partners_met}}, {"fb_id": {"$nin": blocked_users}}]})
    return Helper.get_json({"count": users.count()})


# POST {fb_id:...}
# GET {count:...}
@user_endpoint.route("/get-matched-count", methods=["POST"])
def get_matched_user_count():
    data = request.json
    fb_id = str(data["fb_id"])
    partners_met = list(mongo.db.conversations.find({"fb_id": fb_id}))[0]

    if "partners" in partners_met:
        partners_met = partners_met["partners"]
    else:
        partners_met = []

    return Helper.get_json({"count": len(partners_met)})


# DELETE {fb_id: 123456789}
@user_endpoint.route('/delete', methods=["DELETE"])
def delete_user():
    users_col = mongo.db["users"]
    data = json.loads(request.data)
    fb_id = str(data["fb_id"])
    users_col.remove({"fb_id": fb_id})
    return Helper.get_json({"success": True})


# POST {fb_id: 123456789, lat: ..., lng: ...}
@user_endpoint.route('/update-location', methods=["POST"])
def update_location():
    data = request.json
    fb_id = str(data["fb_id"])

    users_col = mongo.db.users
    user = list(users_col.find({"fb_id": fb_id}))[0]
    user["lat"] = data["lat"]
    user["lng"] = data["lng"]
    user["locality"] = Helper.get_locality(data["lat"], data["lng"])

    users_col.save(user)
    return Helper.get_json({"success": True, "user": user})


# POST {"my_id":..., "partner_id":...}
@user_endpoint.route("/block-user", methods=["POST"])
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

    return Helper.get_json({"success": True, "user": my_profile})


# POST {fb_id: ...}
@user_endpoint.route("/get-blocked-users", methods=["POST"])
def get_blocked_users():
    data = request.json
    fb_id = str(data["fb_id"])

    conversations_col = mongo.db.conversations
    users = conversations_col.find({"fb_id": fb_id})

    if users.count() == 0:
        return Helper.get_json([])

    users = list(users)[0]["blocked"]
    return Helper.get_json(users)


# POST {"my_id":..., "block_id":...}
@user_endpoint.route("/unblock-user", methods=["POST"])
def unblock_user():
    data = request.json
    my_id = str(data["my_id"])
    partner_id = str(data["partner_id"])

    conversations_col = mongo.db.conversations

    # now add the partner's id to this user's blocked list
    conversations_col.update({"fb_id": my_id}, {"$pull": {"blocked": partner_id}})
    my_profile = list(conversations_col.find({"fb_id": my_id}))[0]

    return Helper.get_json({"success": True, "user": my_profile})
