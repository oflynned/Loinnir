import json

import requests
from flask import Blueprint, request

from app.api.v1.users import User
from app.app import mongo
from app.api.v1.admin import Admin
from app.helpers.datasets import Datasets
from app.helpers.fake_datasets import FakeDatasets
from app.helpers.geo import Geo
from app.helpers.helper import Helper

services_endpoint = Blueprint("services", __name__)


# POST { suggestion: <string>, version: <string>, time: <long> }
@services_endpoint.route("/send-suggestion", methods=["POST"])
def send_suggestion():
    data = request.json
    data["time"] = Helper.get_current_time_in_millis()
    mongo.db.suggestions.save(data)
    return Helper.get_json({"success": True})


# POST { username: <string>, secret: <string> }
@services_endpoint.route("/get-suggestions", methods=["POST"])
def get_suggestions():
    if Admin.authenticate_user(request.json):
        suggestions = list(mongo.db.suggestions.find())
        output = []

        for suggestion in suggestions:
            user = User.get_user(suggestion["fb_id"])
            suggestion["user"] = user
            del suggestion["fb_id"]
            output.append(suggestion)

        return Helper.get_json(output)

    return Helper.get_json({"success": False})


# POST { lat: <float>, lng: <float> }
# RETURN { locality: <string>, county: <string> }
@services_endpoint.route("/get-nearest-town", methods=["POST"])
def get_nearest_town():
    data = request.json
    lat = data["lat"]
    lng = data["lng"]
    locality = Geo.get_locality(lat, lng)
    return Helper.get_json({"locality": locality["town"], "county": locality["county"]})


@services_endpoint.route("/get-fake-users", methods=["GET"])
def get_fake_users():
    return Helper.get_json(FakeDatasets.generate_fake_users())


@services_endpoint.route("/create-fake-users", methods=["GET"])
def create_fake_users():
    for user in FakeDatasets.generate_fake_users():
        user["partners"] = []
        user["blocked"] = []
        mongo.db.users.insert(user)

    return Helper.get_json({"success": True})


@services_endpoint.route("/create-all-counties-fake-users", methods=["GET"])
def create_all_counties_fake_users():
    for user in FakeDatasets.generate_all_counties_fake_users():
        user["partners"] = []
        user["blocked"] = []
        mongo.db.users.insert(user)

    return Helper.get_json({"success": True})


@services_endpoint.route("/get-all-counties-fake-users", methods=["GET"])
def get_all_counties_fake_users():
    return Helper.get_json(FakeDatasets.generate_all_counties_fake_users())


class Services:
    @staticmethod
    def groom_ni_towns():
        with open("app/datasets/ni_towns.txt", "r") as f:
            data = str(f.read()).split("\n")

        counties_hash = {
            "Antrim": "Aontroim",
            "Armagh": "Ard Mhacha",
            "Carlow": "Ceatharlach",
            "Cavan": "An Cabhán",
            "Clare": "An Clár",
            "Cork": "Corcaigh",
            "Derry": "Doire",
            "Donegal": "Dún na nGall",
            "Down": "An Dún",
            "Dublin": "Áth Cliath",
            "Fermanagh": "Fear Manach",
            "Galway": "Gaillimh",
            "Kerry": "Ciarraí",
            "Kildare": "Cill Dara",
            "Kilkenny": "Cill Ceannaigh",
            "Laois": "Laois",
            "Leitrim": "Liatroim",
            "Limerick": "Luimneach",
            "Longford": "An Longfort",
            "Louth": "Lú",
            "Mayo": "Maigh Eo",
            "Meath": "An Mhí",
            "Monaghan": "Muineachán",
            "Offaly": "Uíbh Fhailí",
            "Roscommon": "Ros Comáin",
            "Sligo": "Sligeach",
            "Tipperary": "Tiobráid Árann",
            "Tyrone": "Tír Eoghain",
            "Waterford": "Port Láirge",
            "Westmeath": "An Iarmhí",
            "Wexford": "Loch Garman",
            "Wicklow": "Cill Mhantáin"
        }

        output = []

        for item in data:
            dataset = item.split("/")
            output.append({
                "town": dataset[2],
                "lat": float(dataset[3]),
                "lng": float(dataset[4]),
                "county": counties_hash[dataset[0]]
            })

        with open('output.json', 'w') as f:
            f.write(json.dumps(output))

    @staticmethod
    def bisort_alphabetically():
        data = Datasets.open_json_file("groomed_populated_areas_localised")
        sorted_data = sorted(data, key=lambda k: (str(k["county"]), str(k["town"])))

        with open("app/datasets/groomed_populated_areas_localised.json", "w") as f:
            f.write(json.dumps(sorted_data))

    @staticmethod
    def groom_counties():
        data = Datasets.open_json_file("groomed_populated_areas_localised")
        counties = []

        counties_hash = {
            "Antrim": "Aontroim",
            "Armagh": "Ard Mhacha",
            "Carlow": "Ceatharlach",
            "Cavan": "An Cabhán",
            "Clare": "An Clár",
            "Cork": "Corcaigh",
            "Derry": "Doire",
            "Donegal": "Dún na nGall",
            "Down": "An Dún",
            "Dublin": "Áth Cliath",
            "Fermanagh": "Fear Manach",
            "Galway": "Gaillimh",
            "Kerry": "Ciarraí",
            "Kildare": "Cill Dara",
            "Kilkenny": "Cill Ceannaigh",
            "Laois": "Laois",
            "Leitrim": "Liatroim",
            "Limerick": "Luimneach",
            "Longford": "An Longfort",
            "Louth": "Lú",
            "Mayo": "Maigh Eo",
            "Meath": "An Mhí",
            "Monaghan": "Muineachán",
            "Offaly": "Uíbh Fhailí",
            "Roscommon": "Ros Comáin",
            "Sligo": "Sligeach",
            "Tipperary": "Tiobráid Árann",
            "Tyrone": "Tír Eoghain",
            "Waterford": "Port Láirge",
            "Westmeath": "An Iarmhí",
            "Wexford": "Loch Garman",
            "Wicklow": "Cill Mhantáin"
        }

        for i in range(len(data)):
            locale = data[i]
            lat = locale["lat"]
            lng = locale["lng"]
            url = "https://maps.googleapis.com/maps/api/geocode/json?" + \
                  "latlng=" + str(lat) + "," + str(lng) + \
                  "&key=AIzaSyDKoWbfgPBfKVEgOiyG5EcSwU-ldqbN1NQ"

            r = requests.get(url)
            r = json.loads(r.text)
            relevant_data = r["results"][0]["address_components"]

            is_found = False

            for component in relevant_data:
                if not is_found:
                    component_name = str(component["long_name"]).strip()
                    if component_name[:6] == "County":
                        component_name = component_name[7:].strip()

                    if component_name in counties_hash.keys():
                        county = counties_hash[str(component_name)]
                        counties.append(county)
                        is_found = True

        output = []
        for i in range(len(data) - 1):
            location = data[i]
            location["county"] = counties[i - 1]
            output.append(location)

        with open("app/datasets/groomed_populated_areas_localised.json", "w") as f:
            f.write(json.dumps(output))
