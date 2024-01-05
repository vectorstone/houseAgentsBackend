# 基础镜像,必须存在
FROM openjdk:8u312-jdk-oracle
# 作者信息
MAINTAINER gavin
#系统编码
ENV LANG=C.UTF-8 LC_ALL=C.UTF-8
#声明一个挂载点，容器内此路径会对应宿主机的某个文件夹

# 解决时区的问题
RUN ln -snf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo "Asia/Shanghai" > /etc/timezone

VOLUME /tmp
# 向外暴露的端口
EXPOSE 8888
# 要添加到镜像中的文件
ADD target/system-service.jar /system-service.jar
# 指定容器内的工作路径为根路径
WORKDIR /
# echo "Asia/Shanghai" > /etc/timezone
# 执行的命令会在工作路径下执行
# 在这里不要使用nohup的命令,不然的话docker的控制台里面会没有对应的日志输出(虽然通过logback.xml里面配置的日志的输出的方式和路径会依然生效)
#ENTRYPOINT ["nohup" , "java" , "-jar" , "system-service.jar",">springboot.log" ,"2>&1", "&"]
ENTRYPOINT ["java" , "-jar" , "/system-service.jar"]