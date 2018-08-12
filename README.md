# DartsViewerApp

## Introduction
The Android app to follow the results of the DSAB leagues.

The DSAB (Deutscher Sportautomatenbund e.V.) organizes electronic darts events in Germany. This includes nation-wide tournaments as well as amateur and professional leagues.
The results of the leagues in Germany are published on the DSAB website (http://www.dsab-vfs.de). Multiple websites for regional associations also publish the results within their region.
Usually the information about a certain league and their results require a couple of clicks and data is presented in multiple formats (e.g. HTML and PDF).

The *DartsViewerApp* provided here allows to load the data from the DSAB website and present it in a structured way. 

## Features
The *DartsViewerApp* offer a simple and structured way to access the results of the DSAB leagues. Additional features make the handling of the app even more convenient.

- Access to all DSAB leagues in all regional associations
- Access to historical data
- Marking leagues and teams as favorites
- Show the ranking in several variants
- Team statistics including the teams match plan and the player results
- Shows the adress of a team and also on a digital map

#### Planned features
Several features are planned or at least thought of.

- Searching the historical data to track a team or player
- Check-out helper
- DSAB dart rules
- ... and more

## Screenshots
Some screenshots from the app in action.

TODO

## App permissions
The *DartsViewerApp* requires the following permissions to run properly:

- INTERNET
  - Reading the information from the DSAB website
- ACCESS_NETWORK_STATE
  - Checking if network is available
- WRITE_EXTERNAL_STORAGE
  - Cache information which has been downloaded already
- ACCESS_COARSE_LOCATION
  - Access the current approximate location  
- ACCESS_FINE_LOCATION
  - Access the current precise location 
- ACCESS_LOCATION_EXTRA_COMMANDS
  - Additional location information

## Source code
The source code of the app is made available on GitHub: https://github.com/senvB/dartsViewerApp.

The library is released under the terms of the GNU Affero General Public License (version 3 or later).

The app uses a DSAB data loader library which is also available on GitHub (https://github.com/senvB/dsabDataLoader)

The code is built with Android Studio using Gradle. It may not be perfectly set up as I am quite new to Idea and Gradle (switching from Eclipse and Maven).
In order to build the whole software the code for the app and the code for the data loader need to be imported as modules into a new project.

#### Dependencies
The code makes use of the following libraries.

- Parsing DSAB data from their website
  - dsabDataLoader (https://github.com/senvB/dsabDataLoader)
- Digital map display and address reverse geocoding
  - com.google.android.gms:play-services-maps (https://developers.google.com/android/guides/overview)
- Android app compatibility 
  - com.android.support:appcompat-v7 (https://developer.android.com/topic/libraries/support-library/packages)
- IO
  - commons-io:commons-io (https://commons.apache.org/proper/commons-io/)

## About the author
My name is Sven and I am playing electronic darts as one of my hobbies. This project brings darts in combination with my second hobby: coding. Whenever there is some spare time I try to code a little bit and learn new stuff.
Living currently in Berlin you may also find my name in the players list when using the *DartsViewerApp*.

In case you would like to use the library or the app I would be happy to receive a message from you. Also if you would like to contribute or have a nice idea of a new feature, then please don not hesitate to contact me. This also counts for any kind of bugs you encounter when using my code.

The website of the app can be found under https://dartsviewer.senv.de . This website is in German.

Any kind of feedback is welcome under <a href="dartsviewerfeedback@senv.de">dartsviewer@senv.de</a>
