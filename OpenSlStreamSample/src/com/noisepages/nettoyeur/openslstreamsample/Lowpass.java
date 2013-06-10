package com.noisepages.nettoyeur.openslstreamsample;

import java.io.IOException;

public class Lowpass {

	static {
		System.loadLibrary("lowpass");
	}
	
	private long streamPtr;
	
	public Lowpass(int sampleRate, int bufferSize) throws IOException {
		streamPtr = configure(sampleRate, bufferSize);
		if (streamPtr == 0) {
			throw new IOException("Unsupported audio parameters: " + sampleRate + ", " + bufferSize);
		}
	}
	
	/**
	 * Must be called before this object is garbage collected.
	 */
	public void close() {
		if (streamPtr == 0) {
			throw new IllegalStateException("Stream closed.");
		}
		close(streamPtr);
		streamPtr = 0;
	}

	public void start() throws IOException {
		if (streamPtr == 0) {
			throw new IllegalStateException("Stream closed.");
		}
		if (start(streamPtr) != 0) {
			throw new IOException("Unable to start OpenSL stream.");
		}
	}

	public void stop() {
		if (streamPtr == 0) {
			throw new IllegalStateException("Stream closed.");
		}
		stop(streamPtr);
	}
	
	/**
	 * @param Smoothing factor in percent, 0 <= alpha <= 100.
	 */
	public void setAlpha(int alpha) {
		if (streamPtr == 0) {
			throw new IllegalStateException("Stream closed.");
		}
		setAlpha(streamPtr, alpha);
	}
	
	private static native long configure(int sampleRate, int bufferSize);
	private static native void close(long streamPtr);
	private static native int start(long streamPtr);
	private static native void stop(long streamPtr);
	private static native void setAlpha(long streamPtr, int alpha);
}
