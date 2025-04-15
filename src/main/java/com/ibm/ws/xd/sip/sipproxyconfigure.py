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


allsps = convertToList(AdminConfig.getid("/SIPProxySettings:/"))
for sps in allsps:
   print sps
   AdminConfig.create('SIPRoutingRule' ,sps, [['isEnabled','true'],['clusterName','TestClusterA']])
   AdminConfig.modify(sps, [['defaultClusterName', 'TestClusterA']])


#
# Create SIP service rules
#

allrules = convertToList(AdminConfig.getid("/Server:odr/Rules:service_SIP_serverRequest"))
for rules in allrules:
   print rules
   attrs= [ ["matchExpression","request.uri like '/A/%'"],
            ["matchAction","Default_TC_Platinum"],
            ["priority","0"]
          ]
   AdminConfig.create("MatchRule",rules,attrs)
   attrs= [ ["matchExpression","request.uri like '/A/%'"],
            ["matchAction","Default_TC_Silver"],
            ["priority","1"]
          ]
   AdminConfig.create("MatchRule",rules,attrs)


AdminConfig.save()

