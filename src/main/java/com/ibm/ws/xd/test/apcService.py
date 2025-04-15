import sys

def findAPC():
    sys.stdout.write("apcService.py:findAPC \n")
    return AdminConfig.list("AppPlacementController")

def modifyAPC(apc, name, value):
    sys.stdout.write("apcService.py:modifyAPC   apc="+apc+"\n")
    sys.stdout.write("apcService.py:modifyAPC  name="+name+"\n")
    sys.stdout.write("apcService.py:modifyAPC value="+value+"\n")
    AdminConfig.modify(apc,[[name,value]])
    return

def saveAPC():
    sys.stdout.write("apcService.py:saveAPC\n")
    AdminConfig.save()
    return 


sys.stdout.write("apcService.py: \n")
if len(sys.argv) < 2 or len(sys.argv) > 2:
    sys.stdout.write("apcService.py: Invalid number of arguments\n")
else:
    apc=findAPC()
    modifyAPC(apc, sys.argv[0], sys.argv[1])
    saveAPC()
