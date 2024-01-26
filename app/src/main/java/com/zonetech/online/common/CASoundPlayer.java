package com.zonetech.online.common;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;

import com.zonetech.online.utils.Utils;

import java.util.Iterator;

public class CASoundPlayer implements SoundPool.OnLoadCompleteListener {
	
	public interface OnLoadCompleteListener {
		void onLoadComplete(CASoundPlayer soundPlayer, int sampleId, int status);
	}
	
	public SoundPool mSoundPool;
	private Activity mContext;
	private Bundle mSoundIds;
	
	private OnLoadCompleteListener mOnLoadCompleteListener;
	
	@SuppressLint("InlinedApi")
	@SuppressWarnings("deprecation")
	public CASoundPlayer(Activity context, int maxStreams) {
		mContext = context;
		
		mSoundIds = new Bundle();
		mContext.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			SoundPool.Builder soundBuilder = new SoundPool.Builder();
			
			soundBuilder.setMaxStreams(maxStreams);
			
			AudioAttributes.Builder attrbsBuilder = new AudioAttributes.Builder();
			attrbsBuilder.setUsage(AudioAttributes.USAGE_MEDIA);
			soundBuilder.setAudioAttributes(attrbsBuilder.build());
			
			mSoundPool = soundBuilder.build();
		} else {
			mSoundPool = new SoundPool(maxStreams, AudioManager.STREAM_MUSIC, 0);
		}
		mSoundPool.setOnLoadCompleteListener(this);
	}
	
	@Override
	public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
		mSoundIds.putBoolean(String.valueOf(sampleId), true);
		
		if(mOnLoadCompleteListener != null) {
			mOnLoadCompleteListener.onLoadComplete(this, sampleId, status);
		}
	}
	
	public int load(int rawStream, int priority) {
		int id = mSoundPool.load(mContext, rawStream, priority);
		mSoundIds.putBoolean(String.valueOf(id), false);
		return id;
	}
	
	public void play(final int soundId) {
        try {
            AudioManager audioManager;
            audioManager = (AudioManager) mContext.getSystemService(Activity.AUDIO_SERVICE);
            float actualVolume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            float maxVolume = (float) audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            float volume = actualVolume / maxVolume;

            if (mSoundIds.containsKey(String.valueOf(soundId)) &&
                    mSoundIds.getBoolean(String.valueOf(soundId))) {
                mSoundPool.play(soundId, volume, volume, 1, 0, 1f);
            }
        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
	}

	public void setOnLoadCompleteListener(OnLoadCompleteListener listener) {
		mOnLoadCompleteListener = listener;
	}
	
	public OnLoadCompleteListener getOnLoadCompleteListener() {
		return mOnLoadCompleteListener;
	}

	public void stop(int id){
		try {
			mSoundPool.stop(Integer.valueOf(id));
		} catch(Throwable e) {}
	}
	public void release() {
		try {
			Iterator<String> ids = mSoundIds.keySet().iterator();
			while(ids.hasNext()) {
				String id = ids.next();
				if(id != null) {
					try {
						mSoundPool.stop(Integer.valueOf(id));
					} catch(Throwable e) {}
				}
			}
			try {
				mSoundPool.release();
			} catch(Throwable e) {}
		} catch(Throwable e) {}
	}
	
	public void setLoop(int streamId, int loop) {
		mSoundPool.setLoop(streamId, loop);
	}
}
