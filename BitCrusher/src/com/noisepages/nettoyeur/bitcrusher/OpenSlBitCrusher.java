package com.noisepages.nettoyeur.bitcrusher;

import java.io.IOException;

public class OpenSlBitCrusher {

	static {
		System.loadLibrary("bitcrusher");
	}
	
	private long streamPtr;
	
	public OpenSlBitCrusher(int sampleRate, int bufferSize) throws IOException {
		streamPtr = configureNative(sampleRate, bufferSize);
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
		closeNative(streamPtr);
		streamPtr = 0;
	}

	public void start() throws IOException {
		if (streamPtr == 0) {
			throw new IllegalStateException("Stream closed.");
		}
		if (startNative(streamPtr) != 0) {
			throw new IOException("Unable to start OpenSL stream.");
		}
	}

	public void stop() {
		if (streamPtr == 0) {
			throw new IllegalStateException("Stream closed.");
		}
		stopNative(streamPtr);
	}
	
	/**
	 * @param number of bits to crush, 0 <= bits < 15; 0 means no effect.
	 */
	public void crush(int bits) {
		if (streamPtr == 0) {
			throw new IllegalStateException("Stream closed.");
		}
		crushNative(streamPtr, bits);
	}
	
	private static native long configureNative(int sampleRate, int bufferSize);
	private static native void closeNative(long streamPtr);
	private static native int startNative(long streamPtr);
	private static native void stopNative(long streamPtr);
	private static native void crushNative(long streamPtr, int bits);
}
