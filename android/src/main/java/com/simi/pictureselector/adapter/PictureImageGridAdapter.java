package com.simi.pictureselector.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.simi.pictureselector.R;
import com.simi.pictureselector.adapter.holder.BaseRecyclerMediaHolder;
import com.simi.pictureselector.config.InjectResourceSource;
import com.simi.pictureselector.config.PictureMimeType;
import com.simi.pictureselector.config.SelectorConfig;
import com.simi.pictureselector.entity.LocalMedia;
import com.simi.pictureselector.permissions.PermissionChecker;

import java.util.ArrayList;


/**
 * @author：
 * @date：2016-12-30 12:02
 * @describe：PictureImageGridAdapter
 */
public class PictureImageGridAdapter extends RecyclerView.Adapter<BaseRecyclerMediaHolder> {
    /**
     * 拍照
     */
    public final static int ADAPTER_TYPE_CAMERA = 1;
    /**
     * 图片
     */
    public final static int ADAPTER_TYPE_IMAGE = 2;
    /**
     * 视频
     */
    public final static int ADAPTER_TYPE_VIDEO = 3;
    /**
     * 音频
     */
    public final static int ADAPTER_TYPE_AUDIO = 4;

    /**
     * 添加部分权限选择媒体
     */
    public final static int ADAPTER_TYPE_ADD_MEDIA = 5;

    private boolean isDisplayCamera;

    private boolean isDisplayAddSelect;

    private ArrayList<LocalMedia> mData = new ArrayList<>();

    private final SelectorConfig mConfig;

    private final Context mContext;


    public void notifyItemPositionChanged(int position) {
        this.notifyItemChanged(position);
    }

    public PictureImageGridAdapter(Context context, SelectorConfig mConfig) {
        this.mConfig = mConfig;
        this.mContext = context;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setDataAndDataSetChanged(ArrayList<LocalMedia> result) {
        if (result != null) {
            this.mData = result;
            notifyDataSetChanged();
        }
    }

    public boolean isDisplayCamera() {
        return isDisplayCamera;
    }

    public void setDisplayCamera(boolean displayCamera) {
        isDisplayCamera = displayCamera;
    }

    public boolean isDisplayAddSelect() {
        return isDisplayAddSelect;
    }

    public void setDisplayAddSelect(boolean displayAddSelect) {
        if (PermissionChecker.hasReadMediaVisualUserSelected(mContext)) {
            isDisplayAddSelect = displayAddSelect;
        } else {
            isDisplayAddSelect = false;
        }
        if (displayAddSelect) {
            if (PermissionChecker.hasNoReadMediaPermission(mContext)) {
                isDisplayAddSelect = true;
            }
        }
    }

    public ArrayList<LocalMedia> getData() {
        return mData;
    }

    public boolean isDataEmpty() {
        return mData.size() == 0;
    }

    @Override
    public int getItemViewType(int position) {
        if (isDisplayCamera && position == 0) {
            return ADAPTER_TYPE_CAMERA;
        } else if (isDisplayAddSelect && position == getItemCount() - 1) {
            return ADAPTER_TYPE_ADD_MEDIA;
        } else {
            int adapterPosition = isDisplayCamera ? position - 1 : position;
            // 这里不再需要处理 adapterPosition 越界，前面判断已剔除添加按钮位置
            String mimeType = mData.get(adapterPosition).getMimeType();
            if (PictureMimeType.isHasVideo(mimeType)) {
                return ADAPTER_TYPE_VIDEO;
            } else if (PictureMimeType.isHasAudio(mimeType)) {
                return ADAPTER_TYPE_AUDIO;
            }
            return ADAPTER_TYPE_IMAGE;
        }
    }


    @NonNull
    @Override
    public BaseRecyclerMediaHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return BaseRecyclerMediaHolder.generate(parent, viewType, getItemResourceId(viewType), mConfig);
    }

    /**
     * getItemResourceId
     *
     * @param viewType
     * @return
     */
    private int getItemResourceId(int viewType) {
        int layoutResourceId;
        switch (viewType) {
            case ADAPTER_TYPE_CAMERA:
                return R.layout.ps_item_grid_camera;
            case ADAPTER_TYPE_ADD_MEDIA:
                return R.layout.ps_item_grid_add_select; // 你需要新增这个布局资源
            case ADAPTER_TYPE_VIDEO:
                layoutResourceId = InjectResourceSource.getLayoutResource(mContext, InjectResourceSource.MAIN_ITEM_VIDEO_LAYOUT_RESOURCE, mConfig);
                return layoutResourceId != InjectResourceSource.DEFAULT_LAYOUT_RESOURCE ? layoutResourceId : R.layout.ps_item_grid_video;
            case ADAPTER_TYPE_AUDIO:
                layoutResourceId = InjectResourceSource.getLayoutResource(mContext, InjectResourceSource.MAIN_ITEM_AUDIO_LAYOUT_RESOURCE, mConfig);
                return layoutResourceId != InjectResourceSource.DEFAULT_LAYOUT_RESOURCE ? layoutResourceId : R.layout.ps_item_grid_audio;
            default:
                layoutResourceId = InjectResourceSource.getLayoutResource(mContext, InjectResourceSource.MAIN_ITEM_IMAGE_LAYOUT_RESOURCE, mConfig);
                return layoutResourceId != InjectResourceSource.DEFAULT_LAYOUT_RESOURCE ? layoutResourceId : R.layout.ps_item_grid_image;
        }
    }


    @Override
    public void onBindViewHolder(final BaseRecyclerMediaHolder holder, final int position) {
        int viewType = getItemViewType(position);
        if (viewType == ADAPTER_TYPE_CAMERA) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        listener.openCameraClick();
                    }
                }
            });
        } else if (viewType == ADAPTER_TYPE_ADD_MEDIA) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.openAddSelectClick();
                    }
                }
            });
        } else {
            int adapterPosition = isDisplayCamera ? position - 1 : position;
            if (isDisplayAddSelect) {
                adapterPosition = adapterPosition >= mData.size() ? mData.size() - 1 : adapterPosition;
            }
            LocalMedia media = mData.get(adapterPosition);
            holder.bindData(media, adapterPosition);
            holder.setOnItemClickListener(listener);
        }
    }


    @Override
    public int getItemCount() {
        int count = mData.size();
        if (isDisplayCamera) {
            count++;
        }
        if (isDisplayAddSelect) {
            count++;
        }
        return count;
    }


    private OnItemClickListener listener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {

        /**
         * 拍照
         */
        void openCameraClick();

        /**
         * 打开Android13以上权限图片选择
         */
        void openAddSelectClick();

        /**
         * 列表item点击事件
         *
         * @param selectedView 所产生点击事件的View
         * @param position     当前下标
         * @param media        当前LocalMedia对象
         */
        void onItemClick(View selectedView, int position, LocalMedia media);

        /**
         * 列表item长按事件
         *
         * @param itemView
         * @param position
         */
        void onItemLongClick(View itemView, int position);

        /**
         * 列表勾选点击事件
         *
         * @param selectedView 所产生点击事件的View
         * @param position     当前下标
         * @param media        当前LocalMedia对象
         */
        int onSelected(View selectedView, int position, LocalMedia media);
    }
}
