import os
import time
import urllib.parse

from bson import json_util
from flask import Response


class Helper:
    @staticmethod
    def get_current_time_in_millis():
        return time.time() * 1000

    @staticmethod
    def get_json(data):
        return Response(
            json_util.dumps(data),
            mimetype='application/json'
        )

    @staticmethod
    def get_path(mode):
        if mode == "prod":
            return os.path.abspath(os.path.join(os.path.dirname(__file__), '..', '..', 'loinnir_auth.json'))
        else:
            return os.path.abspath(
                os.path.join(os.path.dirname(__file__), '..', '..', '..', '..', 'loinnir_auth.json'))

    @staticmethod
    def get_decoded_name(encoded_name):
        return urllib.parse.unquote(encoded_name).replace("+", " ")
