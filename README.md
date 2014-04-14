Compass
=======

This sample inserts a live card to the left of the Glass clock that displays a
compass. Tapping the live card presents a menu with two options:

- Read aloud: read the compass's current heading using text-to-speech
- Stop: remove the compass from the timeline

The compass also contains a small list of landmarks that will appear on the
screen when the user is within 10 km of those locations. See the
`res/raw/landmarks.json` file to add your own.

## Getting started

Check out our documentation to learn how to get started on
https://developers.google.com/glass/gdk/index

## Running the sample on Glass

You can use your IDE to compile and install the sample or use
[`adb`](https://developer.android.com/tools/help/adb.html)
on the command line:

    $ adb install -r CompassSample.apk

To start the sample, say "ok glass, show a compass" from the Glass clock
screen or use the touch menu.
