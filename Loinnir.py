import sys

from app import app

mode = "dev"

if __name__ == '__main__':
    if len(sys.argv) > 1:
        env = sys.argv[1]
        if env == "prod":
            mode = "prod"
            app.run()

    else:
        app.run(host='0.0.0.0', port=3000)
