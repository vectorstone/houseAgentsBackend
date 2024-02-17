[前端项目](https://github.com/vectorstone/houseAgentsFront)
[后端项目](https://github.com/vectorstone/houseAgentsBackend)
# 使用说明
## 背景:
房源管理系统是自己开源的一个独立制作的项目，适合用来管理一些房源信息，支持将现有的房源的数据一键导入，方便快速的查找和管理相关的房源。
主要包含了权限管理模块、房源导入导出模块以及房源的增删改查模块。

# 项目部署
## 说明
初始状态只有admin一个管理员用户,默认密码: 123456
## 环境需求
JDK1.8 , Maven3.6.3 , MySQL8.0 , Redis
## 配置文件修改
将文件clone到本地后请修改如下的配置文件
src/main/resources/application.yaml
### 修改MySQL数据库连接信息
配置文件中我使用了jasypt来保护敏感信息,建议大家都这么做,具体的使用步骤参考我的这篇post
[jasypt加密工具的使用](http://wswxgpp.eu.org/2023/09/07/springboot%E9%A1%B9%E7%9B%AE%E4%B8%AD%E9%81%BF%E5%85%8D%E6%9A%B4%E9%9C%B2%E6%95%8F%E6%84%9F%E4%BF%A1%E6%81%AF%E7%9A%84%E6%96%B9%E6%B3%95/)
### 修改redis数据库的连接信息
也使用了加密的处理,参考上面加密工具的使用来进行设置

## 前端项目部署
clone下来前端项目后,在项目的根目录下执行如下命令:
```sh
npm run build:prod # 打包的时候运行的命令
```
之后会在目标的targer文件夹里面生成对应的打包好的文件(后面这些文件需要上传到nginx的html目录里面)
## nginx
需要部署nginx反向代理服务器,通过docker的方式或者编译安装的方式都可以,使用的过程中没有什么区别
### nginx配置文件如下
```nginx
server {
	listen 80;
	server_name 服务器的ip地址;

client_max_body_size 20m;

	location / {
			root /var/www/html;
			index index.html index.htm;
	}


  location /prod-api/ {
  		#root /www/vod;
  		#index index.html index.htm;
      proxy_pass http://127.0.0.1:8888/;
  }


	error_page 500 502 503 504 /50x.html;
	location = /50x.html{
			root html;
	}
}

```

## 数据库表
请将项目根目录下的SQL脚本导入到你的本地的数据库中
