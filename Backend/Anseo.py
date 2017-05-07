from flask import Flask, jsonify
from flask_pymongo import PyMongo

app = Flask(__name__)
app.config["MONGO_DBNAME"] = "anseo"
app.config["MONGO_URI"] = "mongodb://localhost:27017/anseo"
app.debug = True
mongo = PyMongo(app)


@app.route('/', methods=["GET"])
def hello_world():
    return jsonify({"success": True})


@app.route('/api/v1/users', methods=["POST"])
def create_user():
    pass


@app.route('/api/v1/users', methods=["GET"])
def get_user(uid):
    user_db = mongo.db.users
    user = user_db.find_one({"_id": uid})

    if user:
        output = user
    else:
        output = {"success": False}

    return jsonify(output)


@app.route('/api/v1/users', methods=["DELETE"])
def delete_user():
    pass


@app.route('/api/v1/users', methods=["PUT"])
def edit_user():
    pass


if __name__ == '__main__':
    app.run(host='127.0.0.1', port=3000)
