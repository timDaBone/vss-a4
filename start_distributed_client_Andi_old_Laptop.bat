cd "build\classes\"
start cmd /K rmiregistry 1099
timeout 3
java -Djava.security.policy=file:/server.policy vss.a4.client.DistributedClient 192.168.1.66 192.168.1.67 1099
cmd