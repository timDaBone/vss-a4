cd "build\classes\"
start cmd /K rmiregistry 1099
timeout 3
start cmd /K java -Djava.rmi.server.codebase=file:/build\classes\ -Djava.security.policy=file:/server.policy vss.a4.server.VssA4Server
PAUSE