package com.ipl.analysis.service;

import com.ipl.analysis.dto.PlayerDto;
import com.ipl.analysis.mapper.PlayerMapper;
import com.ipl.analysis.model.Player;
import com.ipl.analysis.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final PlayerMapper playerMapper;

    @Cacheable(value = "players", key = "#pageable")
    public Page<PlayerDto> getAllPlayers(Pageable pageable) {
        return playerRepository.findAll(pageable).map(playerMapper::toDto);
    }

    public PlayerDto getPlayerById(Long id) {
        return playerRepository.findById(id)
                .map(playerMapper::toDto)
                .orElseThrow(() -> new RuntimeException("Player not found"));
    }

    @Cacheable(value = "playerSearch", key = "#name")
    public List<PlayerDto> searchPlayers(String name) {
        return playerRepository.findByNameContainingIgnoreCase(name, PageRequest.of(0, 10))
                .stream()
                .map(playerMapper::toDto)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "teamPlayers", key = "#teamName")
    public List<PlayerDto> getPlayersByTeam(String teamName) {
        return playerRepository.findByTeam(teamName)
                .stream()
                .map(playerMapper::toDto)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "topPlayers", key = "{#role, #limit}")
    public List<PlayerDto> getTopPlayers(String role, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<Player> players;
        if (role.toLowerCase().contains("batsman")) {
            players = playerRepository.findTopBatsmen(role, pageable);
        } else {
            players = playerRepository.findTopBowlers(role, pageable);
        }
        return players.stream().map(playerMapper::toDto).collect(Collectors.toList());
    }
}
