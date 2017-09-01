import json
from random import randint

from flask import Blueprint, request

from app.app import mongo
from app.helpers.helper import Helper
from app.helpers.geo import Geo

user_endpoint = Blueprint("users", __name__)


class User:
    @staticmethod
    def does_user_exist(fb_id):
        return len(list(mongo.db.users.find({"fb_id": fb_id}))) > 0

    @staticmethod
    def get_user(fb_id):
        results = list(mongo.db.users.find({"fb_id": fb_id}))
        if len(results) > 0:
            return results[0]
        else:
            return None


# POST
# {
# fb_id: <int>, forename: <encoded string>, surname: <encoded string>, gender: [female, male], profile_pic: <url string>
# locality: <string>, lng: <float>, lat: <float>, locality: <encoded string>, show_location: <boolean>
# }
#
# RETURN { success: <boolean>, *reason: <string> }
@user_endpoint.route('/create', methods=["POST"])
def create_user():
    users_col = mongo.db.users
    data = request.json
    data["fb_id"] = str(data["fb_id"])
    data["gender"] = "female" if (data["gender"] == "female") else "male"

    locality = Geo.get_locality(float(data["lat"]), float(data["lng"]))
    data["locality"] = locality["town"]
    data["county"] = locality["county"]

    # blank lists -- will be populated as the user interacts with profiles
    data["blocked"] = []
    data["partners"] = []

    if User.does_user_exist(data["fb_id"]):
        return Helper.get_json({"success": False, "reason": "User already exists"})

    users_col.insert(data)
    return Helper.get_json({"success": True})


# POST { fb_id: <string>, *params: <value> }
# RETURN { success: <boolean> }
@user_endpoint.route("/edit", methods=["POST"])
def edit_user():
    data = request.json
    user = User.get_user(str(data["fb_id"]))

    for key in dict(data):
        user[key] = data[key]

    mongo.db.users.save(user)

    return Helper.get_json({"success": True, "updated_data": user})


# POST { fb_id: <string> }
# RETURN { <user> }
@user_endpoint.route('/get', methods=["POST"])
def get_user():
    data = request.json
    fb_id = str(data["fb_id"])
    user = User.get_user(fb_id)

    if user is not None:
        return Helper.get_json(user)

    return Helper.get_json({"success": False, "reason": "User doesn't exist"})


# POST { fb_id: <string> }
# RETURN [ { ... }, { ... }, ... ]
@user_endpoint.route('get-all', methods=["POST"])
def get_all_users():
    data = request.json
    fb_id = str(data["fb_id"])
    if User.does_user_exist(fb_id):
        return Helper.get_json(mongo.db.users.find())

    return Helper.get_json({"success": False})


# POST { fb_id: <string> }
# RETURN [ <user>, ... ]
@user_endpoint.route('/get-others', methods=["POST"])
def get_other_users():
    data = request.json
    fb_id = str(data["fb_id"])
    other_users = list(mongo.db.users.find({"fb_id": {"$ne": fb_id}, "show_location": True}))
    return Helper.get_json(other_users)


# POST { fb_id: <string> }
# RETURN [ <user>, ... ]
@user_endpoint.route('/get-nearby', methods=["POST"])
def get_nearby_users():
    data = request.json
    fb_id = str(data["fb_id"])

    # find local users and exclude self from lookup
    my_locality = User.get_user(fb_id)["locality"]
    nearby_users = list(mongo.db.users.find({"fb_id": {"$ne": fb_id}, "locality": my_locality}))
    return Helper.get_json(nearby_users)


# POST { fb_id: <string> }
# RETURN { count: <int>, locality: <string> }
@user_endpoint.route('/get-nearby-count', methods=["POST"])
def get_nearby_users_count():
    data = request.json
    fb_id = str(data["fb_id"])

    # find local users and exclude self from lookup
    my_profile = User.get_user(fb_id)
    my_locality = my_profile["locality"]
    excluded_users = my_profile["blocked"]
    excluded_users.append(fb_id)
    nearby_users = list(mongo.db.users.find({"fb_id": {"$nin": excluded_users}, "locality": my_locality}))
    return Helper.get_json({"count": len(nearby_users), "locality": my_locality})


# POST { fb_id: <string> }
# RETURN <user>
@user_endpoint.route('/get-random', methods=["POST"])
def get_random_user():
    data = request.json
    fb_id = str(data["fb_id"])

    # first check to see that you're not the only user on the platform
    partner_choice = list(mongo.db.users.find({"fb_id": {"$ne": fb_id}}))
    if len(partner_choice) > 0:
        # now check that you actually have new people to interact with
        # exclude self and others chatted to already in partners list
        my_profile = list(mongo.db.users.find({"fb_id": fb_id}))[0]
        users_matched = my_profile["partners"]
        users_blocked = my_profile["partners"]

        # have you interacted with someone before?
        # if not, then you have a full range of choice, minus yourself
        if len(users_matched) == 0:
            random_index = randint(0, len(partner_choice) - 1)
            return Helper.get_json(partner_choice[random_index])

        # if so, then exclude them and yourself from lookup
        else:
            users_matched.append(fb_id)
            users = list(mongo.db.users.find({
                "$and": [
                    {"fb_id": {"$nin": users_matched}},
                    {"fb_id": {"$nin": users_blocked}}
                ]}))

            if len(users) > 1:
                random_index = randint(0, len(users))
                return Helper.get_json(users[random_index])
            elif len(users) == 1:
                return Helper.get_json(users[0])
            else:
                return Helper.get_json([])


# POST { fb_id: <string> }
# RETURN  { count: <int> }
@user_endpoint.route("/get-unmatched-count", methods=["POST"])
def get_unmatched_user_count():
    data = request.json
    fb_id = str(data["fb_id"])
    my_profile = User.get_user(fb_id)
    partners_met = my_profile["partners"]
    blocked_users = my_profile["blocked"]

    excluded_profiles = partners_met + blocked_users
    excluded_profiles.append(fb_id)

    # don't show users matched and blocked
    remaining_user_pool = list(mongo.db.users.find({"fb_id": {"$nin": excluded_profiles}}))
    return Helper.get_json({"count": len(remaining_user_pool)})


# POST { fb_id: <string> }
# RETURN { count: <int> }
@user_endpoint.route("/get-matched-count", methods=["POST"])
def get_matched_user_count():
    data = request.json
    fb_id = str(data["fb_id"])
    partners_met = User.get_user(fb_id)["partners"]
    return Helper.get_json({"count": len(partners_met)})


# DELETE { fb_id: <string> }
# RETURN { success: <boolean> }
@user_endpoint.route('/delete', methods=["DELETE"])
def delete_user():
    fb_id = str(request.json["fb_id"])

    # delete the user
    mongo.db.users.remove({"fb_id": fb_id})

    # delete all chats the user was a partner of
    mongo.db.partner_conversations.remove({"$or": [{"to_id": fb_id}, {"from_id": fb_id}]})

    # delete all messages from the user in locality chats
    mongo.db.locality_conversations.remove({"fb_id": fb_id})

    # unsubscribe the user from anyone they were matched with and remove from the blocked list
    query = {"$or": [{"partners": {"$in": [fb_id]}}, {"blocked": {"$in": [fb_id]}}]}
    users_with_relationship = list(mongo.db.users.find(query))
    for user in users_with_relationship:
        mongo.db.users.update({"fb_id": user["fb_id"]}, {"$pull": {"partners": fb_id}})
        mongo.db.users.update({"fb_id": user["fb_id"]}, {"$pull": {"blocked": fb_id}})

    return Helper.get_json({"success": True})


# POST { fb_id: <string>, lat: <float>, lng: <float> }
# RETURN { success: <boolean> }
@user_endpoint.route('/update-location', methods=["POST"])
def update_location():
    data = request.json
    fb_id = str(data["fb_id"])

    user = list(mongo.db.users.find({"fb_id": fb_id}))[0]
    user["lat"] = data["lat"]
    user["lng"] = data["lng"]

    locality = Geo.get_locality(data["lat"], data["lng"])
    user["locality"] = locality["town"]
    user["county"] = locality["county"]

    mongo.db.users.save(user)
    return Helper.get_json({"success": True, "user": user})


# POST { my_id: <string>, partner_id: <string> }
# RETURN { <updated user> }
@user_endpoint.route("/block-user", methods=["POST"])
def block_user():
    data = request.json
    my_id = str(data["my_id"])
    partner_id = str(data["partner_id"])

    # first unsubscribe both users
    mongo.db.users.update({"fb_id": my_id}, {"$pull": {"partners": partner_id}})
    mongo.db.users.update({"fb_id": partner_id}, {"$pull": {"partners": my_id}})

    # now add the partner's id to this user's blocked list
    mongo.db.users.update({"fb_id": my_id}, {"$push": {"blocked": partner_id}})
    return Helper.get_json(User.get_user(my_id))


# POST { fb_id: <string> }
# RETURN [ <blocked fb id>, ... ]
@user_endpoint.route("/get-blocked-users", methods=["POST"])
def get_blocked_users():
    data = request.json
    fb_id = str(data["fb_id"])
    return Helper.get_json(User.get_user(fb_id)["blocked"])


# POST { my_id: <string>, partner_id: <string> }
# RETURN { <updated user data> }
@user_endpoint.route("/unblock-user", methods=["POST"])
def unblock_user():
    data = request.json
    my_id = str(data["my_id"])
    partner_id = str(data["partner_id"])

    # now remove the partner's id from this user's blocked list
    mongo.db.users.update({"fb_id": my_id}, {"$pull": {"blocked": partner_id}})
    return Helper.get_json(User.get_user(my_id))
