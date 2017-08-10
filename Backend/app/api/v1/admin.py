import json

from flask import Blueprint, request

admin_endpoint = Blueprint("admin", __name__)


@admin_endpoint.route("/get-maintenance-mode", methods=["GET"])
def maintenance_mode():
    with open("../../datasets/admin.json", "r") as f:
        data = json.loads(f.read())
        return data["maintenance_mode"]


@admin_endpoint.route("/set-maintenance-mode", methods=["POST"])
def set_maintenance_mode():
    received = request.json

    with open("../../datasets/admin.json", "r") as f:
        data = json.loads(f.read())

    data["maintenance_mode"] = received["maintenance_mode"]
    with open("../../datasets/admin.json", "w") as f:
        json.dump(data, f)
