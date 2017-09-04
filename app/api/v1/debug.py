from flask import Blueprint

from app.app import mongo
from app.helpers.helper import Helper

debug_endpoint = Blueprint("debug", __name__)


@debug_endpoint.route("/get-all-partner-conversations", methods=["GET"])
def get_all_partner_conversations():
    return Helper.get_json(list(mongo.db.partner_conversations.find({})))


@debug_endpoint.route("/get-all-locality-conversations", methods=["GET"])
def get_all_locality_conversations():
    return Helper.get_json(list(mongo.db.locality_conversations.find({})))


@debug_endpoint.route("/get-all-users", methods=["GET"])
def get_all_users():
    return Helper.get_json(list(mongo.db.users.find({})))
