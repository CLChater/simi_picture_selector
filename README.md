# simi_picture_selector

react native 原生图片选择器

### API

1. **openSelector** : 打开图片选择器

```
可选参数 {isSingle: false, maxImageNum: 6, maxVideoNum: 1, selectMimeType: 0,selectLanguage: 'zh', isCrop:false, isMixSelect:false}

isSingle：是否为单选模式
maxImageNum：最大选择图片张数
maxVideoNum：最大选择视频个数
selectMimeType：0-all 或 1-image 或 2-video 或 3-audio
selectLanguage：zh-简体中文 或 en-英文
isCrop：是否剪裁
isMixSelect：是否混选视频、图片
imageSizeLimit：图片大小限制
videoSizeLimit：视频大小限制

```

```
import SimiPictureSelector from 'simi_picture_selector';

SimiPictureSelector.openSelector({isSingle: false, maxImageNum: 6, maxVideoNum: 1, selectMimeType: 0,selectLanguage: 'zh', isCrop:false, isMixSelect:false});
```




