# 平台能力索引

本索引帮助业务应用判断“哪些能力可以直接组合、应从哪里接入”。能力的完整契约与阶段状态以 [MuYunSpring 平台文档](https://github.com/ximatai/MuYunSpring/tree/main/docs/platform) 和对应版本的测试为准；本页只提供二开导航，不复制框架实现细节。

## 选择入口

| 你要解决的问题 | 应优先使用 | 静态业务模块 | 动态对象 |
| --- | --- | --- | --- |
| 标准新增、查询、修改、删除 | `AbstractAbilityService` 与标准 Web 投影 | 支持 | 支持 |
| 记录启停、软删、回收站、排序、树 | 对应 Ability 与标准字段契约 | 支持 | 支持，按元数据开启 |
| 引用候选、标题投影、关联读取 | `ReferenceAbility`、引用注解或元数据关系 | 支持 | 支持 |
| 租户、当前用户、动作权限、数据权限 | 平台 IAM 上下文与标准 Service/Web 链路 | 支持 | 支持 |
| 页面列表、表单、菜单入口、附件与查重 | 页面交付能力与标准模块 Web 投影 | 支持 | 支持 |
| 编码、导入导出、生单、回写 | 业务自动化能力 | 按专题接入 | 按专题接入 |
| 工作流任务、审批、待办 | 工作流任务能力 | 按专题接入 | 按专题接入 |
| 配置包、健康检查、迁移治理 | 治理能力 | 平台/运维入口 | 平台/运维入口 |

## 业务基础能力

### 标准 CRUD 与能力组合

静态业务对象从模型、`BaseDao` 和 `AbstractAbilityService` 开始。Service 按业务需要组合 Ability，而不是在 Controller 中重新实现通用流程。

常见选择：

| 业务需要 | Service 组合方向 | 稳定字段/语义 |
| --- | --- | --- |
| 逻辑删除与恢复 | `SoftDeleteAbility` 或 `RecycleBinAbility` | 平台基线删除语义 |
| 启用、停用 | `EnableAbility` | `enabled` |
| 同级排序 | `SortAbility` | `sortOrder` |
| 层级对象 | `TreeAbility` | `parentId`，并包含排序语义 |
| 缓存 | `CacheAbility` | 按 Service 生命周期失效 |
| 被其他对象引用 | `ReferenceAbility` | 标题、候选与完整性链路 |
| 聚合子表 | `ChildrenAbility` | 聚合保存与删除链路 |

先从 [Todo Demo](DEVELOPMENT.md#2-先阅读可运行的-demo) 的 `TodoItemService` 阅读最小组合；新增领域只启用实际需要的能力。

### 引用与关联

引用不是业务代码中的手写 ID 查询。静态模块使用 `@ReferenceTo`、`@ReferenceLoad`、`@ReferencedBy` 与对应 Service Ability 声明关系；动态对象在元数据中声明同一语义。这样引用候选、标题投影、租户过滤、软删过滤和引用完整性由平台统一处理。

适用时优先阅读框架的[引用与读取投影契约](https://github.com/ximatai/MuYunSpring/blob/main/docs/architecture/STATIC_REFERENCE_READ_PROJECTION.md)。

## 交付与治理能力

### Web 与页面交付

标准静态模块在 `app-<domain>-web` 中使用 `WebSupport`、`CrudWeb` 和 `@PlatformStaticModule` 投影。平台根据 Service 已组合的能力暴露相应的标准动作；需要独立 HTTP 语义时，仍由 Web 模块声明 Controller，并调用领域 Service 完成业务操作。

页面交付还覆盖菜单入口、页面 bootstrap、列表查询、表单保存、附件、查重与引用候选。入口文档：[页面能力](https://github.com/ximatai/MuYunSpring/tree/main/docs/platform/topics/page)。

### 身份、租户与权限

当前用户、租户范围、动作权限和数据权限属于默认平台链路。领域 Service 不应通过手写查询条件绕开租户作用域；标准 Web 投影会将请求带入相同的身份与权限上下文。

入口文档：[身份与权限](https://github.com/ximatai/MuYunSpring/tree/main/docs/platform/topics/identity-permission)。

### 动态对象与平台配置

结构主要由配置决定的对象使用应用、模块、元数据、字段和关系定义进入动态运行态；运行态仍复用 CRUD、能力、权限、审计与数据访问契约。业务规则稳定且需要 Java 类型时，优先采用静态领域模块。

入口文档：[配置](https://github.com/ximatai/MuYunSpring/tree/main/docs/platform/topics/configuration) 与 [动态运行态](https://github.com/ximatai/MuYunSpring/tree/main/docs/platform/topics/runtime)。

### 业务自动化、工作流与治理

编码规则、导入导出、生单与回写属于业务自动化；流程定义、审批和待办属于工作流任务；配置包、健康检查、版本归档和迁移 dry-run 属于治理能力。这些能力在业务确有触发场景时按专题接入，不预先在每个领域模块中铺设。

- [业务自动化](https://github.com/ximatai/MuYunSpring/tree/main/docs/platform/topics/business-automation)
- [工作流任务](https://github.com/ximatai/MuYunSpring/tree/main/docs/platform/topics/workflow-task)
- [治理](https://github.com/ximatai/MuYunSpring/tree/main/docs/platform/topics/governance)

## 接入前检查

1. 这是稳定 Java 业务规则，还是主要由配置决定的动态对象？
2. 是否已有 Ability 或平台专题覆盖该问题？有则先组合或声明能力，不重复实现通用链路。
3. 业务事实应产生在领域 Service 还是 Web 层？除 HTTP 协议适配外，优先放在领域 Service。
4. 该能力是否需要真实数据库、权限或 Web 装配验证？需要时在本应用补对应的运行态测试。
