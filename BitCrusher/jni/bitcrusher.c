#include "bitcrusher.h"

#include <stdlib.h>

#include "opensl_stream/opensl_stream.h"

static void process(void *context, int sample_rate, int buffer_frames,
     int input_channels, const short *input_buffer,
     int output_channels, short *output_buffer) {
  short mask = __sync_fetch_and_or((short *)context, 0);
  if (input_channels > 0) {
    int i;
    for (i = 0; i < buffer_frames; ++i) {
      int j;
      for (j = 0; j < output_channels; ++j) {
        output_buffer[output_channels * i + j] =
            input_buffer[input_channels * i] & mask;
      }
    }
  }
}

static struct bitcrusher {
  OPENSL_STREAM *os;
  short mask;
};

JNIEXPORT jlong JNICALL Java_com_noisepages_nettoyeur_bitcrusher_OpenSlBitCrusher_configureNative
(JNIEnv *env, jclass clazz, jint sampleRate, jint bufferSize) {
  struct bitcrusher *bc = malloc(sizeof(struct bitcrusher));
  if (bc) {
    bc->mask = -1;
    bc->os = opensl_open(sampleRate, 1, 2, bufferSize, process, &bc->mask);
    if (!bc->os) {
      free(bc);
      bc = NULL;
    }
  }
  return (jlong) bc;
}

JNIEXPORT void JNICALL Java_com_noisepages_nettoyeur_bitcrusher_OpenSlBitCrusher_closeNative
(JNIEnv *env, jclass clazz, jlong bc) {
  opensl_close(((struct bitcrusher *)bc)->os);
  free((void *)bc);
}

JNIEXPORT jint JNICALL Java_com_noisepages_nettoyeur_bitcrusher_OpenSlBitCrusher_startNative
(JNIEnv *env, jclass clazz, jlong bc) {
  return opensl_start(((struct bitcrusher *)bc)->os);
}

JNIEXPORT void JNICALL Java_com_noisepages_nettoyeur_bitcrusher_OpenSlBitCrusher_stopNative
(JNIEnv *env, jclass clazz, jlong bc) {
  opensl_pause(((struct bitcrusher *)bc)->os);
}

JNIEXPORT void JNICALL Java_com_noisepages_nettoyeur_bitcrusher_OpenSlBitCrusher_crushNative
(JNIEnv *env, jclass clazz, jlong bc, jint crush) {
  short *mask = &((struct bitcrusher *)bc)->mask;
  __sync_bool_compare_and_swap(mask, *mask, -1 << crush);
}
