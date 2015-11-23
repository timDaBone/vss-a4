cd "build\classes\"
start cmd /K rmiregistry 1099
timeout 3
java -Djava.security.policy=file:/server.policy -Dsun.rmi.transport.connectionTimeout=1000 -Dsun.rmi.transport.tcp.responseTimeout=1000 vss.a4.client.DistributedClient 192.168.1.66 192.168.1.58 1099
cmd