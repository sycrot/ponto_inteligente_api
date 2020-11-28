package com.thiagofarias.pontointeligente.api.services;

import java.util.List;
import java.util.Optional;

import com.thiagofarias.pontointeligente.api.entities.Funcionario;

public interface FuncionarioService {
	
	Funcionario persistir(Funcionario funcionario);
	
	Optional<Funcionario> buscarPorCpf(String cpf);
	
	Optional<Funcionario> buscarPorEmail(String email);
	
	Optional<Funcionario> buscarPorId(Long id);
	
	List<Funcionario> buscarPorEmpresaId(Long id);

}
