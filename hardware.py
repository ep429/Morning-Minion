from temboo.Library.CloudMine.ObjectStorage import ObjectSet
from temboo.Library.CloudMine.ObjectStorage import ObjectGet
from temboo.Library.CloudMine.ObjectStorage import ObjectDelete
from temboo.core.session import TembooSession
from json import JSONEncoder
import json
#import pyupm_grove as grove
import calendar
import time

#Create button object
#button = grove.GroveButton(2)
# Create a session with your Temboo account details
session = TembooSession("ep429", "final", "X5I2VQfHKDwfrXlmkwBcIBBajKi4CO9f")
readEpochi = 0
time2 = calendar.timegm(time.gmtime()) 
timeI = 0

while(1):
    # Check with the database every 6 hours
    if(time2 > (timeI*1000 + 3600000*6)):
        timeI = time2
        time2 = calendar.timegm(time.gmtime())*1000
        # Get the latest input to db
        # Instantiate the Choreo
        objectGetChoreo = ObjectGet(session)
        # Get an InputSet object for the Choreo
        objectGetInputs = objectGetChoreo.new_input_set()
        # Set the Choreo inputs
        objectGetInputs.set_APIKey("d7218c11ec37447d9491dd8821b09214")
        objectGetInputs.set_Limit("1")
        objectGetInputs.set_Count("true")
        objectGetInputs.set_ApplicationIdentifier("337183ded56a4ffaaf526a5cbe0ca3ea")
        # Execute the Choreo
        objectGetResults = objectGetChoreo.execute_with_results(objectGetInputs)
        # Print the Choreo outputs
        #print("Response: " + objectGetResults.get_Response())
        # Get the epoch ehen we should start recording.
        inventory = json.loads(objectGetResults.get_Response())
        key1 = ''
        for key in inventory["success"]:
            readEpoch = int(inventory["success"][key]["epoch"])
            key1 = key
        objectDeleteChoreo = ObjectDelete(session)
        objectDeleteInputs = objectDeleteChoreo.new_input_set()
        objectDeleteInputs.set_APIKey("d7218c11ec37447d9491dd8821b09214")
        objectDeleteInputs.set_Keys(key1)
        objectDeleteInputs.set_ApplicationIdentifier("337183ded56a4ffaaf526a5cbe0ca3ea");
        objectDeleteResults = objectDeleteChoreo.execute_with_results(objectDeleteInputs);
        
       # Get current time
        currentTimeMs = calendar.timegm(time.gmtime())*1000;
        print readEpoch
        print currentTimeMs
        print("Waiting for alarm time to arrive...")
        alarm = True
        while(alarm):
            print("Waiting for alarm time to arrive...")
            if((readEpoch-200 < calendar.timegm(time.gmtime())*1000)):
                if ((calendar.timegm(time.gmtime())*1000) < (readEpoch + 3600000)):
                    print("Sending data...")
                    timeTaken = calendar.timegm(time.gmtime())*1000
                    jsonString = JSONEncoder().encode({
                        timeTaken: [timeTaken, button.value()]
                    })
                    try:
                        # Instantiate the Choreo
                        objectSetChoreo1 = ObjectSet(session)
                        # Get an InputSet object for the Choreo
                        objectSetInputs = objectSetChoreo1.new_input_set()
                        # Set the Choreo inputs
                        objectSetInputs.set_APIKey("bbf381a5e0f6402c8be36338460424cd")
                        objectSetInputs.set_Data(jsonString)
                        objectSetInputs.set_ApplicationIdentifier("a52a6a50c2a88b4d21598ad6bf3b6de9")
                        # Execute the Choreo
                        objectSetResults = objectSetChoreo1.execute_with_results(objectSetInputs)
                        # Print the Choreo outputs
                        print("Response: " + objectSetResults.get_Response())
                        print("Waiting...")
                        time.sleep(5)
                    except Exception:
                        print("Cloudmine Error")
                        time.sleep(10)
                else:
                    alarm = False
            
