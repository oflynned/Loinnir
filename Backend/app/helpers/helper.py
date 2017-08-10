import os
import time
import urllib.parse

from bson import json_util
from flask import Response


class Helper:
    @staticmethod
    def is_prod_environ():
        return "MONGO_USERNAME" in os.environ and "MONGO_PASSWORD" in os.environ

    @staticmethod
    def get_current_time_in_millis():
        return int(round(time.time() * 1000))

    @staticmethod
    def get_json(data):
        return Response(
            json_util.dumps(data),
            mimetype='application/json'
        )

    @staticmethod
    def get_path():
        return os.path.abspath(
            os.path.join(os.path.dirname(__file__), '..', '..', '..', '..', 'loinnir_auth.json'))

    @staticmethod
    def get_decoded_name(encoded_name):
        return urllib.parse.unquote(encoded_name).replace("+", " ")
