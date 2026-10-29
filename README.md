# 港慧通 · 港口物流管理系统（涪陵龙头港）

> 让多式联运通达顺远

前后端分离的港口物流管理系统，包含 **港口管理（俯视图绘制）**、**泊位管理（到货 / 装货 / 分派承运 / 分派船只 / 装载出发 / 电子出门条）**、**承运商管理**、**船只管理（按船运公司）**，以及 **多租户 + 用户/角色/菜单/权限** 的后台管理。

## 目录结构

```
.
├── sql/main/init.sql            # 建表 + 索引 + 初始化数据（分支 main）
├── ght-port-server/             # 后端（Maven 多模块）
│   ├── ght-port-api/            # 实体、出入参、常量枚举、工具类、服务接口
│   ├── ght-port-service/        # 服务实现、Mapper、MyBatis XML、多租户插件
│   └── ght-port-controller/     # HTTP 接口、鉴权拦截器、全局配置、启动类
└── ght-port-web/                # 前端（Vue3 + Vite + Element Plus）
```

## 技术栈

- 后端：JDK 21、Spring Boot 3.3、MyBatis-Plus 3.5.7、Hutool 5.8、Fastjson2、Maven
- 前端：Vue 3、Vue Router（hash 模式）、Pinia、Element Plus、qrcode、html5-qrcode
- 数据库：MySQL 8

## 快速启动

1. **初始化数据库**

   ```sql
   CREATE DATABASE ght_port DEFAULT CHARSET utf8mb3 COLLATE utf8mb3_bin;
   USE ght_port;
   SOURCE sql/main/init.sql;
   ```

2. **启动后端**（修改 `ght-port-controller/src/main/resources/application-dev.yml` 中的数据库账号）

   ```bash
   cd ght-port-server
   mvn clean package -DskipTests
   java -jar ght-port-controller/target/ght-port-server.jar
   ```

   接口前缀：`http://localhost:18080/api`（端口在 `application.yml` 的 `server.port` 修改，或启动时通过环境变量覆盖：`SERVER_PORT=19090 java -jar ...`）

3. **启动前端**

   ```bash
   cd ght-port-web
   npm install
   npm run dev
   ```

   访问 `http://localhost:5173`（开发时 `/api` 请求会代理到后端）。

   **前端配置文件（修改后端地址）**

   | 文件 | 生效场景 | 说明 |
   | --- | --- | --- |
   | `ght-port-web/.env.development` | `npm run dev` | `VITE_PROXY_TARGET` 后端地址（默认 `http://localhost:18080`）、`VITE_PORT` 前端端口、`VITE_API_BASE` 接口前缀 |
   | `ght-port-web/.env.production` | `npm run build` | `VITE_API_BASE` 接口地址，同域部署填 `/api`，跨域填完整地址 |
   | `ght-port-web/public/config.js` | 打包后运行时 | `apiBaseUrl` 非空时优先生效，打包后可直接修改 `dist/config.js`，无需重新构建 |

   > 司机手机需要能访问出门条链接：请把 `application.yml` 中的 `ght.web-base-url` 改成手机可访问的前端地址（如 `http://192.168.1.10:5173`）。门卫页面的摄像头扫码需要 HTTPS 或 localhost 环境；也可以直接用手机自带相机扫码打开链接。

## 演示账号（默认密码均为 `123456`）

| 所属港口（租户） | 账号 | 角色 |
| --- | --- | --- |
| 港慧通平台 `platform` | admin | 超级管理员（租户、菜单管理） |
| 涪陵龙头港 `fllt` | admin | 租户管理员 |
| 涪陵龙头港 `fllt` | diaodu | 港口调度员 |
| 涪陵龙头港 `fllt` | menwei | 门卫（扫码核销） |
| 涪陵龙头港 `fllt` | 13800000001 | 承运商（顺达物流，自助管理车队） |
| 涪陵龙头港 `fllt` | 13900000001 | 司机（查看自己的出门条） |

## 核心业务流程

1. **港口管理**：从左侧元素面板拖入江河、陆地、道路、泊位、门机、堆场、仓库、储罐、闸口、办公楼、绿植、船舶、文字，自由移动、缩放、旋转、复制、删除，支持撤销/重做、网格吸附、白昼/黄昏/夜间主题。选中泊位可设置编号、名称、作业时间。**移动、移除正在作业的泊位或修改其作业时间时，会提示“正在作业，是否继续操作？”**（前后端双重校验）。
2. **泊位管理**：俯视图实时展示泊位状态——未作业（绿色）、正在作业（琥珀色脉冲 + 靠泊船舶 + 往返作业车）、维修中（黄黑警示条 + 旋转齿轮）。点击泊位查看货物内容、接驳承运商、相关车辆。
3. **泊位到货**：仅“未作业 / 正在作业”泊位可用，录入货物类型（下拉搜索）、名称、货物形态（集装箱 / 散货）、吨位，方数与件数选填，所属船运公司船只（下拉搜索，可多选）；到货后泊位自动进入“正在作业”。
   **泊位装货**：登记待装船的货物（同上，无需选船），再通过 **“分派船只”** 选择船只（可多选），逐船填写装船吨数（方数、件数选填）并点击“装船完成”。
   泊位详情的“货物内容”按到货时间倒序展示，点击货物可查看其承运商与车辆（到货）或装船船只（装货），并可切换查看历史货物。
4. **分派承运**：仅“正在作业”泊位可用，选择承运商（下拉搜索）与该承运商名下的车辆（可多选），“确定”可反复点击（同一车辆可多次运输同一批货物）。每辆车单独填写装载吨数（方数、件数选填），点击 **“装载完成，立即出发”**。
5. **电子出门条**：每次出发生成出门条（开具时间、承运商、车辆、司机、货物、装载量），通过链接发送给司机（默认实现为日志模拟短信，见 `LogNotifyServiceImpl`，可替换为短信网关）。司机打开链接展示二维码，门卫扫码核销后放行；**核销后二维码不能再次使用**（数据库条件更新保证并发安全）。
6. **大屏驾驶舱**：斜视展示港口 3D 场景与全部泊位，汇总累计卸货/装货吨数、作业次数、泊位状态、货物类型统计、近 7 日趋势；右侧轮播各泊位（累计数据、货物类型、当前作业），并展示承运商、船运公司排行；支持一键全屏。
7. **船只管理**：按船运公司展示船只数、累计卸货吨/方、累计装货吨/方；详情可查看正在作业的船只、卸货记录与装货记录。
8. **完成运输**：只有完成运输的车辆才能再次“装载完成，立即出发”。承运商管理与承运商自助端（我的车队）均可设置车辆完成运输。
9. **承运商 / 车辆账号**：新增承运商时以联系电话自动开通“承运商”角色账号；新增车辆时以司机电话自动开通“司机”角色账号。修改运输中的车辆会提示确认。

## 多租户与权限

- 所有业务表带 `tenant_id`，通过 MyBatis-Plus `TenantLineInnerInterceptor` 自动追加租户条件；`sys_tenant`、`sys_menu` 为全局表。
- 新开通租户时，自动从平台租户复制内置角色（租户管理员、港口调度员、承运商、司机、门卫）及其菜单权限，并创建租户管理员账号。
- 后端接口通过 `@RequiresPermission` 注解 + `AuthInterceptor` 进行权限判断；承运商账号由 `DataScopeHelper` 限定只能访问本承运商数据。
- 前端菜单与路由由后端菜单树动态生成，按钮级权限使用 `v-perm` 指令。

## 说明

- 生产环境请修改 `ght.jwt-secret`。
- 密码采用 SHA-256 + 随机盐存储（`PasswordUtil`）。
