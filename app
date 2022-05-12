#!/usr/bin/env python3

from datetime import datetime, timedelta
from functools import wraps
import sqlite3
import uuid

from flask import Flask, jsonify, make_response, request
from flask_sqlalchemy import SQLAlchemy
import jwt
from sqlalchemy import func
from werkzeug.security import check_password_hash, generate_password_hash

MAX_THOUGHT_SIZE = 1024

app = Flask(__name__)

app.config["SECRET_KEY"] = \
    "0272a723777cd31318fee787e3001b96bf5ea49417424b9ec542b2f6d459e96c"
app.config["SQLALCHEMY_DATABASE_URI"] = "sqlite:///data.db"
app.config["SQLALCHEMY_TRACK_MODIFICATIONS"] = False

db = SQLAlchemy(app)

class User(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    public_id = db.Column(db.String(50), unique=True)
    email = db.Column(db.String(256), unique=True)
    password_hash = db.Column(db.String(80))

class Thought(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.String(50))
    text = db.Column(db.String(MAX_THOUGHT_SIZE))

db.create_all()

@app.route("/signup", methods=["POST"])
def signup():
    data = request.form
    email = data.get("email")
    password = data.get("password")

    user = User.query \
        .filter_by(email=email) \
        .first()
    if not user:
        user = User(
            public_id=str(uuid.uuid4()),
            email=email,
            password_hash=generate_password_hash(password)
        )
        db.session.add(user)
        db.session.commit()
        return jsonify({ "message": "Successfully registered" }), 201
    else:
        return jsonify({ "message": "User already exists" }), 202

@app.route("/auth", methods=["POST"])
def auth():
    data = request.form
    email = data.get("email", None)
    password = data.get("password", None)

    if not data or not email or not password:
        # returns 401 if any email or / and password is missing
        return make_response(
            jsonify({ "message": "Could not verify" }),
            401,
            { "WWW-Authenticate": "Basic realm = \"Login required.\"" }
        )

    user = User.query \
        .filter_by(email=email) \
        .first()

    if not user:
        # returns 401 if user does not exist
        return make_response(
            jsonify({ "message": "Could not verify" }),
            401,
            { "WWW-Authenticate": "Basic realm = \"User does not exist.\"" }
        )

    if check_password_hash(user.password_hash, password):
        # generates the JWT Token
        token = jwt.encode({
            "user_id": user.public_id,
            "exp": datetime.utcnow() + timedelta(minutes=5)
        }, app.config["SECRET_KEY"], algorithm="HS256")

        return make_response(jsonify({ "token": token }), 201)
    else:
        # returns 403 if password is wrong
        return make_response(
            jsonify({ "message": "Could not verify" }),
            403,
            { "WWW-Authenticate": "Basic realm = \"Wrong Password.\"" }
        )

def token_required(f):
    @wraps(f)
    def decorated(*args, **kwargs):
        token = request.headers.get("x-access-token")
        # return 401 if token is not passed
        if not token:
            return jsonify({ "message": "Missing token" }), 401

        try:
            # decoding the payload to fetch the stored details
            data = jwt.decode(token, app.config["SECRET_KEY"],
                algorithms="HS256")
            current_user = User.query \
                .filter_by(public_id=data["user_id"]) \
                .first()
        except jwt.InvalidSignatureError:
            return jsonify({ "message": "Invalid token" }), 401
        except jwt.ExpiredSignatureError:
            return jsonify({ "message": "Expired token" }), 401
        # returns the current logged in users contex to the routes
        return f(current_user, *args, **kwargs)
    return decorated

@app.route("/new-thought", methods=["POST"])
@token_required
def new_thought(current_user):
    data = request.form
    text = data.get("text")

    if not text:
        return jsonify({ "message": "Missing text" }), 400
    if len(text) > MAX_THOUGHT_SIZE:
        return jsonify({ "message": "Thought is too long" }), 400

    thought = Thought(user_id=current_user.public_id, text=text)
    db.session.add(thought)
    db.session.commit()

    return jsonify({ "message": "Success" }), 201

@app.route("/random-thought", methods=["GET"])
@token_required
def random_thought(current_user):
    thought = Thought.query \
        .filter_by(user_id=current_user.public_id) \
        .order_by(func.random()) \
        .first()
    if not thought:
        return jsonify({ "message": "There are no thoughts" }), 404
    return jsonify({ "message": thought.text }), 200

if __name__ == "__main__":
    app.run(host="0.0.0.0")
