cd "build\classes\"
java -Djava.security.policy=file:/server.policy vss.a4.client.DistributedClient -Dsun.rmi.transport.tcp.responseTimeout=1000 192.168.1.66 192.168.1.66 1099