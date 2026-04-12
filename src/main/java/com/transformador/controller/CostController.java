package com.transformador.controller;

import com.transformador.dto.TPCostRequestDto;
import com.transformador.dto.TPCostResponseDto;
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
public class CostController {

    private final TPCostCalculationService calculationService;

    @PostMapping("/calculate")
    public ResponseEntity<TPCostResponseDto> calculateCost(@Valid @RequestBody TPCostRequestDto request) {
        log.info("Recebida requisição de cálculo para molde: {}", request.getMolde());
        TPCostResponseDto response = calculationService.calcularCusto(request);
        if (response.isSucesso()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("TP Cost API is running");
    }
}