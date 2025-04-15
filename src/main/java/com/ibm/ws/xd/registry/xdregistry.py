import javax.management.ObjectName as ObjectName
import java.lang.Throwable as Throwable

def convertToList(inlist):
     outlist=[]
     if (len(inlist)>0 and inlist[0]=='[' and inlist[len(inlist)-1]==']'):
        inlist = inlist[1:len(inlist)-1]
        clist = inlist.split(" ")
     else:
        clist = inlist.split("\n")
     for elem in clist:
        elem=elem.rstrip();
        if (len(elem)>0):
           outlist.append(elem)
     return outlist

def genLTPAKeys(password):
     secMbean = AdminControl.queryNames("type=SecurityAdmin,process=dmgr,*")
     AdminControl.invoke(secMbean, "generateKeys", password )
     return AdminControl.invoke_jmx(ObjectName(secMbean), 'exportLTPAKeys', [],[])

# generate keys first

serverMBean = AdminControl.queryNames("type=Server,process=dmgr,*")
version = AdminControl.getAttribute(serverMBean,"platformVersion")
print "serverVersion="+version

print "generating ltpa keys"
sharedKey=None
privateKey=None
publicKey=None

#
# eliminate code to set keys in configuration, WAS seems to autogenerate
#
#if (version<"7.0"):
#    exportedKeys = genLTPAKeys("password")
#    sharedKey = exportedKeys.getProperty("com.ibm.websphere.ltpa.3DESKey")
#    privateKey = exportedKeys.getProperty("com.ibm.websphere.ltpa.PrivateKey")
#    publicKey = exportedKeys.getProperty("com.ibm.websphere.ltpa.PublicKey")
#    print "sharedKey="+sharedKey
#    print "privateKey="+privateKey
#    print "publicKey="+publicKey
#else:
#   print "not generating keys due to server version"
#

print "obtaining Security object"
security=AdminConfig.list("Security")
print "obtaining LTPA object"
ltpa=AdminConfig.list("LTPA",security)
print "obtaining CustomUserRegistry object"
cur=AdminConfig.list("CustomUserRegistry",security)

print "modifying security configuration"
attrs = [ ["enabled","true"],
          ["enforceJava2Security","true"],
          ["activeAuthMechanism",ltpa],
          ["activeUserRegistry",cur]]
AdminConfig.modify(security,attrs)

print "modifying ltpa password"
attrs = [ ["password","password"]]
AdminConfig.modify(ltpa,attrs)

print "setting ltpa configuration"
#  OLD STATIC KEYS
# privateattrs= [ ["byteArray","lr2pAHjWnTflNNJO6A0v4CvHEVaCIIovDLnahe0yaUiohMMECjZalsPSDbuZLOap89hNaNFzt9oiIAJO/t2+53bMsqs0w7tJgnNGWgBsPUDnnrebOUZPcSwWstL3ZczQPY0tQLsCAIL56i4tvRoqao03OYDrm+Y9+KRcYuMRDx94buhRJ772owVSaJIGqwnzd7y74/ijC9tIXhLEJIuA/c/WGXftQb2qCNIYFrTgDI7OEkkkNn83X3oAVrrO0nE4eE2lfBKzTfROJvefHGjIq3OpT0p0yvOpaNzAY5Kmhi92v3bO+Ti7AosAegienMeWc1dW2PEVZ55vy7URhnzJHPeU3mHGelPomFjQzALQHQg="]]
#  publicattrs = [ ["byteArray","AOpwfpa3gWcvLEwq8o7P8M6tYILnKpZa/+10nvEx7qctAEhYGEh5YjaPSOyPH9BB2gRckfshKZ+RJ1N/7Cl9wrNnV42b7jRJ5+lRWqnK0mmJIlovopAJlwmnUrEw8LCnAR0sl+LRnZcYCzna2bFjNgP/P0tno+PHk9UAjmO99jmJAQAB"]]
#  sharedattrs = [ ["byteArray","bpLbXnvm47t2fsZGM3UbxUFIWTLDz2rkO+a3jnggpTQ="]]

if (privateKey != None):
    privateattrs= [ ["byteArray",privateKey]]
    publicattrs = [ ["byteArray",publicKey]]
    sharedattrs = [ ["byteArray",sharedKey]]
    AdminConfig.create("Key",ltpa,privateattrs,"private")
    AdminConfig.create("Key",ltpa,publicattrs,"public")
    AdminConfig.create("Key",ltpa,sharedattrs,"shared")

print "set up custom user registry config"
attrs=[ ["customRegistryClassName","com.ibm.ws.xd.registry.XDRegistry"],
        ["serverId","admin"],
        ["serverPassword","{xor}PjsyNjE="],
        ["limit","0"],
        ["realm","XDRealm"],
        ["ignoreCase","false"]]
AdminConfig.modify(cur,attrs)


print "set up console authorization"
authtable=AdminConfig.list("AuthorizationTableExt")
authzList=convertToList(AdminConfig.list("RoleAssignmentExt"))
for authz in authzList:
   gattrs=[["name","group1"]]
   AdminConfig.create("GroupExt",authz,gattrs)

print "saving configuration"
AdminConfig.save()
print "finished creating security config, syncing nodes"
nodesyncs=convertToList(AdminControl.queryNames("WebSphere:*,type=NodeSync"))
for nodesync in nodesyncs:
   print "syncing: "+nodesync
   AdminControl.invoke(nodesync,"sync")
print "finished."
