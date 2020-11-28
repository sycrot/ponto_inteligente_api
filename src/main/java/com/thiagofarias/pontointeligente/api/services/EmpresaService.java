package com.thiagofarias.pontointeligente.api.services;

import java.util.Optional;

import com.thiagofarias.pontointeligente.api.entities.Empresa;

public interface EmpresaService {
	
	/**
	 * 
	 * @param cnpj
	 * @return Optional<Empresa>
	 */
	Optional<Empresa> buscarPorCnpj(String cnpj);
	
	/**
	 * 
	 * @param empresa
	 * @return Empresa
	 */
	Empresa persistir(Empresa empresa);

}
