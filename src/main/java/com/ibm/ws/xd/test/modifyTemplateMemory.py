#  This program may be used, executed, copied, modified and distributed
#  without royalty for the purpose of developing, using, marketing, or distributing.

#
# script for modify min/max heap sizes for the templates
#
# author: bkmartin

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


templates=convertToList(AdminConfig.listTemplates("Server"))
for template in templates:
   cname = AdminConfig.showAttribute(template,"name")
   if (cname != None):
      servertype = AdminConfig.showAttribute(template,"serverType")
      if (servertype == "APPLICATION_SERVER"):
         print "modifying "+cname
         processDefs = convertToList(AdminConfig.list("JavaProcessDef",template))
         for processDef in processDefs:
            jvmEntries = convertToList(AdminConfig.showAttribute(processDef,"jvmEntries"))
            for jvmEntry in jvmEntries:
                AdminConfig.modify(jvmEntry,[['maximumHeapSize','256'],['initialHeapSize','128']])

print "saving workspace"
AdminConfig.save()
print "finished."

