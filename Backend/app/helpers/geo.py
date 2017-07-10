import math

from app.helpers.datasets import Datasets


class Geo:
    @staticmethod
    def get_distance(my_lat, my_lng, town_lat, town_lng):
        return math.fabs(math.sqrt((town_lat - my_lat) ** 2 + (town_lng - my_lng) ** 2))

    @staticmethod
    def get_locality(lat, lng):
        dataset = Datasets.get_groomed_populated_areas()

        nearest_town = 0
        shortest_distance = 0

        for i, town in enumerate(dataset):
            town_lat = town["lat"]
            town_lng = town["lng"]
            distance = Geo.get_distance(lat, lng, town_lat, town_lng)

            if i == 0:
                shortest_distance = distance
                nearest_town = town["town"]

            if distance < shortest_distance:
                shortest_distance = distance
                nearest_town = town["town"]

        return nearest_town

    @staticmethod
    def add_dist_to_lat(dist_in_km, lat):
        return lat + (dist_in_km / 110.574)

    @staticmethod
    def add_dist_to_lng(dist_in_km, lng):
        return lng + (111.320 * math.cos(dist_in_km))
