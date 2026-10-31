# KPM 资源包（API29–34）

`.kigresource` 是签名 ZIP 数据包，与 `.kigplugin` / `.kiglua` 的安装生命周期独立。
支持 QUICK_TEXT、PHOTOS、FILTER、VOICE、FRAME、STICKERS、CARD_FRAME、FONT。导入后的内容由对应功能管理，不注册插件，不创建卸载条目。

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
- FONT（API32）：payload 指向单个 TTF/OTF（最大40 MB），随包携带字体许可。添加后进入“外观 → 字体”的“我的字体”，不自动切换当前字体。重装相同字体按内容去重，不进入插件管理器。商店预览使用源中的 SVG 字形图片。

图片资源先支持位图；imageFiles 与相框结构保留扩展能力，后续可加入 SVG/图片组合样式而不改变资源包的插件隔离原则。

调用 `AppCommand.ResourcePackage("install", source, expectedPublisher)`，宿主核验并弹出确认，返回调用者专属 token。
使用 `AppCommand.ResourcePackage("status", token = token)` 读取 PENDING / COMPLETED / CANCELLED / DEFERRED / FAILED。
accepted/pending 不是完成。DEFERRED 表示先添加前置服务，完成后重新提交缓存包；导入结果不得依据插件安装列表推断。

商店源使用 schemaVersion=2，资源条目填写 resourceType，发行格式 RESOURCE，保留下载镜像、校验值、签名发布者和依赖版本范围。

## API30：名片边框

`CARD_FRAME` 与拍立得 `FRAME` 分属不同资源库。最低 apiVersion=30。
`content.json` 保留空对象，根目录另放 `card-frames.json`：

```json
{"schemaVersion":1,"items":[{"id":"fine","name":"细线","artwork":"art/fine.svg"}]}
```

artwork 支持包内 SVG 或位图，透明区域保留背景；SVG 建议 viewBox 为 0 0 1024 576。
所有引用文件必须列入 resource.json.files。安装后出现在个人信息 → 名片背景 → 名片边框。

## API30：参数贴纸

STICKERS 可以附带 `generators.json`（允许只含生成器，不含静态条目）：

```json
{"schemaVersion":1,"items":[{"id":"word","name":"艺术字","template":"art/word.svg","width":1024,"height":512,"parameters":[{"id":"text","label":"文字","kind":"TEXT","default":"你好","maxLength":80},{"id":"ink","label":"颜色","kind":"COLOR","default":"#00696E"}],"fonts":{"ArtFont":"fonts/art.ttf"}}]}
```

SVG 用 `{{text}}`、`{{ink}}` 占位；文本会进行 XML 转义。字体可嵌入 TTF/OTF，随包附带许可；`<text font-family="ArtFont">` 会转换为路径，修改某张贴纸不会改动模板或其它贴纸。
参数支持 TEXT / COLOR / NUMBER / CHOICE。NUMBER 必须提供有限的 minimum < maximum；CHOICE 提供 choices。禁止外链、脚本、事件、DOCTYPE 和外部字体。
选中参数贴纸可点击齿轮或再次点击贴纸调整。

## API34：相片信息默认值

STICKERS 参数贴纸可以再附带 `photo-defaults.json`，并将该文件计入签名清单。最低 apiVersion=34：

```json
{"schemaVersion":1,"generators":{"clock":{"time":"TIME","date":"DATE","place":"LOCATION"}}}
```

`generators` 的键对应生成器 id，内层的键对应已声明的 TEXT 参数。支持 DATE、TIME、DATE_TIME、YEAR、MONTH、DAY、MONTH_EN、WEEKDAY、WEEKDAY_EN、LOCATION、WEATHER。

宿主在用户将模板放入相片时读取该相片的拍摄资料，并创建独立实例；不在安装时读取相册，也不使用此刻的时间、位置冒充旧照片。没有信息时显示“未知”（英文日期字段为 Unknown），地点及天气查询复用现有用途说明、权限与超时控制。放入之后的手动修改保留，不会影响模板和其它相片。

SVG 的 `<text>` 可设置 `data-fit-width="800"`，当文字过长时等比缩小到指定宽度；短文字不放大。该值必须是 1–8192 的有限数值，转换为路径后移除。

## API30：通用作品选择器

返回 `PluginOutcome.SelectArtwork(ArtworkSelectionRequest(ArtworkLibrary.POLAROIDS, multiple = true, actionId = "chosen"))`。
另可选 PERSONAL_CARDS、SKETCHES。宿主只展示只读选择器，未确认或取消不会导出。
确认后回调 actionId，inputs["artworks"] 为 JSON 数组，每项含 id、library 和调用者私有 resourcePath；目录可由 request.directory 指定。
取消时调用 cancelActionId（若提供），同样返回空数组。支持单选、多选、预览与按住滑选。
