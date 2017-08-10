import os
import json

from app.helpers.helper import Helper


class Datasets:
    @staticmethod
    def get_fcm_api_key():
        if Helper.is_prod_environ():
            return os.environ["FCM_API_KEY"]

        with open(Helper.get_path(), "r") as f:
            data = json.loads(f.read())
            return data["fcm_api_key"]

    @staticmethod
    def get_places_api_key():
        if Helper.is_prod_environ():
            return os.environ["PLACES_API_KEY"]

        with open(Helper.get_path(), "r") as f:
            data = json.loads(f.read())
            return data["places_api_key"]

    @staticmethod
    def open_json_file(file_name):
        with open("app/datasets/" + file_name + ".json", "r") as f:
            return json.loads(f.read())

    @staticmethod
    def get_groomed_populated_areas():
        with open("app/datasets/groomed_populated_areas_localised.json", "r") as f:
            return json.loads(f.read())
