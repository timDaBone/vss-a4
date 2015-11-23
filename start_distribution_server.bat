cd "build\classes\"
start cmd /K rmiregistry 1099
timeout 3
start cmd /K java -Djava.security.policy=file:/server.policy -Dsun.rmi.transport.connectionTimeout=1000 -Dsun.rmi.transport.tcp.responseTimeout=1000 vss.a4.server.DistributionServer