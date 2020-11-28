package com.thiagofarias.pontointeligente.api.services.impl;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.thiagofarias.pontointeligente.api.entities.Lancamento;
import com.thiagofarias.pontointeligente.api.repositories.LancamentoRepository;
import com.thiagofarias.pontointeligente.api.services.LancamentoService;

@Service
public class LancamentoServiceImpl implements LancamentoService {
	
	private static final Logger log = LoggerFactory.getLogger(LancamentoServiceImpl.class);

	@Autowired
	private LancamentoRepository lancamentoRepository;
			
	public Page<Lancamento> buscarPorFuncionarioId(Long funcionarioId, PageRequest pageRequest) {
		log.info("Buscando lançamento para o funcionário ID{}", funcionarioId);
		return this.lancamentoRepository.findByFuncionarioId(funcionarioId, pageRequest);
	}

	@Cacheable("lancamentoPorId")
	public Optional<Lancamento> buscarPorId(Long id) {
		log.info("Buscando um lançamento pelo ID {}", id);
		return this.lancamentoRepository.findById(id);
	}

	@CachePut("lancamentoPorId")
	public Lancamento Persistir(Lancamento lancamento) {
		log.info("Persistindo o lançamento{}", lancamento);
		return this.lancamentoRepository.save(lancamento);
	}

	public void remover(Long id) {
		log.info("Removendo o lançamento ID {}", id);
		this.lancamentoRepository.deleteById(id);
		
	}

	@Override
	public Optional<Lancamento> buscarUtlimoPorFuncionarioId(Long funcionarioId) {
		log.info("Buscando o último lançamento por ID de funcionário {}", funcionarioId);
		return Optional.ofNullable(this.lancamentoRepository.findFirstByFuncionarioIdOrderByDataCriacaoDesc(funcionarioId));
	}

	public List<Lancamento> buscarTodosPorFuncionarioId(Long funcionarioId) {
		log.info("Buscando todos os lançamentos para o funcionário id {}", funcionarioId);
		return this.lancamentoRepository.findByFuncionarioIdOrderByDataDesc(funcionarioId);
	}

}
