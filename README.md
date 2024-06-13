# flexmodel-server

基于 Quarkus + Vert.x 构建的服务端工程

## 部署

```cmd
docker run -p 8080:8080 -e JAVA_OPTS="-Dflexmodel.datasource.db-kind=mysql -Dflexmodel.datasource.url=jdbc:mysql://localhost:3306/flexmodel -Dflexmodel.datasource.username=<your username> -Dflexmodel.datasource.password=<your password>" -t cjbi/flexmodel:latest
```

支持的数据库请看引擎部分文档

https://github.com/flexmodel-project/flexmodel-engine

