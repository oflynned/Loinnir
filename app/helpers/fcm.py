from flask_pyfcm import FCMNotification

from app.api.v1.users import User
from app.app import mongo
from app.helpers.datasets import Datasets
from app.helpers.helper import Helper


class FCM:
    @staticmethod
    def notify_partner_chat_update(me, partner):
        my_id = me["fb_id"]
        partner_id = partner["fb_id"]

        me.pop("_id")
        partner.pop("_id")

        registration_id = partner["fcm_token"]
        message_title = Helper.get_decoded_name(str(me["forename"] + " " + me["surname"]))
        message_avatar = me["profile_pic"]

        # get latest message from you to notify partner
        my_messages_query = {"$and": [{"from_id": {"$in": [my_id]}}, {"to_id": {"$in": [partner_id]}}]}
        message = list(mongo.db.partner_conversations.find(my_messages_query).sort("time", -1).limit(1))[0]

        # sanitise the _id as we need it to create a notification for the user
        # or to update the chat screen and append it with its uid
        message_id = str(message["_id"])
        message.pop("_id")
        message["_id"] = message_id

        data_content = {
            "notification_type": "new_partner_message",
            "message_title": message_title,
            "message_avatar": message_avatar,
            "from_details": me,
            "to_details": partner,
            "message": message
        }

        # if user gets a block enforced in a chat the messages shouldn't get delivered or notified
        blocked_users = partner["blocked"]
        if my_id not in blocked_users:
            key = Datasets.get_fcm_api_key()
            push_service = FCMNotification(api_key=key)

            # notify a screen refresh and polling for new messages
            # remember that FCM id of 0 is for bots
            if partner_id != 0:
                push_service.notify_single_device(registration_id=registration_id, data_message=data_content)

            return Helper.get_json({"success": True})

        return Helper.get_json({"success": False, "reason": "partner_enforced_block"})

    @staticmethod
    def notify_locality_chat_update(me):
        me.pop("_id")

        locality = me["locality"]

        # remember that FCM token of 0 is for auto-generated bot profiles
        exclusion_ids = [me["fb_id"], 0]
        ids = []

        # do we care about blocked users?
        locality_users = list(mongo.db.users.find({
            "$and": [
                {"fb_id": {"$nin": exclusion_ids}},
                # {"locality": {"$eq": locality}}
            ]}))

        if len(locality_users) > 0:
            for user in locality_users:
                ids.append(user["fcm_token"])

            data_content = {
                "notification_type": "new_locality_update",
                "message_title": me["locality"],
                "message": {},
                "meta_data": {"count": len(locality_users), "locality": me["locality"]}
            }

            # perhaps should not notify users on a new locality message @ spam
            key = Datasets.get_fcm_api_key()
            push_service = FCMNotification(api_key=key)
            push_service.notify_multiple_devices(registration_ids=ids, data_message=data_content)

            return Helper.get_json({"success": True})

        return Helper.get_json({"success": False, "reason": "no_users_in_locality"})

    @staticmethod
    def notify_seen_message(me, partner):
        partner_fcm_token = partner["fcm_token"]
        event_content = {
            "notification_type": "message_seen",
            "message_title": "message_seen",
            "message": {},
            "partner_id": me["fb_id"]
        }

        key = Datasets.get_fcm_api_key()
        push_service = FCMNotification(api_key=key)
        push_service.notify_single_device(registration_id=partner_fcm_token, data_message=event_content)
        return Helper.get_json({"success": True})

    @staticmethod
    def notify_block_enacted_event(blocker, blockee):
        blockee_fcm_token = blockee["fcm_token"]

        event_content = {
            "notification_type": "block_enacted",
            "message_title": "block_enacted",
            "message": {},
            "block_enacter_id": blocker["fb_id"]
        }

        key = Datasets.get_fcm_api_key()
        push_service = FCMNotification(api_key=key)
        push_service.notify_single_device(registration_id=blockee_fcm_token, data_message=event_content)
        return Helper.get_json({"success": True})

    @staticmethod
    def notify_singular_push_notification(title, content, link, notification_id, fb_id):
        event_content = {
            "notification_type": "push_notification",
            "message_title": title,
            "message": {},
            "push_notification_title": title,
            "push_notification_content": content,
            "push_notification_link": link,
            "push_notification_id": notification_id
        }

        fcm_tokens = [User.get_user(fb_id)["fcm_token"]]

        key = Datasets.get_fcm_api_key()
        push_service = FCMNotification(api_key=key)
        push_service.notify_multiple_devices(registration_ids=fcm_tokens, data_message=event_content)
        return Helper.get_json({"success": True})

    @staticmethod
    def notify_push_notification(title, content, link, notification_id, search_filter):
        event_content = {
            "notification_type": "push_notification",
            "message_title": title,
            "message": {},
            "push_notification_title": title,
            "push_notification_content": content,
            "push_notification_link": link,
            "push_notification_id": notification_id
        }

        fcm_tokens = []
        for user in list(mongo.db.users.find(search_filter)):
            fcm_tokens.append(user["fcm_token"])

        key = Datasets.get_fcm_api_key()
        push_service = FCMNotification(api_key=key)
        push_service.notify_multiple_devices(registration_ids=fcm_tokens, data_message=event_content)
        return Helper.get_json({"success": True})
