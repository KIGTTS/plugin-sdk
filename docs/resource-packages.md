# KPM 资源包（API29）

`.kigresource` 是签名 ZIP 数据包，与 `.kigplugin` / `.kiglua` 的安装生命周期独立。
支持 QUICK_TEXT、PHOTOS、FILTER、VOICE、FRAME、STICKERS。导入后的内容由对应功能管理，不注册插件，不创建卸载条目。

根文件 `resource.json` 使用 `ResourcePackageManifest`，包含唯一 id、名称、版本、resourceType 和 `files`（每份载荷的 SHA256）。
`signature/public.der` 是 RSA 公钥；`signature/manifest.sig` 使用 SHA256withRSA 对 resource.json 原始 UTF8 字节签名。
目录发布者指纹是公钥 SHA256，所有载荷都必须列入 files，禁止目录穿越、重复路径及未声明文件。

默认 `content.json` 使用 `ResourcePackageContent`：

- QUICK_TEXT：payload 指向原生 `.kigcards`，重复导入创建新标识与分组。
- VOICE：payload 指向 `.kigvpk`，需要语音合成服务与 Piper，引擎缺失时先引导添加。
- FILTER：filters 指向一个或多个 `.cube` LUT。
- FRAME：frames 包含相纸样式，可通过 imageFiles 引用覆盖图片。
- PHOTOS：snapshot 包含拍立得设计，imageFiles 将设计里的图片 id 映射到载荷文件；可附带 frames。
- STICKERS：snapshot 包含贴纸库与条目，imageFiles 映射图片。

图片资源先支持位图；imageFiles 与相框结构保留扩展能力，后续可加入 SVG/图片组合样式而不改变资源包的插件隔离原则。

调用 `AppCommand.ResourcePackage("install", source, expectedPublisher)`，宿主核验并弹出确认，返回调用者专属 token。
使用 `AppCommand.ResourcePackage("status", token = token)` 读取 PENDING / COMPLETED / CANCELLED / DEFERRED / FAILED。
accepted/pending 不是完成。DEFERRED 表示先添加前置服务，完成后重新提交缓存包；导入结果不得依据插件安装列表推断。

商店源使用 schemaVersion=2，资源条目填写 resourceType，发行格式 RESOURCE，保留下载镜像、校验值、签名发布者和依赖版本范围。
