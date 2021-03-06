import os
from urllib import parse

from flask import Blueprint, request

from app.api.v1.users import User
from app.app import mongo
from app.helpers.datasets import Datasets
from app.helpers.fcm import FCM
from app.helpers.helper import Helper

admin_endpoint = Blueprint("admin", __name__)


@admin_endpoint.route("/clear-locality-chats", methods=["POST"])
def clear_locality_chats():
    if Admin.authenticate_user(request.json):
        messages = list(mongo.db.locality_conversations.find())
        for message in messages:
            mongo.db.locality_conversations.remove(message)

        return Helper.get_json({"success": True})

    return Helper.get_json({"success": False, "reason": "not_authenticated"})


# POST { username: <string>, secret: <string>, locality: <string> }
@admin_endpoint.route("/clear-locality-chat", methods=["POST"])
def clear_locality_chat():
    if Admin.authenticate_user(request.json):
        mongo.db.locality_conversations.remove({"locality": request.json["locality"]})
        return Helper.get_json({"success": True})

    return Helper.get_json({"success": False, "reason": "not_authenticated"})


@admin_endpoint.route("/locality-messages-last-24-hours", methods=["POST"])
def get_locality_messages_last_24_hours():
    if Admin.authenticate_user(request.json):
        messages = list(mongo.db.locality_conversations.find({"time": {"$gt": Admin.get_time_24_hours_ago()}}))
        localities = []
        output = {}

        for message in messages:
            locality = parse.unquote_plus(message["locality"])
            if locality not in localities and len(locality) > 0:
                localities.append(locality)

        for locality in localities:
            output[locality] = []

        for message in messages:
            for locality in localities:
                if parse.unquote_plus(message["locality"]) == locality:
                    message["locality"] = locality
                    output[locality].append(message)

        return Helper.get_json(output)

    return Helper.get_json({"success": False, "reason": "not_authenticated"})


@admin_endpoint.route("/get-area-data", methods=["POST"])
def get_locality_names():
    if Admin.authenticate_user(request.json):
        return Helper.get_json(Datasets.get_area_names())

    return Helper.get_json({"success": False, "reason": "not_authenticated"})


@admin_endpoint.route("/get-user-stats", methods=["POST"])
def get_user_stats():
    if Admin.authenticate_user(request.json):
        return Helper.get_json(Admin.get_user_stats())

    return Helper.get_json({"success": False, "reason": "not_authenticated"})


@admin_endpoint.route("/get-message-stats", methods=["POST"])
def get_message_stats():
    if Admin.authenticate_user(request.json):
        return Helper.get_json(Admin.get_message_stats())

    return Helper.get_json({"success": False, "reason": "not_authenticated"})


@admin_endpoint.route("/get-hidden-location-count", methods=["POST"])
def get_hidden_location_count():
    if Admin.authenticate_user(request.json):
        users_hidden_location = mongo.db.users.find({"show_location": False}).count()
        users_showing_location = mongo.db.users.find({"show_location": True}).count()
        total_user_count = mongo.db.users.find().count()

        return Helper.get_json({
            "hidden_count": users_hidden_location,
            "visible_count": users_showing_location,
            "total_count": total_user_count
        })

    return Helper.get_json({"success": False, "reason": "not_authenticated"})


# POST { username: <string>, secret: <string>, locality: <string> }
# RETURN [ <message>, <message>, ... ]
@admin_endpoint.route("/get-locality-chat-by-name", methods=["POST"])
def get_locality_chat_by_name():
    if Admin.authenticate_user(request.json):
        locality = parse.unquote_plus(request.json["locality"])
        messages = list(mongo.db.locality_conversations.find({"locality": locality}))
        output = []

        for message in messages:
            message["user"] = User.get_user(message["fb_id"])
            output.append(message)

        return Helper.get_json(output)

    return Helper.get_json({"success": False, "reason": "not_authenticated"})


@admin_endpoint.route("/get-partner-id-pairs", methods=["POST"])
def get_partner_id_pairs():
    if Admin.authenticate_user(request.json):
        users = list(mongo.db.users.find())
        user_pairs = []
        for user in users:
            for partner_id in user["partners"]:
                user_pairs.append({user["fb_id"], partner_id})

        # map-reduce list of tuples
        reduced_set = set(map(tuple, user_pairs))
        output = map(list, reduced_set)

        return Helper.get_json(output)

    return Helper.get_json({"success": False, "reason": "not_authenticated"})


# POST { username: <string>, secret: <string>, participants: [ <id>, <id> ] }
@admin_endpoint.route("/get-partner-id-pair-conversation", methods=["POST"])
def get_partner_id_pair_conversation():
    if Admin.authenticate_user(request.json):
        participants = list(request.json["participants"])
        query = {"from_id": {"$in": participants}, "to_id": {"$in": participants}}
        messages = list(mongo.db.partner_conversations.find(query).sort("time", -1))
        output = []

        for message in messages:
            message["user"] = User.get_user(message["from_id"])
            output.append(message)

        return Helper.get_json(output)

    return Helper.get_json({"success": False, "reason": "not_authenticated"})


@admin_endpoint.route("/get-all-locality-conversations", methods=["POST"])
def get_all_locality_conversations():
    if Admin.authenticate_user(request.json):
        messages = list(mongo.db.locality_conversations.find())
        localities = []
        output = {}

        for message in messages:
            locality = parse.unquote_plus(message["locality"])
            if locality not in localities and len(locality) > 0:
                localities.append(locality)

        for locality in localities:
            output[locality] = []

        for message in messages:
            for locality in localities:
                if parse.unquote_plus(message["locality"]) == locality:
                    output[locality].append(message)

        return Helper.get_json(output)

    return Helper.get_json({"success": False, "reason": "not_authenticated"})


@admin_endpoint.route("/get-past-push-notifications", methods=["POST"])
def get_past_push_notifications():
    if Admin.authenticate_user(request.json):
        notifications = list(mongo.db.push_notifications.find())
        return Helper.get_json(notifications)

    return Helper.get_json({"success": False, "reason": "not_authenticated"})


# POST
# push_notification_title: <string>, push_notification_content: <string>, push_notification_link: <string>
@admin_endpoint.route("/broadcast-push-notification", methods=["POST"])
def broadcast_push_notification():
    if Admin.authenticate_user(request.json):
        data = request.json
        title = data["push_notification_title"]
        content = data["push_notification_content"]
        link = data["push_notification_link"]
        users_at_this_time = mongo.db.users.find().count()

        notification = {
            "title": title,
            "content": content,
            "link": link,
            "user_count_at_this_time": users_at_this_time,
            "user_count_delivered_to": 0,
            "user_count_interacted_with": 0,
            "broadcast_time": Helper.get_current_time_in_millis()
        }

        mongo.db.push_notifications.save(notification)
        notification = list(mongo.db.push_notifications.find({"broadcast_time": notification["broadcast_time"]}))[0]

        return FCM.notify_push_notification(title, content, link, str(notification["_id"]))

    return Helper.get_json({"success": False, "reason": "not_authenticated"})


# POST
# push_notification_title: <string>, push_notification_content: <string>,
# push_notification_link: <string>, push_notification_target_fb_id: <string>
@admin_endpoint.route("/broadcast-push-notification-to-id", methods=["POST"])
def broadcast_push_notification_to_id():
    if Admin.authenticate_user(request.json):
        data = request.json
        title = data["push_notification_title"]
        content = data["push_notification_content"]
        link = data["push_notification_link"]
        users_at_this_time = mongo.db.users.find().count()

        notification = {
            "title": title,
            "content": content,
            "link": link,
            "user_count_at_this_time": users_at_this_time,
            "user_count_delivered_to": 0,
            "user_count_interacted_with": 0,
            "broadcast_time": Helper.get_current_time_in_millis()
        }

        mongo.db.push_notifications.save(notification)
        notification = list(mongo.db.push_notifications.find({"broadcast_time": notification["broadcast_time"]}))[0]
        return FCM.notify_singular_push_notification(title, content, link, str(notification["_id"]),
                                                     User.get_user(data["push_notification_target_fb_id"]))

    return Helper.get_json({"success": False, "reason": "not_authenticated"})


# POST
# push_notification_title: <string>, push_notification_content: <string>,
# push_notification_link: <string>, push_notification_target_user_filter: <string>
@admin_endpoint.route("/broadcast-push-notification-to-target-group", methods=["POST"])
def broadcast_push_notification_to_target_group():
    if Admin.authenticate_user(request.json):
        data = request.json
        title = data["push_notification_title"]
        content = data["push_notification_content"]
        link = data["push_notification_link"]
        target_user_filter = parse.unquote_plus(data["push_notification_target_user_filter"])
        users_at_this_time = mongo.db.users.find().count()

        print(request.json)

        notification = {
            "title": title,
            "content": content,
            "link": link,
            "user_count_at_this_time": users_at_this_time,
            "user_count_delivered_to": 0,
            "user_count_interacted_with": 0,
            "broadcast_time": Helper.get_current_time_in_millis()
        }

        mongo.db.push_notifications.save(notification)
        notification = list(mongo.db.push_notifications.find({"broadcast_time": notification["broadcast_time"]}))[0]

        return FCM.notify_push_notification(title, content, link, str(notification["_id"]),
                                            {"$and": [{"county": target_user_filter}, {"fcm_id": {"$ne": 0}}]})

    return Helper.get_json({"success": False, "reason": "not_authenticated"})


@admin_endpoint.route("/authenticate", methods=["POST"])
def authenticate_admin_user():
    return Helper.get_json({"success": Admin.authenticate_user(request.json)})


class Admin:
    @staticmethod
    def get_time_24_hours_ago():
        twenty_four_hours = 1000 * 60 * 60 * 24
        return Helper.get_current_time_in_millis() - twenty_four_hours

    @staticmethod
    def authenticate_fields(username, secret):
        return username == os.environ["ADMIN_USERNAME"] and secret == os.environ["ADMIN_SECRET"]

    @staticmethod
    def authenticate_user(payload):
        given_username = payload["username"]
        given_secret = payload["secret"]
        return given_username == os.environ["ADMIN_USERNAME"] and given_secret == os.environ["ADMIN_SECRET"]

    @staticmethod
    def get_user_stats():
        total_users = list(mongo.db.users.find())
        count_users_total = len(total_users)
        users_active_last_24_hours = mongo.db.users.find(
            {"last_active": {"$gt": Admin.get_time_24_hours_ago()}}).count()

        users_who_enacted_blocks = list(mongo.db.users.find({"blocked": {"$ne": []}}))
        user_block_count = 0
        for user in users_who_enacted_blocks:
            user_block_count += len(user["blocked"])

        county_count = {}
        locality_count = {}

        localities = []
        counties = []

        for user in total_users:
            locality = parse.unquote_plus(user["locality"])
            county = parse.unquote_plus(user["county"])
            if locality not in localities:
                localities.append(locality)
            if county not in counties:
                counties.append(county)

        for locality in localities:
            locality_count[locality] = 0

        for county in counties:
            county_count[county] = 0

        for user in total_users:
            for locality in localities:
                if parse.unquote_plus(user["locality"]) == locality:
                    locality_count[locality] += 1
            for county in counties:
                if parse.unquote_plus(user["county"]) == county:
                    county_count[county] += 1

        return {
            "count_users_total": count_users_total,
            "count_per_county": county_count,
            "count_per_locality": locality_count,
            "users_active_last_24_hours": users_active_last_24_hours,
            "user_block_count": user_block_count
        }

    @staticmethod
    def get_message_stats():
        partner_message_count_24_hours = mongo.db.partner_conversations.find({
            "$and": [
                {"time": {"$gt": Admin.get_time_24_hours_ago()}},
                {"from_id": {"$nin": ["1433224973407916", "1686100871401476"]}},
                {"to_id": {"$nin": ["1433224973407916", "1686100871401476"]}}
            ]}).count()

        locality_message_count_24_hours = mongo.db.locality_conversations.find({
            "$and": [
                {"time": {"$gt": Admin.get_time_24_hours_ago()}},
                {"fb_id": {"$nin": ["1433224973407916", "1686100871401476"]}}
            ]}).count()

        partner_message_count = mongo.db.partner_conversations.find({
            "$and": [
                {"from_id": {"$nin": ["1433224973407916", "1686100871401476"]}},
                {"to_id": {"$nin": ["1433224973407916", "1686100871401476"]}}
            ]}).count()

        locality_message_count = mongo.db.locality_conversations.find({
            "fb_id": {"$nin": ["1433224973407916", "1686100871401476"]}
        }).count()

        return {
            "partner_message_count": partner_message_count,
            "locality_message_count": locality_message_count,
            "total_message_count": partner_message_count + locality_message_count,
            "partner_message_count_24_hours": partner_message_count_24_hours,
            "locality_message_count_24_hours": locality_message_count_24_hours,
            "total_message_count_24_hours": partner_message_count_24_hours + locality_message_count_24_hours
        }
