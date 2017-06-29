from flask import Response
from bson import json_util
from flask_pyfcm import FCMNotification

from app.app import mongo

import os
import sys
import json
import math
import urllib.parse
from random import uniform, randint


class Helper:
    @staticmethod
    def get_json(data):
        return Response(
            json_util.dumps(data),
            mimetype='application/json'
        )

    @staticmethod
    def get_path(mode):

        if mode == "prod":
            return os.path.abspath(os.path.join(os.path.dirname(__file__), '..', '..', '..', '..', 'loinnir_auth.json'))
        else:
            return os.path.abspath(
                os.path.join(os.path.dirname(__file__), '..', '..', '..', '..', '..', '..', 'loinnir_auth.json'))

    @staticmethod
    def get_fcm_api_key(mode):
        with open(Helper.get_path(mode), "r") as f:
            data = json.loads(f.read())
            return data["fcm_api_key"]

    @staticmethod
    def get_distance(my_lat, my_lng, town_lat, town_lng):
        return math.fabs(math.sqrt((town_lat - my_lat) ** 2 + (town_lng - my_lng) ** 2))

    @staticmethod
    def get_locality(lat, lng):
        dataset = Helper.get_groomed_populated_areas()

        nearest_town = 0
        shortest_distance = 0

        for i, town in enumerate(dataset):
            town_lat = town["lat"]
            town_lng = town["lng"]
            distance = Helper.get_distance(lat, lng, town_lat, town_lng)

            if i == 0:
                shortest_distance = distance
                nearest_town = town["town"]

            if distance < shortest_distance:
                shortest_distance = distance
                nearest_town = town["town"]

        return nearest_town

    @staticmethod
    def get_decoded_name(encoded_name):
        return urllib.parse.unquote(encoded_name).replace("+", " ")

    @staticmethod
    def notify_partner_chat_update(my_id, partner_id, mode):
        me = dict(list(mongo.db.users.find({"fb_id": my_id}))[0])
        partner = dict(list(mongo.db.users.find({"fb_id": partner_id}))[0])

        me.pop("_id")
        partner.pop("_id")

        registration_id = partner["fcm_token"]
        message_title = Helper.get_decoded_name(str(me["name"]))
        message_avatar = me["profile_pic"]

        # get latest message from you to notify partner
        my_messages_query = {"$and": [{"from_id": {"$in": [my_id]}}, {"to_id": {"$in": [partner_id]}}]}
        message = list(mongo.db.messages_col.find(my_messages_query).limit(1))

        data_content = {
            "notification_type": "new_partner_message",
            "message_title": message_title,
            "message_avatar": message_avatar,
            "from_details": me,
            "to_details": partner,
            "message": message
        }

        print("Dispatching partner chat update!")

        # TODO check if the partner still exists or hasn't blocked this user
        key = Helper.get_fcm_api_key(mode)
        push_service = FCMNotification(api_key=key)
        push_service.notify_single_device(registration_id=registration_id, data_message=data_content)

        return Helper.get_json({"success": True})

    @staticmethod
    def notify_locality_chat_update(my_id, mode):
        me = dict(list(mongo.db.users.find({"fb_id": my_id}))[0])
        me.pop("_id")

        locality = me["locality"]
        ids = []

        locality_users = list(mongo.db.users.find({
            "$and": [
                {"fb_id": {"$ne": my_id}},
                {"locality": {"$eq": locality}}
            ]}))

        if len(locality_users) > 0:
            for user in locality_users:
                ids.append(user["fcm_token"])

            message_title = me["locality"]
            message = "Tá " + str(len(locality_users)) + " úsáideoir eile sa cheantar seo faoi láthair."

            data_content = {
                "notification_type": "new_locality_information",
                "message_title": message_title,
                "message": message
            }

            print("Dispatching locality chat update!")

            # perhaps should not notify users on a new locality message @ spam
            key = Helper.get_fcm_api_key(mode)
            push_service = FCMNotification(api_key=key)
            push_service.notify_multiple_devices(registration_ids=ids, data_message=data_content)

            return Helper.get_json({"success": True})
        else:
            return Helper.get_json({"success": False, "reason": "no users in locality right now"})

    @staticmethod
    def groom_population_dataset():
        dataset = Helper.get_populated_areas()
        groomed_set = []

        for item in dataset:
            town = item["properties"]["NAMN1"]
            town_lng = item["geometry"]["coordinates"][0]
            town_lat = item["geometry"]["coordinates"][1]
            groomed_set.append({"town": town, "lat": town_lat, "lng": town_lng})

        return groomed_set

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
    def get_groomed_populated_areas():
        with open("app/datasets/groomed_populated_areas_localised.json", "r") as f:
            return json.loads(f.read())

    @staticmethod
    def add_dist_to_lat(dist_in_km, lat):
        return lat + (dist_in_km / 110.574)

    @staticmethod
    def add_dist_to_lng(dist_in_km, lng):
        return lng + (111.320 * math.cos(dist_in_km))

    @staticmethod
    def generate_fake_users(quantity=100):
        users = []

        for i in range(quantity):
            # generate a name set
            with open("app/datasets/forenames.json", "r") as f:
                forenames = list(json.loads(f.read()))
                forename = forenames[randint(0, len(forenames) - 1)]

            with open("app/datasets/surnames.json", "r") as f:
                surnames = list(json.loads(f.read()))
                surname = surnames[randint(0, len(surnames) - 1)]

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

                new_lat_location = Helper.add_dist_to_lat(displacement_lat, locality_lat)
                new_lng_location = Helper.add_dist_to_lat(displacement_lng, locality_lng)
                new_locality = Helper.get_locality(new_lat_location, new_lng_location)

            profile_pic = "http://c1.thejournal.ie/media/2015/10/1916-easter-rising-commemoration-2-390x285.jpg"
            gender = "male" if randint(0, 1) == 0 else "female"
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
                "locality": new_locality,
                "profile_pic": profile_pic
            })

        return users
