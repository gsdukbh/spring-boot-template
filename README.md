# Spring boot template



## 关于

`Springboot template` 是一个模版项目,在`Springboot` 的基础上 集成 `spring secutiry` 、 `JWT`、 `i18n`、`Springdoc` 等功能.

## 多模块项目

多个模块项目构建 请看 multi 分支

## 如何构建你的证书

```bash

openssl genrsa -out server.pem 2048

openssl rsa -in server.pem -pubout -out public.pem
openssl pkcs8 -topk8 -inform pem -in  server.pem -outform pem -nocrypt -out private.pem
```
