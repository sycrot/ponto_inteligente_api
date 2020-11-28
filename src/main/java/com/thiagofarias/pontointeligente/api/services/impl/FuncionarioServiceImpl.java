package com.thiagofarias.pontointeligente.api.services.impl;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.thiagofarias.pontointeligente.api.entities.Funcionario;
import com.thiagofarias.pontointeligente.api.repositories.FuncionarioRepository;
import com.thiagofarias.pontointeligente.api.services.FuncionarioService;

@Service
public class FuncionarioServiceImpl implements FuncionarioService {
	
	private static final Logger log = LoggerFactory.getLogger(FuncionarioServiceImpl.class);

	@Autowired
	private FuncionarioRepository funcionarioRepository;
	
	public Funcionario persistir(Funcionario funcionario) {
		log.info("Persistindo funcionário: {}", funcionario);
		return this.funcionarioRepository.save(funcionario);
	}

	public Optional<Funcionario> buscarPorCpf(String cpf) {
		// TODO Auto-generated method stub
		log.info("Buscando funcionario pelo CPF{}", cpf);
		return Optional.ofNullable(this.funcionarioRepository.findByCpf(cpf));
	}

	public Optional<Funcionario> buscarPorEmail(String email) {
		// TODO Auto-generated method stub
		log.info("Buscando funcionário pelo Email{}", email);
		return Optional.ofNullable(this.funcionarioRepository.findByEmail(email));
	}

	public Optional<Funcionario> buscarPorId(Long id) {
		log.info("Buscando funcionário pelo id{}", id);
		return this.funcionarioRepository.findById(id);
	}

	public List<Funcionario> buscarPorEmpresaId(Long id) {
		log.info("Buscando funcionários pela empresa{}", id);
		return this.funcionarioRepository.findByEmpresaId(id);
	}
	

}
