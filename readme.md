# Squiggle App

### Hosting the frontend on firebase

`firebase login --reauth ` get a new auth token

`firebase init` initialise a project

`firebase deploy`

Project Console: https://console.firebase.google.com/project/squiggle-dd7ef/overview
Hosting URL: https://squiggle-dd7ef.web.app


### Link an external https url to local running app using ngrok

https://dashboard.ngrok.com/get-started/setup/macos

`ngrok config add-authtoken 2uY9yXMfd4eVvVHuf3ABTQcNiCC_x79sccccePfffVTkjrxb `

`ngrok http http://localhost:8080` this will link a public https url to springboot app running on localhost on port 8080
