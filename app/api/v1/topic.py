import os
from datetime import datetime

from flask import Blueprint, request

from app.api.v1.admin import Admin
from app.helpers.fcm import FCM
from app.helpers.helper import Helper

topic_endpoint = Blueprint("topic", __name__)

weekly_topics = [
    "Cá astu? Inis dúinn rud suimiúil fút sa seomra ceantair!",
    "Cad é an gné is fearr san aip seo?",
    "Cad is brí le Gaeltacht dhigiteach?",
    "Céard é eachtra spéisiúil a tharla duit?",
    "An rud náireach a tharla duit?",
    "An Ghaeilge is measa dá bhfaca tú riamh?",
    "Cad í an tslí is fearr le teanga a fhoghlaim?",
    "Conas a bhaineann tú úsáid as an nGaeilge i do shaol?",
    "Cad é rud éigin a chuireann isteach ort?",
    "Conas ar fhoghlaim tú an Ghaeilge?"
]

start_week = 48


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
