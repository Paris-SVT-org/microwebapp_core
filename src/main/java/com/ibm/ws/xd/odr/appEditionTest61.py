#
# longrun app edition test script
# author: brian k. martin  (bkmartin@us.ibm.com)
#
#
#
import jarray
import time
import math
import socket
import javax.management.ObjectName as ObjectName
import java.util.Properties as Properties
import java.util.zip.ZipOutputStream as ZipOutputStream
import java.util.zip.ZipEntry as ZipEntry
import java.io.FileOutputStream as FileOutputStream
import java.io.ByteArrayOutputStream as ByteArrayOutputStream
import java.io.OutputStreamWriter as OutputStreamWriter
import java.io.File as File
import java.io.FileInputStream as FileInputStream
import java.net.URL as URL
import java.net.HttpURLConnection as HttpURLConnection
import java.lang.Throwable as Throwable
import java.util.Date as Date
import java.lang.Thread as Thread
import java.lang.Runnable as Runnable
import java.lang.Float as Float
import java.lang.System as System
import java.util.Random as Random
import java.net.SocketTimeoutException as SocketTimeoutException
import java.net.ConnectException as ConnectException

workerThreads=2
random = Random()


responseTimes=[]
throughPuts=[]

class Worker (Runnable):
    running=1;
    odr=""
    appName=""
    resetSessions=0
    affinityCheck=1
    workerThread=None
    throughput=0
    mean=0.0;
    sumsq=0.0;
    samples=0.0;
    min=Float.MAX_VALUE
    max=Float.MIN_VALUE

    def __init__(self,odr,appName):
        self.running=1
        self.odr = odr
        self.appName = appName

    def stopRunning(self):
       self.running=0

    def disableAffinityCheck(self):
       self.affinityCheck=0

    def enableAffinityCheck(self):
       self.resetSessions=1

    def recordSample(self,sample):
       self.samples=self.samples+1
       oldmean=self.mean
       self.mean = self.mean + ((sample-self.mean)/self.samples)
       self.sumsq = self.sumsq+ (sample-oldmean)*(sample-self.mean)
       self.min = min(self.min,sample)
       self.max = max(self.max,sample)

    def getRTStdDev(self):
      return math.sqrt(self.sumsq/(self.samples-1)) 

    def run(self):
        cookieJar=[]
        sessions=1000
        startTime=System.currentTimeMillis()
        out("worker thread "+Thread.currentThread().getName()+" started")
        while (self.running == 1):
           startReq = System.currentTimeMillis()
           validateHttpRequest(self.odr,self.appName,None,0,cookieJar,sessions,50,self.affinityCheck)
           endReq = System.currentTimeMillis()
           self.recordSample(endReq-startReq)
           if (self.resetSessions == 1):
               cookieJar=[]
               self.affinityCheck=1
        endTime=System.currentTimeMillis()
        self.throughput = long(self.samples)/((endTime-startTime)/1000)
        out("worker thread "+self.workerThread.getName()+" completed "+str(long(self.samples))+" requests at a rate of "+str(self.throughput)+" req/sec")
        strMean = '%.01f' % self.mean
        strDev = '%.01f' % self.getRTStdDev()
        out("worker thread "+self.workerThread.getName()+" response time: min="+str(self.min)+" max="+str(self.max)+" mean="+strMean+" stdev="+strDev)


encodingStr=[]
for firstChar in 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-.':
   for secondChar in 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-.':
        estr= firstChar+secondChar
        encodingStr.append(estr) 


def genDataSet(datapoints):
    minv=0.0
    maxv=100.0
    if len(datapoints)>0:
      minv=datapoints[0]
      maxv=datapoints[0]
    for pt in datapoints:
       minv = min(minv,pt)
       maxv = max(maxv,pt)
    scaleMin = minv
    scaleMax = maxv
    diff=scaleMax-scaleMin
    scaleMin=scaleMin-(.1*diff)
    scaleMax=scaleMax+(.1*diff)
    if (diff < 20.0):
      scaleMin=scaleMin-5.0
      scaleMax=scaleMax+2.0
    scaleMax=math.floor((scaleMax+9.999)/10.0)*10.0
    scaleMin=math.floor((scaleMin-9.999)/10.0)*10.0
    if (minv>=0.0 and scaleMin < 0.0):
      scaleMin=0.0   
    while (math.floor(scaleMax/2.0)>maxv):
        scaleMax=math.floor(scaleMax/2.0)
    print "scaleMin="+str(scaleMin)
    print "scaleMax="+str(scaleMax)
    print "minv="+str(minv)
    print "maxv="+str(maxv)
    result=''
    scaleFactor=scaleMax-scaleMin
    for pt in datapoints:
        result=result+encodingStr[int((pt-scaleMin)*4095.0/scaleFactor)]
    return scaleMin,scaleMax,result
         


class HttpServerWorker (Runnable) :
   conn = None

   def __init__(self,conn):
     self.conn = conn
     t = Thread(self)
     t.start()
     

   def run(self):
        request = self.conn.recv(4096)
        self.conn.send("HTTP/1.0 200 OK\n")
        self.conn.send("Content-type: text/html\n")
        self.conn.send("\n")
        self.conn.send("<html><h1>AppEditionTest Script Performance</h1>")
        minv,maxv,result=genDataSet(throughPuts)
        self.conn.send("<img src=\"http://chart.apis.google.com/chart?chs=320x240&chd=e:"+result+"&cht=lc&chtt=Throughput+(req/sec)&chxt=y&chxr=0,"+str(minv)+","+str(maxv)+"&chg=20,20\" alt=\"Throughput\" />")
        self.conn.send("<br><br>")
        minv,maxv,result=genDataSet(responseTimes)
        self.conn.send("<img src=\"http://chart.apis.google.com/chart?chs=320x240&chd=e:"+result+"&cht=lc&chtt=Response+Time+(ms)&chxt=y&chxr=0,"+str(minv)+","+str(maxv)+"&chg=20,20\" alt=\"Response Time\" />")
        self.conn.send("</html>")
        self.conn.close()

class HttpServer (Runnable) :
   def __init__(self):
     t = Thread(self,"HTTPSERVER")
     t.start()

   def run(self):
      HOST = ''
      PORT = 8888 
      s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
      s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
      s.bind((HOST, PORT))
      s.listen(5)
      while 1:
          conn, addr = s.accept()
          print 'Connected by', addr
          httpWorker = HttpServerWorker(conn)

def convertToList(inlist):
     outlist=[]
     if (len(inlist)>0 and inlist[0]=='[' and inlist[len(inlist)-1]==']'):
        inlist = inlist[1:len(inlist)-1]
        clist = inlist.split("\"")
     else:
        clist = inlist.split("\n")
     for elem in clist:
        elem=elem.rstrip();
        if (len(elem)>0):
           outlist.append(elem)
     return outlist

def out(astring):
   print "["+str(Date())+"] "+astring

def getOdrAddresses():
     odrs=[]
     serverIndexes=convertToList(AdminConfig.list("ServerIndex"))
     for serverIdx in serverIndexes:
          serverEntries=convertToList(AdminConfig.list("ServerEntry",serverIdx))
          for serverEntry in serverEntries:
            type = AdminConfig.showAttribute(serverEntry,"serverType")
            if (type == "ONDEMAND_ROUTER"):
               namedEndPoints = convertToList(AdminConfig.list("NamedEndPoint",serverIdx))
               for namedEndPoint in namedEndPoints:
                  endPointName = AdminConfig.showAttribute(namedEndPoint,"endPointName")
                  if (endPointName=="PROXY_HTTP_ADDRESS"):
                         endPoint = convertToList(AdminConfig.list("EndPoint",namedEndPoint))[0]
                         port = AdminConfig.showAttribute(endPoint,"port")
                         host = AdminConfig.showAttribute(serverIdx,"hostName")
                         AdminConfig.reset()
                         odrs.append(host+":"+port)
     AdminConfig.reset()
     return odrs 


def getBytesForString(astring):
    baos = ByteArrayOutputStream()
    osw = OutputStreamWriter(baos,"UTF8")
    osw.write(astring)
    osw.flush()
    return baos.toByteArray()

def getFileBytes(afile):
    baos = ByteArrayOutputStream()
    fis = FileInputStream(afile)
    bytes = jarray.zeros(1024,'b')
    while 1==1:
       readSize = fis.read(bytes)
       if (readSize == -1):
           break
       baos.write(bytes,0,readSize)
    return baos.toByteArray()


def createWar(appName,edition):
    file = File(appName+".war")
    zip = ZipOutputStream(FileOutputStream(file))
    webXml="<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n"
    webXml+="<web-app id=\""+appName+"\" version=\"2.4\" xmlns=\"http://java.sun.com/xml/ns/j2ee\"\n"
    webXml+="   xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
    webXml+="   xsi:schemaLocation=\"http://java.sun.com/xml/ns/j2ee http://java.sun.com/xml/ns/j2ee/web-app_2_4.xsd\">\n"
    webXml+="</web-app>\n"
    bytes=getBytesForString(webXml)
    zip.putNextEntry(ZipEntry("WEB-INF/web.xml"))
    zip.write(bytes,0,len(bytes))
    zip.closeEntry()
    indexJsp="<% response.setHeader(\"serverVersion\",\""+edition+"\"); %>\n"
    indexJsp+="<html><h1>"+appName+" edition: "+edition+"</h1></html>\n"
    indexJsp+="<p>This is application edition: "+edition+"<br>\n"
    indexJsp+="<% Integer count=(Integer)session.getAttribute(\"count\");\n"
    indexJsp+="   if (count==null) {count=new Integer(0);}\n"
    indexJsp+="   count = new Integer(count.intValue()+1);\n"
    indexJsp+="   session.setAttribute(\"count\",count); %>\n"
    indexJsp+="<p>Session count: <%= session.getAttribute(\"count\") %><br>\n"
    indexJsp+="<p>Session id   : <%= session.getId() %><br>\n"
    indexJsp+="</p></html>\n"
    bytes=getBytesForString(indexJsp)
    zip.putNextEntry(ZipEntry("index.jsp"))
    zip.write(bytes,0,len(bytes))
    zip.closeEntry()
    logoutJsp="<% response.setHeader(\"serverVersion\",\""+edition+"\"); %>\n"
    logoutJsp+="<html><h1>"+appName+" edition: "+edition+"</h1></html>\n"
    logoutJsp+="<p>LOGOUT: This is application edition: "+edition+"<br>\n"
    logoutJsp+="<p>Session id   : <%= session.getId() %><br>\n"
    logoutJsp+="</p></html>\n"
    logoutJsp+="<% session.invalidate(); %>\n"
    bytes=getBytesForString(logoutJsp)
    zip.putNextEntry(ZipEntry("logout.jsp"))
    zip.write(bytes,0,len(bytes))
    zip.closeEntry()
    zip.close()
    return file

def createApplication(appName,edition):
    warfile=createWar(appName,edition)
    appfile = File(appName+".ear")
    zip = ZipOutputStream(FileOutputStream(appfile))
    appXml="<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n"
    appXml+="<application xmlns=\"http://java.sun.com/xml/ns/j2ee\"\n"
    appXml+="         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
    appXml+="         xsi:schemaLocation=\"http://java.sun.com/xml/ns/j2ee\n"
    appXml+="         http://java.sun.com/xml/ns/j2ee/application_1_4.xsd\"\n"
    appXml+="         version=\"1.4\">\n"
    appXml+="   <module><web><web-uri>"+warfile.getName()+"</web-uri><context-root>"+appName+"</context-root></web></module>\n"
    appXml+="</application>\n"
    bytes=getBytesForString(appXml)
    zip.putNextEntry(ZipEntry("META-INF/application.xml"))
    zip.write(bytes,0,len(bytes))
    zip.closeEntry()
    zip.putNextEntry(ZipEntry(warfile.getName()))
    bytes=getFileBytes(warfile)
    zip.write(bytes,0,len(bytes))
    zip.closeEntry()
    zip.close()
    return appfile

class Cookie:
    requests=0
    value=""
    edition=""

def validateHttpRequest(odr,appname,edition,printGood=1,cookieJar=None,sessions=0,requestsPerSession=50,checkAffinity=1):
   page="http://"+odr+"/"+appname+"/index.jsp"
   cookie=None
   if (cookieJar != None and sessions > 0):
       if (len(cookieJar)==sessions):
           cookie = cookieJar.pop(random.nextInt(len(cookieJar)))
           cookie.requests = cookie.requests+1
           if (cookie.requests<requestsPerSession):
              cookieJar.append(cookie)
           else:
              page="http://"+odr+"/"+appname+"/logout.jsp"
   url = URL(page)
   conn = url.openConnection()
   conn.setReadTimeout(60000)
   if (edition!=None):
      conn.setRequestProperty("version",edition)
   if (cookie!=None):
      conn.setRequestProperty("Cookie",cookie.value)
   try:
      stream = conn.getInputStream()
      bytes = jarray.zeros(1024,'b')
      while 1==1:
         readSize = stream.read(bytes)
         if (readSize == -1):
             break
   except SocketTimeoutException:
      out("received read timeout processing request (hung request?")
      if (cookie !=None):
         out("cookie="+cookie.value)
      sys.exit(1)
   except ConnectException:
      out("unable to connect to "+odr)
      sys.exit(1)
   except Throwable:
      out("error occured during http request")
   if (conn.getResponseCode()!=200):
       out("odr="+odr)
       out("http error response code requesting: "+str(url))
       out("response code:"+str(conn.getResponseCode()))
       sys.exit(1)
   if (edition!=None and conn.getHeaderField("serverVersion")!=edition):
       out("odr="+odr)
       out("received response from wrong edition.")
       out("expecting "+edition)
       out("received "+conn.getHeaderField("serverVersion"))
       sys.exit(1)
   if (printGood == 1):
      if (edition == None):
         out("response for edition <default> is OK")
      else:
         out("response for edition "+edition+" is OK")
   if (cookieJar != None and sessions > 0):
      setcookie = conn.getHeaderField("Set-Cookie")
      if (setcookie == None and cookie == None):
         out("odr="+odr)
         out("did not receive expected set-cookie")
         sys.exit(1)
      if (cookie == None):
          cookie = Cookie()
          cookie.value=setcookie
          cookie.requests=1
          cookie.edition = conn.getHeaderField("serverVersion")
          cookieJar.append(cookie)
      elif (setcookie !=None):
         if (checkAffinity == 1 and cookie.edition == conn.getHeaderField("serverVersion")):
             out("odr="+odr)
             out("unexpected loss of session affinity on editon: "+cookie.edition)
             out("cookie replaced old="+cookie.value+" new="+setcookie)
             sys.exit(1)
         if (checkAffinity == 1):
              out("cookie replaced due to edition change old="+cookie.value+" new="+setcookie)
         cookie.value=setcookie
         cookie.edition=conn.getHeaderField("serverVersion")

   return conn.getHeaderField("serverVersion")


def getInstalledAppVersions(appName):
       ret=[]
       list = AdminApp.list().split("\n")
       for l in list:
           if (l.startswith(appName)):
               ret.append(l)
               out("installed app: "+l)
       return ret

def uninstallApplication(appName):
     out("uninstalling "+appName)
     AdminApp.uninstall(appName)


def getDynamicClusters(clusterPrefix):
    ret = []
    clusters = convertToList(AdminConfig.list("DynamicCluster"))
    for cluster in clusters:
        clusterName = AdminConfig.showAttribute(cluster,"name")
        if (clusterName.startswith(clusterPrefix)):
           ret.append(clusterName)
    return ret


def stopDynamicCluster(cluster):
    out("stopping cluster "+cluster)
    setClusterOperationalMode(cluster,"manual")
    time.sleep(10)
    while 1==1:
        runningInstances = getRunningInstances(cluster)
        if  (len(runningInstances)==0):
           break
        for instance in runningInstances:
            out("stopping: "+instance.getKeyProperty("name"))
            AdminControl.invoke_jmx(instance,"stopImmediate",None,None)
        time.sleep(30)

def getRunningInstances(cluster):
    mbean=AdminControl.queryNames("WebSphere:*,process=dmgr,type=DynamicClusterRuntimeManager")
    runningInstances = AdminControl.invoke_jmx(ObjectName(mbean),"runningDynamicClusterInstances",[cluster],['java.lang.String']).toArray()
    return runningInstances


def waitForRunningInstances(clusterName,instances=2,timeout=180):
   loops=0
   while len(getRunningInstances(clusterName))!=instances and loops < (timeout/10):
      loops=loops+1
      time.sleep(10)
   startedInstances=getRunningInstances(clusterName)
   if (len(startedInstances)<instances):
      out("placement controller failed to start cluster "+clusterName)
      sys.exit(1)
   out("cluster: "+clusterName+" running instances:")
   for instance in startedInstances:
      out("started: "+instance.getKeyProperty("name"))
   time.sleep(30)

def setClusterOperationalMode(cluster,mode):
    mbean=AdminControl.queryNames("WebSphere:*,process=dmgr,type=DynamicClusterConfigManager")
    AdminControl.invoke(mbean,"setOperationalMode","["+cluster+" "+mode+"]")

def deleteDynamicCluster(cluster):
    mbean=AdminControl.queryNames("WebSphere:*,process=dmgr,type=DynamicClusterConfigManager")
    stopDynamicCluster(cluster)
    out("deleting cluster "+cluster)
    AdminControl.invoke(mbean,"deleteDynamicCluster","["+cluster+"]")

def createDynamicCluster(cluster):
    out("creating cluster "+cluster)
    mbean=AdminControl.queryNames("WebSphere:*,process=dmgr,type=DynamicClusterConfigManager")
    dcProp = Properties()
    dcProp.put("minInstances","2")
    dcProp.put("minNodes",2)
    clusterProperties = Properties()
    clusterProperties.put("templateName","defaultXD")
    membershipPolicy = "node_nodegroup = \'DefaultNodeGroup\' AND node_property$com.ibm.websphere.wxdopProductShortName = \'WXDOP\'"
    AdminControl.invoke_jmx(ObjectName(mbean),"createDynamicCluster",
                            [cluster,dcProp,clusterProperties,membershipPolicy],
                            ["java.lang.String","java.util.Properties","java.util.Properties","java.lang.String"])

def modifyClusterHeapSize(cluster,maximumHeapSize):
    out("modifying heapsize for cluster "+cluster+" to "+str(maximumHeapSize)+"M")
    id=AdminConfig.getid("/DynamicCluster:"+cluster+"/Server:"+cluster)
    processDefinitions=convertToList(AdminConfig.list("JavaProcessDef",id))
    for processDef in processDefinitions:
        jvmEntries = convertToList(AdminConfig.showAttribute(processDef,"jvmEntries"))
        for jvmEntry in jvmEntries:
              AdminConfig.modify(jvmEntry,[["maximumHeapSize", maximumHeapSize]])

def activateEdition(appName,edition):
    AdminTask.activateEdition(['-appName',appName,'-edition',edition])

def rolloutEdition(appName,edition):
    out("rolling out app "+appName+" edition "+edition)
    AdminTask.rolloutEdition(['-appName',appName,'-edition',edition,'-params','{{rolloutStrategy grouped} {resetStrategy hard} {groupSize 1} {drainageInterval 30}}'])
    out("rolled out app "+appName+" edition "+edition)

def installApplication(appName,earfile,cluster,edition):
    out("-------------------------------------------------------------------------------")
    out("installing application "+earfile.getName()+" edition: "+edition)
    out("-------------------------------------------------------------------------------")
    AdminApp.install(earfile.getAbsolutePath(), "[ -edition "+edition+" -cluster "+cluster+" -usedefaultbindings ]")

def findRoutingWorkClass(appName):
   workclasses=convertToList(AdminConfig.list("WorkClass"))
   for workclass in workclasses:
       name = AdminConfig.showAttribute(workclass,"name")
       if (name == "Default_HTTP_WC" and workclass.find("deployments")==-1 and workclass.find("applications/"+appName)!=-1):
           return workclass
   return None

def removeExistingMatchRules(workclass):
    matchRules=convertToList(AdminConfig.list("MatchRule",workclass))
    for rule in matchRules:
        AdminConfig.remove(rule)

def addMatchRule(workclass,matchAction,matchExpression):
    matchRules=convertToList(AdminConfig.list("MatchRule",workclass))
    priority=len(matchRules)
    attrs = [ [ "matchAction",matchAction],["matchExpression",matchExpression],["priority",priority]]
    AdminConfig.create("MatchRule",workclass,attrs)

def addMatchRuleForEdition(appName,edition):
    workclass=findRoutingWorkClass(appName)
    addMatchRule(workclass,"permit:"+appName+"-edition"+edition,"header$version = \'"+edition+"\'")
    addMatchRule(workclass,"permit:"+appName+"-edition"+edition,"queryparm$edition = \'"+edition+"\'")

def clearMatchRules(appName):
    workclass=findRoutingWorkClass(appName)
    removeExistingMatchRules(workclass)

def syncNodes():
    out("synchronizing cell")
    nodes=convertToList(AdminControl.queryNames("WebSphere:*,type=NodeSync"))
    for node in nodes:
          out("synchronizing "+node)
          syncresult=AdminControl.invoke(node,'sync')
    time.sleep(15)

def getWasNodes():
    nodes=[]
    syncnodes=AdminControl.queryNames_jmx(ObjectName("WebSphere:*,type=NodeSync"),None)
    for node in syncnodes:
       nodes.append(node.getKeyProperty("node"))
    return nodes

def createOdr():
   out("no odr found in configuration.")
   nodes=getWasNodes()
   out("creating odr on node "+nodes[0])
   AdminTask.createOnDemandRouter(nodes[0],"-name odr -templateName http_sip_odr_server")
   AdminConfig.save()
   syncNodes()
   nodeAgent=convertToList(AdminControl.queryNames("WebSphere:*,type=NodeAgent,node="+nodes[0]))
   if len(nodeAgent) == 0:
      out("nodeagent not running on node:"+nodes[0])
      sys.exit(1)
   out("starting odr on node "+nodes[0])
   AdminControl.invoke(nodeAgent[0],"launchProcess","odr")

def odrDebug():
  out("setting odr debug options")
  mbeanStr='WebSphere:*,type=ODRDebug'
  mbeans=convertToList(AdminControl.queryNames(mbeanStr))
  for mbean in mbeans:
    AdminControl.invoke(mbean,'setHttpDebug','404 true 1')
    AdminControl.invoke(mbean,'setHttpDebug','503 true 1')
    AdminControl.invoke(mbean,'setHttpDebug','500 true 1')


#
#
#
#  main()
#
#
#

appName="LongRunTest"
clusterName=appName
odrs=getOdrAddresses()

if len(odrs)==0:
   createOdr()
odrs=getOdrAddresses()
   
if len(odrs)==0:
   out("error: no ODRs configured in cell")
   sys.exit(1)
out("-------------------------------------------------------------------------------")
out(" long run test initializing")
out(" application name: "+appName)
for odr in odrs:
   out(" odr: "+odr)
out(" http worker threads: "+str(workerThreads))
out("-------------------------------------------------------------------------------")
out("")

httpserver=HttpServer()

odrDebug()

out("-------------------------------------------------------------------------------")
out(" cleaning up artifacts from previous run")
out("-------------------------------------------------------------------------------")
installedApps=getInstalledAppVersions(appName)
for app in installedApps:
     uninstallApplication(app)
AdminConfig.save()

clusters=getDynamicClusters(clusterName)
for cluster in clusters:
    deleteDynamicCluster(cluster)
AdminConfig.save()


out("-------------------------------------------------------------------------------")
out(" initializing test ")
out("-------------------------------------------------------------------------------")
out("")
createDynamicCluster(clusterName)
AdminConfig.save()
AdminConfig.reset()
modifyClusterHeapSize(clusterName,"256")

currentVersion=1
previousVersion=currentVersion
verString=str(currentVersion)+".0"
earfile=createApplication(appName,verString)
installApplication(appName,earfile,clusterName,verString)
AdminConfig.save()
AdminConfig.reset()

activateEdition(appName,verString)
syncNodes()
setClusterOperationalMode(clusterName,"automatic")
AdminConfig.save()
AdminConfig.reset()

out("-------------------------------------------------------------------------------")
out(" waiting for servers to start in cluster "+clusterName)
out("-------------------------------------------------------------------------------")
out("")
waitForRunningInstances(clusterName)

iteration=0

while (1 == 1):
   
   iteration=iteration+1
   out("-------------------------------------------------------------------------------")
   out("start of iteration: "+str(iteration))
   out("-------------------------------------------------------------------------------")

   workers = []
   for wi in range(workerThreads):
      for odr in odrs:
         worker = Worker(odr,appName)
         worker.workerThread = Thread(worker,"HTTP:"+str(wi))
         worker.workerThread.start()
         workers.append(worker)

   currVer=str(currentVersion)+".0"
   concVer=str(currentVersion)+".1"
   nextVer=str(currentVersion+1)+".0"

   concCluster=clusterName+"_"+str(currentVersion%20)
   createDynamicCluster(concCluster)
   AdminConfig.save()
   AdminConfig.reset()
   modifyClusterHeapSize(concCluster,"256")

   earfile=createApplication(appName,concVer)
   installApplication(appName,earfile,concCluster,concVer)
   addMatchRuleForEdition(appName,concVer)
   AdminConfig.save()
   AdminConfig.reset()

   earfile=createApplication(appName,nextVer)
   installApplication(appName,earfile,clusterName,nextVer)
   addMatchRuleForEdition(appName,nextVer)
   AdminConfig.save()
   AdminConfig.reset()

   activateEdition(appName,concVer)
   AdminConfig.save()
   AdminConfig.reset()
   syncNodes()

   setClusterOperationalMode(concCluster,"automatic")
   AdminConfig.save()
   AdminConfig.reset()
   syncNodes()

   out("-------------------------------------------------------------------------------")
   out(" waiting for servers to start in concurrent cluster "+concCluster)
   out("-------------------------------------------------------------------------------")
   out("")
   waitForRunningInstances(concCluster)
   waitForRunningInstances(clusterName)

   out("-------------------------------------------------------------------------------")
   out(" validating http requests for concurrent editions")
   out("-------------------------------------------------------------------------------")
   out("")
   for odr in odrs:
      out("validating request to "+currVer);
      validateHttpRequest(odr,appName,currVer)
      out("validating request to "+concVer);
      validateHttpRequest(odr,appName,concVer)
      out("validating request to default version");
      recvVer=validateHttpRequest(odr,appName,None)
      if (recvVer!=currVer):
          out("received response from non-default edition expected="+currVer+" received="+recvVer)
          out("odr="+odr)
          sys.exit(1)
   out("-------------------------------------------------------------------------------")
   out("rolling out new edition")
   out("-------------------------------------------------------------------------------")
   for worker in workers:
      worker.disableAffinityCheck()
   rolloutEdition(appName,nextVer)
   for worker in workers:
      worker.enableAffinityCheck()
   out("-------------------------------------------------------------------------------")
   out(" validating http requests for new default edition and concurrent edition")
   out("-------------------------------------------------------------------------------")
   for odr in odrs:
      out("validating request to default version");
      recvVer=validateHttpRequest(odr,appName,None)
      if (recvVer!=nextVer):
         out("received response from non-default edition expected="+nextVer+" received="+recvVer)
         sys.exit(1)
      out("validating request to "+concVer);
      validateHttpRequest(odr,appName,concVer)
   previousVersion=currentVersion
   currentVersion=currentVersion+1
   out("-------------------------------------------------------------------------------")
   out(" removing old apps and clusters")
   out("-------------------------------------------------------------------------------")
   stopDynamicCluster(concCluster)
   installedApps=getInstalledAppVersions(appName)
   for app in installedApps:
     if (app.find(currVer)!=-1 or app.find(concVer)!=-1):
         uninstallApplication(app)
   AdminConfig.save()
   AdminConfig.reset()
   deleteDynamicCluster(concCluster)
   AdminConfig.save()
   AdminConfig.reset()
   out("-------------------------------------------------------------------------------")
   out(" running light load for a while")
   out("-------------------------------------------------------------------------------")
   time.sleep(300)
   for worker in workers:
      worker.stopRunning()
   rps=0
   samples=0.0
   sum=0.0
   for worker in workers:
      worker.workerThread.join(1000)
      rps=rps+worker.throughput
      samples=samples+worker.samples
      sum=sum+(worker.mean*worker.samples)
   avgrt=sum/samples
   out("total throughput: "+str(rps)+" req/sec")
   out("avg response time: "+str(avgrt)+" ms")
   throughPuts.append(rps)
   responseTimes.append(avgrt)
   out("-------------------------------------------------------------------------------")
   out(" clearing existing match rules")
   out("-------------------------------------------------------------------------------")
   clearMatchRules(appName)
   out("-------------------------------------------------------------------------------")
   out("end of iteration: "+str(iteration))
   out("-------------------------------------------------------------------------------")
