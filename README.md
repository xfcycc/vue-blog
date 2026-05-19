# 菜鸟的小站

菜鸟的小站是一个星际主题的个人博客系统，包含文章展示、归档、分类、标签、友链、关于、留言和后台管理等功能。

项目在常规博客能力的基础上，对前台页面做了主题化设计：导航入口使用星港、星历、星图、星标、星链、星星、星声等命名方式，不同页面根据内容类型设计了独立的视觉风格和交互效果。

## 演示地址

[https://www.caiguoyu.cn](https://www.caiguoyu.cn)

> 支持桌面端和移动端访问。

## 特色功能

- 星港：展示博客封面、站点信息、文章入口和主要导航。
- 星历：以命令行输入动画作为开场，输入完成后展示文章时间线。
- 星图：以 Bento 卡片网格展示文章分类，包含分类编号、图标、文章数量和悬停动效。
- 星标：以漂浮词云展示文章标签，点击标签后进入以标签为中心的文章星座视图。
- 星链：展示友链站点和外部链接组成的太阳系俯视图。
- 星星：展示作者介绍和个人信息。
- 星声：弹幕风格的留言板。
- 后台管理：支持文章、分类、标签、评论、页面配置等内容管理。

## 效果展示



![image-20260519171941939](https://pic.caiguoyu.cn/typora/202605191719252.png)

![image-20260519172013675](https://pic.caiguoyu.cn/typora/202605191720721.png)

![image-20260519172029052](https://pic.caiguoyu.cn/typora/202605191720101.png)

<img src="https://pic.caiguoyu.cn/typora/202605191721529.png" alt="image-20260519172101493" style="zoom:50%;" />

<img src="https://pic.caiguoyu.cn/typora/202605191721907.png" alt="image-20260519172133860" style="zoom:50%;" />

![image-20260519172217702](https://pic.caiguoyu.cn/typora/202605191722744.png)

![image-20260519172237676](https://pic.caiguoyu.cn/typora/202605191722726.png)



![image-20260519172315570](https://pic.caiguoyu.cn/typora/202605191723608.png)



![image-20260519172343101](https://pic.caiguoyu.cn/typora/202605191723155.png)

![image-20260519173548887](https://pic.caiguoyu.cn/typora/202605191735060.png)

## 技术栈

- **后端**: Java 8, springboot 2
- **数据库**: MySQL, Redis
- **前端**: Vue 2, Vite 

## 启动方法

### 前台

```bash
cd blog-vue/blog
nvm use 24
pnpm install
pnpm run serve
```

### 后台

```bash
cd blog-vue/admin
nvm use 22
npm install
npm run serve
```
