package com.rokid.speech;

import android.util.Log;
import android.util.SparseArray;

public class Speech extends GenericConfig<SpeechConfig> {
	public Speech(String configFile) {
		_callbacks = new SparseArray<SpeechCallback>();
		_sdk_speech = _sdk_create();
		if (configFile != null)
			config(configFile, SpeechConfig.class);
	}

	public void finalize() {
		_sdk_release(_sdk_speech);
		_sdk_delete(_sdk_speech);
	}

	public void prepare() {
		_sdk_prepare(_sdk_speech);
	}

	public void release() {
		_sdk_release(_sdk_speech);
	}

	public int putText(String content, SpeechCallback cb) {
		int id;
		synchronized (_callbacks) {
			id = _sdk_put_text(_sdk_speech, content);
			Log.d(TAG, "put text " + content + ", id = " + id);
			if (id > 0)
				_callbacks.put(id, cb);
		}
		return id;
	}

	public int startVoice(SpeechCallback cb) {
		return startVoice(cb, null, null);
	}

	public int startVoice(SpeechCallback cb,
			SpeechOptions frameworkOptions,
			SpeechOptions skillOptions) {
		int id;
		synchronized (_callbacks) {
			id = _sdk_start_voice(_sdk_speech, frameworkOptions,
					skillOptions);
			Log.d(TAG, "start voice, id = " + id);
			if (id > 0)
				_callbacks.put(id, cb);
		}
		return id;
	}

	public void putVoice(int id, byte[] data) {
		putVoice(id, data, 0, data.length);
	}

	public void putVoice(int id, byte[] data, int offset, int length) {
		_sdk_put_voice(_sdk_speech, id, data, offset, length);
	}

	public void endVoice(int id) {
		_sdk_end_voice(_sdk_speech, id);
	}

	public void cancel(int id) {
		_sdk_cancel(_sdk_speech, id);
	}

	public void config(String key, String value) {
		_sdk_config(_sdk_speech, key, value);
	}

	// invoke by native poll thread
	private void handle_callback(SpeechResult res) {
		assert(res.id > 0);
		SpeechCallback cb;
		boolean del_cb = false;
		synchronized (_callbacks) {
			cb = _callbacks.get(res.id);
		}
		if (cb != null) {
			try {
				switch(res.type) {
					case SpeechResult.INTERMEDIATE:
						cb.onIntermediateResult(res.id, res.asr, res.extra);
						break;
					case SpeechResult.START:
						cb.onStart(res.id);
						break;
					case SpeechResult.END:
						cb.onComplete(res.id, res.asr, res.nlp, res.action);
						del_cb = true;
						break;
					case SpeechResult.CANCELLED:
						cb.onCancel(res.id);
						del_cb = true;
						break;
					case SpeechResult.ERROR:
						cb.onError(res.id, res.err);
						del_cb = true;
						break;
				}
			} catch (Exception e) {
				Log.w(TAG, "invoke callback through binder occurs exception");
				e.printStackTrace();
				del_cb = true;
			}
		}
		if (del_cb) {
			synchronized (_callbacks) {
				_callbacks.remove(res.id);
			}
		}
	}

	private static native void _sdk_init(Class speech_cls,
			Class res_cls, Class options_cls);

	private native long _sdk_create();

	private native void _sdk_delete(long sdk_speech);

	private native boolean _sdk_prepare(long sdk_speech);

	private native void _sdk_release(long sdk_speech);

	private native int _sdk_put_text(long sdk_speech, String content);

	private native int _sdk_start_voice(long sdk_speech,
			SpeechOptions foptions, SpeechOptions soptions);

	private native void _sdk_put_voice(long sdk_speech, int id,
			byte[] voice, int offset, int length);

	private native void _sdk_end_voice(long sdk_speech, int id);

	private native void _sdk_cancel(long sdk_speech, int id);

	private native void _sdk_config(long sdk_speech, String key,
			String value);

	private SparseArray<SpeechCallback> _callbacks;

	private long _sdk_speech;

	static {
		System.loadLibrary("rokid_speech_jni");
		_sdk_init(Speech.class, SpeechResult.class, SpeechOptions.class);
	}

	private static final String TAG = "speech.sdk";

	private static class SpeechResult {
		public int id;
		public int type;
		public int err;
		public String asr;
		public String nlp;
		public String action;
		public String extra;

		private static final int INTERMEDIATE = 0;
		private static final int START = 1;
		private static final int END = 2;
		private static final int CANCELLED = 3;
		private static final int ERROR = 4;
	}
}

class SpeechConfig extends GenericConfigParams {
}
