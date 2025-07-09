// src/index.ts
import {NativeModules} from 'react-native';

const {SimiSelector} = NativeModules;

class SimiPictureSelector {

    /**
     * 打开simi照片选择器
     * 自定义选择单个模式、图片张数、视频个数、语言国际化等
     *
     * @param option 可选参数 {isSingle: false, maxImageNum: 6, maxVideoNum: 1, selectMimeType:0, selectLanguage:0, isCrop:false}
     *
     * isSingle：是否为单选模式
     * maxImageNum：最大选择图片张数
     * maxVideoNum：最大选择视频个数
     * selectMimeType：0-all 或 1-image 或 2-video 或 3-audio
     * selectLanguage：zh-简体中文 或 en-英文
     * isCrop：是否剪裁
     * isMixSelect：是否混选视频、图片
     * imageSizeLimit：图片大小限制
     * videoSizeLimit：视频大小限制
     */
    static async openSelector(option: any): Promise<any> {
        if (SimiSelector) {
            try {
                return await SimiSelector.openSelector(option);
            } catch (error) {
                console.error('Failed to init from SimiSelector:', error);
                throw error;
            }
        } else {
            console.warn('SimiSelector module is not available.');
            return Promise.reject('SimiSelector module is not available.');
        }
    }
}

export default SimiPictureSelector;
