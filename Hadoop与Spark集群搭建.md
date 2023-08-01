# Hadoop集群搭建

基于Hadoop2.7.4搭建伪分布式集群

上传压缩包，解压，在etc/hadoop目录下配置以下文件

**hadoop.env.sh配置**

找到JAVA_HOME的配置，将其修改为机器上的java绝对路径，**不能使用环境变量，hadoop读取不到**

```
export JAVA_HOME=/export/servers/jdk1.8.0_281
```

**core-site.xml配置**

```xml
<configuration>
<!-- 用于设置Hadoop的文件系统，由URI指定 -->
 <property>
    <name>fs.defaultFS</name>y
    <value>hdfs://node1:9311</value>
 </property>
<!-- 配置Hadoop存储数据目录,默认/tmp/hadoop-${user.name} -->
 <property>
   <name>hadoop.tmp.dir</name>
   <value>/export/servers/hadoop-2.7.4/hadoop_data/tmp_data</value>
</property>
<!--  缓冲区大小，实际工作中根据服务器性能动态调整 -->
 <property>
   <name>io.file.buffer.size</name>
   <value>4096</value>
 </property>
<!--  开启hdfs的垃圾桶机制，删除掉的数据可以从垃圾桶中回收，单位分钟 -->
 <property>
   <name>fs.trash.interval</name>
   <value>10080</value>
 </property>
</configuration>
```

**hdfs-site.xml配置**

```xml
<configuration>
    <!-- 指定SecondaryNameNode的主机和端口 -->
    <property>
        <name>dfs.namenode.secondary.http-address</name>
        <value>node2:50090</value>
    </property>
    <!-- 指定namenode的页面访问地址和端口 -->
    <property>
        <name>dfs.namenode.http-address</name>
        <value>node1:50070</value>
    </property>
    <!-- 指定namenode元数据的存放位置 -->
    <property>
        <name>dfs.namenode.name.dir</name>
        <value>file:///export/servers/hadoop-2.7.4/hadoop_data/namenode_data</value>
    </property>
    <!--  定义datanode数据存储的节点位置 -->
    <property>
        <name>dfs.datanode.data.dir</name>
        <value>file:///export/servers/hadoop-2.7.4/hadoop_data/datanode_data</value>
    </property>
    <!-- 定义namenode的edits文件存放路径 -->
    <property>
        <name>dfs.namenode.edits.dir</name>
        <value>file:///export/servers/hadoop-2.7.4/hadoop_data/nn/edits</value>
    </property>
    <!-- 配置检查点目录 -->
    <property>
        <name>dfs.namenode.checkpoint.dir</name>
        <value>file:///export/servers/hadoop-2.7.4/hadoop_data/snn/name</value>
    </property>
    <property>
        <name>dfs.namenode.checkpoint.edits.dir</name>
        <value>file:///export/servers/hadoop-2.7.4/hadoop_data/dfs/snn/edits</value>
    </property>
    <!-- 文件切片的副本个数-->
    <property>
        <name>dfs.replication</name>
        <value>3</value>
    </property>
    <!-- 设置HDFS的文件权限-->
    <property>
        <name>dfs.permissions</name>
        <value>false</value>
    </property>
    <!-- 设置一个文件切片的大小：128M-->
    <property>
        <name>dfs.blocksize</name>
        <value>134217728</value>
    </property>
    <!-- 指定DataNode的节点配置文件 -->
    <property>
        <name>dfs.hosts</name>
        <value>/export/servers/hadoop-2.7.4/etc/hadoop/slaves</value>
    </property>
</configuration>
```

**mapred-site.xml配置**

```xml
<configuration>
    <!-- 指定分布式计算使用的框架是yarn -->

    <property>

        <name>mapreduce.framework.name</name>

        <value>yarn</value>

    </property>


    <!-- 开启MapReduce小任务模式 -->

    <property>

        <name>mapreduce.job.ubertask.enable</name>

        <value>true</value>

    </property>


    <!-- 设置历史任务的主机和端口 -->

    <property>

        <name>mapreduce.jobhistory.address</name>

        <value>node1:10020</value>

    </property>


    <!-- 设置网页访问历史任务的主机和端口 -->

    <property>

        <name>mapreduce.jobhistory.webapp.address</name>

        <value>node1:19888</value>

    </property>
</configuration>

```

**mapred-env.sh**

```
export JAVA_HOME=/export/servers/jdk1.8.0_281
```

**yarn-site.xml**

```xml
<configuration>
    <!-- 配置yarn主节点的位置 -->

    <property>

        <name>yarn.resourcemanager.hostname</name>

        <value>node1</value>

    </property>


    <property>

        <name>yarn.nodemanager.aux-services</name>

        <value>mapreduce_shuffle</value>

    </property>


    <!-- 开启日志聚合功能 -->

    <property>

        <name>yarn.log-aggregation-enable</name>

        <value>true</value>

    </property>

    <!-- 设置聚合日志在hdfs上的保存时间 -->

    <property>

        <name>yarn.log-aggregation.retain-seconds</name>

        <value>604800</value>

    </property>

    <!-- 设置yarn集群的内存分配方案 -->

    <property>

        <name>yarn.nodemanager.resource.memory-mb</name>

        <value>2048</value>

    </property>

    <property>

        <name>yarn.scheduler.minimum-allocation-mb</name>

        <value>2048</value>

    </property>

    <property>

        <name>yarn.nodemanager.vmem-pmem-ratio</name>

        <value>2.1</value>

    </property>
    <!-- Site specific YARN configuration properties -->

</configuration>

```

**slaves**

```
node1
node2
node3
```

配置完成，创建需要的目录

```
mkdir -p /export/servers/hadoop-2.7.4/hadoop_data/tmp_data

mkdir -p /export/servers/hadoop-2.7.4/hadoop_data/namenode_data

mkdir -p /export/servers/hadoop-2.7.4/hadoop_data/datanode_data

mkdir -p /export/servers/hadoop-2.7.4/hadoop_data/nn/edits

mkdir -p /export/servers/hadoop-2.7.4/hadoop_data/snn/name

mkdir -p /export/servers/hadoop-2.7.4/hadoop_data/dfs/snn/edits
```

将整个配置好的hadoop目录分发到其他节点

```
scp -r /export/servers/hadoop-2.7.4/ node2:/export/servers/

scp -r /export/servers/hadoop-2.7.4/ node3:/export/servers/
```

配置hadoop环境变量，三个节点都要配置

```
export HADOOP_HOME=/export/servers/hadoop-2.7.4
export PATH=:$HADOOP_HOME/bin:$HADOOP_HOME/sbin:$PATH
```

启动集群

首次启动加一个操作

```
hadoop namenode -format
```

一键启动/export/servers/hadoop-2.7.4/sbin下

```text
start-dfs.sh
start-yarn.sh
mr-jobhistory-daemon.sh start historyserver
```

页面访问

hadoop地址：192.168.182.129:50070

yarn管理器地址：192.168.182.129:8088

历史服务：192.168.182.129:19888/jobhistory

windows要加域名映射

```
192.168.182.129  node1
192.168.182.130  node2
192.168.182.131  node3
```

上传文件

```
hadoop fs -put a.txt  /
```

# Spark集群搭建

## Anaconda3环境搭建

镜像站下载压缩包：https://mirrors.tuna.tsinghua.edu.cn/anaconda/archive/

上传到/export/servers

执行

```
sh ./Anaconda3-2021.11-Linux-x86_64.sh
```

回车

yes

输入安装路径

yes

安装结束，重连一次服务器

配置镜像

在~/.condarc文件，增加以下内容

```
channels:
  - defaults
show_channel_urls: true
default_channels:
  - https://mirrors.tuna.tsinghua.edu.cn/anaconda/pkgs/main
  - https://mirrors.tuna.tsinghua.edu.cn/anaconda/pkgs/r
  - https://mirrors.tuna.tsinghua.edu.cn/anaconda/pkgs/msys2
custom_channels:
  conda-forge: https://mirrors.tuna.tsinghua.edu.cn/anaconda/cloud
  msys2: https://mirrors.tuna.tsinghua.edu.cn/anaconda/cloud
  bioconda: https://mirrors.tuna.tsinghua.edu.cn/anaconda/cloud
  menpo: https://mirrors.tuna.tsinghua.edu.cn/anaconda/cloud
  pytorch: https://mirrors.tuna.tsinghua.edu.cn/anaconda/cloud
  simpleitk: https://mirrors.tuna.tsinghua.edu.cn/anaconda/cloud
```

创建pyspark环境

```
conda create -n pyspark python=3.6  # 基于python3.6创建pyspark虚拟环境
conda activate pyspark      # 激活（切换）到pyspark虚拟环境
```

下载需要的包

```
pip install pyspark==2.4.0 jieba pyhive -i https://pypi.tuna.tsinghua.edu.cn/simple
```

以上步骤在每台机器都做一遍，也可以scp分发

## Spark安装

下载：http://archive.apache.org/dist/spark/spark-2.4.0/

解压重命名：spark-2.4.0

配置环境变量在/etc/profile

```
export JAVA_HOME=/export/servers/jdk1.8.0_281
export JRE_HOME=$JAVA_HOME
export HADOOP_HOME=/export/servers/hadoop-2.7.4
export SPARK_HOME=/export/servers/spark-2.4.0
export HADOOP_CONF_DIR=$HADOOP_HOME/etc/hadoop
export PYSPARK_PYTHON=/export/servers/anaconda3/envs/pyspark/bin/python
export PATH=:$SPARK_HOME/bin:$HADOOP_HOME/bin:$HADOOP_HOME/sbin:$JAVA_HOME/bin:$JRE_HOME/bin:$PATH
```

在~/.bashrc配置，在最后加上这三行

```
export PYSPARK_PYTHON=/export/servers/anaconda3/envs/pyspark/bin/python
export JAVA_HOME=/export/servers/jdk1.8.0_281
export PATH=$JAVA_HOME/bin:$PATH
```

spark配置文件，在spark-2.4.0/conf

spark-env.sh配置

```
export JAVA_HOME=/export/servers/jdk1.8.0_281
export SPARK_DIST_CLASSPATH=$(/export/servers/hadoop-2.7.4/bin/hadoop classpath)
## HADOOP软件配置文件目录，读取HDFS上文件和运行YARN集群
HADOOP_CONF_DIR=/export/servers/hadoop-2.7.4/etc/hadoop
YARN_CONF_DIR=/export/servers/hadoop-2.7.4/etc/hadoop
## 指定spark老大Master的IP和提交任务的通信端口
# 告知Spark的master运行在哪个机器上
export SPARK_MASTER_HOST=node1
# 告知sparkmaster的通讯端口
export SPARK_MASTER_PORT=7077
# 告知spark master的webui端口
SPARK_MASTER_WEBUI_PORT=9300
# worker cpu可用核数
SPARK_WORKER_CORES=1
# worker可用内存
SPARK_WORKER_MEMORY=1g
# worker的工作通讯地址
SPARK_WORKER_PORT=7078
# worker的webui地址
SPARK_WORKER_WEBUI_PORT=9301
## 设置历史服务器
# 配置的意思是  将spark程序运行的历史日志 存到hdfs的/sparklog文件夹中
SPARK_HISTORY_OPTS="-Dspark.history.fs.logDirectory=hdfs://node1:9311/sparklog/ -Dspark.history.fs.cleaner.enabled=true"
```

spark-default.conf

```
# 开启spark的日期记录功能
spark.eventLog.enabled  true
# # 设置spark日志记录的路径，这个路径要在Hadoop先创建好
 spark.eventLog.dir   hdfs://node1:9311/sparklog/
# # 设置spark日志是否启动压缩
 spark.eventLog.compress  false
```

slaves配置

```
node1
node2
node3
```

log4j.properties,复制一份模板就行

配置完成，以上配置三台机器都要做，或者在一台做了分发到另外两台

验证 

输入命令：pyspark(python环境)或者spark-shell(scala环境)

启动集群，在spark-2.4.0/sbin目录

```
./start-all.sh
```

一定要用./因为已经有hadoop在了

访问spark：

192.168.182.129:9300



停止：

```
stop-all.sh
```

