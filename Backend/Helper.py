import json


class Helper:
    @staticmethod
    def get_fcm_api_key():
        with open("../../../../loinnir_auth.json", "r") as f:
            data = json.loads(f.read())
            return data["fcm_api_key"]

    @staticmethod
    def get_places_api_key():
        with open("../../../../loinnir_auth.json", "r") as f:
            data = json.loads(f.read())
            return data["places_api_key"]

    @staticmethod
    def get_populated_areas():
        with open("./populated_areas.json", "r") as f:
            data = json.loads(f.read())
            # returns json array
            return data["features"]

    @staticmethod
    def get_groomed_populated_areas():
        with open("./groomed_populated_areas_localised.json", "r") as f:
            return json.loads(f.read())
