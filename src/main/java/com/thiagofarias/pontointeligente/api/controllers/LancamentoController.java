package com.thiagofarias.pontointeligente.api.controllers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.apache.commons.lang3.EnumUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.thiagofarias.pontointeligente.api.dtos.LancamentoDto;
import com.thiagofarias.pontointeligente.api.entities.Funcionario;
import com.thiagofarias.pontointeligente.api.entities.Lancamento;
import com.thiagofarias.pontointeligente.api.enums.TipoEnum;
import com.thiagofarias.pontointeligente.api.response.Response;
import com.thiagofarias.pontointeligente.api.services.FuncionarioService;
import com.thiagofarias.pontointeligente.api.services.LancamentoService;

@RestController
@RequestMapping("/api/lancamentos")
@CrossOrigin(origins="*")
public class LancamentoController {
	
	private static final Logger log = LoggerFactory.getLogger(LancamentoController.class);
	private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	@Autowired
	private LancamentoService lancamentoService;
	
	@Autowired
	private FuncionarioService funcionarioService;
	
	@Value("${paginacao.qtd_por_pagina}")
	private int qtdPorPagina;
	
	// returns an employee's listing of entries
	@GetMapping(value = "/funcionario/{funcionarioId}")
	public ResponseEntity<Response<Page<LancamentoDto>>> listarPorFuncionarioId(
			@PathVariable("funcionarioId") Long funcionarioId,
			@RequestParam(value="pag", defaultValue="0") int pag,
			@RequestParam(value="ord", defaultValue="data") String ord,
			@RequestParam(value="dir", defaultValue="DESC") String dir
			) {
		
		log.info("Buscando lançamentos por ID do funcionário: {}, página{}", funcionarioId, pag);
		Response<Page<LancamentoDto>> response = new Response<Page<LancamentoDto>>();
		
		PageRequest pageRequest = PageRequest.of(pag, this.qtdPorPagina, Direction.valueOf(dir), ord);
		Page<Lancamento> lancamentos = this.lancamentoService.buscarPorFuncionarioId(funcionarioId, pageRequest);
		Page<LancamentoDto> lancamentosDto = lancamentos.map(lancamento -> this.converterLancamentoDto(lancamento));
		
		response.setData(lancamentosDto);
		return ResponseEntity.ok(response);
		
	}
	
	@GetMapping(value = "/funcionario/{funcionarioId}/todos")
	public ResponseEntity<Response<List<LancamentoDto>>> listarTodosPorFuncionarioId (@PathVariable("funcionarioId") Long funcionarioId) {
		
		log.info("Buscando todos os lançamentos por ID do funcionário: {}", funcionarioId);
		Response<List<LancamentoDto>> response = new Response<List<LancamentoDto>>();
		
		List<Lancamento> lancamentos = this.lancamentoService.buscarTodosPorFuncionarioId(funcionarioId);
		List<LancamentoDto> lancamentosDto = lancamentos.stream().map(lancamento -> this.converterLancamentoDto(lancamento)).collect(Collectors.toList());
		
		response.setData(lancamentosDto);
		return ResponseEntity.ok(response);
		
	}
	
	@GetMapping(value = "/{id}")
	public ResponseEntity<Response<LancamentoDto>> listarPorId(@PathVariable("id") Long id) {
		
		log.info("Buscando lançamentos por ID: {}", id);
		Response<LancamentoDto> response = new Response<LancamentoDto>();
		Optional<Lancamento> lancamento = this.lancamentoService.buscarPorId(id);
		
		if (!lancamento.isPresent()) {
			log.info("Lançamento não encontrado");
			response.getErrors().add("Lancamento não encontrado para o id "+id);
			return ResponseEntity.badRequest().body(response);
		}
		
		response.setData(this.converterLancamentoDto(lancamento.get()));
		return ResponseEntity.ok(response);
		
	}
	
	@PostMapping
	public ResponseEntity<Response<LancamentoDto>> adicionar(@Valid @RequestBody LancamentoDto lancamentoDto, BindingResult result) throws ParseException {
		
		log.info("Adicionando lançamento: {}",lancamentoDto.toString());
		Response<LancamentoDto> response = new Response<LancamentoDto>();
		validarFuncionario(lancamentoDto, result);
		Lancamento lancamento = this.converterDtoParaLancamento(lancamentoDto, result);
		
		if (result.hasErrors()) {
			log.error("Erro validando lançamento: {}", result.getAllErrors());
			result.getAllErrors().forEach(error -> response.getErrors().add(error.getDefaultMessage()));
			return ResponseEntity.badRequest().body(response);
		}
		
		lancamento = this.lancamentoService.Persistir(lancamento);
		response.setData(this.converterLancamentoDto(lancamento));
		return ResponseEntity.ok(response);
		
	}
	
	@PutMapping(value = "/{id}")
	public ResponseEntity<Response<LancamentoDto>> atualizar(
			@PathVariable("id") Long id,
			@Valid @RequestBody LancamentoDto lancamentoDto,
			BindingResult result
			) throws ParseException {
		
		log.info("Atualizando lançamento: {}", lancamentoDto.toString());
		Response<LancamentoDto> response = new Response<LancamentoDto>();
		validarFuncionario(lancamentoDto, result);
		lancamentoDto.setId(Optional.of(id));
		Lancamento lancamento = this.converterDtoParaLancamento(lancamentoDto, result);
		
		if (result.hasErrors()) {
			log.error("Erro validando lançamentos: {}", result.getAllErrors());
			result.getAllErrors().forEach(error -> response.getErrors().add(error.getDefaultMessage()));
			return ResponseEntity.badRequest().body(response);
		}
		
		lancamento = this.lancamentoService.Persistir(lancamento);
		response.setData(this.converterLancamentoDto(lancamento));
		return ResponseEntity.ok(response);
		
	}
	
	@DeleteMapping(value = "/{id}")
	@PreAuthorize("hasAnyRole('ADMIN')")
	public ResponseEntity<Response<String>> remover(@PathVariable("id") Long id) {
		
		log.info("Removendo lançamento: {}", id);
		Response<String> response = new Response<String>();
		Optional<Lancamento> lancamento = this.lancamentoService.buscarPorId(id);
		
		if (!lancamento.isPresent()) {
			log.info("Erro ao remover devido ao lançamento ID: {} ser inválido", id);
			response.getErrors().add("Erro ao remover lançamento. Registro não encontrado para o id "+id);
			return ResponseEntity.badRequest().body(response);
		}
		
		this.lancamentoService.remover(id);
		return ResponseEntity.ok(new Response<String>());
		
	}
	
	@GetMapping(value = "/funcionario/{funcionarioId}/ultimo")
	public ResponseEntity<Response<LancamentoDto>> ultimoPorFuncionarioId(@PathVariable("funcionarioId") Long funcionarioId) {
		
		log.info("Buscando o último lançamento por ID do funcionário: {}", funcionarioId);
		Response<LancamentoDto> response = new Response<LancamentoDto>();
		Optional<Lancamento> lancamento = this.lancamentoService.buscarUtlimoPorFuncionarioId(funcionarioId);
		
		if (lancamento.isPresent()) {
			LancamentoDto lancamentoDto = this.converterLancamentoDto(lancamento.get());
			response.setData(lancamentoDto);
		}
		
		return ResponseEntity.ok(response);
	}
	
	private void validarFuncionario(LancamentoDto lancamentoDto, BindingResult result) {
		
		if (lancamentoDto.getFuncionarioId() == null) {
			result.addError(new ObjectError("funcionario", "Funcionário não informado"));
		}
		
		log.info("Validando funcionário id {}: ", lancamentoDto.getFuncionarioId());
		Optional<Funcionario> funcionario = this.funcionarioService.buscarPorId(lancamentoDto.getFuncionarioId());
		if (!funcionario.isPresent()) {
			result.addError(new ObjectError("funcionario", "Funcionário não econtrado. ID inexistente"));
		}
		
	}
	
	private LancamentoDto converterLancamentoDto(Lancamento lancamento) {
		
		LancamentoDto lancDto = new LancamentoDto();
		lancDto.setId(Optional.of(lancamento.getId()));
		lancDto.setData(this.dateFormat.format(lancamento.getData()));
		lancDto.setTipo(lancamento.getTipo().toString());
		lancDto.setDescricao(lancamento.getDescricao());
		lancDto.setLocalizacao(lancamento.getLocalizacao());
		lancDto.setFuncionarioId(lancamento.getFuncionario().getId());
		
		return lancDto;
		
	}
	
	private Lancamento converterDtoParaLancamento(LancamentoDto lancamentoDto, BindingResult result) throws ParseException {
		
		Lancamento lancamento = new Lancamento();
		
		if (lancamentoDto.getId().isPresent()) {
			Optional<Lancamento> lanc = this.lancamentoService.buscarPorId(lancamentoDto.getFuncionarioId());
			if (lanc.isPresent()) {
				lancamento = lanc.get();
			} else {
				result.addError(new ObjectError("lancamento", "Lançamento não encontrado"));
			}
		} else {
			lancamento.setFuncionario(new Funcionario());
			lancamento.getFuncionario().setId(lancamentoDto.getFuncionarioId());
		}
		
		lancamento.setDescricao(lancamentoDto.getDescricao());
		lancamento.setLocalizacao(lancamentoDto.getLocalizacao());
		lancamento.setData(this.dateFormat.parse(lancamentoDto.getData()));
		
		if (EnumUtils.isValidEnum(TipoEnum.class, lancamentoDto.getTipo())) {
			lancamento.setTipo(TipoEnum.valueOf(lancamentoDto.getTipo()));
		} else {
			result.addError(new ObjectError("tipo", "Tipo inválido"));
		}
		
		return lancamento;
		
	}

}
