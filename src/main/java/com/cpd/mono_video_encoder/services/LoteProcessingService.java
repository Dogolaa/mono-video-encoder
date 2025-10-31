package com.cpd.mono_video_encoder.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.cpd.mono_video_encoder.model.VideoProcessamento;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
public class LoteProcessingService {

    private static final Logger log = LoggerFactory.getLogger(LoteProcessingService.class);
    private final Path uploadDir = Paths.get("./uploads");

    private final VideoOrchestratorService videoOrchestrator;
    private final VideoPersistenceService persistenceService;
    private final EmailService emailService;

    @Autowired
    public LoteProcessingService(VideoOrchestratorService videoOrchestrator,
                                 VideoPersistenceService persistenceService,
                                 EmailService emailService) {
        this.videoOrchestrator = videoOrchestrator;
        this.persistenceService = persistenceService;
        this.emailService = emailService;
    }

    public String processarLote(List<MultipartFile> videoFiles, String emailDestino, String nomeUsuario) {

        long startTime = System.currentTimeMillis();
        int totalVideosProcessados = 0;
        int totalFalhas = 0;

        StringBuilder emailReport = new StringBuilder("Olá, " + nomeUsuario + "!\n\n");
        emailReport.append("Seu processamento em lote foi concluído:\n\n");

        log.info("LOTE: Iniciando processamento de {} vídeos para {}", videoFiles.size(), nomeUsuario);

        for (MultipartFile videoFile : videoFiles) {
            if (videoFile.isEmpty()) {
                continue;
            }

            String nomeOriginal = videoFile.getOriginalFilename();
            String idUnico = UUID.randomUUID().toString();
            Path originalPath = uploadDir.resolve(idUnico + "_" + nomeOriginal);
            Path finalPath = uploadDir.resolve(idUnico + "_FINAL.mp4");

            VideoProcessamento registro = persistenceService.criarRegistroPendente(nomeOriginal, emailDestino, nomeUsuario);
            log.info("LOTE: Processando vídeo {} (ID: {})...", nomeOriginal, registro.getId());

            try {
                persistenceService.marcarComoProcessando(registro);

                Files.copy(videoFile.getInputStream(), originalPath, StandardCopyOption.REPLACE_EXISTING);

                videoOrchestrator.processarVideoMonolito(
                        originalPath.toString(),
                        finalPath.toString()
                );

                persistenceService.marcarComoConcluido(registro, finalPath.toString());

                emailReport.append("- '").append(nomeOriginal).append("': Processado com sucesso.\n");
                totalVideosProcessados++;
                log.info("LOTE: Vídeo {} processado com sucesso.", nomeOriginal);

            } catch (Exception e) {
                log.error("LOTE: Falha no processamento do vídeo: {}", nomeOriginal, e);

                persistenceService.marcarComoFalha(registro, e.getMessage());

                emailReport.append("- '").append(nomeOriginal).append("': FALHA NO PROCESSAMENTO.\n");
                totalFalhas++;
            } finally {
                try {
                    Files.deleteIfExists(originalPath);
                } catch (IOException e) {
                    log.warn("LOTE: Não foi possível limpar o arquivo original: {}", originalPath, e);
                }
            }
        }

        log.info("LOTE: Processamento em lote finalizado.");

        try {
            emailService.enviarEmailSimples(
                    emailDestino,
                    "Seu lote de vídeos foi processado!",
                    emailReport.toString()
            );
        } catch (Exception e) {
            log.error("LOTE: Falha ao enviar e-mail de resumo", e);
        }

        long duration = System.currentTimeMillis() - startTime;
        return String.format(
                "Processo monolítico concluído. %d de %d vídeos processados com sucesso (%d falhas) em %d segundos.",
                totalVideosProcessados,
                videoFiles.size(),
                totalFalhas,
                (duration / 1000)
        );
    }
}