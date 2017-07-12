from flask import Blueprint, request

import json
import requests
import time

from app.helpers.geo import Geo
from app.helpers.datasets import Datasets
from app.helpers.fake_datasets import FakeDatasets
from app.helpers.helper import Helper
from app.app import mongo

services_endpoint = Blueprint("services", __name__)


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
        mongo.db.users.insert(user)

    return Helper.get_json({"success": True})


class Services:
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
            "Kilkenny": "Cill Cheannaigh",
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

            # https://maps.googleapis.com/maps/api/geocode/json?latlng=,&key=AIzaSyDKoWbfgPBfKVEgOiyG5EcSwU-ldqbN1NQ

            r = requests.get(url)
            r = json.loads(r.text)
            relevant_data = r["results"][0]["address_components"]

            is_found = False

            for component in relevant_data:
                if not is_found:
                    component_name = str(component["long_name"]).strip()
                    if component_name[:6] == "County":
                        component_name = component_name[7:].strip()
                        print("Contains county!", component_name, component_name in counties_hash)

                    print(component_name)
                    if component_name in counties_hash.keys():
                        county = counties_hash[str(component_name)]
                        counties.append(county)
                        print(str(i) + "/" + str(len(data) - 1), locale["town"], county, "\n")
                        is_found = True

        print(counties)

        output = []
        for i in range(len(data) - 1):
            location = data[i]
            location["county"] = counties[i]
            output.append(location)

        with open("app/datasets/groomed_populated_areas_localised.json", "w") as f:
            f.write(json.dumps(output))
