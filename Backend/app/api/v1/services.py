from flask import Blueprint, request
from app.helpers.helper import Helper
from app.app import mongo

services_endpoint = Blueprint("services", __name__)


# POST {lat: ..., lng: ...}
# GET {locality: ...}
@services_endpoint.route("/get-nearest-town", methods=["POST"])
def get_nearest_town():
    data = request.json
    lat = data["lat"]
    lng = data["lng"]

    return Helper.get_json({"locality": Helper.get_locality(lat, lng)})


@services_endpoint.route("/get-fake-users", methods=["GET"])
def get_fake_users():
    return Helper.get_json(Helper.generate_fake_users())


@services_endpoint.route("/create-fake-users", methods=["GET"])
def create_fake_users():
    for user in Helper.generate_fake_users():
        mongo.db.users.insert(user)

    return Helper.get_json({"success": True})
