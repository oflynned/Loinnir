from flask import Blueprint, request
from app.helpers.helper import Helper

services_endpoint = Blueprint("services", __name__)


# POST {lat: ..., lng: ...}
# GET {locality: ...}
@services_endpoint.route("/get-nearest-town", methods=["POST"])
def get_nearest_town():
    data = request.json
    lat = data["lat"]
    lng = data["lng"]

    return Helper.get_json({"locality": Helper.get_locality(lat, lng)})
