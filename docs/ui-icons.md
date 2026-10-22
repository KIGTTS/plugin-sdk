# UI 图标与字体来源

主程序离线内置 Google Material Symbols Rounded，通过名称查找字形并转换为原生可着色矢量，保持主题色；未知名称按已有图标回退。SDK `UiAction`、`SettingsEntry`、`PageAction` 和支持的页面组件可提供 `symbol`；`PluginIcon.NONE` 允许不占图标位置。Agent优先使用 `icon_catalog` 取得真实名称，不猜测不存在的符号。

Robot2使用Google的 `robot_2` 路径，Apache-2.0；[原始矢量](https://github.com/google/material-design-icons/blob/master/symbols/android/robot_2/materialsymbolsrounded/robot_2_24px.xml)。字体与代码点表同源于 [Google material-design-icons](https://github.com/google/material-design-icons/tree/master/variablefont)，完整许可在 `third-party/material-symbols/LICENSE.txt` 和APK的Apache-2.0许可证中。

原始变量字体SHA-256：`32e4011709e055596eb8d2dd0f7cb547b47c1d40ac9ecb8fad54f7f7eac202e7`。使用fonttools==4.56.0运行 `python tools/Build-MaterialSymbols.py <下载的原始ttf>` 可复建：FILL=0、GRAD=0、opsz=24、wght=400，保留Unicode字形，去掉应用不使用的GSUB/GPOS/GDEF布局表。下载地址与摘要固定在脚本；上游改变时先审核，不静默接纳新字节。代码点表为原始名称→Unicode映射，应用离线查找，不从远程加载字体。

网页快捷文本默认显示网页图标，不自动截图。`metadata["icon.symbol"]` 指定离线图标；`icon.asset` 引用宿主保存的独立图片/SVG副本；用户要求背景图时设置 `icon.mode=thumbnail`，缺省为icon。编辑页可搜索、导入及恢复默认，变化有淡入过渡。

SVG使用AndroidSVG 1.4（Apache-2.0，Paul LeBeau）实际渲染；限制512KiB、2000节点、32层，禁脚本、实体、外链、style和use/href继承。导入位图最多8MiB并缩采样。Agent的 `render_svg` 写自己的工作目录PNG，`import_icon` 产生宿主持久副本ID；不能用任意路径替代资源所有者。图标导入不授予网页联网权限。

Markdown使用commonmark-java 0.24.0及表格、删除线、任务列表、自动链接扩展（BSD-2-Clause），其autolink依赖为MIT。完整许可证随APK的assets/licenses分发，来源与派生说明见NOTICE-ui.txt；不把第三方许可改标为本项目MIT。
