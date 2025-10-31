package com.cpd.mono_video_encoder.services;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;

@Service
public class TranscoderService {

    private static final Logger log = LoggerFactory.getLogger(TranscoderService.class);

    public void transcode(String inputFile, String outputFile) throws Exception {
        FFmpegFrameGrabber grabber = null;
        FFmpegFrameRecorder recorder = null;

        try {
            grabber = new FFmpegFrameGrabber(inputFile);
            grabber.start();

            recorder = new FFmpegFrameRecorder(outputFile, grabber.getImageWidth(), grabber.getImageHeight(), grabber.getAudioChannels());
            recorder.setFormat("mp4");
            recorder.setFrameRate(grabber.getFrameRate());

            recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
            recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
            recorder.setVideoOption("crf", "23");

            if (grabber.getAudioChannels() > 0) {
                recorder.setAudioChannels(grabber.getAudioChannels());
                recorder.setAudioCodec(grabber.getAudioCodec());
                recorder.setSampleRate(grabber.getSampleRate());
            }

            recorder.start();
            Frame frame;
            while ((frame = grabber.grab()) != null) {
                recorder.record(frame);
            }
        } finally {
            if (recorder != null) {
                try { recorder.stop(); } catch (Exception e) { log.error("Erro ao parar recorder", e); }
                try { recorder.release(); } catch (Exception e) { log.error("Erro ao liberar recorder", e); }
            }
            if (grabber != null) {
                try { grabber.stop(); } catch (Exception e) { log.error("Erro ao parar grabber", e); }
                try { grabber.release(); } catch (Exception e) { log.error("Erro ao liberar grabber", e); }
            }
        }
    }
}