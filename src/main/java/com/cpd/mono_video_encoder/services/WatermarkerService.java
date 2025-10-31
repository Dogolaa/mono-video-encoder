package com.cpd.mono_video_encoder.services;

import org.bytedeco.javacv.*;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Point;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;

import static org.bytedeco.opencv.global.opencv_imgproc.*;

@Service
public class WatermarkerService {

    private static final Logger log = LoggerFactory.getLogger(WatermarkerService.class);

    public void addWatermark(String inputFile, String outputFile, String watermarkText) throws Exception {
        FFmpegFrameGrabber grabber = null;
        FFmpegFrameRecorder recorder = null;
        OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();

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
                recorder.setAudioCodec(grabber.getAudioCodec());
                recorder.setSampleRate(grabber.getSampleRate());
            }

            recorder.start();
            Scalar color = new Scalar(255, 255, 255, 0);
            int font = FONT_HERSHEY_SIMPLEX;

            Frame frame;
            while ((frame = grabber.grab()) != null) {
                if (frame.image != null) {
                    Mat mat = converter.convert(frame);
                    putText(mat, watermarkText, new Point(20, 40), font, 1.2, color, 2, LINE_AA, false);
                    Frame watermarkedFrame = converter.convert(mat);
                    recorder.record(watermarkedFrame);
                    mat.release();
                } else if (frame.samples != null) {
                    recorder.record(frame);
                }
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