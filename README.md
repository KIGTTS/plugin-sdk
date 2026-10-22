# KIGTTS Plugin SDK

KIGTTS插件开发接口，采用MIT协议。API16兼容范围由接口及宿主共同校验，管理桥独立版本化。

```powershell
git clone https://github.com/KIGTTS/plugin-sdk.git
./gradlew.bat :plugin-api:test :plugin-android-api:assembleDebug
```

需要JDK17与Android SDK36。Android与平台中立接口分别维护在plugin-android-api和plugin-api。
插件以固定SDK子模块开发，可独立构建，不依赖主软件仓库。正式插件必须保留自己的许可证和第三方NOTICE；SDK的MIT许可不会替换插件许可。签名私钥属于发布者，不进入源码或发行包。

[接口](docs/plugin-api.md) · [插件包](docs/plugin-packages.md) · [权限](docs/plugin-permissions.md) · [网页UI](docs/web-ui.md)
