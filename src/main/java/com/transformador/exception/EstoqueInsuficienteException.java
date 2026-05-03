package com.transformador.exception;

import java.util.List;

public class EstoqueInsuficienteException extends RuntimeException {
    private final List<String> erros;

    public EstoqueInsuficienteException(List<String> erros) {
        super("Estoque insuficiente: " + String.join("; ", erros));
        this.erros = erros;
    }

    public List<String> getErros() {
        return erros;
    }
}