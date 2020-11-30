/**
 *	Echostar Doorbell Sensor
 * 
 */
metadata {
	definition (name: "Echostar Doorbell Sensor", namespace: "Echostar", author: "Echostar") {		
        fingerprint profileId: "0104", endpointId: "12", inClusters: "0000,0003,0009,0001", outClusters: "0003,0006,0008,0019", manufacturer: " Echostar", model: "   Bell", deviceJoinName: "Echostar Doorbell Sensor"
	}
	preferences {        
        input name: "logEnable", type: "bool", description: "", title: "Enable Debug Logging", defaultValue: true
	}
}

def installed() {
    device.updateSetting("logEnable", [value: "true", type: "bool"])    
    logDebug("installed called")	
	runIn(1800,logsOff)
}

def updated() {
	logDebug("updated called")    
	log.info("debug logging is: ${logEnable == true}")    
    state.clear()
	unschedule()
	if (logEnable) runIn(1800,logsOff)
}

def parse(String description) {
	logDebug("parse called")
	logDebug("got description: ${description}")	
    def descriptionMap = zigbee.parseDescriptionAsMap(description)
    if (descriptionMap.cluster == "0006" || descriptionMap.clusterId == "0006" || descriptionMap.clusterInt == 6) {       
        if (descriptionMap.command == "00" || descriptionMap.command == "01") {
            def switchCommand = hexStrToUnsignedInt(descriptionMap.command)            
			def inputName = switchCommand == 1 ? "Front" : "Rear"
            logDebug("input name is ${inputName}")
            def childDevice = getChildDevices()?.find { it.deviceNetworkId == "${device.deviceNetworkId}-${inputName}" }
            if (childDevice == null) {    
                logDebug("creating child device for input: ${inputName}")			
                addChildDevice("Echostar", "Echostar Doorbell Sensor Child Button", "${device.deviceNetworkId}-${inputName}", [name: "Echostar Doorbell Sensor (${inputName})", isComponent: false]) 
                childDevice = getChildDevices()?.find { it.deviceNetworkId == "${device.deviceNetworkId}-${inputName}" }
            }    		
            if (childDevice) {
                childDevice.sendButtonEvent()
            }
            else {
                logDebug("could not find child device for input: ${inputName}")			
            }      
		}
        else {
            logDebug("switch (0006) command: ${command} skipped")
        }
	}
    else {
        log.warn("skipped: ${descriptionMap}")			
        if (descriptionMap.containsKey("clusterInt")) {
            def zigbeeCluster = zigbee.clusterLookup(descriptionMap.clusterInt)
            if (zigbeeCluster != null) {
                log.warn("zigbeeCluster label: ${zigbeeCluster.clusterLabel} enum: zigbee.${zigbeeCluster.clusterEnum} int: ${zigbeeCluster.clusterInt}")
            }
        }	
	}   
}

def processEvent(event) {
    logDebug("processEvent called data: ${event}")
    return createEvent(event)
}

def logDebug(msg) {
	if (logEnable != false) {
		log.debug("${msg}")
	}
}

def logsOff() {
    log.warn "debug logging disabled"
    device.updateSetting("logEnable", [value:"false", type: "bool"])
}

def getLogging() {
	return logEnable
}