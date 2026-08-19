package com.agendaclientes.api.agendamento.application;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.agendaclientes.api.agendamento.domain.Agendamento;
import com.agendaclientes.api.agendamento.domain.AgendamentoRepository;
import com.agendaclientes.api.cliente.domain.ClienteRepository;
import com.agendaclientes.api.shared.exception.ResourceNotFoundException;

@Service
public class AgendamentoServiceImpl implements AgendamentoService {

    private final AgendamentoRepository agendamentoRepository;
    private final ClienteRepository clienteRepository;

    public AgendamentoServiceImpl(AgendamentoRepository agendamentoRepository, ClienteRepository clienteRepository) {
        this.agendamentoRepository = agendamentoRepository;
        this.clienteRepository = clienteRepository;
    }

    @Override
    public Agendamento criar(UUID usuarioId, UUID clienteId, Instant dataHora, Integer duracaoMinutos,
            String observacoes) {
        if (!clienteRepository.existsByIdAndUsuarioId(clienteId, usuarioId)) {
            throw new ResourceNotFoundException("Cliente não encontrado: " + clienteId);
        }
        Agendamento agendamento = Agendamento.novo(usuarioId, clienteId, dataHora, duracaoMinutos, observacoes);
        return agendamentoRepository.save(agendamento);
    }

    @Override
    public Agendamento atualizar(UUID usuarioId, UUID id, Instant dataHora, Integer duracaoMinutos,
            String observacoes) {
        Agendamento agendamento = buscarPorId(usuarioId, id);
        agendamento.atualizarDados(dataHora, duracaoMinutos, observacoes);
        return agendamentoRepository.save(agendamento);
    }

    @Override
    public Agendamento buscarPorId(UUID usuarioId, UUID id) {
        return agendamentoRepository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento não encontrado: " + id));
    }

    @Override
    public List<Agendamento> listarTodos(UUID usuarioId) {
        return agendamentoRepository.findAllByUsuarioId(usuarioId);
    }

    @Override
    public List<Agendamento> listarPorData(UUID usuarioId, LocalDate data) {
        ZoneId zone = ZoneId.systemDefault();
        Instant inicio = data.atStartOfDay(zone).toInstant();
        Instant fimExclusivo = data.plusDays(1).atStartOfDay(zone).toInstant();
        return agendamentoRepository.findByUsuarioIdAndPeriodo(usuarioId, inicio, fimExclusivo);
    }

    @Override
    public Agendamento cancelar(UUID usuarioId, UUID id) {
        Agendamento agendamento = buscarPorId(usuarioId, id);
        agendamento.cancelar();
        return agendamentoRepository.save(agendamento);
    }

    @Override
    public Agendamento concluir(UUID usuarioId, UUID id) {
        Agendamento agendamento = buscarPorId(usuarioId, id);
        agendamento.concluir();
        return agendamentoRepository.save(agendamento);
    }

    @Override
    public void remover(UUID usuarioId, UUID id) {
        if (!agendamentoRepository.existsByIdAndUsuarioId(id, usuarioId)) {
            throw new ResourceNotFoundException("Agendamento não encontrado: " + id);
        }
        agendamentoRepository.deleteById(id);
    }
}
