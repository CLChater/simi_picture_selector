# simi_picture_selector

react native 原生图片选择器

### API

1. **openSelector** : 打开图片选择器 可选参数 {isSingle: false, maxImageNum: 6, maxVideoNum: 1, selectMimeType: 0} // selectMimeType：0-all 或 1-image 或 2-video 或 3-audio


```
import SimiPictureSelector from 'simi_picture_selector';

SimiPictureSelector.openSelector({isSingle: false, maxImageNum: 6, maxVideoNum: 1, selectMimeType: 0});
```




