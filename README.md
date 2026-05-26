# RemoteService
Trabalho da Unidade 2 de Programação Distribuída: implementação da mesma lógica do projeto anterior usando o `Middleware` no lado servidor e `HttpClient` no lado cliente (heartbeat e gateway).

## Pré-requisito: gerar o JAR do Middleware
No projeto `Middleware/middleware`, gere e instale o JAR no repositório local do Maven:

```powershell
mvn -f .\Middleware\middleware\pom.xml clean install
```

Esse comando cria o `.jar` e instala como dependência `br.imd.ufrn:middleware:1.0-SNAPSHOT`.

## Compilar o RemoteService

```powershell
mvn -f .\RemoteService\pom.xml clean package
```

## Subir os processos
Abra terminais separados:

1. Gateway (heartbeat em `9000`, API em `9001`)

```powershell
mvn -f .\RemoteService\pom.xml exec:java "-Dexec.mainClass=br.imd.ufrn.remoteservice.gateway.GatewayApp"
```

2. UserService instância 1 (`9006`)

```powershell
mvn -f .\RemoteService\pom.xml exec:java "-Dexec.mainClass=br.imd.ufrn.remoteservice.server.RemoteServiceServerApp" "-Dexec.args=users 1"
```

3. UserService instância 2 (`9007`)

```powershell
mvn -f .\RemoteService\pom.xml exec:java "-Dexec.mainClass=br.imd.ufrn.remoteservice.server.RemoteServiceServerApp" "-Dexec.args=users 2"
```

4. MessageService instância 1 (`9004`)

```powershell
mvn -f .\RemoteService\pom.xml exec:java "-Dexec.mainClass=br.imd.ufrn.remoteservice.server.RemoteServiceServerApp" "-Dexec.args=messages 1"
```

5. MessageService instância 2 (`9005`)

```powershell
mvn -f .\RemoteService\pom.xml exec:java "-Dexec.mainClass=br.imd.ufrn.remoteservice.server.RemoteServiceServerApp" "-Dexec.args=messages 2"
```

## Testar via gateway

Criar usuário:

```powershell
curl -X POST http://127.0.0.1:9001/users -H "Content-Type: application/json" -d "{\"userId\":1,\"username\":\"fernanda\"}"
```

Enviar mensagem:

```powershell
curl -X POST http://127.0.0.1:9001/messages -H "Content-Type: application/json" -d "{\"senderId\":1,\"receiverId\":2,\"text\":\"oi\"}"
```
