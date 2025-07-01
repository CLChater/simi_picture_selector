# react-native-eagleeye

RN版本的统计和反作弊的SDK

### Android初始化

1. react native 引用插件

```
"react-native-eagleeye": "git+https://github.com/CLChater/react-native-eagleeye",
```

2. 运行命令

```
！！！Warning Tip！！！

插件：react-navigation，目前支持5.*.* 以上版本，后续插件版本升级会做相对更新！！！


node node_modules/react-native-eagleeye/HawkeyeDataRNHook.js -run
```

3. 新增maven仓库

```
your-react-native-project/
├── android/
│   ├── app/
│   ├── build.gradle           <<<--------# 新增maven仓库
│   ├── settings.gradle
├── node_modules/
├── package.json

allprojects {
    repositories {
        ......
        maven { url "https://raw.githubusercontent.com/CLChater/sdk_eagleeye/main" } //新增
        //     maven { url "https://gitee.com/simitalk/sdk_eagleeye/raw/main" } //gitee 镜像
    }
}
```

4. MainApplication的onCreate方法中增加EagleEyeDataAPI.init(this);

```
your-react-native-project/
├── android/
│   ├── app/
        └──MainApplication.java         # 你的Application类
│   ├── libs/
│   ├── build.gradle                    
│   ├── settings.gradle
├── node_modules/
├── package.json

import com.coolook.eagleye.EagleEyeDataAPI;

  @Override
  public void onCreate() {
    EagleEyeDataAPI.init(this);
  }
```

### React Native 初始化
初始化获取设备信息
参数 -> yourApplyKey
返回参数 -> { "msg": 返回信息-string, "suc": 是否成功-bool }

```
import EagleEyeAgent from 'react-native-eagleeye';
// rn调用初始化
 EagleEyeAgent.init(Platform.OS === 'ios' ? 'yourApplyKey' : 'yourApplyKey').then(value => {
      console.log('EagleEyeAgent init = ', value);
    });
```

### API

1. **trackEvent** : 自定义行为上报 参数 -> (事件名称-string,事件参数-string)

```
EagleEyeAgent.trackEvent('yourEventName','事件参数（json字符串）');    // 自定义上报
```

2. **userProperty** : 自定义用户属性 参数 -> (属性名-string,属性值-string)

```
EagleEyeAgent.userProperty('key','value');    // 自定义用户属性
```

3. **cleanUserProperty** : 根据key删除某个用户属性 参数 -> (属性名-string)

```
EagleEyeAgent.cleanUserProperty('key');    // 根据key删除某个用户属性
```

4. **cleanAllUserProperties** : 清除所有用户属性

```
EagleEyeAgent.cleanAllUserProperties();    // 清除所有用户属性
```

5. **getId** : 获取设备唯一ID  EagleEyeAgent.init初始化成功后调用 返回参数 -> (设备ID-string)

```
EagleEyeAgent.getId().then( id => {
    // 初始化成功后可获取 返回值-> 获取设备唯一ID
});
```

### iOS
如需要获取IDFA权限，可在同意用户协议后获取权限，并在项目Info.plist中添加IDFA权限描述
示例

```
import { check, request, PERMISSIONS, RESULTS } from 'react-native-permissions';

 initIDFAPermissions = async () => {
    //ios获取IDFA权限
    setTimeout(async () => {
      try {
        if (Platform.OS === 'ios') {
          const permission = PERMISSIONS.IOS.APP_TRACKING_TRANSPARENCY;
          const result = await check(permission);

          if (result === RESULTS.DENIED) {
            const requestResult = await request(permission);
            if (requestResult === RESULTS.GRANTED) {
              console.log('IDFA权限通过');
            } else {
              console.log('IDFA权限被拒绝');
            }
          } else if (result === RESULTS.GRANTED) {
            console.log('IDFA权限通过');
          } else {
            console.log('IDFA权限状态:', result);
          }
        }
      } catch (error) {
        console.error('获取IDFA权限时出错:', error);
      }
    }, 1000);
  };
```


