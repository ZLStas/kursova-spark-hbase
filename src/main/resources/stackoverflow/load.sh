#kubectl exec -it stas-hbase-hdfs-nn-0 -- bin/hadoop fs -cd /user/stanislav
#kubectl exec -it stas-hbase-hdfs-nn-0 -- bin/hadoop fs -chown stanislav:stanislav /usr/stanislav
#kubectl exec -it stas-hbase-hdfs-nn-0 -- bin/hadoop fs -put /home/stanislav/learning/kursova-spark-hbase/src/main/resources/stackoverflow/stackoverflow.csv stackoverflow.csv
#kubectl exec -it stas-hbase-hdfs-nn-0 -- bin/hadoop fs -ls /resources/
sudo kubectl exec -it stas-hbase-hdfs-nn-0 -- bin/hadoop fs -ls /