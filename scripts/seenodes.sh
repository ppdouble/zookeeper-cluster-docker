docker ps -a | grep zookeeper
#查找ip
for i in `seq 1 3`;
do
	echo -n "$(docker inspect --format '{{ (index .NetworkSettings.Networks "myzkcomposeprj_brzk-kafka").IPAddress }}' "zookeeper-cluster-$i")":2181$i" ";
  echo "stat" | nc localhost ${i}2181 | grep "Mode";
done

