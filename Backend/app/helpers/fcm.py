from flask_pyfcm import FCMNotification

from app.app import mongo
from app.helpers.helper import Helper
from app.helpers.datasets import Datasets


class FCM:
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

        # if user gets a block enforced in a chat the messages shouldn't get delivered or notified
        blocked_users = list(mongo.db.users.find({"fb_id": partner_id}))[0]["blocked"]
        if my_id not in blocked_users:
            key = Datasets.get_fcm_api_key(mode)
            push_service = FCMNotification(api_key=key)
            push_service.notify_single_device(registration_id=registration_id, data_message=data_content)

            return Helper.get_json({"success": True})

        return Helper.get_json({"success": False, "reason": "partner has enforced a block on you"})

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

            data_content = {
                "notification_type": "new_locality_information",
                "message_title": me["locality"],
                "message": {"count": len(locality_users), "locality": me["locality"]}
            }

            # perhaps should not notify users on a new locality message @ spam
            key = Datasets.get_fcm_api_key(mode)
            push_service = FCMNotification(api_key=key)
            push_service.notify_multiple_devices(registration_ids=ids, data_message=data_content)

            return Helper.get_json({"success": True})
        else:
            return Helper.get_json({"success": False, "reason": "no users in locality right now"})
