# 专用插件包与引擎迁移

## 当前构建与分发

主程序 APK 不携带语音引擎、模型或工具实现。当前外置包统一为 0.8.0/API9：

| 插件 ID | 用途 |
| --- | --- |
| service.speech-synthesis | 合成服务、跨引擎语音包管理、内置系统 TTS 保底 |
| service.speech-recognition | 聆听字幕、语音输入、连续识别与引擎路由 |
| engine.piper | Piper 独立合成引擎 |
| engine.sensevoice | 完整录音与分段离线识别 |
| engine.streaming-asr | 在线增量识别与多语言识别包 |
| tools.cloud-agent | 云端助手与多会话聊天 |
| runtime.lua | 独立 Lua 插件编辑器与运行时 |
| tools.package-store | 包源、版本、镜像与前置依赖管理 |
| tools.native-builder | Java/Kotlin 源码项目构建、签名与导出 |

`tools.quick-text-example` 为可选接口示例。系统 TTS 只作为语音合成服务自带保底，**不再构建或分发独立 engine.system-tts 包**；共享系统实现只进入服务包，不进入主 APK。

使用 PowerShell 7、JDK 17 和 Android SDK 36：

```powershell
pwsh -File tools/Build-EnginePlugins.ps1
pwsh -File tools/Build-EnginePlugins.ps1 -Modules streaming-asr
pwsh -File tools/Build-EnginePlugins.ps1 -Modules quick-text-tools
```

输出位于 `build/plugin-packages/<插件ID>-0.8.0.kigplugin`。脚本默认使用仓库 `.tools/gradle-8.13` 与 `.tools/android-sdk`，可先构建 Android library 后使用 -SkipBuild 打包。临时目录默认在当前用户 LocalAppData/Temp/KIGTTSNEXT，可用 -WorkDirectory 指定。D8 只接收插件 classes.jar、实际运行依赖和本地库；API/Kotlin/协程由工作进程父加载器提供。

首次打包在 `.tools/plugin-signing/private.der` 生成本地 RSA 私钥，不入 Git。后续更新保留同一发布者；正式发布可用 -SigningKey 指定 PKCS#8 DER 私钥。更新递增 plugin-spec.json.versionCode，descriptor.version 与清单一致；签名身份不能随意替换。

## 原生包结构

```text
plugin.json                 # API、入口、版本、能力、ABI、payload SHA-256
code/classes.dex            # Android DEX，不是普通 JVM JAR
code/classes2.dex           # 可选多 DEX
lib/<ABI>/*.so              # 插件专属本地库
assets/                     # 只读资源与许可证
signature/public.der        # RSA SubjectPublicKeyInfo
signature/manifest.sig      # 原始 plugin.json 的 SHA256withRSA 签名
```

原生运行时为 android-dex。API8 及更早的包须同发布者升级为 API9；更新保留原模型与配置，不应通过卸载升级模型引擎。插件不能在安装时添加 Activity、Service 或 Android 资源 ID，界面通过公共协议由宿主 MD3e 组件渲染。

Lua `.kiglua` 由独立 [插件编辑器](https://github.com/LHT02/KIGTTSNEXT/blob/36e4d8fbb1a199009c01c95ae65f4e97b1759e81/docs/lua-plugins.md) 导入、审核与执行；商店分发使用额外的清单 hash 与 RSA 签名。手动导入的普通 Lua 源 ZIP 不等于商店签名包。Python 运行时尚未实现。Java/Kotlin 源码项目可由 [原生构建扩展](https://github.com/LHT02/KIGTTSNEXT/blob/36e4d8fbb1a199009c01c95ae65f4e97b1759e81/docs/native-builder.md) 生成 `.kigplugin`，不把编译器装进主 APK。模板、发布源 Schema 与签名辅助脚本见独立私有仓库 [KIGTTS-PLUGIN-TEMPLATES](https://github.com/LHT02/KIGTTS-PLUGIN-TEMPLATES)。

## 安装与生命周期

从“设置 → 插件管理”安装本地 `.kigplugin`，也可使用 [独立管理器](https://github.com/LHT02/KIGTTSNEXT/blob/36e4d8fbb1a199009c01c95ae65f4e97b1759e81/docs/plugin-manager.md) 或 [插件商店](https://github.com/LHT02/KIGTTSNEXT/blob/36e4d8fbb1a199009c01c95ae65f4e97b1759e81/docs/package-store.md)。首次安装显示名称、版本、许可证、能力与发布者指纹，用户确认后安装；商店的 expectedPublisher 不会替用户授予信任。

安装与加载检查全部有效载荷，拒绝路径穿越、重复路径、清单外文件、超限资源、API/ABI 不兼容、未批准的降级及发布者变化。Android 14+ 代码文件在打开写入通道后、写入前置为只读。安装后独立工作进程调用 attach，建立规范 UI/引擎代理；PCM 使用有界管道，不放入大 Binder 事务。

停用保留数据；卸载删除包、模型、配置、网络与外置资源。更新、资源导入、停用与卸载会先结束相关音频会话并销毁对应进程，避免继续使用旧类/本地库/模型映射。当前有 12 个外置插件工作进程槽。进程中断报告失败，停用后重新启用可恢复；退出宿主运行时会释放已加载进程。

这些普通进程与宿主共享 Android UID，提供崩溃隔离，不构成权限沙箱。能力声明约束公开宿主接口，不能阻止任意原生 DEX/JNI 直接访问 Android API。Lua 的库/指令限制也不等于操作系统沙箱；安装源代码与原生包均需检查来源。

## 数据目录

插件的网络、模型、外置文件、配置、缓存与导出都归属：

```text
<应用专属文件目录>/appdata/Kigtts/plugin/plugindata/<pluginId>/
  config/
  models/
  resources/
  downloads/
  tmp/
  files/
  cache/
```

常见外置路径为 `/storage/emulated/0/Android/data/com.kigtts.next/files/appdata/Kigtts/plugin/plugindata/<pluginId>/`；外置不可用时保持相同内部相对目录。插件代码与只读包资源位于内部 `files/kigplugin/packages/`，与可写下载/模型分开。

PluginStorage 只接受规范相对路径。导入先复制到本插件 tmp，验证后交换目录，失败清理暂存并保留旧文件。正式下载接口使用 HTTPS、可校验 SHA-256，取消清理中途文件。SenseVoice 自定义下载必须提供可信 SHA-256。不得把插件资源散落在宿主 SharedPreferences、公共 Download 或其它插件目录。

## 合成与语音包

语音合成服务通过“设置 → 音频 → 语音合成 / 语音包管理”进入，统一声音、语速、系统语言与跨引擎语音包。无需安装独立系统引擎即可使用内置系统 TTS；外部提供者未配置、停用或在当前段输出前失败时保底，取消不重播，已输出段失败不重复朗读。系统语言数据仍由 Android 的 TTS 应用管理。

Piper 移植旧 ONNX/eSpeak/拼音/日文读音与近似适配。支持 `.kigvpk`/ZIP manifest 与 ONNX+同名 JSON ZIP、嵌套目录、多个声音、更新、头像、备注、置顶、排序、导出与确认删除。模型在 engine.piper/models/voices，发音资源在 engine.piper/resources，不混入服务或主程序。当前完整本地库支持 arm64-v8a；原工程预编译库、固定 eSpeak 源归档与独立构建入口保存在插件 native-source，不能将“提供构建入口”说成已验证独立 NDK 构建。

旧包兼容 manifest.files.model/config/phonemizer、Windows 分隔符、标量/数组音素映射、词典、采样率和多声音。旧元数据 voicepack.json 的名称、备注、头像、置顶、顺序与未知字段保留，支持 .bak 恢复，并迁移上一版 voice-meta.json。旧包未声明 phoneme_type 且带词典时按旧版 text 路径；标准 ONNX+JSON 默认 eSpeak。

更新先验证后交换，失败恢复旧资源，保持包 ID、声音选择及用户资料。导出保留原文件并提供旧版可接受的 manifest/config/phonemizer 三件组及 voicepack.json，标准 eSpeak 包补明确类型和空词典。跨引擎列表将一个语音包与包内多个声音分层；系统声音使用服务内置虚拟包。停用引擎的已知目录可查看/排序，修改与合成需先启用。

导入原件暂存于服务 resources/inbox，broker 只将相对资源复制到目标引擎 tmp，结束清理。模型、词典与外置文件始终属于各自插件。低延迟复用与空闲回收见 [合成生命周期](https://github.com/LHT02/KIGTTSNEXT/blob/36e4d8fbb1a199009c01c95ae65f4e97b1759e81/docs/tts-lifecycle.md)。

## 识别

[语音识别服务](https://github.com/LHT02/KIGTTSNEXT/blob/36e4d8fbb1a199009c01c95ae65f4e97b1759e81/docs/recognition-service.md)统一拥有聆听字幕和语音输入，调用独立识别引擎。SenseVoice 沿用 16 kHz 单声道离线模型，完整录音最多 30 秒；分段模式依据音量、500 ms 静默及最长 12 秒切段逐句推理，提供 Final，不伪造在线 Partial。

SenseVoice 可导入 ONNX+tokens.txt 或二者 ZIP，包括嵌套资源包。7z、Kokoro、降噪、声纹、AEC 和说话人分离未在此版迁移。在线增量、多语言包及 `.kigasr` 见 [流式识别](https://github.com/LHT02/KIGTTSNEXT/blob/36e4d8fbb1a199009c01c95ae65f4e97b1759e81/docs/streaming-recognition.md)。模型、下载、导入和推理库都在引擎包/目录，服务与主程序不解析模型。

移植合成/识别和系统共享实现保留原工程 GPLv3；主程序/API/通用运行时、识别服务及新增在线插件使用各自 MIT。sherpa、ONNX Runtime、eSpeak、发音器与词典许可证随插件保留，模型许可证随下载资源保留。

## 验证与历史记录

当前回归范围包括包签名/实际产物、版本与完整性、路径边界、导入回滚、PCM 帧、无模型不开麦克风、取消与有界流式结果、以及主 APK 无引擎/模型/插件包资产。API9 当次构建、手机安装和验收结果以 [Release 验证记录](https://github.com/LHT02/KIGTTSNEXT/blob/36e4d8fbb1a199009c01c95ae65f4e97b1759e81/docs/release-testing.md) 为准。

以下是此前版本的真机记录，保留用于追踪，不作为 API9 新功能验收：

- **API2，Android 16**：实际 DEX/JNI/ONNX、系统 TTS 84,096 字节 PCM 与 AudioTrack 播放、启停、导入、完整/分段识别及取消后再识别通过；1 项综合仪器测试 16.646 秒。
- **API3，Android 16**：SpeechServiceDeviceTest 20.535 秒，服务内置系统 TTS 93,312 字节、经 broker 的 Piper 55,296 字节 PCM，均实际播放；取消后复用 56,320 字节，停用提供者后保底 87,552 字节。测试旧工程英文开发模型，仅验证功能，不代表中文/自然度验收。
- **API3 识别升级**：SpeechUpgradeDeviceTest 6.689 秒，复用既有 SenseVoice 模型。完整录音“开放时间早上9点至下午5点。”，分段“开饭时间早上9点至下午5点。”仍有同一错字。该阶段旧独立系统包已更新并停用；它属于历史分发方式。
- **2026-10-12，API8**：Release 与当时六个 0.7.x 包已安装，保留发布者、启停、模型；可选快捷工具示例未额外安装。Piper/系统保底复用和空闲实测详见 [历史性能记录](https://github.com/LHT02/KIGTTSNEXT/blob/36e4d8fbb1a199009c01c95ae65f4e97b1759e81/docs/tts-lifecycle.md)。

API2/3 SenseVoice 使用官方固定模型提交 `2365baeacb507f821a0c8120fcee3d484dba7a07`，model SHA-256 `c71f0ce00bec95b07744e116345e33d8cbbe08cef896382cf907bf4b51a2cd51` 与 zh.wav：[模型来源](https://huggingface.co/csukuangfj/sherpa-onnx-sense-voice-zh-en-ja-ko-yue-2024-07-17)。当时完整识别含初次加载约 1823 ms；模型/tokens/LICENSE 在插件 models，不在主 APK 或 Git。现场麦克风、嘈杂环境、精度与长期会话需独立验证。

历史检查修复 ART 自动生成 code/oat 的加载校验以及管道 AIDL 复制误报；当前仍拒绝额外 DEX，使用显式 END_INPUT，缺失结束帧视为输入中断。PluginRuntimeDeviceTest、SpeechServiceDeviceTest 与 StreamingRecognitionDeviceTest 在 androidTest 中，测试资源应放对应插件 Appdata，模型存在且实际执行后才可报告成功。测试资产仅进入 AndroidTest APK。安装/签名/数据保留与真实播放的最新结果集中记录，避免将旧 smoke 结果代替新功能验收。

## 安装器图标

在 `plugin-spec.json` 注册 `"icon": {"symbol":"lightbulb"}`，使用离线 Material Symbols Rounded；或注册 `"icon": {"path":"assets/icon.svg"}`，使用包内 SVG、PNG、JPEG、WebP 图片。两者二选一，图片上限 1 MiB，必须位于 assets 且纳入 files 哈希清单与包签名。SVG 仅使用本地矢量路径/渐变，不允许脚本、外部图片/字体或外部引用；位图解码限制像素数。安装器读取图标无需加载插件代码或申请网络权限。旧包未声明 icon 时继续使用 plugin-info.json 的 symbol 或类别图标。