package com.cpd.mono_video_encoder.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Paths;

@Service
public class VideoOrchestratorService {

    private static final Logger log = LoggerFactory.getLogger(VideoOrchestratorService.class);

    private final ResizerService resizerService;
    private final WatermarkerService watermarkerService;
    private final TranscoderService transcoderService;

    @Autowired
    public VideoOrchestratorService(ResizerService resizerService,
                                    WatermarkerService watermarkerService,
                                    TranscoderService transcoderService) {
        this.resizerService = resizerService;
        this.watermarkerService = watermarkerService;
        this.transcoderService = transcoderService;
    }

    /**
     * O orquestrador monolítico. Chama cada serviço de trabalho
     * de forma SEQUENCIAL e BLOQUEANTE.
     */
    public void processarVideoMonolito(String inputFile, String outputFile) throws Exception {

        // Caminhos temporários para os estágios intermediários
        String resizedFile = inputFile + "_resized.mp4";
        String watermarkedFile = inputFile + "_watermarked.mp4";

        try {
            // 1. Redimensionar (simulando 480p)
            log.info("[MONOLITO-ORQUESTRADOR] Estágio 1: Chamando ResizerService...");
            resizerService.resizeVideo(inputFile, resizedFile, 854, 480); // 480p
            log.info("[MONOLITO-ORQUESTRADOR] Estágio 1: ResizerService concluído.");

            // 2. Adicionar Marca D'água
            log.info("[MONOLITO-ORQUESTRADOR] Estágio 2: Chamando WatermarkerService...");
            watermarkerService.addWatermark(resizedFile, watermarkedFile, "CPD - MONOLITO");
            log.info("[MONOLITO-ORQUESTRADOR] Estágio 2: WatermarkerService concluído.");

            // 3. Transcodificar (Finalizar)
            log.info("[MONOLITO-ORQUESTRADOR] Estágio 3: Chamando TranscoderService...");
            transcoderService.transcode(watermarkedFile, outputFile);
            log.info("[MONOLITO-ORQUESTRADOR] Estágio 3: TranscoderService concluído.");

        } finally {
            // Limpa os arquivos temporários intermediários
            Files.deleteIfExists(Paths.get(resizedFile));
            Files.deleteIfExists(Paths.get(watermarkedFile));
        }
    }
}