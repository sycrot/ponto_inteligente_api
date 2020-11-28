package com.thiagofarias.pontointeligente.api.controllers;

import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.thiagofarias.pontointeligente.api.dtos.CadastroPFDto;
import com.thiagofarias.pontointeligente.api.entities.Empresa;
import com.thiagofarias.pontointeligente.api.entities.Funcionario;
import com.thiagofarias.pontointeligente.api.enums.PerfilEnum;
import com.thiagofarias.pontointeligente.api.response.Response;
import com.thiagofarias.pontointeligente.api.services.EmpresaService;
import com.thiagofarias.pontointeligente.api.services.FuncionarioService;
import com.thiagofarias.pontointeligente.api.utils.PasswordUtil;

@RestController
@RequestMapping("api/cadastrar-pf")
@CrossOrigin(origins = "*")
public class CadastroPFController {
	
	private static final Logger log = LoggerFactory.getLogger(CadastroPFController.class);
	
	@Autowired
	private EmpresaService empresaService;
	
	@Autowired
	private FuncionarioService funcionarioService;
	
	public CadastroPFController() {
		
	}
	
	@PostMapping
	public ResponseEntity<Response<CadastroPFDto>> cadastrar(@Valid @RequestBody CadastroPFDto cadastroPFDto, BindingResult result) throws NoSuchAlgorithmException {
		log.info("Cadastrando PF: {}", cadastroPFDto.toString());
		Response<CadastroPFDto> response = new Response<CadastroPFDto>();
		
		validarDadosExistentes(cadastroPFDto, result);
		Funcionario funcionario = this.converterDtoParaFuncionario(cadastroPFDto, result);
		
		if (result.hasErrors()) {
			log.error("Erro validando dados de cadastro PF: {}", result.getAllErrors());
			result.getAllErrors().forEach(error -> response.getErrors().add(error.getDefaultMessage()));
			return ResponseEntity.badRequest().body(response);
		}
		
		Optional<Empresa> empresa = this.empresaService.buscarPorCnpj(cadastroPFDto.getCnpj());
		empresa.ifPresent(emp -> funcionario.setEmpresa(emp));
		this.funcionarioService.persistir(funcionario);
		
		response.setData(this.converterCadastroPFDto(funcionario));
		return ResponseEntity.ok(response);
	
	}
	
	private void validarDadosExistentes(CadastroPFDto cadastroPFDto, BindingResult result) {
		Optional<Empresa> empresa = this.empresaService.buscarPorCnpj(cadastroPFDto.getCnpj());
		
		if(!empresa.isPresent()) {
			result.addError(new ObjectError("empresa", "Empresa não cadastrada"));
		}
		
		this.funcionarioService.buscarPorCpf(cadastroPFDto.getCpf()).ifPresent(func -> result.addError(new ObjectError("funcionario", "CPF já existente")));
		this.funcionarioService.buscarPorEmail(cadastroPFDto.getEmail()).ifPresent(func -> result.addError(new ObjectError("funcionario", "Email já existente")));
	
	}
	
	private Funcionario converterDtoParaFuncionario(CadastroPFDto cadastroPFDto, BindingResult result) throws NoSuchAlgorithmException {
	
			Funcionario funcionario = new Funcionario();
			funcionario.setNome(cadastroPFDto.getNome());
			funcionario.setEmail(cadastroPFDto.getEmail());
			funcionario.setCpf(cadastroPFDto.getCpf());
			funcionario.setPerfil(PerfilEnum.ROLE_USUARIO);
			funcionario.setSenha(PasswordUtil.gerarBCrypt(cadastroPFDto.getSenha()));
			cadastroPFDto.getQtdHorasAlmoco().ifPresent(qtdHorasAlmoco -> funcionario.setQtdHorasAlmoco(Float.valueOf(qtdHorasAlmoco)));
			cadastroPFDto.getQtdHorasTrabalhoDia().ifPresent(qtdHorasDia -> funcionario.setQtdHorasTrabalhadas(Float.valueOf(qtdHorasDia)));
			cadastroPFDto.getValorHora().ifPresent(valorHora -> funcionario.setValorHora(new BigDecimal(valorHora)));
			
			return funcionario;
	
	}
	
	private CadastroPFDto converterCadastroPFDto(Funcionario funcionario) {
		
		CadastroPFDto cadPFDto = new CadastroPFDto();
		cadPFDto.setId(funcionario.getId());
		cadPFDto.setNome(funcionario.getNome());
		cadPFDto.setEmail(funcionario.getEmail());
		cadPFDto.setCpf(funcionario.getCpf());
		cadPFDto.setCnpj(funcionario.getEmpresa().getCnpj());
		funcionario.getQtdHorasAlmocoOpt().ifPresent(qtdHorasAlmoco -> cadPFDto.setQtdHorasAlmoco(Optional.of(Float.toString(qtdHorasAlmoco))));
		funcionario.getQtdHorasTrabalhadasOt().ifPresent(qtdHorasDia -> cadPFDto.setQtdHorasTrabalhoDia(Optional.of(Float.toString(qtdHorasDia))));
		funcionario.getValorHoraOpt().ifPresent(valorHora -> cadPFDto.setValorHora(Optional.of(valorHora.toString())));
		
		
		return cadPFDto;
	}

}
