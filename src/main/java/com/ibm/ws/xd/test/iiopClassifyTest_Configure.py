#
# createWorkClass - creates default WorkClass named "JTest_<Application>_WorkClass"
# with some basic rules 
#
# Date Created: 04/13/2005
#
#
###
def findWorkClass(aWCName):
   wcNames=AdminConfig.list("WorkClass").split("\n")
   for wcName in wcNames:
       print "WorkClass Name Object="+wcName
       wcName=wcName.rstrip()
       if (len(wcName) > 0 ):
          name=AdminConfig.showAttribute(wcName,"name")
          if (aWCName == name and wcName.find("deployments") == -1):
             print "matched WorkClass: " + wcName
             return wcName
   return ""  
 
def findTransactionClass(aTCName):
   tcNames=AdminConfig.list("TransactionClass").split("\n")
   for tcName in tcNames:
       print "Transaction Class Name Object="+tcName
       tcName=tcName.rstrip()
       if (len(tcName) > 0 ):
          name=AdminConfig.showAttribute(tcName,"name")
          if (aTCName == name):
             print "matched Transaction Class: " + name
             return name
   return ""

def findDeployment(applName):
   appNames=AdminConfig.list("Deployment").split("\n")
   for appName in appNames:
       print "appName="+appName
       appName=appName.rstrip()
       if (len(appName) > 0 ):
          if (appName.startswith(applName)):
             print "matched application: " + appName
             return appName
   return ""

def setProtocolForWorkClass(aProtocol="HTTP"):
   if (aProtocol == "SOAP"):
      protocolType = "SOAPWORKCLASS"
   elif (aProtocol == "IIOP"):
      protocolType = "IIOPWORKCLASS"
   else:
      protocolType = "HTTPWORKCLASS"
   return protocolType 

##
# Creates a WorkClass with some basic rules 
# for Routing policy
##
def createRoutingWorkClass(anAppName,aWebModule,aWorkClassName,aProtocolType):
   routingWorkClass=""
   routingWorkClassModule=""
   routingRules=""
   actionValue="permit"
   action1Value=actionValue+":"+anAppName
   action2Value="reject:404"
   appLongName=findDeployment(anAppName)
   appShortName=anAppName
   workClassName=aWorkClassName
   moduleNameValue=aWebModule+".war"
   protocolType = "HTTPWORKCLASS"
   myAddress = java.net.InetAddress.getLocalHost().getHostAddress()
   myExpression="header$ClientIPV4 = '" +myAddress + "'"
   if (appLongName!=""):
       wcRuleAttributes = [ ["matchAction",actionValue],
                              ["matchExpression",myExpression]]
       wcRule2Attributes = [ ["matchAction",action2Value],
                              ["matchExpression","*"]]
       wcModuleAttributes = [ ["moduleName",moduleNameValue],
                              ["matchExpression","/*"],
                              ["id",workClassName+":!:"+appShortName+":!:"+moduleNameValue]]
       taskCmdArgs =  "[-appname "+ appShortName+" -wcname "+workClassName+" -type "+aProtocolType+" -actiontype "+actionValue+" -action "+appShortName+"]"
       AdminTask.createRoutingPolicyWorkClass(taskCmdArgs)
       routingWorkClass=findWorkClass(workClassName)
       if (routingWorkClass != ""):
          routingRules=AdminConfig.create("MatchRule",routingWorkClass,wcRuleAttributes)
          routingRules=AdminConfig.create("MatchRule",routingWorkClass,wcRule2Attributes)
          routingWorkClassModule=AdminConfig.create("WorkClassModule",routingWorkClass,wcModuleAttributes)
   else:
      print "The application specified does not exist in this WAS deployment"
      java.lang.System.exit(1)
      
##
# removes WorkClass and with some basic rules 
# for routing and SLA policies
##
def removeDefaultWorkClass(myAppName, aWorkClassName):
   print "removeDefaultWorkClass() myAppName, aWorkClassName "+myAppName+" "+aWorkClassName
   defaultWorkClass=""
   defaultWorkClassModule=""
   #nameValue="JTest_" +myAppName+"_WorkClass"
   nameValue=aWorkClassName   
   appLongName=findDeployment(myAppName)
   print "the app name returned is " +appLongName
   if (appLongName!=""):
       print "About to get workclasses"
       workclasses=AdminConfig.list("WorkClass").split("\n")
       for wclassId in workclasses:
                wclassId=wclassId.rstrip()
                if (wclassId.find(nameValue)>-1):
                     print "Deleting workclass " + wclassId
                     AdminConfig.remove(wclassId)
       
   else:
      print "The application specified does not exist in this WAS deployment"
      java.lang.System.exit(1)
#

##
# Creates a WorkClass with some basic rules 
# for SLA policy
##
def createSLAWorkClass(anAppName,aWebModule,aWorkClassName,aTCName,aDefaultTCName,aProtocolType,aIIOPRule,aFqEjbMethodName):
   slaWorkClass=""
   slaWorkClassModule=""
   slaRules=""
   
   tcName=findTransactionClass(aTCName)
   print " "
   
   tcDefaultName=findTransactionClass(aDefaultTCName)
   print " "
   
   print "tcName="+tcName
   print "tcDefaultName="+tcDefaultName

   appLongName=findDeployment(anAppName)
   print "appLongName="+appLongName   

   print "aFqEjbMethodName="+aFqEjbMethodName
   
   appShortName=anAppName
   print "appShortName="+appShortName
   
   workClassName=aWorkClassName
   print "workClassName="+workClassName
   
   moduleNameValue=aWebModule+".jar"
   print "moduleNameValue="+moduleNameValue
   
   protocolType = setProtocolForWorkClass(aProtocolType)  
   print "protocolType="+protocolType
   
   myAddress = java.net.InetAddress.getLocalHost().getHostAddress()
   print "myAddress="+myAddress
   
   theExpression="header$ClientHost='" + myAddress + "'"
   theExpression=aIIOPRule
   print "aIIOPRule="+aIIOPRule
      
   if (appLongName!="" and tcName != ""):

       wclassAttributes = [ ["matchAction",tcDefaultName],
                             ["name",workClassName],
                             ["type",protocolType],
                             ["description","This is a custom workclass."]]

       wcRuleAttributes = [ ["matchAction",tcName],
                              ["matchExpression",theExpression]]

       wcModuleAttributes = [ ["moduleName",moduleNameValue],
                              ["matchExpression",aFqEjbMethodName],
                              ["id",workClassName+":!:"+appShortName+":!:"+moduleNameValue]]

       slaWorkClass=AdminConfig.create("WorkClass",appLongName,wclassAttributes)
       
       slaRules=AdminConfig.create("MatchRule",slaWorkClass,wcRuleAttributes)
       
       slaWorkClassModule=AdminConfig.create("WorkClassModule",slaWorkClass,wcModuleAttributes)
       
   else:
   
      print "The application specified does not exist in this WAS deployment"
      java.lang.System.exit(1)
      
#
#  main() - 
#
myProtocolType="IIOP"
if(len(sys.argv) > 0):
     myAppName = sys.argv[0]
else:
     myAppName = "A"


# ---------------------------------
# Remove work classes
# ---------------------------------
myWorkClassName="IIOP_A_WorkClass_001_application"
removeDefaultWorkClass(myAppName, myWorkClassName);
myWorkClassName="IIOP_A_WorkClass_002_ejbmodule"
removeDefaultWorkClass(myAppName, myWorkClassName);
myWorkClassName="IIOP_A_WorkClass_003_ejb"
removeDefaultWorkClass(myAppName, myWorkClassName);
myWorkClassName="IIOP_A_WorkClass_004_ejbmethod"
removeDefaultWorkClass(myAppName, myWorkClassName);
# myWorkClassName="IIOP_A_WorkClass_005_ejbtype"
# removeDefaultWorkClass(myAppName, myWorkClassName);
myWorkClassName="IIOP_A_WorkClass_006_port"
removeDefaultWorkClass(myAppName, myWorkClassName);
# myWorkClassName="IIOP_A_WorkClass_007_clientport"
# removeDefaultWorkClass(myAppName, myWorkClassName);
# myWorkClassName="IIOP_A_WorkClass_008_clienthost"
# removeDefaultWorkClass(myAppName, myWorkClassName);
# myWorkClassName="IIOP_A_WorkClass_009_serverhost"
# removeDefaultWorkClass(myAppName, myWorkClassName);

# ---------------------------------
# FOR IIOP CLASSIFY TEST # 001 - application
# ---------------------------------
myTCName="tc_A_IIOP_001"
myDefaultTCName="Default_TC_PlatinumIIOP"
myAppName="A"
myWorkClassName="IIOP_A_WorkClass_001_application"
myWebModule="IIOPTestEjb_Server"
myIIOPRule="application = 'A'"
myFqEjbMethodName="Demo:classifyTest_001"
createSLAWorkClass(myAppName,myWebModule,myWorkClassName,myTCName,myDefaultTCName,myProtocolType,myIIOPRule,myFqEjbMethodName)
# ---------------------------------

# ---------------------------------
# FOR IIOP CLASSIFY TEST # 002 - ejbmodule
# ---------------------------------
myTCName="tc_A_IIOP_002"
myDefaultTCName="Default_TC_PlatinumIIOP"
myAppName="A"
myWorkClassName="IIOP_A_WorkClass_002_ejbmodule"
myWebModule="IIOPTestEjb_Server"
myIIOPRule="ejbmodule = 'IIOPTestEjb_Server.jar'"
myFqEjbMethodName="Demo:classifyTest_002"
createSLAWorkClass(myAppName,myWebModule,myWorkClassName,myTCName,myDefaultTCName,myProtocolType,myIIOPRule,myFqEjbMethodName)

# ---------------------------------
# FOR IIOP CLASSIFY TEST # 003 - ejb
# ---------------------------------
myTCName="tc_A_IIOP_003"
myDefaultTCName="Default_TC_PlatinumIIOP"
myAppName="A"
myWorkClassName="IIOP_A_WorkClass_003_ejb"
myWebModule="IIOPTestEjb_Server"
myIIOPRule="ejb = 'Demo'"
myFqEjbMethodName="Demo:classifyTest_003"
createSLAWorkClass(myAppName,myWebModule,myWorkClassName,myTCName,myDefaultTCName,myProtocolType,myIIOPRule,myFqEjbMethodName)

# ---------------------------------
# FOR IIOP CLASSIFY TEST # 004 - ejbmethod
# ---------------------------------
myTCName="tc_A_IIOP_004"
myDefaultTCName="Default_TC_PlatinumIIOP"
myAppName="A"
myWorkClassName="IIOP_A_WorkClass_004_ejbmethod"
myWebModule="IIOPTestEjb_Server"
myIIOPRule="ejbmethod = 'classifyTest_004'"
myFqEjbMethodName="Demo:classifyTest_004"
createSLAWorkClass(myAppName,myWebModule,myWorkClassName,myTCName,myDefaultTCName,myProtocolType,myIIOPRule,myFqEjbMethodName)

# ---------------------------------
# FOR IIOP CLASSIFY TEST # 005 - ejbtype
# ---------------------------------
# myTCName="tc_A_IIOP_005"
# myDefaultTCName="Default_TC_PlatinumIIOP"
# myAppName="A"
# myWorkClassName="IIOP_A_WorkClass_005_ejbtype"
# myWebModule="IIOPTestEjb_Server"
# myIIOPRule="ejbtype = 'ejbtype'"
# myFqEjbMethodName="Demo:classifyTest_005"
# createSLAWorkClass(myAppName,myWebModule,myWorkClassName,myTCName,myDefaultTCName,myProtocolType,myIIOPRule,myFqEjbMethodName)

# ---------------------------------
# FOR IIOP CLASSIFY TEST # 006 - port
# ---------------------------------
myTCName="tc_A_IIOP_006"
myDefaultTCName="Default_TC_PlatinumIIOP"
myAppName="A"
myWorkClassName="IIOP_A_WorkClass_006_port"
myWebModule="IIOPTestEjb_Server"
myIIOPRule="port <> 0"
myFqEjbMethodName="Demo:classifyTest_006"
createSLAWorkClass(myAppName,myWebModule,myWorkClassName,myTCName,myDefaultTCName,myProtocolType,myIIOPRule,myFqEjbMethodName)

# ---------------------------------
# FOR IIOP CLASSIFY TEST # 007 - clientport
# ---------------------------------
# myTCName="Default_TC_PlatinumIIOP"
# myDefaultTCName="tc_A_IIOP_007"
# myAppName="A"
# myWorkClassName="IIOP_A_WorkClass_007_clientport"
# myWebModule="IIOPTestEjb_Server"
# myIIOPRule="clientport = 0"
# myFqEjbMethodName="Demo:classifyTest_007"
# createSLAWorkClass(myAppName,myWebModule,myWorkClassName,myTCName,myDefaultTCName,myProtocolType,myIIOPRule,myFqEjbMethodName)

# ---------------------------------
# FOR IIOP CLASSIFY TEST # 008 - clienthost
# ---------------------------------
# myTCName="tc_A_IIOP_008"
# myDefaultTCName="Default_TC_PlatinumIIOP"
# myAppName="A"
# myWorkClassName="IIOP_A_WorkClass_008_clienthost"
# myWebModule="IIOPTestEjb_Server"
# myIIOPRule="clienthost = 'clienthost'"
# myFqEjbMethodName="Demo:classifyTest_008"
# createSLAWorkClass(myAppName,myWebModule,myWorkClassName,myTCName,myDefaultTCName,myProtocolType,myIIOPRule,myFqEjbMethodName)

# ---------------------------------
# FOR IIOP CLASSIFY TEST # 009 - serverhost
# ---------------------------------
# myTCName="tc_A_IIOP_009"
# myDefaultTCName="Default_TC_PlatinumIIOP"
# myAppName="A"
# myWorkClassName="IIOP_A_WorkClass_009_serverhost"
# myWebModule="IIOPTestEjb_Server"
# myIIOPRule="serverhost = 'serverhost'"
# myFqEjbMethodName="Demo:classifyTest_009"
# createSLAWorkClass(myAppName,myWebModule,myWorkClassName,myTCName,myDefaultTCName,myProtocolType,myIIOPRule,myFqEjbMethodName)



# createRoutingWorkClass(myAppName,myWebModule,myWorkClassName,myProtocolType)
print "*********************************"
print "Default Work Class created"
print "Default Match Rule created"
print "Default Work Class Module created"
print "*********************************"	
print "Saving workspace"
AdminConfig.save()
print "finished."
