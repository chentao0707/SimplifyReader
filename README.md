###简阅


一款基于Google Material Design设计开发的Android客户端，包括新闻简读，图片浏览，视频爽看 ，音乐轻听以及二维码扫描五个子模块。项目采取的是MVP架构开发，由于还是摸索阶段，可能不是很规范。但基本上应该是这么个套路，至少我个人认为是这样的~恩，就是这样的！

###效果图

![image](https://raw.githubusercontent.com/chentao0707/server/master/SimplifyReader/images/all_in_one.jpg)


![image](https://raw.githubusercontent.com/chentao0707/server/master/SimplifyReader/images/project.gif)

![image](https://raw.githubusercontent.com/chentao0707/server/master/SimplifyReader/images/qrcode.gif)

![image](https://raw.githubusercontent.com/chentao0707/server/master/SimplifyReader/images/project_struct.png)

###Demo下载
[火速跳转](https://fir.im/simplifyreader) | [Download-APK](https://raw.githubusercontent.com/chentao0707/server/master/SimplifyReader/apks/app-debug.apk)

![image](https://raw.githubusercontent.com/chentao0707/server/master/SimplifyReader/images/download.png)

###模块分析

####新闻简读

* 介绍：API使用的是凤凰新闻客户端的接口，我只是简单的获取了新闻的列表和详情数据，由于api和凤凰新闻客户端完全一致，鉴于侵权问题我就不开源出来了。至于接口是如何获取的？Google，百度，调试获取日志，我能说的只有这么多。

* 功能：列表页使用自定义的ListView(自动加载更多）显示新闻列表，详情使用的是WebView加载，支持滑动返回。对于多图
新闻的处理，使用的和主流新闻客户端类似，滑动切换多张图片，可双指缩放图片大小！

####图片浏览

* 介绍：API使用的是百度图片的搜索接口，由于网上有很多的开发者开源了这个接口，所以我也就放出来，如有侵权请及时告知。

* 功能：列表页使用的瀑布流效果（增加了下拉刷新和上拉加载更多）详情页和列表页的切换增加了一个图片放大或缩小到指定位置的效果，图片也可以双指缩放！

####视频爽看

* 介绍：API使用的是优酷开放平台的SDK，不过要吐槽的一点是，优酷的SDK真心不好用，还是Eclipse版本的，我是一点点移植到Android Studio平台的，但是它的接口还是很丰富的，好好的利用一下，还是能够做出一个优秀的APP的。

* 功能：列表页使用了Google的CardView，简单的获取了一些视频的基本数据。播放页使用了优酷提供的视频播放器组件，传入视频ID就可以播放视频了，只要调通了SDK，其他的都是一些简单的数据获取！

####音乐轻听

* 介绍：API获取的是豆瓣音乐的数据，由于也不是开放API，如有侵权请及时告知。

* 功能：播放音乐的界面是我自定义的一个唱机的View，很多思路都是从网上学习借鉴过来的，自己重新造了个轮子。UI和网易云音乐对比的话，只能说是形似神不似了，没有人家做的细致！

####二维码扫描

* 介绍：这个完全是我自己单独开发的类库，之前也有开源出来，这次又再一次重构优化，后期会单独剥离二维码扫描模块，做成类库和Demo的形式，提供Android Studio版本。

* 功能：扫描界面使用xml进行布局，然后加入属性动画。这样布局更具有优势，更利于多屏幕适配。解码模块使用的是两个主流的开源库Zbar和ZXing，进过多次测试发现，ZBar虽然扫描效率和速度高于ZXing，但是经常扫描出错误的信息，可能由于太灵敏的缘故把，综合二者的优缺点还是建议使用ZXing来解码，并且这个项目还在长期维护更新！

###致谢

* 苦于没有后台支持，找到这些支持JSON数据格式的开放接口可谓是煞费苦心，前前后后尝试了很多次才找到，也感谢网友们提供的接口！

* 界面的原型都是我自己构思的，后期的切图美化主要是Chris帮忙完成的，很感谢他业余时间和我一起完成这样一个APP！

* 项目中大量使用了Github上面优秀的开源项目，我会列举出来！其他一些代码片段就不一一致谢了，很感谢这些开放源码的技术大牛们，让我学到了很多！

* 最后如果觉得我的项目对你有所帮助，请点击我的支付宝付款码请我喝杯咖啡把~当然我也希望你们能够多多**fork**，多多**star**，多多**follow**，这将给我更多的动力开源更多的项目！

###开源项目说明

> **ButterKnife**

* **Link:** [https://github.com/JakeWharton/butterknife](https://github.com/JakeWharton/butterknife)

* **License:** `Licensed under the Apache License, Version 2.0 (the "License");`

> **AndroidTagGroup** 

* **Link:** [https://github.com/2dxgujun/AndroidTagGroup](https://github.com/2dxgujun/AndroidTagGroup)

* **License:** `Licensed under the Apache License, Version 2.0 (the "License");`

> **NineOldAndroids** 

* **Link:** [https://github.com/JakeWharton/NineOldAndroids](https://github.com/JakeWharton/NineOldAndroids) 

* **License:** `Licensed under the Apache License, Version 2.0 (the "License");`

> **SystemBarTint**

* **Link:** [https://github.com/jgilfelt/SystemBarTint](https://github.com/jgilfelt/SystemBarTint)

* **License:** `Licensed under the Apache License, Version 2.0 (the "License");`

> **Android-Universal-Image-Loader**

* **Link:** [https://github.com/nostra13/Android-Universal-Image-Loader](https://github.com/nostra13/Android-Universal-Image-Loader)

* **License:** `Licensed under the Apache License, Version 2.0 (the "License");`

> **PhotoView**

* **Link:** [https://github.com/chrisbanes/PhotoView](https://github.com/chrisbanes/PhotoView)

* **License:** `Licensed under the Apache License, Version 2.0 (the "License");`

> **OkHttp** 

* **Link:** [https://github.com/square/okhttp](https://github.com/square/okhttp)

* **License:** `Licensed under the Apache License, Version 2.0 (the "License");`

> **SmartTabLayout**

* **Link:** [https://github.com/ogaclejapan/SmartTabLayout](https://github.com/ogaclejapan/SmartTabLayout)

* **License:** `Licensed under the Apache License, Version 2.0 (the "License");`

> **SwipeBackLayout**

* **Link:** [https://github.com/ikew0ng/SwipeBackLayout](https://github.com/ikew0ng/SwipeBackLayout)

* **License:** `Licensed under the Apache License, Version 2.0 (the "License");`

> **ImageBlurring**

* **Link:** [https://github.com/qiujuer/ImageBlurring](https://github.com/qiujuer/ImageBlurring)

* **License:** `Licensed under the Apache License, Version 2.0 (the "License");`

> **PinterestLikeAdapterView**

* **Link:** [https://github.com/dodola/PinterestLikeAdapterView](https://github.com/dodola/PinterestLikeAdapterView)

* **License:** `Licensed under the Apache License, Version 2.0 (the "License");`

> **Material-Dialogs**

* **Link:** [https://github.com/afollestad/material-dialogs](https://github.com/afollestad/material-dialogs)

> **EventBus**

* **Link:** [https://github.com/greenrobot/EventBus](https://github.com/greenrobot/EventBus)

> **Gson** 

* **Link:** [https://github.com/google/gson](https://github.com/google/gson)

> **Volley** 

* **Link:** [https://android.googlesource.com/platform/frameworks/volley](https://android.googlesource.com/platform/frameworks/volley)

> **Umeng** 

* **Link:** [http://www.umeng.com/](http://www.umeng.com/)

> **Youku**

* **Link:** [http://open.youku.com/](http://open.youku.com/)

###打赏我

![Image](https://raw.githubusercontent.com/SkillCollege/server/master/SimplifyReader/images/pay_qrcode.png)

###关于我

* **QQ:** 1076559197
* **QQ Tribe:** 345470463
* **Weibo:** [http://weibo.com/obsessive1990](http://weibo.com/obsessive1990)
* **Email:** [1076559197@qq.com](mailto:1076559197@qq.com) | [tchen0707@gmail.com](mailto:tchen0707@gmail.com)
* **Github:** [https://github.com/chentao0707](https://github.com/chentao0707)
* **Blog:** [http://www.qingcore.com](http://www.qingcore.com)

###项目主页

[http://www.qingcore.com/SimplifyReader/](http://www.qingcore.com/SimplifyReader/)

###License

```
Copyright (c) 2015 [1076559197@qq.com | tchen0707@gmail.com]

Licensed under the Apache License, Version 2.0 (the "License”);
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
   
   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
