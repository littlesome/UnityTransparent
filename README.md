# UnityTransparent

![效果图](https://raw.githubusercontent.com/littlesome/UnityTransparent/master/Screenshot.jpg)

### 实现了Unity与安卓原生界面的融合，基本思路：

1. 使用TextureView代替SurfaceView。关于二者的区别网上比较多，不在这里说明了。这里为了不修改Unity源码，用了一种比较hacky的方式：

* 查找到SurfaceView，宽高设置为0
* 通过反射拿到mCallbacks，保存Unity设置的callback，我们自己调用，清空原数组
* 创建一个TextureView，setOpaque(false)设置为透明，创建TextureView.SurfaceTextureListener，在回调内处理原Unity设置的回调
* 将TextureView添加为UnityPlayer的子View

2. 到这里理应可以正常显示了，然而测试发现仍然不透明...

用调试工具抓了一下渲染过程，发现Unity在安卓上额外做了一步操作：把alpha强制改成1后输出... 

后面Unity2017.2可选了，详见 https://forum.unity3d.com/threads/big-performance-issue-with-unity5-on-android.338847/

这里需要改libunity.so的二进制文件来临时绕过
```
precision mediump float;
VARYING_IN vec2 texCoord;
#ifdef DECLARE_FRAG_COLOR
    DECLARE_FRAG_COLOR;
#endif
uniform sampler2D tex;
%svoid main()
{
    vec4 c = SAMPLE_TEXTURE_2D(tex, texCoord);
    FRAG_COLOR = %s; 
}
```

改为

```
precision mediump float;
VARYING_IN vec2 texCoord;
#ifdef DECLARE_FRAG_COLOR
    DECLARE_FRAG_COLOR;
#endif
uniform sampler2D tex;
%svoid main()
{
    vec4 c = SAMPLE_TEXTURE_2D(tex, texCoord);
    FRAG_COLOR = c; 
}

```

3. 改完后终于可以透明了，然而发现颜色有点怪怪的，感觉颜色叠加了。没仔细研究安卓的混合模式，最简单的修改方法就是Camera的Background设置为#00000000
