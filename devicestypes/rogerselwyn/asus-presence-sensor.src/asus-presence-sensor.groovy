/**
 *  Copyright 2019 Roger Selwyn
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *	RogerSelwyn Presence sensor
 *
 *	Author: Roger Selwyn, Based on original work by Stuart Buchanan with thanks
 *
 *	Date: 2019-02-21 v1.0 Initial Release
 */
metadata {
	// Automatically generated. Make future change here.
	definition (name: "ASUS Presence Sensor", namespace: "RogerSelwyn", author: "Roger Selwyn") {
		capability "Presence Sensor"
		capability "Actuator"
		capability "Sensor"
        
		command "setHome"
		command "setAway"
        command "setDetails"
        
        attribute "personName", "string"
        attribute "personMAC", "string"
        attribute "personInstance", "number"
	}

	simulator {
		status "present": "presence: present"
		status "not present": "presence: not present"
	}

	tiles {
		standardTile("presence", "device.presence", width: 2, height: 2, canChangeBackground: true) {
			state("present", labelIcon:"st.presence.tile.mobile-present", backgroundColor:"#53a7c0", icon:"https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/nest_dev_pres_icon.png")
			state("not present", labelIcon:"st.presence.tile.mobile-not-present", backgroundColor:"#ffffff", icon:"https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/nest_dev_away_icon.png")
		}
		main "presence"
		details "presence"
	}
}

def parse(String description) {
	//def pair = description.split(":")
	//createEvent(name: pair[0].trim(), value: pair[1].trim())
	log.trace "Parsing '${description}'"

}

// handle commands
def setHome() {
	log.trace "Executing 'setHome'"
	sendEvent(name: "presence", value: "present")
}


def setAway() {
	log.trace "Executing 'setAway'"
	sendEvent(name: "presence", value: "not present")
}

def setDetails(personDetails) {
	log.trace "Executing 'setDetails'"
    sendEvent(name: "personName", value: personDetails.personName)
    if (personDetails.personMAC) {
	     sendEvent(name: "personMAC", value: personDetails.personMAC)
   }
    sendEvent(name: "personInstance", value: personDetails.personInstance)
}

