cd "build\classes\"
start cmd /K rmiregistry 1099
timeout 3
java -Djava.rmi.server.codebase=file:/build\classes\ -Djava.security.policy=file:/server.policy test.vss.a4.server.VssA4Server
cmd