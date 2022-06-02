docker container stop zookeeper-cluster-1 zookeeper-cluster-2 zookeeper-cluster-3
docker container rm zookeeper-cluster-1 zookeeper-cluster-2 zookeeper-cluster-3
docker network rm myzkcomposeprj_brzk-kafka

sudo rm -rf zookeeper-*
