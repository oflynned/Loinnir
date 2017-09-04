import json
import sys
from random import uniform, randint, shuffle

import requests

from app.helpers.geo import Geo


class FakeDatasets:
    @staticmethod
    def generate_fake_users(quantity=100):
        users = []

        for i in range(quantity):
            r = requests.get("https://randomuser.me/api/").json()["results"][0]
            gender = "male" if r["name"]["title"] == "mr" else "female"
            forename = FakeDatasets.capitalise(r["name"]["first"])
            surname = FakeDatasets.capitalise(r["name"]["last"])

            with open("app/datasets/groomed_populated_areas_localised.json", "r") as f:
                localities = list(json.loads(f.read()))
                locality = localities[randint(0, len(localities) - 1)]

                # fuzz the randomly chosen locality by up to +- 25km
                # generate a random value between 0-50 and subtract 25
                # then regenerate the nearest locality

                locality_lat = locality["lat"]
                locality_lng = locality["lng"]

                displacement_lat = uniform(0, 50) - 25
                displacement_lng = uniform(0, 50) - 25

                new_lat_location = Geo.add_dist_to_lat(displacement_lat, locality_lat)
                new_lng_location = Geo.add_dist_to_lat(displacement_lng, locality_lng)
                new_locality = Geo.get_locality(new_lat_location, new_lng_location)

            profile_pic = r["picture"]["large"]
            fb_id = str(randint(0, sys.maxsize - 1))
            show_location = True

            users.append({
                "fcm_id": 0,
                "fb_id": fb_id,
                "forename": forename,
                "surname": surname,
                "gender": gender,
                "show_location": show_location,
                "lat": new_lat_location,
                "lng": new_lng_location,
                "locality": new_locality["town"],
                "county": new_locality["county"],
                "profile_pic": profile_pic,
                "blocked": [],
                "partners": []
            })

        return users

    @staticmethod
    def capitalise(item):
        return item[:1].upper() + item[1:]

    @staticmethod
    def generate_all_counties_fake_users():
        users = []
        counties_added = []
        place_index = 0

        r = requests.get("https://randomuser.me/api/").json()["results"][0]
        gender = "male" if r["name"]["title"] == "mr" else "female"
        forename = FakeDatasets.capitalise(r["name"]["first"])
        surname = FakeDatasets.capitalise(r["name"]["last"])
        profile_pic = r["picture"]["large"]

        with open("app/datasets/groomed_populated_areas_localised.json", "r") as f:
            localities = list(json.loads(f.read()))

        for i in range(32):
            shuffle(localities)

            was_county_added = False

            while not was_county_added:
                locality = localities[place_index]
                county = locality["county"]

                if county not in counties_added:
                    locality_lat = locality["lat"]
                    locality_lng = locality["lng"]
                    town = locality["town"]

                    fb_id = str(randint(0, sys.maxsize - 1))
                    show_location = True

                    users.append({
                        "fcm_id": 0,
                        "fb_id": fb_id,
                        "forename": forename,
                        "surname": surname,
                        "gender": gender,
                        "show_location": show_location,
                        "lat": locality_lat,
                        "lng": locality_lng,
                        "locality": town,
                        "county": county,
                        "profile_pic": profile_pic,
                        "blocked": [],
                        "partners": []
                    })

                    counties_added.append(county)
                    was_county_added = True

                place_index += 1

        return users
