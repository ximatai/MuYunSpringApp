# App 前端

该目录是 MuYunSpringApp 的独立前端消费者。它消费 MuYunSpring 发布的 Workbench、标准模块运行器、HTTP/session/menu client 与 UI adapter，不复制平台壳、菜单页签或标准 CRUD 页面。

App 只承担业务页面、业务路由注册和应用级组合。`/app/todo` 是自定义业务入口；`demo.todo_item` 的标准管理入口由平台运行器交付，两者使用同一后端模块与数据。

源码按以下边界组织：

```text
src/App.vue              登录、工作台、页签和平台页面兜底
src/app/auth/            App 级认证会话编排，调用平台 AuthClient
src/modules/<domain>/    业务页面、业务 client 和模块声明
src/modules/index.ts     业务模块注册表
```

新增业务从 `src/modules/<domain>/` 开始；模块通过平台 `ModuleContext` 访问标准业务对象，不手写 URL、Authorization 或 CRUD 协议。业务模块在自己的 `index.ts` 声明菜单 route 与页面组件，再注册到 `src/modules/index.ts`。平台页面仍由 `PlatformAdminOutlet` 交付。

平台 App 样式是公开入口：应用必须在自己的启动文件导入 `@ximatai/muyun-web-app/style.css`。这避免打包器、链接方式或未来 npm 发布导致 Workbench 样式遗漏。

自定义业务页面优先从同一公开包消费 `UiButton`、`UiInput` 和 `UiSwitch`。标准模块的列表、抽屉与表单由 `PlatformAdminOutlet` 在包内交付，App 不直接拼装这些内部组件。不要直接使用 Ant Design Vue 或以原生控件另起一套后台交互样式。

先在 MuYunSpring 生成本地消费者包，再在此目录执行：

```bash
npm ci
npm run install:framework-local -- /path/to/ximatai-muyun-web-app-<version>.tgz
npm run build
```

日常本机联调优先链接框架生成包：先在框架仓库执行 `npm run pack:consumer --prefix muyun-web`，再执行：

```bash
npm link /path/to/MuYunSpring/build/consumer-npm/staging/web-app
```

链接仍只消费框架包的公开 `exports`、声明文件和样式；框架每次重新打包后用 `npm run dev:linked` 重启 Vite，强制刷新被替换生成包的依赖缓存，不需要 npm 正式发布。交付前再使用 tarball 覆盖安装做一次干净消费者验证。消费者仓库不提交框架仓库路径或 tarball。

Vite 已保留链接包路径，因此它的 peer dependencies 仍由本 App 的 `node_modules` 提供；不要向框架生成目录安装依赖。
