import os
import json

from app.app import mongo


class Datasets:
    @staticmethod
    def get_fcm_api_key():
        return os.environ["FCM_API_KEY"]

    @staticmethod
    def get_places_api_key():
        return os.environ["PLACES_API_KEY"]

    @staticmethod
    def open_json_file(file_name):
        with open("app/datasets/" + file_name + ".json", "r") as f:
            return json.loads(f.read())

    @staticmethod
    def get_groomed_populated_areas():
        with open("app/datasets/groomed_populated_areas_localised.json", "r") as f:
            return json.loads(f.read())

    @staticmethod
    def get_locality_names():
        data = Datasets.get_groomed_populated_areas()
        output = []
        for area in data:
            output.append(area["town"])

        return output

    @staticmethod
    def get_county_names():
        data = Datasets.get_groomed_populated_areas()
        output = []
        for area in data:
            output.append(area["county"])

        return output

    @staticmethod
    def get_area_names():
        data = Datasets.get_groomed_populated_areas()
        output = []
        for area in data:
            locality_count = len(list(mongo.db.users.find({"locality": area["town"]})))
            county_count = len(list(mongo.db.users.find({"county": area["county"]})))

            output.append({
                "locality": area["town"],
                "county": area["county"],
                "locality_count": locality_count,
                "county_count": county_count
            })

        count = len(list(mongo.db.users.find({"locality": "abroad"})))
        output.append({
            "locality": "Abroad",
            "county": "abroad",
            "locality_count": count,
            "county_county": count
        })

        return output
