# 部署说明

> 本文只写**变量名**，不写变量的值。文中所有 `${...}` 都需要在部署前于环境中设置好。
> 排障手记与验收记录属本地笔记，不随仓库分发。

---

## 零、照着敲（日常只用这一套）

### 开工前三个检查

**① Docker Desktop 是活的。**

```powershell
docker ps
```

有输出（或者有正在跑的容器）就说明活的。报错就先把 Docker Desktop 打开。

**② 当前终端带着环境变量。**

```powershell
$env:DB_PASSWORD.Length
$env:JWT_SECRET.Length
$env:DEEPSEEK_API_KEY.Length
```

三个都打印出**非 0 的数字**才继续。**这个写法只打印长度、不显示值**，可以放心敲。空着或者 0 就说明这个终端没有那三个变量 —— 见第三节能设哪些、以及为什么必须在这个终端里敲 compose。

**③ Ollama 起着，而且是关掉 Vulkan 起的。** AI 那部分要用它。

```powershell
Invoke-RestMethod -Uri http://localhost:11434/api/embed -Method Post -ContentType 'application/json' -Body '{"model":"qwen3-embedding:0.6b","input":"ping"}'
```

数一下回来的向量长度，是 **1024** 就对了。报 500 或者连不上，见第五节的 Ollama 那一小节。

### 起项目

```powershell
cd D:\java\project\java_fight
mvn clean package
docker compose up -d --build
docker compose ps
```

`docker compose ps` 里 `mysql` 要变成 **`Up ... (healthy)`**、`redis` 和 `app` 都是 `Up`，三个齐了才算起好。app 不是 `Up` 就看日志：`docker compose logs app --tail=100`。

然后验证：

```powershell
curl.exe -s -i http://localhost:8080/findAll
```

看到 `HTTP/1.1 401` 就是成功了（没带 token 本来就该被拦）。看到 200 反而说明拦截器没生效。

### 改完之后怎么重来

**改 Java 代码或 `src/main/resources` 下的配置** —— jar 变了，得重新打包和重建镜像：

```powershell
mvn clean package; docker compose up -d --build
```

**只改 `docker-compose.yml`** —— jar 没变，镜像不用重建，`up -d` 自己会发现配置变了、把 app 容器用新配置重建一遍：

```powershell
docker compose up -d
```

**改 `db/init.sql`** —— 不会自动生效，它只在数据卷为空时跑一次。要生效得删卷重来，见第二节「数据库初始化」。

### 停

```powershell
docker compose down        # 停并删容器，保留数据卷
docker compose stop app    # 只停 app，mysql / redis 留着
docker compose down -v     # ⚠️ 连数据卷一起删
```

---

## 一、怎么打包

前置：JDK 21、Maven（项目自带 `mvnw`，不想装 Maven 可以用它）。

在项目根目录 `D:\java\project\java_fight` 下执行：

```
mvn clean package
```

产物落在 `target\java_fight-0.0.1-SNAPSHOT.jar`。

这个命令会先跑全部测试（当前 **24 条，1 条是 `@Disabled`**），测试红了就不打包 —— 这是有意的，别习惯性加 `-DskipTests`。**跑测试需要 Redis 容器起着**（`docker start java_fight-redis-1`），否则必然红两处，那是环境问题不是代码问题。

**顺序不能反。** Dockerfile 第 4 行是 `COPY target/java_fight-0.0.1-SNAPSHOT.jar app.jar`，它是从**构建上下文**（`docker build ... .` 里那个 `.`，也就是项目目录）拿文件的。所以必须先 `package` 出 jar，`docker build` 才有东西可抄。

**一个耦合点。** 上面这行把 jar 的完整文件名写死了，里面带着版本号 `0.0.1-SNAPSHOT`。哪天 `pom.xml` 里改了版本号，这行必须跟着改，否则 `docker build` 会在 COPY 那一步报找不到文件。

**镜像里的路径。** 因为 Dockerfile 第 2 行有 `WORKDIR /app`，而 COPY 的目标 `app.jar` 不带前导斜杠，所以 jar 在容器里的真实路径是 `/app/app.jar`。

**⚠️ `mvn clean` 会把 `target\` 下的日志、响应体一并删掉。** 要留的证据在跑 clean 之前先拷进 `evidence\`。

---

## 二、怎么起容器

### 方式 A：Compose 一次起三个（推荐，也是最终形态）

```powershell
docker compose up -d --build
```

`--build` 表示顺带重新构建 app 的镜像；不加就用已有镜像。

三个服务：`mysql`（MySQL 8.0）、`redis`（Redis 7）、`app`（本项目）。

**端口对照表**（这里最容易搞混）：

|  | 宿主机 | 容器内 |
|---|---|---|
| MySQL | **3307** | 3306 |
| Redis | **6380** | 6379 |
| 应用 | 8080 | 8080 |

宿主机上连它们用 `localhost:3307` / `localhost:6380`；而容器里的 app 连它们走的是**服务名 + 容器端口**：`mysql:3306`、`redis:6379` —— 主机名是**服务名**（Compose 内部 DNS 解析），端口是**容器端口，不是往外映射的那个**。

**为什么要错开**：本机原来就有 MySQL 占 3306、可能有 Redis 占 6379，所以往外映射到 3307 / 6380 避开。

**起来之后怎么确认：**

```powershell
docker compose ps
curl.exe -s -i http://localhost:8080/findAll
```

第二条期待 `HTTP/1.1 401` + `{"msg":"未登录或登录已过期"}`。用 401 而不是 200 来探活更准：它同时证明应用活着**并且**登录拦截器在岗。

### 数据库初始化 —— `init.sql` 只跑一次

`db/init.sql` 挂在容器的 `/docker-entrypoint-initdb.d`，而 MySQL 官方镜像有条规矩：**`/var/lib/mysql` 里只要有东西，就当「已经初始化过」，底下的脚本一条都不跑。**

后果有两条，都实打实撞过：

- **第一次 `up` 会自动建表并插入账号；之后再改 `init.sql` 重启不生效。** 要生效只能删卷重来（见下）。
- **老数据卷会「冻住」建卷那一刻的 schema。** 2026-10-07 实测：卷是 5 天前建的，那会儿 `init.sql` 里还只有 `tb_user`，后补的三张表（`tb_conversation` / `tb_message` / `tb_doc_chunk`）根本没长出来。症状是登录能成功、建会话炸，日志里是 `SQLSyntaxErrorException: Table 'java_fight.tb_conversation' doesn't exist`。

删卷重来：

```powershell
docker volume ls | Select-String java_fight   # 先确认要删的是哪个
docker compose down -v
docker compose up -d
```

**`-v` 会连容器里那份库的数据一起删，慎用。** 这在本项目是可接受的：容器那份是**可弃副本**，真数据在本机 `localhost:3306` 那份。删完重来正好顺带验证「`init.sql` 能不能独立把整套表建出来」—— 而这恰恰是「别人 clone 能不能跑起来」的判据。

### 容器之间怎么排序起 —— `depends_on` + `healthcheck`

```yaml
  mysql:
    healthcheck:
      test: [ "CMD", "mysqladmin", "ping", "-h", "localhost", "-p${DB_PASSWORD}" ]
      interval: 5s
      timeout: 5s
      retries: 10

  app:
    depends_on:
      mysql:
        condition: service_healthy
      redis:
        condition: service_started
```

两个词分工不同，**缺一半等于白配**：

- **`depends_on` 管先后，不管就绪。** 光写 `depends_on: mysql`，Docker 只保证 mysql 容器**先被启动**，然后立刻去起 app。可 MySQL 第一次启动要初始化数据目录、把 `init.sql` 跑完，这要几十秒；app 起来就去连 3306，那会儿它还没在监听，直接 `Connection refused` 退出。
- **`healthcheck` 是给容器配的体检命令。** Docker 按 `interval` 反复跑它，体检通过后这个容器才从 `starting` 变 `healthy`。
- 合起来：`condition: service_healthy` 让 app **等 MySQL 体检通过**再启动。redis 启动是毫秒级、也没配体检，`service_started` 就够。

**怎么确认它在工作**：`docker compose ps` 里 mysql 那行会出现 `(healthy)`。

### 方式 B：只用 Docker 手动起应用（早期做法，保留备查）

在 MySQL 已经跑着的前提下：

```powershell
docker build -t java_fight:1.0 .

docker run -d --name java_fight -p 8080:8080 `
  -e DB_PASSWORD=$env:DB_PASSWORD `
  -e JWT_SECRET=$env:JWT_SECRET `
  -e SPRING_DATASOURCE_URL="jdbc:mysql://host.docker.internal:3306/java_fight?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true" `
  java_fight:1.0
```

要点：**容器里的 `localhost` 是容器自己，不是宿主机** —— 所以连宿主机上的东西得用 `host.docker.internal`。方式 A 里 MySQL / Redis 同网，直接写服务名就行，不用绕宿主机；只有**宿主机上的 Ollama** 没有容器在 compose 里，才必须用 `host.docker.internal`（见第五节）。

### 拉镜像

`mysql:8.0`、`redis:7-alpine`、`eclipse-temurin:21-jre` 都来自 Docker Hub。本机可用的镜像源只有 `https://docker.1ms.run` 一个。判断源活不活看 **401** —— 401 表示服务在、等认证，是正常的；403 是服务在但拒绝你，不能据此认为源可用。

---

## 三、要设哪些环境变量

**要在宿主机环境里设的（用 `setx` 持久化，设完要重开终端才生效）：**

| 变量名 | 作用 | 谁在用 |
|---|---|---|
| `DB_PASSWORD` | MySQL root 密码 | Compose 喂给 `mysql` 的 `MYSQL_ROOT_PASSWORD`；同时喂给 `app`，对应 `application.properties` 的 `${DB_PASSWORD}` |
| `JWT_SECRET` | JWT 签名密钥 | Compose 喂给 `app`，对应 `application.properties` 的 `jwt.secret=${JWT_SECRET}` |
| `DEEPSEEK_API_KEY` | DeepSeek 云 API 的 key | Compose 喂给 `app`，对应 `application.properties` 的 `spring.ai.deepseek.api-key=${DEEPSEEK_API_KEY}` |

**不用你设、`docker-compose.yml` 已经给 app 写死的：**

| 变量名 | 盖住的是哪一行 |
|---|---|
| `SPRING_DATASOURCE_URL` | `spring.datasource.url`（改成走服务名 `mysql:3306`） |
| `SPRING_DATA_REDIS_HOST` / `SPRING_DATA_REDIS_PORT` | `spring.data.redis.host` / `port`（改成 `redis:6379`） |
| `SPRING_AI_OLLAMA_BASE_URL` | `spring.ai.ollama.base-url`（改成 `http://host.docker.internal:11434`） |
| `SPRING_PROFILES_ACTIVE` | `spring.profiles.active`（从 `dev` 改成 `prod`，见下） |

### ⚠️ 两种「名字」，别混 —— 这是最容易踩的一处

`application.properties` 里有两类完全不同的东西：

- **`${...}` 是占位符查找**，比如 `${DEEPSEEK_API_KEY}`：启动时去所有配置源里找一个**名叫 `DEEPSEEK_API_KEY`** 的值。**名字必须一字不差**，找不到就直接启动失败（`Could not resolve placeholder`），而且**是整个应用起不来**，不是某个接口报错。
- **`SPRING_DATASOURCE_URL` 这种是属性名**：靠 Spring Boot 的**宽松绑定**生效 —— 全大写、`.` 和 `-` 都换成 `_`，就等价于那个属性名。所以 `spring.ai.ollama.base-url` 对应 `SPRING_AI_OLLAMA_BASE_URL`。它**盖掉**配置文件里那一行，不要求名字相同。

**一句话**：占位符是**去要一个值**，环境变量属性名是**盖掉一行**。

**还有一个必须记住的边界**：**宿主机的环境变量不会自动带进容器。** 容器的环境变量就是 compose `app.environment` 里那几行现建的一份。所以「宿主机上有」不等于「容器里有」——`DEEPSEEK_API_KEY` 就是漏过一次的那个。

### 关于 profile

`application.properties` 里写死 `spring.profiles.active=dev`，会让**本地和容器都用 dev**。dev 那套只做一件事：`logging.level.root=DEBUG`，把每条 SQL、每个参数、框架内部全打出来。

容器里用 `SPRING_PROFILES_ACTIVE=prod` 盖成 `application-prod.properties`（`root=INFO`）。**这是一笔交换**：日志量和干扰降下来，代价是**出问题时手里没有细节了** —— 容器里再出事，得先把 profile 换回来才看得见。

### 值从哪来 / 能不能用 `.env`

**本项目没有 `.env` 文件。** Compose 里的 `${DEEPSEEK_API_KEY}` 是从**运行 `docker compose` 的那个终端的环境变量**里做替换的。所以必须在已经带着这几个变量的终端里执行，否则 Compose 会警告 `variable is not set` 并把值替换成**空串** —— 而空串也算「解析成功」，所以**容器照样 `Up`，只在真去连接时才炸**。

**别把 `docker compose config` 的输出贴出去。** 那条命令会把所有 `${...}` 展开成真实值打印出来，包含密码。要确认变量有没有传进去，看 `docker compose up` 的输出里有没有 `variable is not set` 就够了。

**可以用 `.env`**（Compose 会自动读项目根目录下的它），但**它绝对不能进 Git** —— 第七节推送前那一关要挡住它。

---

## 四、Redis

**它在这个项目里干什么**：缓存用户查询结果（Cache Aside）、登录失败计数限流。**事实源是 MySQL，Redis 只是可丢弃的副本** —— 它整个清空，业务只会变慢，不会出错。

**配置怎么接上的**：`application.properties` 里写的是 `spring.data.redis.host=localhost` / `port=6380`，**容器里的 `localhost` 是容器自己、6380 上什么都没有**。所以 compose 给 app 注入了 `SPRING_DATA_REDIS_HOST: redis` 和 `SPRING_DATA_REDIS_PORT: 6379` 把它盖掉。**注意端口是 6379 不是 6380** —— 6380 是宿主机往外映射的那个。

**没有配 volume**（和 mysql 不一样）。所以 `docker compose down` 之后 Redis 里的数据就没了。这是有意的：它是可丢弃副本。**副作用要知情：`down` 之后第一次登录的失败计数、以及所有缓存都会从零开始。**

**宿主机怎么连它调试**：`localhost:6380`。

**⚠️ 一个 Redis、两个 MySQL。** 宿主机 `localhost:3306` 的库与容器 `3307→3306` 的库是两份数据，但只有一个 Redis（容器那个）。**两条链路同一时间只准开一条**，否则「先改库再删缓存」那套推理会失效（A 改本地库、删了缓存，B 回源读到自己那个旧库，又把旧值写回缓存）。

---

## 五、AI 部分（两条通路，分工不同）

| | 干什么 | 跑在哪 | 谁掏钱 |
|---|---|---|---|
| **DeepSeek 云 API** | 对话（`/chat`） | 公网 | 按量付费，要 `DEEPSEEK_API_KEY` |
| **Ollama** | 向量化（embedding，RAG 检索用） | **宿主机上的一个进程，不是容器** | 免费，本地跑 |

### DeepSeek 那一侧

- 属性：`spring.ai.model.chat=deepseek`（决定用哪个厂商的自动配置）、`spring.ai.deepseek.chat.model=deepseek-flash`（决定报文里的 `model` 字段）。**属性名没有 `.options.` 这一层。**
- key 走占位符 `${DEEPSEEK_API_KEY}`，容器里由 compose 注入（第三节）。
- 超时由 Boot 的 `RestClient` 决定，写在 `spring.http.clients.connect-timeout` / `read-timeout`。

### Ollama 那一侧 —— 三件必须在文档里写死的事

**① Ollama 跑在宿主机上，所以容器里不能写 `localhost`。** compose 给 app 注入了：

```yaml
      SPRING_AI_OLLAMA_BASE_URL: http://host.docker.internal:11434
```

`host.docker.internal` 是 Docker Desktop 准备的特殊名字，在容器里解析成宿主机地址。

**② Ollama 是手工启动的进程，不是 Windows 服务自启**，开机不会自己起来。装机后要先装 Ollama、拉模型：

```powershell
ollama pull qwen3-embedding:0.6b
```

**这个模型（约 639 MB）只存在于本机，不在容器镜像里** —— 换机器、重装系统、别人 clone 你的仓库，都必须自己重新拉一遍。**这就是「别人 clone 下来能不能跑起来」的关键一环。**

**③ 本机必须关掉 Ollama 的 Vulkan 后端，否则 embedding 会崩。** 症状：

```
POST /api/embed → 500 {"error":"llama-server process has terminated: exit status 0xe06d7363"}
```

Java 侧看着像 `TransientAiException: HTTP 500`，**很容易误判成代码问题或框架问题，其实完全在 Ollama 那一侧**。根因（`%LOCALAPPDATA%\Ollama\server.log`）：`ggml_vulkan: Compute pipeline creation failed for xe_fa_decode_ph1`。

修法（持久化，设完重开终端）：

```powershell
setx OLLAMA_VULKAN 0
```

**判据**：启动日志里出现 `load_tensors: CPU model buffer size = ...`，而不是 `Vulkan0 model buffer size = ...`；并且服务端环境转储里是 `OLLAMA_VULKAN:false`。**换机器不一定需要这一步**（取决于显卡驱动），但症状一样时要先想到它。

**先一刀切开责任方**：出 500 时用 `curl.exe` 直接打一次 `/api/embed`。直打也 500 → 是 Ollama 自己的问题；直打正常而 Java 报错 → 才轮到查代码。

---

## 六、别人 clone 下来怎么跑起来

按顺序，一条都不能跳：

1. 装 JDK 21、Maven、Docker Desktop。
2. 装 Ollama，`ollama pull qwen3-embedding:0.6b`；Windows 本机再加 `setx OLLAMA_VULKAN 0` 并重开终端。
3. 设三个宿主机环境变量：`DB_PASSWORD`、`JWT_SECRET`、`DEEPSEEK_API_KEY`（用 `setx`，设完重开终端）。**`DEEPSEEK_API_KEY` 要自己申请。**
4. `cd` 到项目根目录，`mvn clean package`。
5. `docker compose up -d --build`。
6. `docker compose ps` —— 等 mysql 变 `(healthy)`、三个服务都 `Up`。
7. `curl.exe -s -i http://localhost:8080/findAll` —— 期待 `401`。
8. 登录拿 token，走一次「建会话 → 对话 → 看历史」。

**不在这份文档里的东西**：`db/init.sql` 里那几个测试账号是**开发期手动建的**，换机器后自己 `INSERT` 或者走注册接口。

---

## 七、推送 GitHub 之前必须查

**密钥推到公开仓库是真实事故，不是洁癖。**

- `docker-compose.yml` 里的 MySQL 密码必须是 `${DB_PASSWORD}` 这种**变量引用**，不能是明文。
- `DEEPSEEK_API_KEY` 的值绝不能出现在任何被提交的文件里。
- **`.gitignore` 必须挡住**：`target/`、`*.jar`、IDE 目录（`.idea/`、`*.iml`）、日志、`.env`。**其中 `.env` 现在还没被挡住** —— 一旦用了 `.env` 放 key，必须先补上这一条。
- **容易被漏的第三条**：`evidence/` 和交接记录里抄过带 `Bearer <token>` 的完整命令，**token 也是凭据**，推送前一起查。
