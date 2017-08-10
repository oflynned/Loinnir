import math

from app.helpers.datasets import Datasets


class Geo:
    @staticmethod
    def get_distance(my_lat, my_lng, town_lat, town_lng):
        return math.fabs(math.sqrt((town_lat - my_lat) ** 2 + (town_lng - my_lng) ** 2))

    @staticmethod
    def get_locality(lat, lng):
        if not Geo._is_abroad(lat, lng):
            dataset = Datasets.get_groomed_populated_areas()

            county = 0
            nearest_town = 0
            shortest_distance = 0

            for i, town in enumerate(dataset):
                town_lat = town["lat"]
                town_lng = town["lng"]
                distance = Geo.get_distance(lat, lng, town_lat, town_lng)

                if i == 0:
                    shortest_distance = distance
                    nearest_town = town["town"]
                    county = town["county"]

                if distance < shortest_distance:
                    shortest_distance = distance
                    nearest_town = town["town"]
                    county = town["county"]

            return {"town": nearest_town, "county": county}

        else:
            return {"town": "abroad", "county": "abroad"}

    @staticmethod
    def _get_country_name():
        pass

    @staticmethod
    def _is_abroad(lat, lng):
        # TL 55.562678, -11.052482
        # TR 55.562678, -5.386214
        # BL 51.256233, -11.052482
        # BR 51.256233, -5.386214

        is_outside_lat = (lat < 51.256233) or (lat > 55.562678)
        is_outside_lng = (lng < -11.052482) or (lng > -5.386214)
        return is_outside_lat and is_outside_lng

    @staticmethod
    def add_dist_to_lat(dist_in_km, lat):
        return lat + (dist_in_km / 110.574)

    @staticmethod
    def add_dist_to_lng(dist_in_km, lng):
        return lng + (111.320 * math.cos(dist_in_km))

    @staticmethod
    def _get_country(country):
        countries_hash = {

        }

        return countries_hash[country]
