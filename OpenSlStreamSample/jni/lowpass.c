#include "lowpass.h"

#include <stdlib.h>

#include "opensl_stream/opensl_stream.h"

static struct lowpass {
  OPENSL_STREAM *os;
  int alpha;
  int prev;
};

static void process(void *context, int sample_rate, int buffer_frames,
     int input_channels, const short *input_buffer,
     int output_channels, short *output_buffer) {
  if (input_channels > 0) {
    struct lowpass *lp = (struct lowpass *)context;
    // We use gcc atomics here since alpha may change concurrently.
    int alpha = __sync_fetch_and_or(&lp->alpha, 0);
    int i;
    for (i = 0; i < buffer_frames; ++i) {
      int v = (alpha * input_buffer[input_channels * i] +
               (100 - alpha) * lp->prev) / 100;
      lp->prev = v;
      int j;
      for (j = 0; j < output_channels; ++j) {
        output_buffer[output_channels * i + j] = v;
      }
    }
  }
}

JNIEXPORT jlong JNICALL Java_com_noisepages_nettoyeur_openslstreamsample_Lowpass_configure
(JNIEnv *env, jclass clazz, jint sampleRate, jint bufferSize) {
  struct lowpass *lp = malloc(sizeof(struct lowpass));
  if (lp) {
    lp->alpha = 100;
    lp->os = opensl_open(sampleRate, 1, 2, bufferSize, process, lp);
    if (!lp->os) {
      free(lp);
      lp = NULL;
    }
  }
  return (jlong) lp;
}

JNIEXPORT void JNICALL Java_com_noisepages_nettoyeur_openslstreamsample_Lowpass_close
(JNIEnv *env, jclass clazz, jlong p) {
  struct lowpass *lp = (struct lowpass *)p;
  opensl_close(lp->os);
  free(lp);
}

JNIEXPORT jint JNICALL Java_com_noisepages_nettoyeur_openslstreamsample_Lowpass_start
(JNIEnv *env, jclass clazz, jlong p) {
  struct lowpass *lp = (struct lowpass *)p;
  lp->prev = 0;
  return opensl_start(lp->os);
}

JNIEXPORT void JNICALL Java_com_noisepages_nettoyeur_openslstreamsample_Lowpass_stop
(JNIEnv *env, jclass clazz, jlong p) {
  struct lowpass *lp = (struct lowpass *)p;
  opensl_pause(lp->os);
}

JNIEXPORT void JNICALL Java_com_noisepages_nettoyeur_openslstreamsample_Lowpass_setAlpha
(JNIEnv *env, jclass clazz, jlong p, jint alpha) {
  struct lowpass *lp = (struct lowpass *)p;
  __sync_bool_compare_and_swap(&lp->alpha, lp->alpha, alpha);
}
