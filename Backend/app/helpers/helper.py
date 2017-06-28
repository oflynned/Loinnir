from flask import Response
from bson import json_util

import json
import math


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
            return "../../loinnir_auth.json"
        else:
            return "../../../../loinnir_auth.json"

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

    def get_decoded_name(encoded_name):
        return urllib.parse.unquote(encoded_name).replace("+", " ")

    def notify_partner_chat_update(my_id, partner_id):
        me = dict(list(mongo.db.users.find({"fb_id": my_id}))[0])
        partner = dict(list(mongo.db.users.find({"fb_id": partner_id}))[0])

        me.pop("_id")
        partner.pop("_id")

        registration_id = partner["fcm_token"]
        message_title = get_decoded_name(str(me["name"]))
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

        return get_json({"success": True})

    def notify_locality_chat_update(my_id):
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

            return get_json({"success": True})
        else:
            return get_json({"success": False, "reason": "no users in locality right now"})

    @staticmethod
    def generate_fake_users():
        for i in range(100):
            area = ""
            name = ""
            lat = ""
            lng = ""
            profile_pic = ""

            # get random town from json
            # add/subtract random value on lat & lng up to 10km
            # regenerate locality for the fake profile
