package com.cpd.mono_video_encoder.controller;

import com.cpd.mono_video_encoder.services.LoteProcessingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class VideoUploadController {

    private static final Logger log = LoggerFactory.getLogger(VideoUploadController.class);

    private final LoteProcessingService loteProcessingService;

    @Autowired
    public VideoUploadController(LoteProcessingService loteProcessingService) {
        this.loteProcessingService = loteProcessingService;
    }

    @PostMapping("/processar-videos")
    public ResponseEntity<String> processarVideosMonolito(
            @RequestParam("videos") List<MultipartFile> videoFiles,
            @RequestParam("emailDestino") String emailDestino,
            @RequestParam("nomeUsuario") String nomeUsuario) {

        if (videoFiles.isEmpty() || videoFiles.get(0).isEmpty()) {
            return ResponseEntity.badRequest().body("Nenhum vídeo enviado.");
        }

        log.info("CONTROLLER: Requisição recebida para processar {} vídeos.", videoFiles.size());

        try {
            String responseMsg = loteProcessingService.processarLote(
                    videoFiles,
                    emailDestino,
                    nomeUsuario
            );

            log.info("CONTROLLER: Requisição finalizada.");
            return ResponseEntity.ok(responseMsg);

        } catch (Exception e) {
            log.error("CONTROLLER: Falha inesperada ao delegar para o LoteService", e);
            return ResponseEntity.internalServerError().body("Erro catastrófico no servidor: " + e.getMessage());
        }
    }
}