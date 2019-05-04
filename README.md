# Smartthings Repository

ASUS Wifi Presence Detector - Integrates to ASUS Merlin Router to detect MAC addresses present on the network.

* Put scripts in /jffs/scripts after enabling scripts on your router
* Install SmartApp to Smartthings (after adding and publishing via IDE)
* Tokens required below can be found in IDE via My Locations / Other / ASUS Wifi Presence. SmartApp token is the id shown below 'Enable local child app discovery' at the bottom.
* Create /tmp/CheckUser directory on router
* Create accessToken file in CheckUser directory with access token in it
* Create smartApp file in Checkuser directory with SmartApp token in it
* Put MAC Addresses seperated by commas into SmartApp
* Run init-start on router to setup schedule (or run CheckIfHome for one off run)
* If you then change settings on SmartApp, and want to force sync to router, toggle wifi off on device that is already configured and connected to network, then run CheckIfHome 
