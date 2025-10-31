package com.cpd.mono_video_encoder.services;

import com.cpd.mono_video_encoder.model.VideoProcessamento;
import com.cpd.mono_video_encoder.model.enums.StatusProcessamento;
import com.cpd.mono_video_encoder.repository.VideoProcessamentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class VideoPersistenceService {

    private final VideoProcessamentoRepository videoRepository;

    @Autowired
    public VideoPersistenceService(VideoProcessamentoRepository videoRepository) {
        this.videoRepository = videoRepository;
    }

    public VideoProcessamento criarRegistroPendente(String nomeOriginal, String email, String usuario) {
        VideoProcessamento registro = new VideoProcessamento(
                nomeOriginal,
                email,
                usuario,
                StatusProcessamento.PENDENTE
        );
        return videoRepository.save(registro);
    }

    public void marcarComoProcessando(VideoProcessamento registro) {
        registro.setStatus(StatusProcessamento.PROCESSANDO);
        videoRepository.save(registro);
    }

    public void marcarComoConcluido(VideoProcessamento registro, String pathFinal) {
        registro.setStatus(StatusProcessamento.CONCLUIDO);
        registro.setPathFinal(pathFinal);
        registro.setDataConclusao(LocalDateTime.now());
        videoRepository.save(registro);
    }

    public void marcarComoFalha(VideoProcessamento registro, String mensagemErro) {
        registro.setStatus(StatusProcessamento.FALHA);
        registro.setMensagemErro(mensagemErro);
        registro.setDataConclusao(LocalDateTime.now());
        videoRepository.save(registro);
    }
}