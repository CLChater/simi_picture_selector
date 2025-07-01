// src/index.ts
import {NativeModules} from 'react-native';

const {SimiSelector} = NativeModules;

class SimiPictureSelector {

    /**
     * 打开simi照片选择器
     * 自定义选择单个模式、图片张数、视频个数
     *
     * @param option 可选参数 {isSingle: false, maxImageNum: 6, maxVideoNum: 1}
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
