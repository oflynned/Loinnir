from app import app
import sys

mode = "dev"

if __name__ == '__main__':
    if len(sys.argv) > 1:
        env = sys.argv[1]
        if env == "prod":
            mode = "prod"
            app.run(host='0.0.0.0', port=80)
    else:
        app.run(host='0.0.0.0', port=3000)

# TODO
"""
    fcm
    unread/read messages
    pagination
    auto generate password to protect public api by tokens
"""
