cd "build\classes\"
cmd /K java -Djava.security.policy=file:/server.policy vss.a4.server.UserInterface 192.168.1.58
cmd