/**
 *	Echostar Doorbell Sensor Child Button
 * 
 */
metadata {
	definition (name: "Echostar Doorbell Sensor Child Button", namespace: "Echostar", author: "Echostar") {
		capability "PushableButton"
		capability "Sensor"
	}
	preferences {
		input name: "debounceTime", type: "enum", description: "", title: "Suppress Duplicate Events Time", options: [[0:"off"],[1:"1s"],[2:"2s"],[3:"3s"],[4:"4s"],[5:"5s"],[6:"6s"],[7:"7s"],[8:"8s"],[9:"9s"],[10:"10s"]], defaultValue: 5
	}
}

def installed() {
    logDebug("installed called")
	device.updateSetting("debounceTime", [value: "5", type: "enum"])
	state["currentstate"] = 0
}

def updated() {
    logDebug("updated called")
	log.info("suppress duplicate events time is: ${debounceTime == "0" ? "off" : debounceTime + "s"}")
	unschedule()
}

def processEvent(event) {
    logDebug("processEvent called data: ${event}")
    return createEvent(event)
}

def sendButtonEvent() {
    logDebug("sendButtonEvent called")		
	if (debounceTime != "0") {
		def debounceTime = debounceTime.toInteger()
		def currentState = device.currentValue("pushed")  
		if (state["currentstate"] == 0) {
			state["currentstate"] = 1
			sendEvent(processEvent(name: "pushed", value: "1", descriptionText: "${device.displayName} was pushed", isStateChange: true))
			runIn(debounceTime, deBounce)
		}
		else {
			logDebug("events suppressed for ${debounceTime}s")
		}
	}
	else {
    	sendEvent(processEvent(name: "pushed", value: "1", descriptionText: "${device.displayName} was pushed", isStateChange: true))
	}
}

def deBounce() {
	logDebug("deBounce called")
	state["currentstate"] = 0
}

def logDebug(msg) {
	if (parent.getLogging() != false) {
		log.debug("${msg}")
	}
}