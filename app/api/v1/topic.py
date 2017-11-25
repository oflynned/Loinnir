from datetime import datetime

from flask import Blueprint

from app.helpers.helper import Helper

topic_endpoint = Blueprint("topic", __name__)

weekly_topics = [
    "Cuir tú fhéin in aithe don phobal sa seomra ceantair",
    "Conas ar fhoghlaim tú an Ghaeilge?",
    "Cad í an tslí is fearr teanga a fhoghlaim?",
    "Conas a bhaineann tú úsáid as an nGaeilge i do shaol?",
    "Céard é an rud is Gaelaí?",
    "Céard í an Ghaeilge is measa dá bhfaca tú riamh?",
    "Cad é an gné is fearr duit san aip seo?",
    "Cad é rud éigin a chuireann isteach ort?",
    "Cad iad do thuairimí faoi ... ?"
]


@topic_endpoint.route("/get", methods=["GET"])
def get_weekly_topic():
    start_week = 47
    current_week = datetime.utcnow().isocalendar()[1]
    topic = weekly_topics[current_week - start_week]
    return Helper.get_json({
        "week": current_week,
        "topic": topic
    })
