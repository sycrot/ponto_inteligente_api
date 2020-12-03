package com.thiagofarias.pontointeligente.api;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.stereotype.Component;

import com.thiagofarias.pontointeligente.api.entities.Empresa;
import com.thiagofarias.pontointeligente.api.entities.Funcionario;
import com.thiagofarias.pontointeligente.api.entities.Lancamento;
import com.thiagofarias.pontointeligente.api.enums.PerfilEnum;
import com.thiagofarias.pontointeligente.api.enums.TipoEnum;
import com.thiagofarias.pontointeligente.api.repositories.EmpresaRepository;
import com.thiagofarias.pontointeligente.api.repositories.FuncionarioRepository;
import com.thiagofarias.pontointeligente.api.repositories.LancamentoRepository;
import com.thiagofarias.pontointeligente.api.utils.PasswordUtil;


@SpringBootApplication
@EnableCaching
public class PontoInteligenteTfApplication {
	
	@Autowired
	private EmpresaRepository empresaRepository;
	
	@Autowired
	private FuncionarioRepository funcionarioRepository;
	
	@Autowired
	private LancamentoRepository lancamentoRepository;

	public static void main(String[] args) {
		SpringApplication.run(PontoInteligenteTfApplication.class, args);
	}
	
	@Component
	public class CommandLineAppStartupRunner implements CommandLineRunner {

		@Override
		public void run(String... args) throws Exception {
			Empresa empresa = new Empresa();
			empresa.setRazaoSocial("Empresa 1");
			empresa.setCnpj("61245817000114");
			empresaRepository.save(empresa);
			
			Funcionario funcionarioAdmin = new Funcionario();
			funcionarioAdmin.setCpf("51516554000");
			funcionarioAdmin.setEmail("admin@admin.com");
			funcionarioAdmin.setNome("Admin");
			funcionarioAdmin.setPerfil(PerfilEnum.ROLE_ADMIN);
			funcionarioAdmin.setSenha(PasswordUtil.gerarBCrypt("123456"));
			funcionarioAdmin.setEmpresa(empresa);
			funcionarioRepository.save(funcionarioAdmin);
			
			Funcionario funcionario = new Funcionario();
			funcionario.setCpf("61418589020");
			funcionario.setEmail("funcionario@func.com");
			funcionario.setNome("Funcionario");
			funcionario.setPerfil(PerfilEnum.ROLE_USUARIO);
			funcionario.setSenha(PasswordUtil.gerarBCrypt("123456"));
			funcionario.setEmpresa(empresa);
			funcionarioRepository.save(funcionario);
			
			empresaRepository.findAll().forEach(System.out::println);
			funcionarioRepository.findByEmpresaId(empresa.getId()).forEach(System.out::println);
			
			gerarLancamentos(funcionario, 20);
		}
		
	}
	
	private void gerarLancamentos(Funcionario funcionario, int numLancamentos) {
		int tipoPos = 0;
		TipoEnum[] tipos = TipoEnum.values();
		
		Lancamento lancamento;
		
		for (int i=0; i<numLancamentos; i++) {
			lancamento = new Lancamento();
			lancamento.setData(new Date());
			lancamento.setTipo(tipos[tipoPos++]);
			lancamento.setLocalizacao("53.4546692, -2.2221622");
			lancamento.setFuncionario(funcionario);
			lancamentoRepository.save(lancamento);
			if(tipoPos == tipos.length) {
				tipoPos = 0;
			}
		}
	}
	

}
