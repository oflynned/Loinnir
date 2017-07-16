import sys

from app import app
from app.api.v1.services import Services

mode = "dev"

if __name__ == '__main__':
    if len(sys.argv) > 1:
        env = sys.argv[1]
        if env == "prod":
            mode = "prod"
            app.run(host='0.0.0.0', port=80)
        elif env == "gen":
            Services.bisort_alphabetically()

    else:
        app.run(host='0.0.0.0', port=3000)

# TODO
"""
    fcm
    unread/read messages
    pagination
    auto generate password to protect public api by tokens
"""
