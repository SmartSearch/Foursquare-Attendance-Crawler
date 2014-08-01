FoursquareAttendanceCrawler
=============================

## Settings

Open the `etc/settings-example.json` file and change the `foursquare_api_accounts` and `crawl_folder` properties. You can obtain credentials for the Foursquare API by creating an application at https://foursquare.com/developers/apps.

After setting the parameters to the appropriate values, change the name of the file to `etc/settings.json`, otherwise the program will not find it:

  $ mv etc/settings-example.json etc/settings.json


## Listing all the venues of a city

  $ java -Dfile.encoding=UTF-8 -classpath bin:lib/commons-io-2.4.jar:lib/commons-lang-2.6.jar:lib/gson-1.7.1.jar eu.smartfp7.foursquare.ExhaustiveTrendsCrawling london
