from flask import Blueprint, request

services_endpoint = Blueprint("services", __name__)


# POST {lat: ..., lng: ...}
# GET {locality: ...}
@services_endpoint.route("/api/v1/services/get-nearest-town", methods=["POST"])
def get_nearest_town():
    data = request.json
    lat = data["lat"]
    lng = data["lng"]

    return get_json({"locality": get_locality(lat, lng)})
