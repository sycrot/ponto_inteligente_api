package com.thiagofarias.pontointeligente.api.controllers;

import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.thiagofarias.pontointeligente.api.dtos.FuncionarioDto;
import com.thiagofarias.pontointeligente.api.entities.Funcionario;
import com.thiagofarias.pontointeligente.api.response.Response;
import com.thiagofarias.pontointeligente.api.services.FuncionarioService;
import com.thiagofarias.pontointeligente.api.utils.PasswordUtil;

@RestController
@RequestMapping("/api/funcionarios")
@CrossOrigin(origins = "*")
public class FuncionarioController {
	
	private static final Logger log = LoggerFactory.getLogger(FuncionarioController.class);
	
	@Autowired
	private FuncionarioService funcionarioService;
	
	public FuncionarioController() {
		
	}
	
	@PutMapping(value = "/{id}")
	public ResponseEntity<Response<FuncionarioDto>> atualizar(
			@PathVariable("id") Long id,
			@Valid @RequestBody FuncionarioDto funcionarioDto,
			BindingResult result
			) throws NoSuchAlgorithmException {
		
		log.info("Atualizando funcionário: {}", funcionarioDto.toString());
		Response<FuncionarioDto> response = new Response<FuncionarioDto>();
		Optional<Funcionario> funcionario = this.funcionarioService.buscarPorId(id);
		
		if (!funcionario.isPresent()) {
			result.addError(new ObjectError("funcionario", "Funcionário não encontrado"));
		}
		
		this.atualizarDadosFuncionario(funcionario.get(), funcionarioDto, result);
		
		if (result.hasErrors()) {
			log.error("Erro validando funcionário: {}", result.getAllErrors());
			result.getAllErrors().forEach(error -> response.getErrors().add(error.getDefaultMessage()));
			return ResponseEntity.badRequest().body(response);
		}
		
		this.funcionarioService.persistir(funcionario.get());
		response.setData(this.converterFuncionarioDto(funcionario.get()));
		
		return ResponseEntity.ok(response);
	}
	
	@GetMapping(value ="/empresa/{id}")
	public ResponseEntity<Response<List<FuncionarioDto>>> atualizar(@PathVariable("id") Long id) {
		
		log.info("Buscando funcionários por id de empresa: {}", id);
		Response<List<FuncionarioDto>> response = new Response<List<FuncionarioDto>>();
		
		List<Funcionario> funcionarios = funcionarioService.buscarPorEmpresaId(id);
		
		response.setData(funcionarios.stream().map(func -> converterFuncionarioDto(func)).collect(Collectors.toList()));
		
		return ResponseEntity.ok(response);
	}
	
	private void atualizarDadosFuncionario(Funcionario funcionario, FuncionarioDto funcionarioDto, BindingResult result) throws NoSuchAlgorithmException {
		
		if (!funcionario.getEmail().equals(funcionarioDto.getEmail())) {
			this.funcionarioService.buscarPorEmail(funcionarioDto.getEmail()).ifPresent(func -> result.addError(new ObjectError("email", "Email já existente")));
			funcionario.setEmail(funcionarioDto.getEmail());
		}
		
		funcionario.setQtdHorasAlmoco(null);
		funcionarioDto.getQtdHorasAlmoco().ifPresent(qtdHorasAlmoco -> funcionario.setQtdHorasAlmoco(Float.valueOf(qtdHorasAlmoco)));
		
		funcionario.setQtdHorasTrabalhadas(null);
		funcionarioDto.getQtdHorasTrabalhoDia().ifPresent(qtdHorasDia -> funcionario.setQtdHorasTrabalhadas(Float.valueOf(qtdHorasDia)));
		
		funcionario.setValorHora(null);
		funcionarioDto.getValorHora().ifPresent(valorHora -> funcionario.setValorHora(new BigDecimal(valorHora)));
		
		if (funcionarioDto.getSenha().isPresent()) {
			funcionario.setSenha(PasswordUtil.gerarBCrypt(funcionarioDto.getSenha().get()));
		}
		
	}
	
	private FuncionarioDto converterFuncionarioDto(Funcionario funcionario) {
		
		FuncionarioDto funcDto = new FuncionarioDto();
		funcDto.setId(funcionario.getId());
		funcDto.setEmail(funcionario.getEmail());
		funcDto.setNome(funcionario.getNome());
		funcionario.getQtdHorasAlmocoOpt().ifPresent(qtdHorasAlmoco -> funcDto.setQtdHorasAlmoco(Optional.of(Float.toString(qtdHorasAlmoco))));
		funcionario.getQtdHorasTrabalhadasOt().ifPresent(qtdHorasTrabDia -> funcDto.setQtdHorasTrabalhoDia(Optional.of(Float.toString(qtdHorasTrabDia))));
		funcionario.getValorHoraOpt().ifPresent(valorHora -> funcDto.setValorHora(Optional.of(valorHora.toString())));
		
		return funcDto;
		
	}

}
