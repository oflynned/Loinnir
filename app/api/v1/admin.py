from flask import Blueprint, request

from app.app import mongo
from app.helpers.helper import Helper

import os

admin_endpoint = Blueprint("admin", __name__)


@admin_endpoint.route("/active-users-last-24-hours", methods=["POST"])
def get_active_users_last_24_hours():
    if authenticate_user(request.json):
        query = {"last_active": {"$gt": get_time_24_hours_ago()}}
        count = mongo.db.users.find(query).count()
        return Helper.get_json({"count": int(count)})

    return Helper.get_json({"success": False})


# TODO remove -- debug function
@admin_endpoint.route("/clear-dud-accounts", methods=["POST"])
def clear_dud_accounts():
    if authenticate_user(request.json):
        users = list(mongo.db.users.find())
        for user in users:
            if "fb_id" not in user:
                mongo.db.users.remove(user)

        return Helper.get_json(list(mongo.db.users.find()))

    return Helper.get_json({"success": False})


@admin_endpoint.route("/total-user-count", methods=["POST"])
def get_total_user_count():
    if authenticate_user(request.json):
        count = mongo.db.users.find().count()
        return Helper.get_json({"count": int(count)})

    return Helper.get_json({"success": False})


def get_users_per_locality():
    pass


def get_users_per_county():
    pass


@admin_endpoint.route("/message-stats", methods=["POST"])
def get_message_stats():
    if authenticate_user(request.json):
        partner_message_count_24_hours = mongo.db.partner_conversations.find(
            {"time": {"gt": get_time_24_hours_ago()}}).count()
        locality_message_count_24_hours = mongo.db.locality_conversations.find(
            {"time": {"gt": get_time_24_hours_ago()}}).count()
        partner_message_count = mongo.db.partner_conversations.find().count()
        locality_message_count = mongo.db.locality_conversations.find().count()

        return Helper.get_json(
            {
                "partner_message_count": partner_message_count,
                "locality_message_count": locality_message_count,
                "total_message_count": partner_message_count + locality_message_count,
                "partner_message_count_24_hours": partner_message_count_24_hours,
                "locality_message_count_24_hours": locality_message_count_24_hours,
                "total_message_count_24_hours": partner_message_count_24_hours + locality_message_count_24_hours,
                "time_24_hours_ago": Helper.get_current_time_in_millis() - get_time_24_hours_ago()
            }
        )

    return Helper.get_json({"success": False})


@admin_endpoint.route("/locality-messages-last-24-hours", methods=["POST"])
def get_locality_messages_last_24_hours():
    if authenticate_user(request.json):
        messages = list(mongo.db.locality_conversations.find({"time": {"gt": get_time_24_hours_ago()}}))
        output = {}

        for message in messages:
            pass

        return Helper.get_json(output)

    return Helper.get_json({"success": False})


@admin_endpoint.route("/total-message-count", methods=["POST"])
def get_total_message_count():
    if authenticate_user(request.json):
        locality_messages_count = mongo.db.locality_conversations.find().count()
        partner_messages_count = mongo.db.partner_conversations.find().count()
        total_count = locality_messages_count + partner_messages_count

        return Helper.get_json({
            "locality_messages_count": locality_messages_count,
            "partner_messages_count": partner_messages_count,
            "total_count": total_count
        })

    return Helper.get_json({"success": False})


@admin_endpoint.route("/get-all-locality-conversations", methods=["POST"])
def get_all_locality_conversations():
    if authenticate_user(request.json):
        count = mongo.db.locality_conversations.find().count()
        return Helper.get_json({"count": count})

    return Helper.get_json({"success": False})


def get_time_24_hours_ago():
    twenty_four_hours = 1000 * 60 * 60 * 24
    return Helper.get_current_time_in_millis() - twenty_four_hours


def authenticate_user(payload):
    given_username = payload["username"]
    given_secret = payload["secret"]
    return given_username == os.environ["ADMIN_USERNAME"] and given_secret == os.environ["ADMIN_SECRET"]
