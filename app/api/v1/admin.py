from flask import Blueprint, request
import flask_login

from app.app import mongo
from app.helpers.helper import Helper
from app.helpers.fcm import FCM

import os
from urllib import parse

admin_endpoint = Blueprint("admin", __name__)


# TODO remove -- debug function
@admin_endpoint.route("/clear-dud-accounts", methods=["POST"])
def clear_dud_accounts():
    if Admin.authenticate_user(request.json):
        users = list(mongo.db.users.find())
        for user in users:
            if "fb_id" not in user:
                mongo.db.users.remove(user)

        return Helper.get_json(list(mongo.db.users.find()))

    return Helper.get_json({"success": False})


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

    return Helper.get_json({"success": False})


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

    return Helper.get_json({"success": False})


# TODO may not be necessary now but could be useful for ML grooming of learning about user interaction
# TODO or also with user blocks/admin intervention -- currently unimplemented
# TODO may also be a breach of privacy
@admin_endpoint.route("/get-all-partner-conversations", methods=["POST"])
def get_all_partner_conversations():
    if Admin.authenticate_user(request.json):
        messages = list(mongo.db.partner_conversations.find())
        match_ids = []
        output = {}

        for message in messages:
            conversation_couple = [message["from_id"], message["to_id"]]
            if conversation_couple not in match_ids:
                match_ids.append(conversation_couple)

        return Helper.get_json(match_ids)

    return Helper.get_json({"success": False})


@admin_endpoint.route("/broadcast-push-notification", methods=["POST"])
def broadcast_push_notification():
    if Admin.authenticate_user(request.json):
        data = request.json
        title = data["push_notification_title"]
        content = data["push_notification_content"]
        link = data["push_notification_link"]

        return FCM.notify_push_notification(title, content, link)

    return Helper.get_json({"success": False})


class Admin(flask_login.UserMixin):
    def __init__(self, id):
        self.id = id

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
        count_users_24_hours = mongo.db.users.find({"last_active": {"$gt": Admin.get_time_24_hours_ago()}}).count()

        county_count = {}
        locality_count = {}

        localities = []
        counties = []

        for user in total_users:
            locality = parse.unquote_plus(user["locality"])
            county = parse.unquote_plus(user["county"])
            if locality not in localities:
                localities.append(locality + " (" + user["county"] + ")")
            if county not in counties:
                counties.append(county)

        for locality in localities:
            locality_count[locality] = 0

        for county in counties:
            county_count[county] = 0

        for user in total_users:
            for locality in localities:
                if parse.unquote_plus(user["locality"]) + " (" + user["county"] + ")" == locality:
                    locality_count[locality] += 1
            for county in counties:
                if parse.unquote_plus(user["county"]) == county:
                    county_count[county] += 1

        return {
            "count_users_last_24_hours": count_users_24_hours,
            "count_users_total": count_users_total,
            "count_per_county": county_count,
            "count_per_locality": locality_count
        }

    @staticmethod
    def get_message_stats():
        partner_message_count_24_hours = mongo.db.partner_conversations.find(
            {"time": {"$gt": Admin.get_time_24_hours_ago()}}).count()
        locality_message_count_24_hours = mongo.db.locality_conversations.find(
            {"time": {"$gt": Admin.get_time_24_hours_ago()}}).count()

        partner_message_count = mongo.db.partner_conversations.find().count()
        locality_message_count = mongo.db.locality_conversations.find().count()

        user_count = mongo.db.users.find().count()
        users_active_last_24_hours = mongo.db.users.find(
            {"last_active": {"$gt": Admin.get_time_24_hours_ago()}}).count()

        users_who_enacted_blocks = list(mongo.db.users.find({"blocked": {"$ne": []}}))
        user_block_count = 0
        for user in users_who_enacted_blocks:
            user_block_count += len(user["blocked"])

        return {
            "partner_message_count": partner_message_count,
            "locality_message_count": locality_message_count,
            "total_message_count": partner_message_count + locality_message_count,
            "partner_message_count_24_hours": partner_message_count_24_hours,
            "locality_message_count_24_hours": locality_message_count_24_hours,
            "total_message_count_24_hours": partner_message_count_24_hours + locality_message_count_24_hours,
            "user_count": user_count,
            "users_active_last_24_hours": users_active_last_24_hours,
            "user_block_count": user_block_count
        }
