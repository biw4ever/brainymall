FROM 10.159.62.232:5000/newjdk1.8
MAINTAINER njyjz

#容器运行时监听的端口
EXPOSE 8801

ADD yjz-zipkin.jar yjz-zipkin.jar

ENTRYPOINT java -Djava.security.egd=file:/dev/./urandom  -Xms128m -Xmx128m -XX:MetaspaceSize=128M -XX:MaxMetaspaceSize=128M -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/var/log/javadump -jar /yjz-zipkin.jar
