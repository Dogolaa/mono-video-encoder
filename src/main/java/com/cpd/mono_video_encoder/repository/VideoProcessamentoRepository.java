package com.cpd.mono_video_encoder.repository;

import com.cpd.mono_video_encoder.model.VideoProcessamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface VideoProcessamentoRepository extends JpaRepository<VideoProcessamento, UUID> {
}