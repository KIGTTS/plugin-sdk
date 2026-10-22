# 插件 API v16（兼容 v9–15）

当前 SDK 为 API16，宿主接受API9–16签名包。助手0.9.13、商店0.10.0及独立界面调试0.1.0需要API14；旧兼容服务保留配置与模型。API8及更早的DEX包需由同一发布者重新构建并递增versionCode。专用包、签名、资源归属及安装规则见 [插件包](plugin-packages.md)。

插件实现 `KigPlugin`，提供 `PluginDescriptor`、可选语音引擎/资源管理器、`PluginContributions` 和页面。外置原生代码在独立工作进程运行，宿主通过规范代理传输数据并渲染 UI；插件不持有宿主 Activity、View 或 Compose 实例。`attach` 提供专属资源环境，`close` 在会话取消并结束后释放资源。

## 能力与宿主操作

能力在包清单 `capabilities` 中声明；环境接口绑定调用插件，JSON 不能伪造资源所有者。`app-control` 提供 `PluginEnvironment.appBroker`；`quick-text-read`/`quick-text-write` 提供 [快捷文本 broker](https://github.com/LHT02/KIGTTSNEXT/blob/36e4d8fbb1a199009c01c95ae65f4e97b1759e81/docs/quick-text-plugin-api.md)。`speech-service`、`asr-service` 分别提供合成和识别代理，`network` 控制正式下载/网络资源入口。能力校验约束宿主接口，原生代码仍使用应用 UID。

`stickers-read`/`stickers-write` 提供 `PluginEnvironment.stickerBroker`，用于拍立得名片、文字/图片图层和自定义贴纸库。支持分页读取、调用方目录图片副本、名片 PNG、批量事务与原生删除审核。`ImportMedia` 和 `PublishQuickMedia` 与快捷文本图片双向转换，另要求对应快捷文本读/写能力，见 [名片与贴纸](https://github.com/LHT02/KIGTTSNEXT/blob/36e4d8fbb1a199009c01c95ae65f4e97b1759e81/docs/stickers-api.md)。

`AppBroker.request(AppCommand)` 支持能力发现、当前状态、原生导航、字幕、自动朗读、朗读/停止、已开放设置、跨插件动作和资源投递。调用前读取 `Describe`，按实际支持的目标与设置键构造请求，不能把任意设置当成已开放能力。

- `PluginAction(pluginId, actionId, inputs)` 只可调用目标贡献中明确列出的 `automationActions`。确认、删除、安装批准等私有按钮不因显示在页面中而成为自动化接口。
- `DeliverResource(pluginId, source, actionId)` 将调用方自身的资源复制到目标 inbox，并调用明确开放的接收入口。编辑器/构建器仍负责检查与审核；返回 accepted/pending 不表示安装或构建完成。
- `managedPackages` 仅报告源码运行时已经批准的项目版本，草稿不计入已安装依赖。
- `sourceAction` 必须同时属于 `automationActions`。源深链接只传入未受信任的 `inputs["url"]`；宿主不自动请求网络或信任源。

`PluginContext.locale` 与 `PluginEnvironment.locale` 提供当前语言标签。`PluginLocalization` 描述插件的默认/支持语言，`PluginStrings` 可读取自身 `assets/i18n/*.json` 并回退默认语言。它们是 UI 本地化接口，不改变语音模型支持的语言，也不代表所有内置文案已经翻译。

## UI 扩展位置

| UiSlot | 位置 |
| --- | --- |
| HEADER_START / HEADER_END | 横版 Logo 两侧 |
| EDITOR_TOP_START / EDITOR_TOP_END | 大字幕左上 / 右上 |
| EDITOR_BOTTOM_START / EDITOR_BOTTOM_END | 大字幕左下 / 右下 |
| EDITOR_MENU | 大字幕菜单条目 |
| LISTENING_BOTTOM_START / LISTENING_BOTTOM_END | 聆听面板底部两侧 |
| QUICK_PANEL_START / QUICK_PANEL_END | 快捷面板横条两侧 |
| TAB_START / TAB_END | 分组滚动栏首尾 |

`UiPresentation.BUTTON` 用于按钮；只有 EDITOR_MENU 使用 MENU_ITEM。`UiButtonStyle.ICON`/ICON_TEXT 分别提供图标或图标文字。宿主统一控制配色、尺寸、圆角、按压形变、触感和过渡；空间不足时压缩为图标，超量入口收进更多菜单。右侧动作从右向左排列。大字幕底部中央留给宿主光标移动条，插件不能占用。

动作 ID 在同一插件内唯一，按 pluginId/actionId 路由，并以 order 和命名空间稳定排序。实时添加、撤销和同 ID 内容修改有过渡；退场快照立即禁止操作，已撤销动作不会执行。贡献变化不重建正在拖动的快捷入口。聆听面板缩为单行时临时隐藏底部按钮，不改变插件贡献。

`PluginContributions.gestures` 可提供 `QUICK_TEXT_LONG_PRESS`。没有贡献时长按无行为，点击/上滑仍展开面板。动作和手势共用唯一 ID；冲突按注册顺序选择贡献者。当前识别服务拥有聆听和语音输入入口，不需要另装入口插件。

## 设置入口

主页设置键直接打开设置，当前 UI 不使用抽屉。插件通过 `SettingsEntry(id, label, pageId, icon, cardId, cardTitle, summary, order)` 将页面并入原生设置卡片：

```kotlin
SettingsEntry("recognition", "语音识别", "manager", PluginIcon.MIC,
    cardId = "audio", cardTitle = "音频", summary = "聆听字幕与语音输入")
```

保留卡片 ID 为 `appearance`、`audio`、`content`、`extensions`，标题和卡片顺序由宿主管理。其它 cardId 可建立自定义分组，使用 cardTitle；条目按 order 排列。`PluginIcon.NONE` 省略条目图标，不保留空白图标位。旧 `drawerEntries` 作为兼容设置入口并入功能与扩展，同 pageId 已有正式 SettingsEntry 时去重；其名字不再表示存在抽屉。

依赖缺失时不注册功能按钮/手势，低频设置仍可访问。详细样式见 [页面规范](https://github.com/LHT02/KIGTTSNEXT/blob/36e4d8fbb1a199009c01c95ae65f4e97b1759e81/docs/plugin-page-style.md)。

## 页面协议与过渡

`page(pageId, context)` 返回 `PluginPage`。声明块包括：

| PageBlock | 宿主组件 |
| --- | --- |
| Text / Notice | 说明、标题与主题提示 |
| Actions / Group | 页面操作、嵌套分组卡片 |
| Toggle / Choice / Input | 设置开关、相连选择按钮、原生输入 |
| SourceEditor | 独立源码页，行号、撤销/重做和键盘上方快捷栏 |
| Files | 面包屑目录、稳定行高、多选、移动/复制与删除确认 |
| Chat | 带稳定消息 ID 的原生聊天气泡、流式状态、输入和停止 |

Toggle 包含 title/checked/actionId/summary/icon；Choice 包含 title/options/selected/icon；Input 包含 title/value/actionId/secret/multiline/icon。输入完成在 `context.inputs["value"]` 回传。NONE 可用于这些设置行省略图标。SourceEditor 保存动作收到 `inputs["value"]`，最大显示 64,000 字符；较大文件不得截断写回。

Files 的 `PluginFileItem` 提供相对 path、name、directory、bytes。打开回调输入 path；operationAction 输入 op（move/copy/rename/delete）、paths（JSON 路径数组）、destination（目标相对目录，重命名时为新名称）。导入/创建/上一级使用 directory。文件 IO、覆盖策略和错误由插件负责，宿主不越过插件资源边界。列表至少 72 dp，副行和尾部操作区始终占位，多选/取消/菜单切换不改变行高。

Chat 提供最多 100 条带 ID 的 USER/ASSISTANT/TOOL 消息、文字、可选服务商公开 reasoning 和 COMPLETE/STREAMING/FAILED/CANCELLED 状态；插件保管完整历史，宿主渲染有界窗口。发送回调输入 message。模型请求、密钥和会话都属于插件。

页面预加载并缓存；进入已有页面不重放所有条目的初次动画。`KigPlugin.uiChanges: Flow<Long>` 是可合并的失效信号，宿主异步刷新快照，不在绘制阶段运行插件。完整源码编辑器/文件页、局部块的增删/更新、按钮与列表落位沿用宿主过渡。短暂 busy 和导航锁定保留颜色，仅禁止输入，避免闪成不可选样式。

二/三级页使用 LargeFlexibleTopAppBar，背景与转场覆盖状态栏，操作避开系统栏。顶层 Actions 映射顶部操作，Group 内动作保留在组内。忙碌环形进度在展开栏位于返回键右侧，单行栏位于标题后。页面进入/返回有完整过渡，预测返回沿该页面入场侧返回，保留父页草稿与滚动；子页无常驻全局菜单入口。

## 动作结果

`execute(actionId, context)` 可以返回 Message、Dialog、Menu、OpenPage、ReplaceEditorText、RefreshUi，以及原生资源导入/导出、TextInput、PresentContent。菜单动作仍交给原插件。ImportResource 完成后输入 resourcePath 相对路径；ExportResource 的 temporary 文件在保存/取消后清理。TextInput 回传 value，可设置 multiline/secret。

跨插件导航与服务使用 OpenPluginPage、SelectTtsService、SelectRecognitionService、SetPluginEnabled、RecognitionRoutingChanged。StartRecognition/EndRecognition 使用下节的会话协议。InstallPackage 请求宿主完整验签与发布者/版本/权限确认，不能绕过审核。HostAction 保留规范兼容命令。

## 语音接口

`PcmAudio` 为帧对齐的有符号小端 PCM16，携带采样率/声道；当前规范范围 8–192 kHz、单/双声道。PCM 管道单块最大 128 KiB，具有有界背压，不将整段录音放入 Binder 事务。

- TTS：`synthesize(TtsRequest): Flow<SynthesisEvent>`，Started、Audio、Finished。
- 完整录音：`recognize(PcmAudio, RecognitionConfig): RecognitionResult`。
- 流式识别：`recognizeStream(Flow<PcmAudio>, RecognitionConfig): Flow<RecognitionEvent>`。

Flow 是冷流，结束输入代表正常录音 EOF；取消应释放当前会话，不强行发送 Final，也不吞掉 CancellationException。空输入不编造文本。AsrInfo.streaming 的 StreamingKind 明确为 NONE、SEGMENTED（逐句离线）或 INCREMENTAL（在线增量），不能用模拟 Partial 把离线模型包装成在线引擎。

`AsrInfo.role` 为 PROVIDER/SERVICE。主程序仅选 SERVICE；声明 asr-service 的服务通过 recognitionBroker 调用独立提供者，禁止自调用/服务嵌套。当前识别服务统一注册聆听、快捷长按和连续输入。`RecognitionSession` 指定 serviceId、CAPTIONS/VOICE_INPUT、配置、continuous、PANEL/FLOATING/BOTH、disableAutomaticRead 和 readFinalized。宿主负责录音、权限和展示，模型/分句仍在插件。

服务输出 `Transcript(committed, partial, sequence, finalized)`：前文固化、尾段可修订，序号每个非空最终句递增，重复句也不丢失。显示窗口默认 12,000 字符并保持 UTF-16 完整；新固化段单独传递，用于连续提交与朗读。连续输入启动时关闭自动朗读，用户再次打开后只朗读新固化段。服务缺失/引擎未就绪时不打开麦克风。详见 [识别服务](https://github.com/LHT02/KIGTTSNEXT/blob/36e4d8fbb1a199009c01c95ae65f4e97b1759e81/docs/recognition-service.md) 和 [增量引擎](https://github.com/LHT02/KIGTTSNEXT/blob/36e4d8fbb1a199009c01c95ae65f4e97b1759e81/docs/streaming-recognition.md)。

## 合成服务与语音包

`TtsInfo.role` 为 PROVIDER/SERVICE，SERVICE 统一路由提供者。SpeechBroker 提供 providers、synthesize、control 与 VoiceCommand；目录只包含独立提供者，停用项可从规范缓存查看，不可执行合成/修改。通用生命周期见 [预热、复用与释放](https://github.com/LHT02/KIGTTSNEXT/blob/36e4d8fbb1a199009c01c95ae65f4e97b1759e81/docs/tts-lifecycle.md)。

语音合成服务内置系统 TTS 保底，不单独分发系统 TTS 引擎。无其它可用提供者或某段输出音频前失败时调用保底；已经输出该段音频后不重读，取消不触发保底。平台连接与缓存也只在服务插件中，主 APK 不链接系统 TTS 实现。

VoiceManager 与 voice-packs 能力支持 IMPORT、SELECT、DELETE、RENAME、REFRESH、UPDATE_METADATA、SET_AVATAR、EXPORT、REORDER。TtsInfo.packs/VoiceInfo.packId 区分语音包与包内声音，VoicePackInfo 提供名称、备注、头像、置顶和操作能力。IMPORT 相对资源由 broker 复制到目标 tmp，调用结束清理；模型解析、配置和语音包兼容由各引擎处理。跨引擎统一管理及 Piper 旧格式见 [插件包](plugin-packages.md)。

PluginPage.management 可返回 ManagementList。ManagementItem 以稳定 ID 描述标题、说明、资源、主动作、菜单、启停和选中；reorderAction 收到 inputs["order"] 的完整 JSON ID 列表。ItemOrder.validate/reconcile/move 可用于校验/保存顺序；不同 group 不跨区排序。界面、阴影、落位与删除确认由宿主组件提供。

## 当前运行方式

外置原生 `.kigplugin` 使用 Android DEX 和可选 JNI；Lua `.kiglua` 由可选 `runtime.lua` 插件审核与运行，不是主程序内置脚本执行器。Python 运行时尚未实现。外置 Agent、Lua、商店、原生构建及其依赖不进入主 APK。

运行时有 12 个工作进程槽，每个启用的原生外置插件使用独立普通进程。原生进程共享 UID，属于崩溃隔离；Lua 的库/指令/资源限制也不是操作系统级权限沙盒。模板和包源 Schema 位于独立私有仓库 [KIGTTS-PLUGIN-TEMPLATES](https://github.com/LHT02/KIGTTS-PLUGIN-TEMPLATES)。

## API 11：识别扩展、聊天与有回执调用

这些合同在API11引入，当前SDK为12、宿主支持API9–12插件。旧聊天构造和复制入口保留；新包按本次编译SDK标记API，不应手工把新字节码标为旧版本。插件商店按兼容范围保留旧前置包。

- `UiSlot.VOICE_INPUT_RELEASE`：围绕发送区的弧形释放按钮，支持图标/图文与voiceText输入。
- `PageBlock.Chat`：conversationId、controls、approval、backgroundRunning；controls支持MODEL/ACCESS，审批由宿主固定在composer上方。ChatMessage补summary、toolName、detail与icon。
- `AppCommand.PluginCall`：只调用目标显式automationActions，返回已完成结果；以宿主身份绑定调用者并过滤保留字段，不能传递虚构批准。
- 生成中的原生聊天快照标记backgroundRunning，切换会话仍保留后台任务通知；结束时释放。

## API11 扩展合同

Chat支持图片资源、thinking/hasReasoning、稳定position、持久队列与分页历史。hasReasoning只表示存在供应商公开思考，不把摘录充当summary；无摘要时显示已思考。保留API9/10及API11首版的构造/copy默认桥。提供商、模型、真实元数据驱动的强度在同一原生弹窗；权限位于输入框上方。

`PageBlock.Browser`、主题/安全区、原生振动与真实WebView截图见[网页UI](web-ui.md)。`symbol`离线Material Symbols和手选图标见[UI图标](ui-icons.md)。`AppCommand.UiAssets`提供目录查询、图标导入和受限SVG渲染；`Permission`由宿主逐能力暂停授权，`ManagedCall`仅允许所属Lua运行时转发真实项目。统一管理的ManagedPackage包含权限、用途、启停、编辑/卸载动作和错误。权限及崩溃恢复边界见[插件权限](plugin-permissions.md)。

## API12 增量

`ChatQueuedMessage.steerAction/reorderAction`提供队列内插队与左手柄排序，保留API11四参数构造和copy桥；完整order数组仅在拖放后提交一次。`AppCommand.BrowserSession`提供受调用方身份约束的独立页面状态/操作/等待/截图/关闭，详细字段和权限见网页UI合同。新命令的包必须标记API12，不应下调版本以安装到旧宿主。

## 备份与还原格式插件

宿主设置的“内容 → 备份与还原”提供原生 `.kigcards` 导入、完整导出与导入前修改预览。它不依赖 AI 助手安装。导入不清空原有词条；原生归档解析继续限制资源大小、路径与数量，并检查提交时的修订号。

兼容插件贡献 `SettingsEntry(id="legacy-import", label="旧格式兼容", pageId="formats", icon=PluginIcon.NONE, cardId="backup", summary="支持的格式说明")`，入口会在该页面的“兼容格式”内显示，支持无图标或离线 `symbol`。`cardId="backup"` 为宿主保留位置，不额外生成设置首页卡片。无需新增自定义 JSON 协议。

格式插件通过自己的原生导入页取得用户选择的资源，只在自己的数据目录解析；用 `QuickTextBroker` 转为标准条目与受控媒体，再走 `Prepare`/`OpenUi(REVIEW)`/提交或标准 `.kigcards` 导入。需声明 quick-text-read/write 与具体用途，宿主继续逐能力授权。禁止直接写数据库、复用其它插件的资源归属或把文件中的指令作为用户批准。娃语、kighelper、旧版 KIGTTS 等兼容器作为单独包发布；本次只提供接入位置和标准合同，不包含这些格式解析器。`host.backup` 是原生备份存储身份，不能安装同名插件。
## API13 增量

新增PluginOutcome.Confirm(title,text,action,cancelLabel)与CapturePhoto(title,directory,actionId)，分别由宿主原生确认窗和系统相机处理；保留API9–12原有数据类构造/copy与序列化字段。使用新结果的包必须声明API13。BrowserSession增加open-subtitle操作值，用于明确标识的大字幕布局模拟，不改旧命令构造。完整主题、安全区、坐标和设备权限合同见WebUiGuide v3与网页UI文档。

## API14 展示合同

`ManagementItem.symbol` 可指定 Material Symbols，旧 PluginIcon/图片仍可用。`ManagementList.style=CATALOG` 提供64dp图标与介绍的商店布局；默认STANDARD保持通用管理列表。旧构造与copy入口保留。

`PageBlock.Markdown(text, images)` 渲染GFM文本，图片只能从插件预先准备的本地PluginResource映射读取；不执行HTML或自动下载外图。`PageBlock.Gallery` 提供最多8张预览。源展示资源合同见 [KPM](https://github.com/LHT02/KIGTTSNEXT/blob/36e4d8fbb1a199009c01c95ae65f4e97b1759e81/docs/package-store.md)。

`CardLayerKind.PHOTO_INFO` 表示相纸备注下方的左对齐附加信息，最多8项、imageId为空；不参与贴纸拖动。全部字段在备注下方以带图标的单行显示，预览和静态/动态导出共用测量。原生日期/时间/日期时间/地点/天气分别使用每卡唯一的`photo-info.date`、`photo-info.time`、`photo-info.date_time`、`photo-info.location`、`photo-info.weather`图层ID，开关替换同类项；插件可保留这些ID维持字段类型，其他PHOTO_INFO使用通用信息图标。读取后编辑卡片时保留这类图层，不改变SDK14构造或序列化合同。

## 可选页面展示元数据

API15新增以下可选元数据，新编译包必须标API15；API9–14既有包的JVM入口继续保留。

`PluginPage.header`以ManagementItem提供无操作的详情图标、标题与简介，图片优先、Material Symbols回退。`onEnterAction`由支持它的宿主在页面真正可见且动作空闲时每次进入调用一次；快照读取、UI刷新和重组不会触发，插件应快速启动后台任务并合并重复请求。`PageBlock.Group(collapsible=true)`默认折叠，宿主保留展开状态并显示展开箭头，可用initiallyExpanded改变初始值。字段均为可选，保留原JVM构造/copy桥和旧JSON兼容；旧JSON读取仍能使用原字段默认值。

## 后台加载与下拉更新（API16）

PluginPage.loading 只控制插件通用标题栏的加载环，不锁定页面按钮或改变列表几何。用uiChanges通知宿主重新读取快照；后台任务完成或失败时必须恢复false。refreshAction在普通页面/管理列表/商店目录列表顶端开放下拉更新及无障碍更新动作，后台loading或操作繁忙时不重复派发。指示器跟随下拉，松手后收回，持续进度由标题栏展示。浏览器、聊天和源码编辑器仍由各自界面管理手势。

页面读取不得启动网络。插件应在execute里启动后台工作并合并重复请求，保留旧内容，失败后仍可重试。API9–15构造、默认参数入口及copy桥保持可用；旧JSON字段默认loading=false、refreshAction=null。新编译包标API16，避免旧宿主误装。

## API17 字幕渲染器

`PluginOutcome.SetSubtitleRenderer(root, mainPage="index.html")` 为当前会话安装本地文字字幕渲染器；`root=null` 只关闭调用插件自身的渲染器。根目录必须归调用方所有，主文件必须是根内现有文件。宿主不替换原文、不创建媒体条目，编辑时仍使用原生输入框；媒体/网页快捷文本沿原展示规则。

网页定义 `window.KigttsSubtitle.update({text, placeholder, fontSizePercent})`，宿主在文档就绪及内容变化时更新，禁止通过重载网页刷新字幕。主题沿宿主网页协议传入；渲染器无互联网、设备或应用控制桥。停用、卸载或工作进程失效时清理会话渲染器；重新启动不自动打开。使用该结果的插件声明API17，已有API9–16包继续兼容。

## API18 字幕控制与绘制手势

- `HostAction(TOGGLE_SUBTITLE_FULLSCREEN)` 切换字幕全屏。只读输入 `host.subtitleFullscreen` 为 `true` / `false`，同名动作参数无法覆盖宿主值。
- 渲染根目录可选 `renderer.json`（UTF-8 JSON 对象，最多4KiB）。宿主监听原子替换并通过 `KigttsSubtitle.update` 的可选 `settings` 对象即时通知原页面，不重载、不清空字幕。建议临时文件写完后原子替换；旧版渲染器可忽略该字段。
- `GestureAction(..., SUBTITLE_STROKE)` 仅在前台文字字幕启用单笔采样。动作输入包含 `gesture.points`（JSON `[[x,y],...]`，最多512点）、`gesture.durationMs`、`gesture.width`、`gesture.height`，坐标以旋转后的字幕局部像素为单位。点按、长按、双指仍由宿主处理；活跃绘制区域优先接收单指划动。
- `SendQuickText(entryId)` 按当前库中的既有ID发送，宿主重新解析词条并检查 `quick-text-read` 授权，随后复用点击词条的展示/播放逻辑。删除的ID不发送旧内容。`NoAction` 用于忽略无法识别或不适用的手势，不显示通知。

使用这些接口的包声明API18；已有API9–17包继续兼容。插件自行保存绑定ID并监听词条删除，不应保存待发送的内容副本或按同名词条自动重绑。
宿主在 `PluginContext.inputs["host.subtitleRenderer"]` 提供当前渲染器所属插件ID，未启用时为空字符串；该宿主字段覆盖动作输入中的同名值。插件据此生成开关标签/图标并判断切换方向，避免渲染回退或宿主会话重建后依赖进程内布尔值而出现第一次点击无效。

## API19 原生控件弹窗

`PluginOutcome.Controls(title, controls, resetActionId)` 提供内部滚动的原生设置面板。`PluginControl.Slider`用于连续数值或带steps的档位；`Toggle`为开关；`Choice`通过SEGMENTED或SELECT选择按钮组或下拉选择器；`Color`使用宿主HSV/HEX取色器，值为无符号ARGB Long。它们都带title/actionId，变更调用execute并通过inputs["value"]传回十进制浮点数、true/false、选项ID或ARGB十进制Long。宿主合并高频滑条输入并串行提交，最终值不因worker繁忙而丢失。返回NoAction保留面板；返回Controls刷新全部初值。重置动作没有value。

字幕渲染器铺满相纸/字幕卡片背景；update.safeArea的top/right/bottom/left为CSS像素，已经换算设备密度，文字灯珠应避让此区域，背景仍铺满。renderer.json可选keepScreenOn、followSystemBrightness、screenBrightness（0.1–1）、quickSwipeOpensQuickText；仅在该渲染器可见时生效，离开或停用恢复原窗口亮度及常亮状态。快速左滑打开现有快捷文本面板，关闭时保留主界面侧滑操作。