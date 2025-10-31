package com.cpd.mono_video_encoder.model;

import com.cpd.mono_video_encoder.model.enums.StatusProcessamento;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "processamento_videos")
public class VideoProcessamento {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private String nomeOriginal;

    private String pathFinal;

    @Column(nullable = false)
    private String emailDestino;

    @Column(nullable = false)
    private String nomeUsuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusProcessamento status;

    private String mensagemErro;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    private LocalDateTime dataConclusao;

    public VideoProcessamento() {
        this.dataCriacao = LocalDateTime.now();
    }

    public VideoProcessamento(String nomeOriginal, String emailDestino, String nomeUsuario, StatusProcessamento status) {
        this.nomeOriginal = nomeOriginal;
        this.emailDestino = emailDestino;
        this.nomeUsuario = nomeUsuario;
        this.status = status;
        this.dataCriacao = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getNomeOriginal() {
        return nomeOriginal;
    }

    public void setNomeOriginal(String nomeOriginal) {
        this.nomeOriginal = nomeOriginal;
    }

    public String getPathFinal() {
        return pathFinal;
    }

    public void setPathFinal(String pathFinal) {
        this.pathFinal = pathFinal;
    }

    public String getEmailDestino() {
        return emailDestino;
    }

    public void setEmailDestino(String emailDestino) {
        this.emailDestino = emailDestino;
    }

    public String getNomeUsuario() {
        return nomeUsuario;
    }

    public void setNomeUsuario(String nomeUsuario) {
        this.nomeUsuario = nomeUsuario;
    }

    public StatusProcessamento getStatus() {
        return status;
    }

    public void setStatus(StatusProcessamento status) {
        this.status = status;
    }

    public String getMensagemErro() {
        return mensagemErro;
    }

    public void setMensagemErro(String mensagemErro) {
        this.mensagemErro = mensagemErro;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public LocalDateTime getDataConclusao() {
        return dataConclusao;
    }

    public void setDataConclusao(LocalDateTime dataConclusao) {
        this.dataConclusao = dataConclusao;
    }
}