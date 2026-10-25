# KIGTTS Plugin SDK

KIGTTS插件开发接口，采用MIT协议。API29兼容范围由接口及宿主共同校验，管理桥独立版本化。

API29 增加 `AppCommand.OpenWeb`（字幕网页 / Custom Tabs）与独立的 [KPM 资源包](docs/resource-packages.md) 导入及结果回执。网页和 NFC 使用统一的官网信任与第三方提示规则；添加到快捷文本始终确认。

```powershell
git clone https://github.com/KIGTTS/plugin-sdk.git
./gradlew.bat :plugin-api:test :plugin-android-api:assembleDebug
```

需要JDK17与Android SDK36。Android与平台中立接口分别维护在plugin-android-api和plugin-api。
插件以固定SDK子模块开发，可独立构建，不依赖主软件仓库。正式插件必须保留自己的许可证和第三方NOTICE；SDK的MIT许可不会替换插件许可。签名私钥属于发布者，不进入源码或发行包。

[接口](docs/plugin-api.md) · [插件包](docs/plugin-packages.md) · [权限](docs/plugin-permissions.md) · [网页UI](docs/web-ui.md)

## API23 · 行内操作与下载进度

`ManagementList.itemActions` 为指定条目提供宿主主题按钮；`ManagementProgressAction` 支持文案、操作、确定或不确定进度环。按钮独立于条目详情点击，进度区保留稳定触控空间。旧 ManagementList 构造和 copy 方法保持兼容。

## API24 · 管理条目与单选

`ManagementItem.badges` 提供简短类型标签，`selection = ManagementSelection(action, enabled)` 声明独立单选控件；当前选中状态仍使用 `selected`，整行点击仍使用 `primaryAction`。开关与单选保留在行尾，辅助操作由宿主放在下排，宽度不足才收入更多菜单。相邻同组条目使用连贯圆角。保留 API9–23 的构造和 copy 入口。

## API25 · 商店导航与滚动加载

目录列表可通过 `ManagementList.shortcuts` 在顶部声明宫格入口，`filters` 显示主题色 Tab。`loadMore` 提供到达列表末尾时调用的动作；追加条目后更新动作标识，加载完毕后设为 null。宿主保留现有列表和滚动位置，每个标识仅自动调用一次。旧构造器、默认掩码和 copy 入口继续保留。

API25 展示：`SetSubtitleRenderer.presentation` 接受 `PanelPresentation(fullscreenBackground = 0xff000000L, fadeControlsWhenIdle = true)`；外围色为 ARGB，仅全屏使用。`SetListeningPanel.fadeControlsWhenIdle` 同样支持闲置按钮淡化。宿主保留安全区、触摸与键盘可恢复按钮，默认约6秒后淡化并遵守系统无障碍超时。旧请求默认保持原样。

## API26 · 图标提醒气泡

`UiAction.badge`、`PageAction.badge`、`SettingsEntry.badge` 共用 `PluginBadge`。设为 `null` 隐藏；`PluginBadge()` 显示圆点；`PluginBadge(count = 8, description = "8 个插件可更新")` 显示数字，超过 99 由宿主显示 `99+`。数量必须大于 0；没有待处理项目时传 `null`。

`tone` 支持 `BadgeTone.THEME`（主题色）、`WARNING`（错误/警告色）和 `CAUTION`（黄色，适合待审核）。`description` 是给无障碍服务的完整说明，数字缩写不影响读屏。插件通过 `uiChanges` 更新贡献快照；不要为了刷新气泡重新加载插件或重建页面。状态由插件管理，宿主不会代为清除提醒。旧构造器、默认参数与 copy 入口保持兼容。
