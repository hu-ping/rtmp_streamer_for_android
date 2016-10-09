package com.ksy.recordlib.service.magicfilter;


import com.ksy.recordlib.service.core.SMRecordClientConfig;
import com.ksy.recordlib.service.magicfilter.camera.CameraEngine;
import com.ksy.recordlib.service.magicfilter.filter.helper.MagicFilterType;
import com.ksy.recordlib.service.magicfilter.helper.SavePictureTask;
import com.ksy.recordlib.service.magicfilter.utils.MagicParams;
import com.ksy.recordlib.service.magicfilter.widget.MagicCameraView;
import com.ksy.recordlib.service.magicfilter.widget.base.MagicBaseView;
import com.ksy.recordlib.service.util.OnClientErrorListener;

import java.io.File;

/**
 * Created by why8222 on 2016/2/25.
 */
public class MagicEngine {
    private static MagicEngine magicEngine;

    public static MagicEngine getInstance(){
        if(magicEngine == null)
            throw new NullPointerException("MagicEngine must be built first");
        else
            return magicEngine;
    }

    private MagicEngine(Builder builder){

    }

    public void setConfig(SMRecordClientConfig mConfig) {
        if(MagicParams.magicBaseView instanceof MagicCameraView) {
            ((MagicCameraView) MagicParams.magicBaseView).setConfig(mConfig);
        }
    }

    public void setOnClientErrorListener(OnClientErrorListener onClientErrorListener) {
        if(MagicParams.magicBaseView instanceof MagicCameraView) {
            ((MagicCameraView) MagicParams.magicBaseView).setOnClientErrorListener(onClientErrorListener);
        }
    }

    public void setFilter(MagicFilterType type){
        MagicParams.magicBaseView.setFilter(type);
    }

    public void savePicture(File file, SavePictureTask.OnPictureSaveListener listener){
        SavePictureTask savePictureTask = new SavePictureTask(file, listener);
        MagicParams.magicBaseView.savePicture(savePictureTask);
    }

    public void startRecord(){
        if(MagicParams.magicBaseView instanceof MagicCameraView)
            ((MagicCameraView)MagicParams.magicBaseView).startRecord();
    }

    public void stopRecord(){
        if(MagicParams.magicBaseView instanceof MagicCameraView)
            ((MagicCameraView)MagicParams.magicBaseView).stopRecord();
    }

    public void setBeautyLevel(int level){
        if(MagicParams.magicBaseView instanceof MagicCameraView && MagicParams.beautyLevel != level) {
            MagicParams.beautyLevel = level;
            ((MagicCameraView) MagicParams.magicBaseView).onBeautyLevelChanged();
        }
    }

    public void switchCamera(){
        CameraEngine.switchCamera();
    }

    public static class Builder{

        public MagicEngine build(MagicBaseView magicBaseView) {
            MagicParams.context = magicBaseView.getContext();
            MagicParams.magicBaseView = magicBaseView;
            return new MagicEngine(this);
        }

        public Builder setVideoPath(String path){
            MagicParams.videoPath = path;
            return this;
        }

        public Builder setVideoName(String name){
            MagicParams.videoName = name;
            return this;
        }

    }
}
