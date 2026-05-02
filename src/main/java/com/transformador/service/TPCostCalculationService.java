package com.transformador.service;

import com.transformador.dto.*;
import com.transformador.model.*;
import com.transformador.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TPCostCalculationService {

        private final ProdutoRepository produtoRepository;
        private final ProdutoFornecedorRepository produtoFornecedorRepository;
        private final ProdutoCobreRepository produtoCobreRepository;
        private final CarretelRepository carretelRepository;
        private final NucleoRepository nucleoRepository;
        private final EpoxiComposicaoRepository epoxiComposicaoRepository;
        private final PoliuretanoComposicaoRepository poliuretanoComposicaoRepository;

        // Constantes
        private static final BigDecimal PI = new BigDecimal("3.141592653589793");
        private static final BigDecimal DOIS = new BigDecimal("2");
        private static final BigDecimal DEZ = new BigDecimal("10");
        private static final BigDecimal MIL = new BigDecimal("1000");
        private static final BigDecimal MILHAO = new BigDecimal("1000000");
        private static final BigDecimal ESPESSURA_ISOLAMENTO_PRIMARIO = new BigDecimal("0.1"); // mm
        private static final BigDecimal ESPESSURA_ISOLAMENTO_SECUNDARIO = new BigDecimal("0.4"); // mm
        private static final BigDecimal MASSA_ISOLADOR = new BigDecimal("3.0"); // kg
        private static final BigDecimal CUSTO_INDIRETO_FIXO = new BigDecimal("300.00"); // R$
        private static final BigDecimal PRECO_MAO_DE_OBRA_POR_KG = new BigDecimal("25.00"); // R$
        private static final BigDecimal LARGURA_ROLO_BORRACHA_M = new BigDecimal("1.5"); // m
        private static final BigDecimal PAPEL_KG_POR_CAMADA = new BigDecimal("0.007"); // kg
        private static final BigDecimal VERNIZ_KG_POR_CAMADA = new BigDecimal("0.0005"); // kg
        private static final BigDecimal COLA_KG_POR_CAMADA = new BigDecimal("0.0005"); // kg

        @Transactional(readOnly = true)
        public TPCostResponseDto calcularCusto(TPCostRequestDto request) {
                try {
                        // 1. Carretel e Núcleo
                        Carretel carretel = carretelRepository.findByTamanho(request.getCarretel())
                                        .orElseThrow(
                                                        () -> new RuntimeException(
                                                                        "Carretel não encontrado para o tamanho: "
                                                                                        + request.getCarretel()));
                        String tipoNucleo = request.getTensaoMaxima() + "kV "
                                        + (request.getTipoLigacao().equals("Fase-Terra") ? "FT" : "FF");
                        Nucleo nucleo = nucleoRepository.findByTipo(tipoNucleo)
                                        .orElseThrow(() -> new RuntimeException(
                                                        "Núcleo não encontrado para o tipo: " + tipoNucleo));

                        // 2. Fios (primário e secundário) - retorna um objeto com custo, massa,
                        // camadas, raio médio
                        FioResult fioPrimario = calcularFioPrimario(request, carretel);
                        FioResult fioSecundario = calcularFioSecundario(request, carretel);

                        // 3. Borracha
                        BigDecimal[] borracha = calcularCustoEMassaBorracha(carretel, fioSecundario.getRaioMedioMm(),
                                        fioSecundario.getNumeroCamadas());

                        // 4. Papel, verniz, cola
                        BigDecimal[] papelVernizCola = calcularCustoEMassaPapelVernizCola(
                                        fioPrimario.getNumeroCamadas(),
                                        fioSecundario.getNumeroCamadas());

                        // 5. Cano
                        BigDecimal[] cano = calcularCustoEMassaCano(carretel);

                        // 6. Núcleo + cinta
                        BigDecimal custoNucleoKg = produtoFornecedorRepository.findLowestPriceByProdutoCodigo("INS-049")
                                        .orElse(BigDecimal.ZERO);
                        BigDecimal custoNucleo = nucleo.getMassaKg().multiply(custoNucleoKg);
                        BigDecimal[] cintaNucleo = calcularCustoEMassaCintaNucleo(nucleo);
                        BigDecimal massaNucleoTotal = nucleo.getMassaKg().add(cintaNucleo[1]);
                        BigDecimal custoNucleoTotal = custoNucleo.add(cintaNucleo[0]);

                        // 7. Chapa de latão
                        BigDecimal precoChapaKg = produtoFornecedorRepository.findLowestPriceByProdutoCodigo("INS-048")
                                        .orElse(BigDecimal.ZERO);
                        BigDecimal custoChapa = carretel.getFitaLataoMassaKg().multiply(precoChapaKg);
                        BigDecimal massaChapa = carretel.getFitaLataoMassaKg();

                        // 8. Acolchoamento poliuretano
                        BigDecimal[] poliuretano = calcularCustoEMassaPoliuretano();

                        // 9. Resina epóxi
                        BigDecimal[] epoxi = calcularCustoEMassaEpoxi(carretel, request);
                        BigDecimal custoEpoxi = epoxi[0];
                        BigDecimal massaEpoxi = epoxi[1];

                        // 10. Insumos simples
                        BigDecimal custoInsumos = calcularCustoInsumos(request.getInsumos());
                        BigDecimal massaInsumos = calcularMassaInsumos(request.getInsumos());

                        // 11. Totais
                        BigDecimal custoTotalMateriais = fioPrimario.getCusto()
                                        .add(fioSecundario.getCusto())
                                        .add(borracha[0])
                                        .add(papelVernizCola[0])
                                        .add(cano[0])
                                        .add(custoNucleoTotal)
                                        .add(custoChapa)
                                        .add(poliuretano[0])
                                        .add(custoEpoxi)
                                        .add(custoInsumos);

                        BigDecimal massaTotal = fioPrimario.getMassa()
                                        .add(fioSecundario.getMassa())
                                        .add(borracha[1])
                                        .add(papelVernizCola[1])
                                        .add(cano[1])
                                        .add(massaNucleoTotal)
                                        .add(massaChapa)
                                        .add(poliuretano[1])
                                        .add(massaEpoxi)
                                        .add(massaInsumos);

                        BigDecimal custoMaoDeObra = massaTotal.multiply(PRECO_MAO_DE_OBRA_POR_KG);
                        BigDecimal custoTotal = custoTotalMateriais.add(custoMaoDeObra).add(CUSTO_INDIRETO_FIXO);

                        return TPCostResponseDto.builder()
                                        .sucesso(true)
                                        .mensagem("Custo de fabricação calculado com sucesso")
                                        .custoTotal(custoTotal)
                                        .build();
                } catch (Exception e) {
                        log.error("Erro no cálculo", e);
                        return TPCostResponseDto.builder()
                                        .sucesso(false)
                                        .mensagem("Erro no cálculo: " + e.getMessage())
                                        .build();
                }
        }

        private FioResult calcularCustoFio(ProdutoCobre fio, Integer espirasTotal,
                        BigDecimal circunferenciaMm, BigDecimal larguraEspiraMm,
                        boolean isPrimario) {
                // Raio inicial (mm)
                BigDecimal raioInicial = circunferenciaMm.divide(DOIS.multiply(PI), 6, RoundingMode.HALF_UP);

                // Espiras por camada
                BigDecimal espirasPorCamada = larguraEspiraMm.multiply(fio.getEspirasPorCm())
                                .divide(DEZ, 2, RoundingMode.HALF_UP);

                // Para primário, dividir espiras em duas metades
                BigDecimal espirasPorMetade = isPrimario
                                ? BigDecimal.valueOf(espirasTotal).divide(DOIS, 0, RoundingMode.CEILING)
                                : BigDecimal.valueOf(espirasTotal);

                BigDecimal numeroCamadas = espirasPorMetade.divide(espirasPorCamada, 0, RoundingMode.CEILING);

                // Crescimento radial (Δr)
                BigDecimal deltaR = numeroCamadas.multiply(fio.getDiametro());

                // Espessura total do isolamento
                BigDecimal espessuraIsolamentoPorCamada = isPrimario
                                ? ESPESSURA_ISOLAMENTO_PRIMARIO
                                : ESPESSURA_ISOLAMENTO_SECUNDARIO;
                BigDecimal espessuraIsolamentoTotal = numeroCamadas.multiply(espessuraIsolamentoPorCamada);

                // Raio médio da espira
                BigDecimal raioMedio = raioInicial
                                .add(deltaR.divide(DOIS, 6, RoundingMode.HALF_UP))
                                .add(espessuraIsolamentoTotal.divide(DOIS, 6, RoundingMode.HALF_UP));

                // Metragem de fio por espira (mm)
                BigDecimal mmPorEspira = DOIS.multiply(PI).multiply(raioMedio);

                // Km de fio
                BigDecimal kmFio = mmPorEspira.multiply(BigDecimal.valueOf(espirasTotal))
                                .divide(MILHAO, 6, RoundingMode.HALF_UP);

                // Massa (kg)
                BigDecimal massaFio = kmFio.multiply(fio.getKgPorKm());

                // Custo (menor preço do fio por kg)
                BigDecimal precoPorKg = produtoFornecedorRepository
                                .findLowestPriceByProdutoCodigo(fio.getCodigoProduto())
                                .orElseThrow(() -> new RuntimeException(
                                                "Preço não encontrado para fio: " + fio.getCodigoProduto()));

                BigDecimal custo = massaFio.multiply(precoPorKg);

                return FioResult.builder()
                                .custo(custo)
                                .massa(massaFio)
                                .numeroCamadas(numeroCamadas)
                                .raioMedioMm(raioMedio)
                                .build();
        }

        private BigDecimal calcularCustoInsumos(List<InsumoRequestDto> insumos) {
                if (insumos == null || insumos.isEmpty()) {
                        return BigDecimal.ZERO;
                }
                BigDecimal total = BigDecimal.ZERO;
                for (InsumoRequestDto item : insumos) {
                        BigDecimal precoUnitario = produtoFornecedorRepository
                                        .findLowestPriceByProdutoCodigo(item.getIdInsumo())
                                        .orElseThrow(() -> new RuntimeException(
                                                        "Preço não encontrado para insumo: " + item.getIdInsumo()));
                        BigDecimal custoItem = precoUnitario.multiply(item.getQuantidade());
                        total = total.add(custoItem);
                }
                return total;
        }

        private BigDecimal[] calcularCustoEMassaEpoxi(Carretel carretel, TPCostRequestDto request) {
                BigDecimal massaEpoxiCarretel = carretel.getEpoxiCarretelKg() != null ? carretel.getEpoxiCarretelKg()
                                : BigDecimal.ZERO;
                BigDecimal massaEpoxiCapa = carretel.getEpoxiCapaKg() != null ? carretel.getEpoxiCapaKg()
                                : BigDecimal.ZERO;
                BigDecimal massaEncapsulamento = request.getMassaEncapsulamentoMolde() != null
                                ? request.getMassaEncapsulamentoMolde()
                                : BigDecimal.ZERO;
                BigDecimal massaIsoladores = BigDecimal.valueOf(request.getNumeroIsoladores()).multiply(MASSA_ISOLADOR);

                BigDecimal massaTotalResina = massaEpoxiCarretel.add(massaEpoxiCapa).add(massaEncapsulamento)
                                .add(massaIsoladores);

                List<EpoxiComposicao> composicoes = epoxiComposicaoRepository.findAll();
                BigDecimal custoTotal = BigDecimal.ZERO;
                for (EpoxiComposicao comp : composicoes) {
                        BigDecimal massaComponente = massaTotalResina.multiply(comp.getFator());
                        BigDecimal precoComponente = produtoFornecedorRepository
                                        .findLowestPriceByProdutoCodigo(comp.getMaterial().getCodigo())
                                        .orElseThrow(() -> new RuntimeException(
                                                        "Preço não encontrado para componente: "
                                                                        + comp.getComponente()));
                        custoTotal = custoTotal.add(massaComponente.multiply(precoComponente));
                }
                return new BigDecimal[] { custoTotal, massaTotalResina };
        }

        private FioResult calcularFioPrimario(TPCostRequestDto request, Carretel carretel) {
                ProdutoCobre fio = produtoCobreRepository.findByAwg(request.getAwgPrimario())
                                .orElseThrow(() -> new RuntimeException(
                                                "Fio AWG " + request.getAwgPrimario() + " não encontrado"));
                BigDecimal circunferencia = carretel.getCircunferenciaMm();
                BigDecimal larguraEspira = carretel.getPrimarioLarguraEspiraMm();
                return calcularCustoFio(fio, request.getEspirasPrimario(), circunferencia, larguraEspira, true);
        }

        private FioResult calcularFioSecundario(TPCostRequestDto request, Carretel carretel) {
                ProdutoCobre fio = produtoCobreRepository.findByAwg(request.getAwgSecundario())
                                .orElseThrow(() -> new RuntimeException(
                                                "Fio AWG " + request.getAwgSecundario() + " não encontrado"));
                BigDecimal circunferencia = carretel.getCanoCircunferenciaMm();
                BigDecimal larguraEspira = carretel.getSecundarioLarguraEspiraMm();
                return calcularCustoFio(fio, request.getEspirasSecundario(), circunferencia, larguraEspira, false);
        }

        private BigDecimal[] calcularCustoEMassaBorracha(Carretel carretel, BigDecimal raioMedioSecundarioMm,
                        BigDecimal numeroCamadasSecundario) {
                // raioMedioSecundarioMm vem do cálculo do fio secundário
                BigDecimal circunferenciaMediaMm = DOIS.multiply(PI).multiply(raioMedioSecundarioMm);
                // Área da fita = largura_espira (mm) * circunferencia_media (mm) -> mm²
                BigDecimal larguraEspiraMm = carretel.getSecundarioLarguraEspiraMm();
                BigDecimal areaFitaMm2 = larguraEspiraMm.multiply(circunferenciaMediaMm);
                // Converter para m² (1 m² = 1.000.000 mm²)
                BigDecimal areaFitaM2 = areaFitaMm2.divide(MILHAO, 6, RoundingMode.HALF_UP);
                // 1 metro linear do rolo tem 1,5 m² (largura fixa)
                BigDecimal areaRoloPorMetroLinear = LARGURA_ROLO_BORRACHA_M; // 1,5 m²
                BigDecimal fracaoPorFita = areaFitaM2.divide(areaRoloPorMetroLinear, 6, RoundingMode.HALF_UP);
                // Preço por metro linear do produto BORRACHA COLUMBIA (INS-064)
                BigDecimal precoMetroBorracha = produtoFornecedorRepository.findLowestPriceByProdutoCodigo("INS-064")
                                .orElseThrow(() -> new RuntimeException("Preço da borracha não encontrado"));
                BigDecimal custoPorFita = fracaoPorFita.multiply(precoMetroBorracha);
                BigDecimal custoTotalBorracha = custoPorFita.multiply(numeroCamadasSecundario);
                // Massa: podemos obter a massa por metro linear da borracha (se cadastrado em
                // produto.massa_por_unidade)
                BigDecimal massaPorMetroBorracha = produtoRepository.findByCodigo("INS-064")
                                .map(Produto::getMassaPorUnidade)
                                .orElse(BigDecimal.ZERO);
                BigDecimal massaTotalBorracha = fracaoPorFita.multiply(massaPorMetroBorracha)
                                .multiply(numeroCamadasSecundario);
                return new BigDecimal[] { custoTotalBorracha, massaTotalBorracha };
        }

        private BigDecimal[] calcularCustoEMassaPapelVernizCola(BigDecimal numeroCamadasPrimario,
                        BigDecimal numeroCamadasSecundario) {
                BigDecimal totalCamadas = numeroCamadasPrimario.add(numeroCamadasSecundario);
                BigDecimal massaPapel = totalCamadas.multiply(PAPEL_KG_POR_CAMADA);
                BigDecimal massaVerniz = totalCamadas.multiply(VERNIZ_KG_POR_CAMADA);
                BigDecimal massaCola = totalCamadas.multiply(COLA_KG_POR_CAMADA);
                BigDecimal massaTotal = massaPapel.add(massaVerniz).add(massaCola);

                BigDecimal precoPapelKg = produtoFornecedorRepository.findLowestPriceByProdutoCodigo("INS-060")
                                .orElse(BigDecimal.ZERO);
                BigDecimal precoVernizKg = produtoFornecedorRepository.findLowestPriceByProdutoCodigo("INS-050")
                                .orElse(BigDecimal.ZERO);
                BigDecimal precoColaKg = produtoFornecedorRepository.findLowestPriceByProdutoCodigo("INS-051")
                                .orElse(BigDecimal.ZERO);

                BigDecimal custoPapel = massaPapel.multiply(precoPapelKg);
                BigDecimal custoVerniz = massaVerniz.multiply(precoVernizKg);
                BigDecimal custoCola = massaCola.multiply(precoColaKg);
                BigDecimal custoTotal = custoPapel.add(custoVerniz).add(custoCola);

                return new BigDecimal[] { custoTotal, massaTotal };
        }

        private BigDecimal[] calcularCustoEMassaPoliuretano() {
                List<PoliuretanoComposicao> composicoes = poliuretanoComposicaoRepository.findAll();
                BigDecimal custoTotal = BigDecimal.ZERO;
                BigDecimal massaTotal = BigDecimal.ZERO;
                for (PoliuretanoComposicao comp : composicoes) {
                        BigDecimal precoKg = produtoFornecedorRepository
                                        .findLowestPriceByProdutoCodigo(comp.getMaterial().getCodigo())
                                        .orElseThrow(() -> new RuntimeException(
                                                        "Preço do componente " + comp.getComponente()
                                                                        + " não encontrado"));
                        custoTotal = custoTotal.add(comp.getQuantidadeKg().multiply(precoKg));
                        massaTotal = massaTotal.add(comp.getQuantidadeKg());
                }
                return new BigDecimal[] { custoTotal, massaTotal };
        }

        private BigDecimal[] calcularCustoEMassaCano(Carretel carretel) {
                BigDecimal alturaMetros = carretel.getCanoAlturaMm().divide(MIL, 3, RoundingMode.HALF_UP);
                BigDecimal precoMetroCano = produtoFornecedorRepository.findLowestPriceByProdutoCodigo("INS-053")
                                .orElseThrow(() -> new RuntimeException("Preço do cano não encontrado"));
                BigDecimal custoCano = alturaMetros.multiply(precoMetroCano);
                BigDecimal massaCano = carretel.getCanoMassaKg() != null ? carretel.getCanoMassaKg() : BigDecimal.ZERO;
                return new BigDecimal[] { custoCano, massaCano };
        }

        private BigDecimal[] calcularCustoEMassaCintaNucleo(Nucleo nucleo) {
                BigDecimal metrosCinta = nucleo.getCintaMetro();
                BigDecimal precoMetroCinta = produtoFornecedorRepository.findLowestPriceByProdutoCodigo("INS-057")
                                .orElseThrow(() -> new RuntimeException("Preço da cinta não encontrado"));
                BigDecimal custoCinta = metrosCinta.multiply(precoMetroCinta);
                BigDecimal massaCinta = nucleo.getCintaMassaKg() != null ? nucleo.getCintaMassaKg() : BigDecimal.ZERO;
                return new BigDecimal[] { custoCinta, massaCinta };
        }

        private BigDecimal calcularMassaInsumos(List<InsumoRequestDto> insumos) {
                if (insumos == null)
                        return BigDecimal.ZERO;
                BigDecimal massaTotal = BigDecimal.ZERO;
                for (InsumoRequestDto item : insumos) {
                        Produto produto = produtoRepository.findByCodigo(item.getIdInsumo())
                                        .orElseThrow(() -> new RuntimeException(
                                                        "Produto não encontrado: " + item.getIdInsumo()));
                        if (produto.getMassaPorUnidade() != null) {
                                massaTotal = massaTotal
                                                .add(produto.getMassaPorUnidade().multiply(item.getQuantidade()));
                        }
                }
                return massaTotal;
        }
}