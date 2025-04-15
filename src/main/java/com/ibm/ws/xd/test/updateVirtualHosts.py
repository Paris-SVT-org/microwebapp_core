global virtualHost, port

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

def updateVirtualHost():
    host=AdminConfig.getid("/VirtualHost:"+virtualHost)
    aliases=convertToList(AdminConfig.showAttribute(host,"aliases"))
    found = 0
    for alias in aliases:
        hostname=AdminConfig.showAttribute(alias,"hostname")
        if (hostname == "*"):
            num=AdminConfig.showAttribute(alias,"port");
            if (num == port):
                found = 1
                break
                
    if (found == 0):
        attrs=[["hostname","*"],["port",port]]
        AdminConfig.create("HostAlias",host,attrs)


if (len(sys.argv) > 1):
    virtualHost=sys.argv[0].rstrip()
    port=sys.argv[1].rstrip()
    updateVirtualHost()
    print "saving workspace"
    AdminConfig.save()
    print "finished."
else:
    print "Missing arguments"
