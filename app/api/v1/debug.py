from flask import Blueprint, request

from app.app import mongo
from app.helpers.helper import Helper
from app.api.v1.admin import Admin

debug_endpoint = Blueprint("debug", __name__)


@debug_endpoint.route("/get-all-partner-conversations", methods=["POST"])
def get_all_partner_conversations():
    if Admin.authenticate_user(request.json):
        return Helper.get_json(list(mongo.db.partner_conversations.find()))
    return Helper.get_json({"success": False})


@debug_endpoint.route("/get-all-locality-conversations", methods=["POST"])
def get_all_locality_conversations():
    if Admin.authenticate_user(request.json):
        return Helper.get_json(list(mongo.db.locality_conversations.find()))
    return Helper.get_json({"success": False})


@debug_endpoint.route("/get-all-users", methods=["POST"])
def get_all_users():
    if Admin.authenticate_user(request.json):
        return Helper.get_json(list(mongo.db.users.find()))
    return Helper.get_json({"success": False})


@debug_endpoint.route("/get-all-push-notifications", methods=["POST"])
def get_all_push_notifications():
    if Admin.authenticate_user(request.json):
        return Helper.get_json(list(mongo.db.push_notifications.find()))
    return Helper.get_json({"success": False})
