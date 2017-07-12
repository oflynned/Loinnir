import json

from app.helpers.helper import Helper


class Datasets:
    @staticmethod
    def get_fcm_api_key(mode):
        with open(Helper.get_path(mode), "r") as f:
            data = json.loads(f.read())
            return data["fcm_api_key"]

    @staticmethod
    def get_places_api_key(mode):
        with open(Helper.get_path(mode), "r") as f:
            data = json.loads(f.read())
            return data["places_api_key"]

    @staticmethod
    def get_populated_areas():
        with open("app/datasets/populated_areas.json", "r") as f:
            data = json.loads(f.read())
            return data["features"]

    @staticmethod
    def open_json_file(file_name):
        with open("app/datasets/" + file_name + ".json", "r") as f:
            return json.loads(f.read())

    @staticmethod
    def groom_population_dataset():
        dataset = Datasets.get_populated_areas()
        groomed_set = []

        for item in dataset:
            town = item["properties"]["NAMN1"]
            town_lng = item["geometry"]["coordinates"][0]
            town_lat = item["geometry"]["coordinates"][1]
            groomed_set.append({"town": town, "lat": town_lat, "lng": town_lng})

        return groomed_set

    @staticmethod
    def get_groomed_populated_areas():
        with open("app/datasets/groomed_populated_areas_localised.json", "r") as f:
            return json.loads(f.read())
