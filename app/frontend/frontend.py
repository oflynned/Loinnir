from flask import Blueprint, render_template

frontend = Blueprint("frontend", __name__)


@frontend.route("/", methods=["GET"])
def index():
    return render_template("index.html")


@frontend.route("/tos", methods=["GET"])
def tacaiocht():
    return render_template("tos.html")


@frontend.route("/ceadunais", methods=["GET"])
def ceadunais():
    return render_template("ceadunais.html")


@frontend.route("/priobhaideacht", methods=["GET"])
def priobhaideacht():
    return render_template("priobhaideacht.html")


@frontend.route("/faq", methods=["GET"])
def faq():
    return render_template("faq.html")
