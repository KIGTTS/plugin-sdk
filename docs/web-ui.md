# 网页与插件浏览器界面

API11 的 `PageBlock.Browser` 在原生插件页面中嵌入浏览器，指定本插件的 `PluginResource` 目录、相对 `mainPage` 与明确的 `allowedActions`。资源路径必须在所属插件目录内。网页快捷文本的“选择主页面”只遍历当前词条项目，不打开全局媒体池。

本地来源为 `https://appassets.androidplatform.net/content/`。页面建议加入：

```html
<meta name="viewport" content="width=device-width,initial-scale=1">
<link rel="stylesheet" href="/kigtts/theme.css">
<link rel="stylesheet" href="/kigtts/m3e.css">
<script src="/kigtts/components.js" defer></script>
```

`--md-sys-color-*` 使用应用实际调色板，`--kigtts-corner` 跟随圆角设置；`window.KigttsTheme.colors/dark`、根元素 `data-kigtts-theme` 与 `kigtts-theme-change` 事件提供亮暗色变化。共用样式包含 `m3e-button`（tonal/outlined/text）、`m3e-icon-button`、`m3e-card`、`m3e-field`、`m3e-chips`、`m3e-segmented`、`m3e-switch`、`m3e-slider`。它们是网页样式，不是 Android 原生控件。

图标按钮必须有可访问标签，触控目标至少48px；使用flex/grid、min-width:0和可换行文本，避免固定尺寸画布或滚动陷阱。适配竖屏、横屏、窄屏、放大字体和键盘。页面使用 `--kigtts-safe-top/right/bottom/left` 避让宿主覆盖区，可叠加系统 `env(safe-area-inset-*)`；`100dvh` 提供 `100vh` 回退。画布/视频保持比例，改变尺寸不清空数据。

本地受信任页面可通过 `Kigtts.postMessage(JSON.stringify({action:'声明的动作',inputs:{key:'string'}}))` 发送允许列表内的本插件动作。远程网页没有此桥。共用脚本提供 `KigttsUI.haptic('click')` 或 `data-haptic="confirm"`：支持click、confirm、reject、selection、toggle-on、toggle-off、long-press、drag-start、drag-end。宿主遵循用户和系统震动设置，只接受本地主框架近期真实手势，并限频；不要每帧调用，不接受自定义时长/波形。

网页词条首次真正访问外网时按词条请求权限；可在编辑页改为询问、允许或禁用。本地资源免联网授权。仅本次允许覆盖当前页面加载；因CSP阻止的首次fetch/连接授权后会重新加载，需保存页面临时状态。授权前CSP阻止连接，授权后的POST保留WebView原生请求体和Cookie处理，第三方Cookie关闭。插件浏览器沿用该插件的network权限。

`AppCommand.BrowserCapture` / Agent的 `browser_screenshot` 使用真实Android WebView生成单个视口PNG和错误/控制台诊断，仅写入调用插件的 `workspace/screenshots/`。指定一个已保存网页mediaId或本插件Browser页面pageId；尺寸240–2048且不超过3MP。不截图其它应用、不模拟触控、不申请新网络授权。不要把被拒绝资源的截图说成联网验收通过；模型必须支持图片输入才能做视觉判断。

网页默认不自动截图为缩略图，使用图标或用户手选图片/SVG。图标与许可证见[UI图标](ui-icons.md)。公共合同也由SDK的 `WebUiGuide.TEXT` 提供给插件与Agent。

## API12 持久验收浏览器

`AppCommand.BrowserSession`支持open/state/click/type/scroll/wait/screenshot/close，传入`sessionId`复用同一页面状态。open指定已保存mediaId或本插件Browser pageId；state返回可见控件的实际选择器和坐标，click/type须使用实际元素。screenshot等字体、图片、DOM稳定及渲染帧；异步页面可用`window.KigttsPageReady=false`阻止过早截图，就绪后改true，或用wait指定可见selector。超时返回明确错误或未稳定诊断，不能报告已通过。

每插件最多3个、总计6个会话，闲置10分钟或宿主界面重建后释放；SESSION_EXPIRED需重新open。会话不提供宿主动作、振动或设备桥，不能代用户授权，网络只使用已有授权。截图仅能写调用方的workspace/screenshots目录。

## 网页设备与下载

普通网页首次实际请求摄像头、麦克风、运动传感器或下载时，原生弹窗按词条/插件和HTTPS origin独立询问，支持授权、仅本次允许、禁用；Android摄像头/麦克风/旧系统存储权限另行请求。进入页面不会预先申请。离场停止设备；跨来源不能继承授权，可在词条网络与网页设备设置中清除设备授权。

使用标准`navigator.mediaDevices.getUserMedia`；运动可监听`devicemotion`/`deviceorientation`或调用`KigttsDevices.requestMotionPermission()`，返回granted/denied。原生未授权传感器由Permissions-Policy关闭，经同意的桥提供约10Hz运动数据。下载可用`KigttsDevices.download(httpsUrlOrBlob,name)`；HTTPS交系统下载，不携带模型密钥，本地Blob/data文件最多8MiB，保存到系统下载目录。页面须处理拒绝和设备不支持，不能循环请求。
