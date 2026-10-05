# 汽车网站拆分改造 + GitHub 发布计划

## Context（背景）

用户需求（D:\Study\carc）：
1. 把项目推到 GitHub（用户 cooden，仓库 car）
2. 车型详情页（model.html）改成与其他页面统一风格
3. 拆成两个网站：①油电成本对比站 ②内容丰富的首页站（新增对比、二手车对比、10万二手车推荐等板块，有广告点）
4. 接入流量管理（代码同样进 git）

### 已确认的现状
- Spring Boot 2.7.18（Java 8 / Maven），静态页在 `backend/src/main/resources/static/`，路由在 `PageController.java`
- index.html / power.html：浅色主题 + #333 黑底导航；model.html：深色渐变主题 + 半透明导航，风格不统一
- 所有页面已有 AdSense 占位（假号 `ca-pub-XXXXXXXXXXXXXXXX`），保留占位即可
- 项目尚未 git init，无 .gitignore，无 gh 命令；根目录还有微信小程序文件（保持不动，随仓库提交）

### 关键决策（用户跳过了提问，按推荐方案执行）
- 拆分：**同一个 Spring Boot 项目 + 一个 git 仓库**，路径区分两个站：内容首页站（`/` 系）与油电成本站（`/power` 系），各自独立导航
- 流量管理：**自建轻量统计**（后端 Filter 记录 PV → `/api/stats` JSON → `/stats` 面板页），页面广告位沿用 AdSense 占位，全部代码进 git
- GitHub：`git init` + remote 指向 `https://github.com/cooden/car.git`，push 时由 Git Credential Manager 弹窗登录（兜底：向用户索要 Token）

---

## 实施步骤

### 阶段 0：Git 初始化（先做，保护后续改动可追踪）
1. 新建 `d:\Study\carc\.gitignore`，忽略：`backend/target/`、`backend/data/`、`*.class`、`.idea/`、`*.iml`、`node_modules/`、`project.private.config.json`
2. 删除 `backend/src/main/resources/static/model copy.html`（无用副本）
3. `git init` + `git add -A` + 基线 commit + `git remote add origin https://github.com/cooden/car.git`（暂不 push，最后阶段推）

### 阶段 1：后端 —— 统计与路由
在 `backend/src/main/java/com/carc/backend/` 下：
1. 新建 `stats/StatsService.java`：`ConcurrentHashMap<String, AtomicLong>` 记录各 URI 的 PV；`record(uri, ip, ua)`、`snapshot()` 返回总 PV / 分页 PV / 当日 PV；提供 `@EnableScheduling` 或惰性跨天清零逻辑；启动时从 `backend/data/stats.json`（Jackson）恢复，每次记录异步/定时落盘。保持简单：内存为主 + 落盘 JSON
2. 新建 `stats/StatsFilter.java`：`implements javax.servlet.Filter`（注意 Java 8 / Boot 2.7 用 javax，非 jakarta），用 `@Component` 注册即全局生效；只统计 GET 且以页面路由结尾的请求，排除 `/api/**`、`/stats`、静态资源（*.png/jpg/ico/js/css）与 favicon
3. 新建 `controller/StatsController.java`：`GET /api/stats` 返回 JSON
4. 修改 `controller/PageController.java`：增加 `/compare`、`/used`、`/used-10w`、`/news`、`/stats` 的 forward 映射

### 阶段 2：内容首页站（`/` 系）
1. **改 index.html**（重做为内容站首页）：
   - 独立导航：首页 | 新车排行 | 新车对比 | 二手车对比 | 10万推荐 | 购车资讯（样式沿用 #333 黑底白字）
   - 板块：①20-50万 SUV 排行（现有 3 款扩充到 8-10 款示例数据，仍用页内 JS 数组）②新车对比入口卡 ③二手车对比入口卡 ④10万二手车推荐入口卡 ⑤购车资讯最新文章卡
   - 广告位 3 处：首屏下方、中部、底部（沿用 `.ads-container` + AdSense 占位结构）
2. **新建 compare.html**：选车对比 —— 两个下拉选车型，横向参数对比表（价格/级别/尺寸/轴距/动力类型/油耗或电耗/0-100/智能配置），内置 6-8 款示例车型数据
3. **新建 used.html**：二手车对比 —— 预算/年份/里程筛选 + 结果卡片列表（车型/年份/里程/参考价/保值率）
4. **新建 used-10w.html**：10万内二手车推荐榜单，竖排卡片（车型/年份/参考价/亮点标签），10 条示例
5. **新建 news.html**：购车资讯列表，卡片网格（标题/摘要/分类/日期），8-10 篇示例文章卡
6. **改 model.html 统一风格**：body 背景改 `#f5f5f5`；导航条改 #333 实底白字并换成内容站菜单（含车型详情）；`.header`、`.subtitle`、`.controls-panel`、`.instructions`、`.info-card`、`.ads-container` 改为浅色卡片 + 深色文字（`#333`/`#2c3e50`）；发光文字色改深蓝；保留 Three.js 全部交互 JS 与 CDN 引用（cdnjs three.min.js 不动）；canvas 容器加白卡片边框
7. **改 about / contact / privacy.html**：补上统一的 #333 导航条（含两站的入口），维持现有内容

### 阶段 3：油电成本站（`/power` 系）
改 power.html：
- 独立导航：成本计算器 | 数据参考 | 首页站入口（样式同 #333）
- 在现有计算器下方新增：①常见车型油耗/电耗数据表（内置 10 款）②年度成本对比结果区（年里程 × 单价）③地区油价/电价参考表
- 底部 FAQ 板块 + 广告位 2 处
- about.html 内容已偏向"全球油电成本计算器"，保留共享

### 阶段 4：流量管理收尾 + SEO
1. stats.html 统计面板：表格（各页 PV）+ 简易柱状图（纯 JS/CSS，不引第三方库），调用 `/api/stats`
2. 所有页面补 SEO meta：`<meta name="description">`、`<meta name="keywords">`，各页标题区分站点前缀（如「XX - 油电成本对比」/「XX - 购车导购」）
3. 广告位与 SEO 代码全部落在 git 中；统计数据文件 `backend/data/stats.json` 被 .gitignore 排除
4. 根目录加 README.md（项目说明 + 两站入口 + 本地运行方式）——用户要求内容纳入 git，README 有助于仓库可读性。**除非用户要求否则不写**（执行时视情况：按规则不主动建 md，故省略）

### 阶段 5：验证与推送
1. `mvn spring-boot:run`（用 Java 8 编译，必要时先 `mvn clean` 避免历史 class 版本 61 问题）
2. 浏览器验证 `/`、`/compare`、`/used`、`/used-10w`、`/news`、`/model`、`/power`、`/stats` 均正常、导航与风格统一、广告占位出现
3. 多次刷新各页后访问 `/api/stats` 确认 PV 递增
4. `git add -A` + commit（分阶段提交历史清晰）→ `git push -u origin main`（触发 GCM 登录弹窗；若失败则向用户要 PAT 重配 remote）

## 关键文件
- `backend/src/main/java/com/carc/backend/controller/PageController.java`（加路由）
- 新增 `backend/src/main/java/com/carc/backend/stats/StatsService.java`、`StatsFilter.java`、`backend/src/main/java/com/carc/backend/controller/StatsController.java`
- 改：`static/index.html`、`static/power.html`、`static/model.html`、`static/about.html`、`static/contact.html`、`static/privacy.html`
- 新建：`static/compare.html`、`static/used.html`、`static/used-10w.html`、`static/news.html`、`static/stats.html`
- 新建根目录 `.gitignore`

## 风险点
- AdSense client/slot 是假号占位，上线需用户替换真实 ID
- Java 8 + Boot 2.7：Filter 用 `javax.servlet` 导入，勿用 jakarta
- 根目录含微信小程序文件，勿误删；push 时一并入库
- push 认证：无 gh 命令，依赖 GCM 弹窗，失败兜底要 PAT
- model.html 的 Three.js 走 CDN，离线会白屏（浅色化不影响此行为）

## 验证方式
- 启动后逐页打开上述路由，检查导航统一、无 404、JS 渲染正常、3D 可交互
- `/api/stats` 返回 JSON 且 PV 随刷新递增
- `git log --oneline` 与 `git remote -v` 确认提交与远端