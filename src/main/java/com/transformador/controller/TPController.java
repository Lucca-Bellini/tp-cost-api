package com.transformador.controller;

import com.transformador.dto.EstoqueRequestDto;
import com.transformador.dto.EstoqueResponseDto;
import com.transformador.dto.TPCostRequestDto;
import com.transformador.dto.TPCostResponseDto;
import com.transformador.exception.EstoqueInsuficienteException;
import com.transformador.service.EstoqueService;
import com.transformador.service.TPCostCalculationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tp")
@RequiredArgsConstructor
@Slf4j
public class TPController {

    private final TPCostCalculationService calculationService;
    private final EstoqueService estoqueService;

    @PostMapping("/calculate")
    public ResponseEntity<TPCostResponseDto> calculateCost(@Valid @RequestBody TPCostRequestDto request) {
        log.info("Recebida requisição de cálculo.");
        TPCostResponseDto response = calculationService.calcularCusto(request);
        return ResponseEntity.ok(response);

    }

    @PostMapping("/consume")
    public ResponseEntity<EstoqueResponseDto> consumeStock(@Valid @RequestBody EstoqueRequestDto request) {
        try {
            estoqueService.validarEDebitar(request.getItens());
            return ResponseEntity.ok(new EstoqueResponseDto(true, "Estoque debitado com sucesso"));
        } catch (EstoqueInsuficienteException e) {
            return ResponseEntity.ok(new EstoqueResponseDto(false, e.getMessage()));
        } catch (Exception e) {
            log.error("Erro inesperado no consumo de estoque", e);
            return ResponseEntity.ok(new EstoqueResponseDto(false, "Erro interno: " + e.getMessage()));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("TP Cost API is running");
    }
}