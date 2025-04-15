from java.lang import System

cellName="cell1"
ihs="xdblade06b13"
odrNodeName="titan09"
appNodeName="draco08"
ngName="AppServerGroup"
dcName="AppCluster"
gcName="ODRsCell2"
appName="A"
application="/root/bin/A.ear"
odr1="ODR1"
odr2="ODR2"
cell2odrNode="xdblade03b10"
suffix=".rtp.raleigh.ibm.com"

#==================================================================================
#
# create node group and add the node which will hold the applications as a member
#
#==================================================================================

print " ***** creating node group: " + ngName + " ..."
print "       checking if node group exists ... "
if(AdminConfig.getid('/Cell:' + cellName + '/NodeGroup:' + ngName + '/') == ""):
     print "        node group not yet created, creating ..."
     AdminTask.createNodeGroup(ngName)
else:
     print "        " + ngName + " has already been created, skipping step"

print " ***** adding node group " + ngName + " member: " + appNodeName + "..."
print "       checking if node group member exists ... "
if(AdminConfig.getid('/NodeGroup:' + ngName + '/NodeGroupMember:/') == ""):
     print "        node group member not yet added, adding ..."
     AdminTask.addNodeGroupMember(ngName, "[-nodeName "+appNodeName+"]")
else:
     print "        " + appNodeName + " is already a member of the node group " + ngName + ", skipping step"

AdminConfig.save()

#==================================================================================
#
# create a dynamic cluster using the previously created node group and allow 
# vertical stacking of applications
#
#==================================================================================

print " ***** creating dynamic cluster: " + dcName + "..."
print "       checking if dynamic cluster already exists ..."
if(AdminConfig.getid('/Cell:' + cellName + '/DynamicCluster:' + dcName + '/') == ""):
     print "        dynamic cluster not yet created, creating ..."
     AdminTask.createDynamicCluster(dcName, '[-membershipPolicy "node_nodegroup = \''+ngName+'\'"]')
     AdminTask.setDynamicClusterVerticalInstances(dcName, '[-numVerticalInstances 2]')
else:
     print "        " + dcName + " has already been created, skipping step"

AdminConfig.save()

#==================================================================================
#
# install application on node used previously as the node group member
#
#==================================================================================

print " ***** installing application " + appName + "..."
apps = AdminApp.list()
print "       checking if application " + appName + " is already installed ..."
if(apps.find(appName) < 0):
     print "        application " + appName + " is not installed, installing ..."
     AdminApp.install(application, "[-appname "+appName+" -cluster "+dcName+"]")
else:
     print "        application " + appName + " is already installed, skipping step"

#==================================================================================
#
# sets cell custom properties to enable session affinity and plugin file generation
#
#==================================================================================

print " ***** setting cell properties to enable session affinity and generate a plugin-cfg.xml file..."
print "       checking if properties already exist..."
cell=AdminConfig.getid('/Cell:' + cellName)
props = AdminConfig.show(cell, ['properties'])
propValue = cellName + ":" + odrNodeName + ":*"
attributeName1 = "odrSessionAffinityEnabled" 
attributeName2 = "ODCPluginCfgOdrList_cfg1"
attributeName3 = "ODCPluginCfgOutputPath_cfg1"
if(props.find(attributeName1) < 0):
     print "        " + attributeName1 + " property has not been created, creating ..."
     attrs=[['name', attributeName1],['value','true']]
     AdminConfig.create('Property', cell, attrs)
else:
     print "        " + attributeName1 + " property has already been created, skipping step"
if(props.find(attributeName2) < 0):
     print "        " + attributeName2 + " property has not been created, creating ..."
     attrs=[['name',attributeName2],['value',propValue]]
     AdminConfig.create('Property', cell, attrs)
else:
     print "        " + attributeName2 + " property has already been created, skipping step"
if(props.find(attributeName3) < 0):
     print "        " + attributeName3 + " property has not been created, creating ..."
     attrs=[['name',attributeName3],['value','/tmp/plugin-cfg1.xml']]
     AdminConfig.create('Property', cell, attrs)
else:
     print "        " + attributeName3 + " property has already been created, skipping step"

#==================================================================================
#
# adds trusted security proxies on each ODR for:
#      all other ODRs on remote cell
#      IBM HTTP Server
#
#==================================================================================

print " ***** Add trusted security proxies on each ODR for all other ODRs on remote cells and IBM HTTP Server"
odr2id = AdminConfig.getid('/Server:' + odr2 + '/')
odr1id = AdminConfig.getid('/Server:' + odr1 + '/')
odr1proxyid = AdminConfig.list('ProxySettings', odr1id)
odr2proxyid = AdminConfig.list('ProxySettings', odr2id)
ihsProp = ihs + suffix
cell2Prop = cell2odrNode + suffix
proxiesOdr1 = AdminConfig.show(odr1proxyid, ['trustedIntermediaryAddresses'])
proxiesOdr2 = AdminConfig.show(odr2proxyid, ['trustedIntermediaryAddresses'])

print "       check if trusted security proxy " + ihsProp + " already exists for " + odr1 + "..."
if(proxiesOdr1.find(ihsProp) < 0):
     print "        trusted security proxy " + ihsProp + " does not exist for " + odr1 + ", adding ..."
     AdminConfig.modify(odr1proxyid, [['trustedIntermediaryAddresses', ihsProp]])
else:
     print "        trusted security proxy " + ihsProp + " already exists for " + odr1 + ", skipping step"
print "       check if trusted security proxy " + cell2Prop + " already exists for " + odr1 + "..."
if(proxiesOdr1.find(cell2Prop) < 0):
     print "        trusted security proxy " + cell2Prop + " does not exist for " + odr1 + ", adding ..."
     AdminConfig.modify(odr1proxyid, [['trustedIntermediaryAddresses', cell2Prop]])
else:
     print "        trusted security proxy " + cell2Prop + " already exists for " + odr1 + ", skipping step"
print "       check if trusted security proxy " + ihsProp + " already exists for " + odr2 + "..."
if(proxiesOdr2.find(ihsProp) < 0):
     print "        trusted security proxy " + ihsProp + " does not exist for " + odr2 + ", adding ..."
     AdminConfig.modify(odr2proxyid, [['trustedIntermediaryAddresses', ihsProp]])
else: 
     print "        trusted security proxy " + ihsProp + " already exists for " + odr2 + ", skipping step"
print "       check if trusted security proxy " + cell2Prop + " already exists for " + odr2 + "..."
if(proxiesOdr2.find(cell2Prop) < 0):
     print "        trusted security proxy " + cell2Prop + " does not exist for " + odr2 + ", adding ..."
     AdminConfig.modify(odr2proxyid, [['trustedIntermediaryAddresses', cell2Prop]])
else:
     print "        trusted security proxy " + cell2Prop + " already exists for " + odr2 + ", skipping step"

#==================================================================================
#
# add multi-cluster routing rule for ODRs on failover
#
#==================================================================================

print " ***** Create multi-cluster routing rule for ODRs on failover"
routeLocations="cluster=*/" + dcName + ",cluster=*/" + gcName
existingRules1 = AdminTask.listRoutingRules('[-odrname '+odr1+' -nodename '+odrNodeName+' -protocol HTTP]')
existingRules2 = AdminTask.listRoutingRules('[-odrname '+odr2+' -nodename '+odrNodeName+' -protocol HTTP]')
print "       checking if rule exists for " + odr1 + "..."
if(existingRules1.find(routeLocations) < 0):
     print "        multicluster routing rule does not exist for " + odr1 + ", creating ..."
     AdminTask.addRoutingRule('[-odrname '+odr1+' -nodename '+odrNodeName+' -protocol HTTP -priority 0 -expression 1=1 -actionType permit -multiclusterAction Failover -routingLocations '+routeLocations+']')
else:
     print "        multicluster routing rule already exists for " + odr1 + ", skipping step"
print "       checking if rule exists for " + odr2 + "..."
if(existingRules2.find(routeLocations) <0):
     print "        multicluter routing rule does not exist for " + odr2 + ", creating ..."
     AdminTask.addRoutingRule('[-odrname '+odr2+' -nodename '+odrNodeName+' -protocol HTTP -priority 0 -expression 1=1 -actionType permit -multiclusterAction Failover -routingLocations '+routeLocations+']')
else:
     print "        multicluster routing rule already exists for " + odr2 + ", skipping step"

#==================================================================================
#
# create generic server cluster of ODRs on remote cell
#
#==================================================================================

print " ***** create a generic server cluster with each ODR port on remote cell"
cellInfo = AdminConfig.getid('/Cell:' + cellName) 
attrs = [['name', gcName], ['protocol', 'HTTP']]
existingGSCs = AdminConfig.getid('/GenericServerCluster:/')
serverProp1 = [['name', 'server'], ['value', 'odr1' + cellName]]
serverProp2 = [['name', 'server'], ['value', 'odr2' + cellName]]
port1 = [['host', cell2odrNode], ['port', 80], ['properties', [serverProp1]]]
port2 = [['host', cell2odrNode], ['port', 81], ['properties', [serverProp2]]]
print "       checking if generic server cluster " + gcName + " exists ..."
if(existingGSCs.find(gcName) < 0):
     print "        generic server cluster " + gcName + " does not exist, creating ..."
     gsc1 = AdminConfig.create('GenericServerCluster', cellInfo, attrs) 
     AdminConfig.modify(gsc1, [['genericServerEndpoints', [port1, port2]]])
else:
     print "        generic server cluster " + gcName + " already exists, skipping step"

print "saving configuration..."
AdminConfig.save()


