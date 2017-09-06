from flask import Blueprint, render_template

from app.api.v1.admin import Admin

frontend = Blueprint("frontend", __name__)


@frontend.route("/", methods=["GET"])
def index():
    return render_template("index.html")


@frontend.route("/tos", methods=["GET"])
def tacaiocht():
    return render_template("tos.html")


@frontend.route("/priobhaideacht", methods=["GET"])
def priobhaideacht():
    return render_template("priobhaideacht.html")


@frontend.route("/faq", methods=["GET"])
def faq():
    return render_template("faq.html")


@frontend.route("/staitistici", methods=["GET", "POST"])
def staitistici():
    user_stats = Admin.get_user_stats()
    locality_stats = user_stats["count_per_locality"]
    county_stats = user_stats["count_per_county"]
    user_stats.pop("count_per_locality")
    user_stats.pop("count_per_county")

    message_stats = Admin.get_message_stats()

    return render_template("staitistici.html",
                           user_stats=user_stats,
                           locality_stats=locality_stats,
                           county_stats=county_stats,
                           message_stats=message_stats)
