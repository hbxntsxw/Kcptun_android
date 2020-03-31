Fork自Kcptun_android，通过修改适配了goflyway，支持CDN，kcp和https。

[预编译安装包latest 下载](https://github.com/koolwiki/goflyway_android/releases/latest)

软件设置如下图，有的手机杀后台比较严重，可以把goflyway加入白名单或者设置成不进行省电优化，这样可以避免在后台中被杀掉。或者在termux里面运行goflyway（推荐采用这种方法）。

代理设置有3种方法，3种方法都可以（推荐第3种）：

1、直接在wifi里面设置代理：直接在连接的wifi里面设置代理为“手动”，主机名称设为“127.0.0.1”，端口号为“8100”。设置好后手机就可以自由上网了。

2、利用Postern设置代理：这种模式需要[Postern](https://play.google.com/store/apps/details?id=com.tunnelworkshop.postern&hl=zh)这个软件（可以去[这里下载](https://github.com/koolwiki/goflyway_android/raw/master/Postern-3.1.2.apk)）配合一起使用。Postern代理服务器栏填127.0.0.1，端口栏填goflyway上面填写的本地端口号（纯端口号，如下图的8100）。

3、利用BifrostV设置代理：这种模式需要[BifrostV](https://play.google.com/store/apps/details?id=com.github.dawndiy.bifrostv&hl=zh)这个软件配合一起使用。BifrostV里面添加代理，选择手动设置，将VMess修改成Socks，然后将主机名称设为“127.0.0.1”，端口号为“8100”，并设置分应用代理，保存。使用是点击左下角的红色开关图标即可。

![UI](https://github.com/koolwiki/goflyway_android/raw/master/UI.png "UI")

Postern设置如下（配置代理规则项，目标地址那里自行编辑需要走代理的域名）：

![配置代理](https://github.com/koolwiki/goflyway_android/raw/master/servers.jpg "配置代理")

![配置代理规则](https://github.com/koolwiki/goflyway_android/raw/master/rules.jpg "配置代理规则")

#感谢 [goflyway项目](https://github.com/coyove/goflyway)

#感谢 [Kcptun_android项目](https://github.com/shutup/Kcptun_android)

#感谢 [KcpTun项目](https://github.com/xtaci/kcptun)
