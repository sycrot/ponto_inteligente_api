package com.thiagofarias.pontointeligente.api.services;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.thiagofarias.pontointeligente.api.entities.Lancamento;

public interface LancamentoService {
	
	Page<Lancamento> buscarPorFuncionarioId(Long funcionarioId, PageRequest pageRequest);

	Optional<Lancamento> buscarPorId(Long id);
	
	Lancamento Persistir(Lancamento lancamento);
	
	void remover(Long id);
	
	Optional<Lancamento> buscarUtlimoPorFuncionarioId(Long funcionarioId);
	
	List<Lancamento> buscarTodosPorFuncionarioId(Long funcionarioId);
}
