package com.noisepages.nettoyeur.openslstreamsample;

import java.io.IOException;

/**
 * Simple lowpass filter implementation using opensl_stream.
 */
public class Lowpass {

	static {
		// Load the associated native library, which does most of the work.
		System.loadLibrary("lowpass");
	}

	private long streamPtr;  // Slightly naughty: We store a C pointer in a field of type long.

	/**
	 * Constructs a lowpass filter object for the given sample rate and buffer size.
	 * 
	 * @param sampleRate in Hertz.
	 * @param bufferSize in frames.
	 * @throws IOException if the sample rate or buffer size are not supported.
	 */
	public Lowpass(int sampleRate, int bufferSize) throws IOException {
		streamPtr = open(sampleRate, bufferSize);
		if (streamPtr == 0) {
			throw new IOException("Unsupported audio parameters: " + sampleRate + ", " + bufferSize);
		}
	}

	/**
	 * Must be called before this object is garbage collected. Safe to call more than once.
	 */
	public void close() {
		if (streamPtr != 0) {
			close(streamPtr);
			streamPtr = 0;
		}
	}

	/**
	 * Starts the OpenSL audio stream; will have no effect if the object has already been started.
     * May not be called after close() has been called.
	 * 
	 * @throws IOException if the stream cannot be started.
	 */
	public void start() throws IOException {
		if (streamPtr == 0) {
			throw new IllegalStateException("Stream closed.");
		}
		if (start(streamPtr) != 0) {
			throw new IOException("Unable to start OpenSL stream.");
		}
	}

	/**
	 * Stops the OpenSL audio stream; will have no effect if the object has already been started.
     * May not be called after close() has been called.
	 */
	public void stop() {
		if (streamPtr == 0) {
			throw new IllegalStateException("Stream closed.");
		}
		stop(streamPtr);
	}

	/**
	 * Sets the smoothing factor alpha; safe to call while the stream is running.
     * May not be called after close() has been called.
	 * 
	 * @param Smoothing factor in percent, 0 <= alpha <= 100.
	 */
	public void setAlpha(int alpha) {
		if (streamPtr == 0) {
			throw new IllegalStateException("Stream closed.");
		}
		setAlpha(streamPtr, alpha);
	}

	/**
     * May not be called after close() has been called.
     * 
	 * @return true if the OpenSL audio stream filter is running.
	 */
	public boolean isRunning() {
		if (streamPtr == 0) {
			throw new IllegalStateException("Stream closed.");
		}
		return isRunning(streamPtr);
	}
	
	private static native long open(int sampleRate, int bufferSize);
	private static native void close(long streamPtr);
	private static native int start(long streamPtr);
	private static native void stop(long streamPtr);
	private static native void setAlpha(long streamPtr, int alpha);
	private static native boolean isRunning(long streamPtr);
}
