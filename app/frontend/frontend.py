import os
import operator

from flask import Blueprint, render_template, redirect, request
import flask_login

from app.api.v1.admin import Admin

frontend = Blueprint("frontend", __name__)
login_manager = flask_login.LoginManager()


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


@frontend.route("/error", methods=["GET"])
def error():
    return render_template("error.html")


@login_manager.user_loader
def load_admin_user(username):
    admin = Admin(username)
    return admin


@login_manager.request_loader
def request_loader(given_request):
    print(given_request)
    admin = Admin(os.environ["ADMIN_USERNAME"])
    # admin.is_authenticated = Admin.authenticate_fields(given_request["id"], given_request["secret"])
    return admin


@frontend.route("/log-out")
def logout():
    flask_login.logout_user()
    return redirect("/")


def unauthorized_user():
    return redirect("error")


@frontend.route("/admin", methods=["GET", "POST"])
def admin_login():
    if request.method == "POST":
        admin_user = Admin(os.environ["ADMIN_USERNAME"])
        flask_login.login_user(admin_user)

        user_stats = Admin.get_user_stats()
        locality_stats = user_stats["count_per_locality"]
        county_stats = user_stats["count_per_county"]
        user_stats.pop("count_per_locality")
        user_stats.pop("count_per_county")

        message_stats = Admin.get_message_stats()

        return render_template("admin-console.html",
                               user=admin_user,
                               user_stats=user_stats,
                               locality_stats=locality_stats,
                               county_stats=county_stats,
                               message_stats=message_stats)

    return render_template("admin-login.html")


@flask_login.login_required
@frontend.route("/admin-console", methods=["GET", "POST"])
def admin_console():
    return render_template("admin-console.html")


@frontend.record_once
def on_load(state):
    login_manager.init_app(state.app)
    login_manager.login_view = "admin"
