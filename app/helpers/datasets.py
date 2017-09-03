import os
import json


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
