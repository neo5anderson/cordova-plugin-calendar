# cordova-plugin-calendar

这个插件定义了一个全局的 `Calendar` 对象，用来添加本地应用的日程。虽然它是一个全局的变量，但是也要在 `deviceready` 事件之后才可用。

```js
document.addEventListener("deviceready", onDeviceReady, false);
function onDeviceReady() {
    console.log(Calendar.version);
}
```

## 安装

```sh
cordova plugin add https://github.com/neo5anderson/cordova-plugin-calendar.git
```

## Properties

- Calendar.version

### Supported Platforms

- [x] Android
- [ ] iOS

## Methods

- Calendar.add

### Supported Platforms

- [x] Android
- [ ] iOS

### Quick Example

```js
// 日程标题
var title = "公告：下午开会";
// 日程备注
var desc = "项目进度汇报：\n-xx\n-oo";
// 日程开始时间
var date = new Date().getTime() + 1000 * 60;
// 提前几分钟提醒
var prior = 5;

Calendar.add(title, desc, date, prior, function() {
    // 添加成功的回调
    console.log('calendar add success');
}, function(err) {
    // 添加失败的回调，通过 `err` 数值确定错误原因
    console.log('calendar add failed: ' + err);
} );
```

