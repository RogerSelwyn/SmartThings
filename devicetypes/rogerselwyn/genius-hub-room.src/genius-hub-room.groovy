/**
 *  Genius Hub Room
 * 
 *  Based on code by Neil Cumpstey - converted to use official Genius Hub API
 * 
 *  A SmartThings device handler which wraps a device on a Genius Hub.
 *
 *  ---
 *  Disclaimer:
 *  This device handler and the associated smart app are in no way sanctioned or supported by Genius Hub.
 *  All work is based on an unpublished api, which may change at any point, causing this device handler or the
 *  smart app to break. I am in no way responsible for breakage due to such changes.
 *
 *  ---
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 */

import java.text.SimpleDateFormat
import groovy.time.*

metadata {
  definition (name: 'Genius Hub Room', namespace: 'RogerSelwyn', author: 'Roger Selwyn') {
    capability 'Actuator'
    capability 'Battery'
    capability 'Health Check'
    capability 'Illuminance Measurement'
    capability 'Refresh'
    capability 'Sensor'
    capability 'Temperature Measurement'
    capability 'Thermostat Heating Setpoint'
	capability "Thermostat"
    capability "Thermostat Mode"


    command 'extraHour'
    command 'refresh'
    command 'revert'

    attribute 'operatingState', 'enum', ['off', 'timer', 'override', 'footprint']
    attribute 'overrideEndTime', 'date'
  }

  preferences {
  }

  tiles(scale: 2) {
    multiAttributeTile(name: 'thermostat', type: 'thermostat', width: 6, height: 4, canChangeIcon: true) {
      tileAttribute('device.temperature', key: 'PRIMARY_CONTROL') {
        attributeState('temperature', label: '${currentValue}°', icon: 'st.alarm.temperature.normal', defaultState: true
        )
      }
      tileAttribute('device.temperature', key: 'VALUE_CONTROL', label: '${currentValue}°') {
        attributeState('up', action: 'setHeatingSetpoint')
        attributeState('down', action: 'setHeatingSetpoint')
      }
      tileAttribute("device.heatingSetpoint", key: "HEATING_SETPOINT") {
          attributeState("default", label:'${currentValue}')
      }
 
      tileAttribute("device.thermostatOperatingState", key: "OPERATING_STATE") {
          attributeState("idle",			backgroundColor:"#44B621")
          attributeState("heating",		 backgroundColor:"#e86d13")
          attributeState("off",		 	backgroundColor:"#00a0dc")
          attributeState("fan only",		  backgroundColor:"#145D78")
          attributeState("pending heat",	  backgroundColor:"#B27515")
          attributeState("pending cool",	  backgroundColor:"#197090")
          attributeState("vent economizer", backgroundColor:"#8000FF")
      }

	tileAttribute("device.thermostatMode", key: "THERMOSTAT_MODE") {
          attributeState("off", label:'${name}')
          attributeState("heat", label:'${name}')
          attributeState("cool", label:'${name}')
          attributeState("auto", label:'${name}')
          attributeState("eco", label:'${name}')
          attributeState("emergency heat", label:'${name}')
      }

     tileAttribute ('device.operatingState', key: 'SECONDARY_CONTROL') {
        attributeState('off', label: '${currentValue}', icon: 'https://raw.githubusercontent.com/cumpstey/Cwm.SmartThings/master/smartapps/cwm/genius-hub-integration.src/assets/genius-hub-off-120.png')
        attributeState('override', label: '${currentValue}', icon: 'https://raw.githubusercontent.com/cumpstey/Cwm.SmartThings/master/smartapps/cwm/genius-hub-integration.src/assets/genius-hub-override-120.png')
        attributeState('timer', label: '${currentValue}', icon: 'https://raw.githubusercontent.com/cumpstey/Cwm.SmartThings/master/smartapps/cwm/genius-hub-integration.src/assets/genius-hub-timer-120.png')
        attributeState('footprint', label: '${currentValue}', icon: 'https://raw.githubusercontent.com/cumpstey/Cwm.SmartThings/master/smartapps/cwm/genius-hub-integration.src/assets/genius-hub-footprint-120.png')
      }
    }
    
    valueTile("temp2", "device.temperature", width: 2, height: 2, decoration: "flat") {
        state("default", label:'${currentValue}\u00b0', icon: 'st.alarm.temperature.normal', 
              backgroundColors: getTempColors())
    }

    standardTile('refresh', 'device', width: 1, height: 1, decoration: 'flat') {
      state 'default', label: '', action: 'refresh', icon: 'st.secondary.refresh'
    }
    standardTile('extraHour', 'device.canModifyOverride', width: 1, height: 1, decoration: 'flat') {
      state 'default', label: null
      state 'yes', label: 'Extra hour', action: 'extraHour'
    }
    standardTile('revert', 'device.canModifyOverride', width: 1, height: 1, decoration: 'flat') {
      state 'default', label: null
      state 'yes', label: '', action: 'revert', icon: 'https://raw.githubusercontent.com/cumpstey/Cwm.SmartThings/master/smartapps/cwm/genius-hub-integration.src/assets/genius-hub-revert-120.png'
    }
    // Suspected SmartThings bugs:
    // 1. A battery value of 49 is coloured green. If an additional checkpoint of `[value: 48, color: '#f1d801']` is added, it's then coloured yellow.
    //    It's really not clear what's going on here. However, it _is_ clear from the way temperatures are treated wrt background colours that it's not
    //    the simple value comparison that the documentation suggests, and that debugging is basically impossible.
    // 2. The yellow colour is pretty greenish - nothing like the hex colour. It looks like it may be a mixture of the background colour of the
    //    multiattribute tile and the value tile.
    valueTile('battery', 'device.battery', inactiveLabel: false, width: 1, height: 1) {
      state 'battery', label: '${currentValue}% battery',
      backgroundColors:[
        [value: 10, color: '#bc2323'],
        [value: 26, color: '#f1d801'],
        [value: 51, color: '#44b621']
      ]
    }
    valueTile('illuminance', 'device.illuminance', inactiveLabel: false, width: 1, height: 1) {
      state 'illuminance', label: '${currentValue} lux'
    }
    valueTile('overrideEndTime', 'device.overrideEndTimeDisplay', width: 3, height: 1) {
      state 'default', label: '${currentValue}'
    }
    valueTile("lastUpdatedDt", "device.lastUpdatedDt", width: 3, height: 1, ) {
      state "default", label: 'Data Last Received:\n${currentValue}'
    }

    main(['temp2'])
    details(['thermostat', 'refresh', 'extraHour', 'revert', 'battery', 'illuminance', 'overrideEndTime', 'lastUpdatedDt'])
  }
}

//#region Methods called by parent app

/**
 * Stores the Genius Hub id of this room in state.
 *
 * @param geniusId  Id of the room zone within the Genius Hub.
 */
void setGeniusId(Integer geniusId) {
  state.geniusId = geniusId
}

/**
 * Stores the configured log level in state.
 *
 * @param logLevel  Configured log level.
 */
void setLogLevel(Integer logLevel) {
  state.logLevel = logLevel
}

/**
 * Returns the Genius Hub id of this room.
 */
Integer getGeniusId() {
  return state.geniusId
}

/**
 * Returns the type of this device.
 */
String getGeniusType() {
  return 'room'
}

/**
 * Updates the state of the room.
 *
 * @param values  Map of attribute names and values.
 */
void updateState(Map values) {

  logger "${device.label}: updateState: ${values}", 'trace'

  sendEvent(name: 'lastUpdatedDt', value: getDtNow()?.toString(), displayed: false, isStateChange: true)

  if (values?.containsKey('operatingState')) {
    sendEvent(name: 'operatingState', value: values.operatingState)
  }

  if (values?.containsKey('setpoint')) {
  	def updateSetpoint = values.setpoint
    if (device.currentValue('operatingState') != 'override' && values?.containsKey('defaultSetpoint')) {
    	updateSetpoint = values.defaultSetpoint
    }
    sendEvent(name: 'heatingSetpoint', value: updateSetpoint, unit: temperatureScale, displayed: true)
	sendEvent(name: 'thermostatSetpoint', value: updateSetpoint, unit: temperatureScale, displayed: false)
    
  }

  if (values?.containsKey('sensorTemperature')) {
    def value = convertCelsiusToHubScale(values.sensorTemperature)
    sendEvent(name: 'temperature', value: value, unit: temperatureScale, displayed: true) 
	def newoperState
    def newMode
    def currentSetpoint = device.currentValue('heatingSetpoint')
    
    if ( currentSetpoint == 4) {
    	newoperState = 'off'
        newMode = 'off'
    }
    else if ( value < currentSetpoint ) {
    	newoperState = 'heating'
        newMode = 'heat'
	}
    else {
    	newoperState = 'idle'
		newMode = 'heat'
	}
        
    if (device.currentValue('operatingState') == 'override') {
    	newMode = 'heat'
    }
    sendEvent(name: 'thermostatOperatingState', value: newoperState, descriptionText: "Device state is", displayed: true)
	sendEvent(name: 'thermostatMode', value: newMode, displayed: false)
  }

  if (values?.containsKey('minBattery')) {
    if (values.minBattery == 255) {values.minBattery = 0}
    sendEvent(name: 'battery', value: values.minBattery, unit: '%', displayed: true)
  }

  if (values?.containsKey('illuminance')) {
    sendEvent(name: 'illuminance', value: values.illuminance, unit: 'lux', displayed: false)
  }
  
  if (values?.containsKey('overrideEndTime')) {
    sendEvent(name: 'overrideEndTime', value: values.overrideEndTime, displayed: false)
  }
 
  updateDisplay()
}

//#endregion Methods called by parent app

//#region Actions

/**
 * Not used in this device handler.
 */
def parse(String description) {
}

def getTempColors() {
	def colorMap
		colorMap = [
			// Celsius Color Range
			[value: 0, color: "#153591"],
			[value: 7, color: "#1e9cbb"],
			[value: 15, color: "#90d2a7"],
			[value: 23, color: "#44b621"],
			[value: 28, color: "#f1d801"],
			[value: 35, color: "#d04e00"],
			[value: 37, color: "#bc2323"],
			// Fahrenheit Color Range
			[value: 40, color: "#153591"],
			[value: 44, color: "#1e9cbb"],
			[value: 59, color: "#90d2a7"],
			[value: 74, color: "#44b621"],
			[value: 84, color: "#f1d801"],
			[value: 95, color: "#d04e00"],
			[value: 96, color: "#bc2323"]
		]
}
/**
 * Extend the override by an hour.
 */
def extraHour() {
  logger "${device.label}: extraHour", 'trace'

  if (device.currentValue('operatingState') == 'override') {
    def overrideEndTime = device.currentValue('overrideEndTime')
    def period = 3600
    if (overrideEndTime) {
      period = (int)(((overrideEndTime.getTime() + 3600 * 1000) - now()) / 1000)
    }

    parent.pushOverridePeriod(state.geniusId, period)
  }
}

/**
 * Refresh all devices.
 */
def refresh() {
  logger "${device.label}: refresh", 'trace'
  parent.refresh(['geniusId': getGeniusId(), geniusType: getGeniusType()])
}

/**
 * Revert the operating mode to the default.
 */
def revert() {
  logger "${device.label}: revert", 'trace'
  
  if (device.currentValue('operatingState') == 'override') {
    parent.revert(state.geniusId)
  }
}

/**
 * Sets the operating mode to override and the target temperature to the specified value.
 *
 * @param value  Target temperature, in either Celsius or Fahrenheit as defined by the SmartThings hub settings.
 */
def setHeatingSetpoint(Double value) {
  logger "${device.label}: setHeatingSetpoint: ${value}", 'trace'

  sendEvent(name: 'heatingSetpoint', value: value, displayed: true )  

  def valueInCelsius = convertHubScaleToCelsius(value)
  parent.pushRoomTemperature(state.geniusId, valueInCelsius)
}

//#endregion Actions

//#region Helpers

/**
 * Update the attributes used to determine how tiles are displayed,
 * based on state.
 */
private void updateDisplay() {
  logger "${device.label}: updateDisplay", 'trace'

  def operatingState = device.currentValue('operatingState')

  def overrideEndTime = device.currentValue('overrideEndTime')

  if (operatingState == 'override') {
    sendEvent(name: 'canModifyOverride', value: 'yes', displayed: false)
    if (overrideEndTime) {
      sendEvent(name: 'overrideEndTimeDisplay', value: "Override ends ${formatDt(overrideEndTime, 'HH:mm')}", displayed: false)
    }
  } else {
    sendEvent(name: 'canModifyOverride', value: '', displayed: false)
    sendEvent(name: 'overrideEndTimeDisplay', value: '', displayed: false)
  }
}

/**
 * Converts a Celsius temperature value to the scale defined in the SmartThings hub settings.
 *
 * @param valueInCelsius  Temperature in Celsius.
 */
private Double convertCelsiusToHubScale(Double valueInCelsius) {
  def value = (temperatureScale == "F") ? ((valueInCelsius * 1.8) + 32) : valueInCelsius
  return value.round(1)
}

/**
 * Converts a temperature value on the scale defined in the SmartThings hub settings to Celsius.
 *
 * @param valueInHubScale  Temperature in the unit defined in the SmartThings hub settings.
 */
private Double convertHubScaleToCelsius(Double valueInHubScale) {
  def value = (temperatureScale == "C") ? valueInHubScale : ((valueInHubScale - 32) / 1.8)
  return value.round(1)
}

/**
 * Log message if logging is configured for the specified level.
 */
private void logger(message, String level = 'debug') {
  switch (level) {
    case 'error':
      if (state.logLevel >= 1) log.error message
      break
    case 'warn':
      if (state.logLevel >= 2) log.warn message
      break
    case 'info':
      if (state.logLevel >= 3) log.info message
      break
    case 'debug':
      if (state.logLevel >= 4) log.debug message
      break
    case 'trace':
      if (state.logLevel >= 5) log.trace message
      break
    default:
      log.debug message
      break
  }
}

def getDtNow() {
	def now = new Date()
	return formatDt(now)
}

def formatDt(dt, dtFormat = "E MMM dd HH:mm:ss z yyyy") {
	def tf = new SimpleDateFormat(dtFormat)
	if(getTimeZone()) { tf.setTimeZone(getTimeZone()) }
	else {
		Logger("SmartThings TimeZone is not found or is not set... Please Try to open your ST location and Press Save...", "warn")
	}
	return tf.format(dt)
}
def getTimeZone() {
	def tz = null
	if(location?.timeZone) { tz = location?.timeZone }
	if(!tz) { Logger("getTimeZone: Hub TimeZone is not found ...", "warn") }
	return tz
}

//#endregion Helpers
