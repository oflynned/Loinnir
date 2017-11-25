import os
from datetime import datetime

from flask import Blueprint, request

from app.api.v1.admin import Admin
from app.helpers.fcm import FCM
from app.helpers.helper import Helper

topic_endpoint = Blueprint("topic", __name__)

weekly_topics = [
    "Cá astu? Inis dúinn rud suimiúil fút sa seomra ceantair!",
    "Conas ar fhoghlaim tú an Ghaeilge?",
    "Cad í an tslí is fearr le teanga a fhoghlaim?",
    "Conas a bhaineann tú úsáid as an nGaeilge i do shaol?",
    "Céard é an rud is Gaelaí?",
    "Céard í an Ghaeilge is measa dá bhfaca tú riamh?",
    "Cad é an gné is fearr duit san aip seo?",
    "Cad é rud éigin a chuireann isteach ort?",
    "Rud náireach a tharla duit?",
    "Cad iad do thuairimí faoi ... ?"
]

start_week = 47


@topic_endpoint.route("/get", methods=["GET"])
def get_weekly_topic():
    current_week, topic = Topic.get_topic_data()
    return Helper.get_json({
        "week": current_week,
        "topic": topic
    })


@topic_endpoint.route("/broadcast", methods=["POST"])
def broadcast_weekly_topic():
    if Admin.authenticate_user(request.json):
        current_week, topic = Topic.get_topic_data()
        title = "Topaic na Seachtaine"
        notification_type = "weekly_topic"
        _id = str(datetime.today().year) + ("0" if current_week < 10 else "") + str(current_week)

        print(os.environ["ENVIRONMENT"])

        if os.environ["ENVIRONMENT"] == "development":
            return FCM.notify_push_notification(title, topic, notification_type, _id)
        else:
            return Helper.get_json({"success": True})
    return Helper.get_json({"success": False})


class Topic:
    @staticmethod
    def get_topic_data():
        current_week = datetime.utcnow().isocalendar()[1]
        topic = weekly_topics[current_week - start_week]
        return current_week, topic
