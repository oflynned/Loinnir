# Loinnir
Ag fionnadh pobail don Ghaeilge. Fionn. Nasc. Braith.

## About
A modern location-based social network for connecting Irish speakers to one another through roulette and locality.
Loinnir means "shine/radiance/lustre" in Irish.
This is the backend server for managing the logic layers of the service, including matchmaking, user data, and broadcast services.

## Running
Heroku CLI is used to spawn worker threads and deploy to the production server. 

The development server is kept generally on localhost using `heroku local web` for using the production server in a local way with benefits of scaling on port 5000. 
A debug server also exists directly through Flask to see debug messages directly using `python3 LoinnirDev.py` on port 3000. 
Make sure you have a local instance of MongoDB running on the default port 27017. 
Environmental variables are used locally with a .env file containing keys.

Dependencies are installed for the environment using `python3 install -r requirements.txt`
