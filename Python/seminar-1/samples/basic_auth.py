import base64
from functools import wraps

from flask import Flask, jsonify, request

app = Flask(__name__)

USERS = {
    "user1": "password1",
}


def check_auth(username, password) -> bool:
    """Проверить авторизационные данные пользователя."""
    return username in USERS and USERS.get(username) == password


def requires_auth(f):
    @wraps(f)
    def decorated(*args, **kwargs):
        auth = request.authorization

        if not auth or not check_auth(auth.username, auth.password):
            return 'Unauthorized', 401, {'WWW-Authenticate': 'Basic realm="Login Required"'}

        return f(*args, **kwargs)

    return decorated


@app.route("/api/data", methods=['GET'])
@requires_auth
def get_data():
    return jsonify({"message": base64.b64encode("Protected data".encode("utf-8")).decode("utf-8")})


if __name__ == '__main__':
    app.run(debug=True)
