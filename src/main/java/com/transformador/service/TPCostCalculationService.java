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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
        // Numéricas
        private static final BigDecimal PI = new BigDecimal("3.141592653589793");
        private static final BigDecimal DOIS = new BigDecimal("2");
        private static final BigDecimal DEZ = new BigDecimal("10");
        private static final BigDecimal MIL = new BigDecimal("1000");
        private static final BigDecimal MILHAO = new BigDecimal("1000000");
        // informadas pelo engenheiro
        private static final BigDecimal ESPESSURA_ISOLAMENTO_PRIMARIO = new BigDecimal("0.1"); // mm
        private static final BigDecimal ESPESSURA_ISOLAMENTO_SECUNDARIO = new BigDecimal("0.4"); // mm
        private static final BigDecimal MASSA_ISOLADOR = new BigDecimal("3.0"); // kg
        private static final BigDecimal PAPEL_KG_POR_CAMADA = new BigDecimal("0.007"); // kg
        private static final BigDecimal VERNIZ_KG_POR_CAMADA = new BigDecimal("0.0005"); // kg
        private static final BigDecimal COLA_KG_POR_CAMADA = new BigDecimal("0.0005"); // kg
        // simplificação à pedido da empresa
        private static final BigDecimal CUSTO_INDIRETO_FIXO = new BigDecimal("300.00"); // R$
        private static final BigDecimal PRECO_MAO_DE_OBRA_POR_KG = new BigDecimal("25.00"); // R$

        @Transactional(readOnly = true)
        public TPCostResponseDto calcularCusto(TPCostRequestDto request) {
                try {

                        List<MaterialConsumo> todosConsumos = new ArrayList<>();

                        // 1. Fios (primário e secundário) - retorna um objeto com custo, massa,
                        // camadas, raio médio
                        Carretel carretel = carretelRepository.findByTamanho(request.getCarretel())
                                        .orElseThrow(
                                                        () -> new RuntimeException(
                                                                        "Carretel não encontrado para o tamanho: "
                                                                                        + request.getCarretel()));
                        FioResult fioPrimario = calcularFioPrimario(request, carretel, todosConsumos);
                        FioResult fioSecundario = calcularFioSecundario(request, carretel, todosConsumos);

                        // 2. Borracha
                        BigDecimal[] borracha = calcularBorracha(carretel, fioSecundario.getRaioMedioMm(),
                                        fioSecundario.getNumeroCamadas(), todosConsumos);
                        BigDecimal custoBorracha = borracha[0];
                        BigDecimal massaBorracha = borracha[1];

                        // 3. Papel, verniz, cola
                        BigDecimal[] papelVernizCola = calcularPapelVernizCola(fioPrimario.getNumeroCamadas(),
                                        fioSecundario.getNumeroCamadas(), todosConsumos);
                        BigDecimal custoPapelVernizCola = papelVernizCola[0];
                        BigDecimal massaPapelVernizCola = papelVernizCola[1];

                        // 4. Cano
                        BigDecimal[] cano = calcularCano(carretel, todosConsumos);
                        BigDecimal custoCano = cano[0];
                        BigDecimal massaCano = cano[1];

                        // 5. Núcleo + cinta
                        String tipoNucleo = request.getTensaoMaxima() + "kV "
                                        + (request.getTipoLigacao().equals("Fase-Terra") ? "FT" : "FF");
                        BigDecimal[] nucleoECinta = calcularNucleoECinta(tipoNucleo, todosConsumos);
                        BigDecimal custoNucleoECinta = nucleoECinta[0];
                        BigDecimal massaNucleoECinta = nucleoECinta[1];

                        // 6. Chapa de latão
                        BigDecimal[] chapaLatao = calcularChapaLatao(carretel, todosConsumos);
                        BigDecimal custoChapa = chapaLatao[0];
                        BigDecimal massaChapa = chapaLatao[1];

                        // 7. Acolchoamento poliuretano
                        BigDecimal[] poliuretano = calcularPoliuretano(todosConsumos);
                        BigDecimal custoPoliuretano = poliuretano[0];
                        BigDecimal massaPoliuretano = poliuretano[1];

                        // 8. Resina epóxi
                        BigDecimal[] epoxi = calcularEpoxi(carretel, request, todosConsumos);
                        BigDecimal custoEpoxi = epoxi[0];
                        BigDecimal massaEpoxi = epoxi[1];

                        // 9. Insumos Simples
                        List<MaterialConsumo> consumosInsumosSimples = calcularCustoInsumos(request.getInsumos());
                        BigDecimal custoInsumos = consumosInsumosSimples.stream()
                                        .map(MaterialConsumo::getCusto)
                                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                        BigDecimal massaInsumos = consumosInsumosSimples.stream()
                                        .map(MaterialConsumo::getMassaKg)
                                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                        // 10. Unir insumos Simples e Específicos
                        // Unificar insumos presentes em multiplas etapas
                        todosConsumos.addAll(consumosInsumosSimples);
                        Map<String, MaterialConsumo> mapaConsumos = new LinkedHashMap<>();
                        for (MaterialConsumo item : todosConsumos) {
                                MaterialConsumo existente = mapaConsumos.get(item.getCodigo());
                                if (existente == null) {
                                        mapaConsumos.put(item.getCodigo(), MaterialConsumo.builder()
                                                        .codigo(item.getCodigo())
                                                        .nome(item.getNome())
                                                        .quantidade(item.getQuantidade())
                                                        .massaKg(item.getMassaKg() != null ? item.getMassaKg()
                                                                        : BigDecimal.ZERO)
                                                        .custo(item.getCusto())
                                                        .build());
                                } else {
                                        existente.setQuantidade(existente.getQuantidade().add(item.getQuantidade()));
                                        existente.setMassaKg(existente.getMassaKg()
                                                        .add(item.getMassaKg() != null ? item.getMassaKg()
                                                                        : BigDecimal.ZERO));
                                        existente.setCusto(existente.getCusto().add(item.getCusto()));
                                }
                        }
                        List<MaterialConsumo> consumosUnificados = new ArrayList<>(mapaConsumos.values());

                        // 11. Totais
                        BigDecimal custoTotalMateriais = fioPrimario.getCusto()
                                        .add(fioSecundario.getCusto())
                                        .add(custoBorracha)
                                        .add(custoPapelVernizCola)
                                        .add(custoCano)
                                        .add(custoNucleoECinta)
                                        .add(custoChapa)
                                        .add(custoPoliuretano)
                                        .add(custoEpoxi)
                                        .add(custoInsumos);

                        BigDecimal massaTotal = fioPrimario.getMassa()
                                        .add(fioSecundario.getMassa())
                                        .add(massaBorracha)
                                        .add(massaPapelVernizCola)
                                        .add(massaCano)
                                        .add(massaNucleoECinta)
                                        .add(massaChapa)
                                        .add(massaPoliuretano)
                                        .add(massaEpoxi)
                                        .add(massaInsumos);

                        BigDecimal custoMaoDeObra = massaTotal.multiply(PRECO_MAO_DE_OBRA_POR_KG);
                        BigDecimal custoTotal = custoTotalMateriais.add(custoMaoDeObra).add(CUSTO_INDIRETO_FIXO);

                        // 12. Construir Resposta
                        return TPCostResponseDto.builder()
                                        .sucesso(true)
                                        .mensagem("Custo de fabricação calculado com sucesso")
                                        .custoTotal(custoTotal)
                                        .custoMaoDeObra(custoMaoDeObra)
                                        .detalhamentoMateriais(consumosUnificados)
                                        .build();
                } catch (Exception e) {
                        log.error("Erro no cálculo", e);
                        return TPCostResponseDto.builder()
                                        .sucesso(false)
                                        .mensagem("Erro no cálculo: " + e.getMessage())
                                        .build();
                }
        }

        // Aux Global
        // Busca nome, massaPorUnidade e calcula massaKg
        // Adiciona à lista de consumo
        private MaterialConsumo materialConsumoFromProduto(String codigo, BigDecimal quantidade, BigDecimal custo) {
                Produto produto = produtoRepository.findByCodigo(codigo)
                                .orElseThrow(() -> new RuntimeException(
                                                "Produto não encontrado para código: " + codigo));
                BigDecimal massaPorUnidade = produtoRepository.findByCodigo(codigo)
                                .map(Produto::getMassaPorUnidade)
                                .orElseThrow(() -> new RuntimeException(
                                                "Massa por unidade não encontrado para o insumo:" + codigo));
                BigDecimal massa = quantidade.multiply(massaPorUnidade);
                return MaterialConsumo.builder()
                                .codigo(codigo)
                                .nome(produto.getNome())
                                .quantidade(quantidade)
                                .massaKg(massa)
                                .custo(custo)
                                .build();
        }

        // 1.0 Aux para Fios
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

        // 1.1 Fio primário
        private FioResult calcularFioPrimario(TPCostRequestDto request, Carretel carretel,
                        List<MaterialConsumo> listaDestino) {
                ProdutoCobre fio = produtoCobreRepository.findByAwg(request.getAwgPrimario())
                                .orElseThrow(() -> new RuntimeException(
                                                "Fio AWG " + request.getAwgPrimario() + " não encontrado"));
                BigDecimal circunferencia = carretel.getCircunferenciaMm();
                BigDecimal larguraEspira = carretel.getPrimarioLarguraEspiraMm();
                FioResult resultado = calcularCustoFio(fio, request.getEspirasPrimario(), circunferencia, larguraEspira,
                                true);

                MaterialConsumo consumo = materialConsumoFromProduto(fio.getCodigoProduto(), resultado.getMassa(),
                                resultado.getCusto());
                listaDestino.add(consumo);

                return resultado;
        }

        // 1.2 Fio secundario
        private FioResult calcularFioSecundario(TPCostRequestDto request, Carretel carretel,
                        List<MaterialConsumo> listaDestino) {
                ProdutoCobre fio = produtoCobreRepository.findByAwg(request.getAwgSecundario())
                                .orElseThrow(() -> new RuntimeException(
                                                "Fio AWG " + request.getAwgSecundario() + " não encontrado"));
                BigDecimal circunferencia = carretel.getCanoCircunferenciaMm();
                BigDecimal larguraEspira = carretel.getSecundarioLarguraEspiraMm();
                FioResult resultado = calcularCustoFio(fio, request.getEspirasSecundario(), circunferencia,
                                larguraEspira, false);

                MaterialConsumo consumo = materialConsumoFromProduto(fio.getCodigoProduto(), resultado.getMassa(),
                                resultado.getCusto());
                listaDestino.add(consumo);

                return resultado;
        }

        // 2. Borracha
        private BigDecimal[] calcularBorracha(Carretel carretel, BigDecimal raioMedioMm,
                        BigDecimal numeroCamadas, List<MaterialConsumo> listaDestino) {
                // Largura da espira secundária (mm)
                BigDecimal larguraEspiraMm = carretel.getSecundarioLarguraEspiraMm();
                // Circunferência média (mm) = 2 * PI * raioMedioMm
                BigDecimal circunferenciaMediaMm = DOIS.multiply(PI).multiply(raioMedioMm);

                // Área de uma fita (m²) = (largura_m) * (circunferencia_m)
                BigDecimal larguraM = larguraEspiraMm.divide(MIL, 6, RoundingMode.HALF_UP);
                BigDecimal circunferenciaM = circunferenciaMediaMm.divide(MIL, 6, RoundingMode.HALF_UP);
                BigDecimal areaFitaM2 = larguraM.multiply(circunferenciaM);

                // Área total = areaFitaM2 * (numeroCamadas + 1)
                BigDecimal areaTotalM2 = areaFitaM2.multiply(numeroCamadas.add(BigDecimal.ONE));

                // Preço por m² (busca do menor preço ativo)
                BigDecimal precoM2 = produtoFornecedorRepository.findLowestPriceByProdutoCodigo("INS-052")
                                .orElseThrow(() -> new RuntimeException("Preço da borracha (INS-052) não encontrado"));

                BigDecimal custoTotal = areaTotalM2.multiply(precoM2);

                // Adiciona à lista de consumo (para o JSON de resposta)
                MaterialConsumo consumo = materialConsumoFromProduto("INS-052", areaTotalM2, custoTotal);
                listaDestino.add(consumo);

                return new BigDecimal[] { custoTotal, consumo.getMassaKg() };
        }

        // 3. Papel, Verniz e Cola
        private BigDecimal[] calcularPapelVernizCola(BigDecimal numeroCamadasPrimario,
                        BigDecimal numeroCamadasSecundario,
                        List<MaterialConsumo> listaDestino) {
                BigDecimal totalCamadas = numeroCamadasPrimario.add(numeroCamadasSecundario);

                // --- Papel (INS-060) ---
                BigDecimal massaPapel = totalCamadas.multiply(PAPEL_KG_POR_CAMADA); // 0.007 kg/camada
                BigDecimal precoPapelKg = produtoFornecedorRepository.findLowestPriceByProdutoCodigo("INS-060")
                                .orElseThrow(() -> new RuntimeException("Preço do papel (INS-060) não encontrado"));
                BigDecimal custoPapel = massaPapel.multiply(precoPapelKg);

                // --- Verniz (INS-050) ---
                BigDecimal massaVerniz = totalCamadas.multiply(VERNIZ_KG_POR_CAMADA); // 0.0005 kg/camada
                BigDecimal precoVernizKg = produtoFornecedorRepository.findLowestPriceByProdutoCodigo("INS-050")
                                .orElseThrow(() -> new RuntimeException("Preço do verniz (INS-050) não encontrado"));
                BigDecimal custoVerniz = massaVerniz.multiply(precoVernizKg);

                // --- Cola (INS-051) ---
                BigDecimal massaCola = totalCamadas.multiply(COLA_KG_POR_CAMADA); // 0.0005 kg/camada
                BigDecimal precoColaKg = produtoFornecedorRepository.findLowestPriceByProdutoCodigo("INS-051")
                                .orElseThrow(() -> new RuntimeException("Preço da cola (INS-051) não encontrado"));
                BigDecimal custoCola = massaCola.multiply(precoColaKg);

                // Adiciona cada um à lista de consumo
                listaDestino.add(materialConsumoFromProduto("INS-060", massaPapel, custoPapel));
                listaDestino.add(materialConsumoFromProduto("INS-050", massaVerniz, custoVerniz));
                listaDestino.add(materialConsumoFromProduto("INS-051", massaCola, custoCola));

                BigDecimal custoTotal = custoPapel.add(custoVerniz).add(custoCola);
                BigDecimal massaTotal = massaPapel.add(massaVerniz).add(massaCola);
                return new BigDecimal[] { custoTotal, massaTotal };
        }

        // 4. Cano
        private BigDecimal[] calcularCano(Carretel carretel, List<MaterialConsumo> listaDestino) {
                // Altura do cano em metros
                BigDecimal alturaMetros = carretel.getCanoAlturaMm().divide(MIL, 3, RoundingMode.HALF_UP);

                // Preço por metro
                BigDecimal precoMetro = produtoFornecedorRepository.findLowestPriceByProdutoCodigo("INS-053")
                                .orElseThrow(() -> new RuntimeException("Preço do cano (INS-053) não encontrado"));

                BigDecimal custo = alturaMetros.multiply(precoMetro);

                MaterialConsumo consumo = materialConsumoFromProduto("INS-053", alturaMetros, custo);
                listaDestino.add(consumo);

                return new BigDecimal[] { custo, consumo.getMassaKg() };
        }

        // 5. Núcleo e Cinta
        private BigDecimal[] calcularNucleoECinta(String tipoNucleo, List<MaterialConsumo> listaDestino) {
                Nucleo nucleo = nucleoRepository.findByTipo(tipoNucleo)
                                .orElseThrow(() -> new RuntimeException("Núcleo não encontrado: " + tipoNucleo));

                // 1. Núcleo (INS-049)
                BigDecimal precoNucleoKg = produtoFornecedorRepository.findLowestPriceByProdutoCodigo("INS-049")
                                .orElseThrow(() -> new RuntimeException("Preço do núcleo (INS-049) não encontrado"));
                BigDecimal custoNucleo = nucleo.getMassaKg().multiply(precoNucleoKg);
                MaterialConsumo consumoNucleo = materialConsumoFromProduto("INS-049", nucleo.getMassaKg(), custoNucleo);
                listaDestino.add(consumoNucleo);

                // 2. Cinta (INS-057)
                BigDecimal precoCintaMetro = produtoFornecedorRepository.findLowestPriceByProdutoCodigo("INS-057")
                                .orElseThrow(() -> new RuntimeException("Preço da cinta (INS-057) não encontrado"));
                BigDecimal custoCinta = nucleo.getCintaMetro().multiply(precoCintaMetro);
                MaterialConsumo consumoCinta = materialConsumoFromProduto("INS-057", nucleo.getCintaMetro(),
                                custoCinta);
                listaDestino.add(consumoCinta);

                BigDecimal custoTotal = custoNucleo.add(custoCinta);
                BigDecimal massaTotal = nucleo.getMassaKg().add(nucleo.getCintaMassaKg());
                return new BigDecimal[] { custoTotal, massaTotal };
        }

        // 6. Chapa de Latão
        private BigDecimal[] calcularChapaLatao(Carretel carretel, List<MaterialConsumo> listaDestino) {
                BigDecimal massa = carretel.getFitaLataoMassaKg();
                if (massa == null || massa.compareTo(BigDecimal.ZERO) == 0) {
                        throw new RuntimeException("Massa da chapa de latão não definida ou inválida para carretel: "
                                        + carretel.getTamanho());
                }

                BigDecimal precoKg = produtoFornecedorRepository.findLowestPriceByProdutoCodigo("INS-048")
                                .orElseThrow(() -> new RuntimeException(
                                                "Preço da chapa de latão (INS-048) não encontrado"));
                BigDecimal custo = massa.multiply(precoKg);

                MaterialConsumo consumo = materialConsumoFromProduto("INS-048", massa, custo);
                listaDestino.add(consumo);

                return new BigDecimal[] { custo, massa };
        }

        // 7. Acolchoamento Poliuretano
        private BigDecimal[] calcularPoliuretano(List<MaterialConsumo> listaDestino) {
                List<PoliuretanoComposicao> composicoes = poliuretanoComposicaoRepository.findAll();
                BigDecimal custoTotal = BigDecimal.ZERO;
                BigDecimal massaTotal = BigDecimal.ZERO;

                for (PoliuretanoComposicao comp : composicoes) {
                        BigDecimal quantidadeKg = comp.getQuantidadeKg();
                        BigDecimal precoKg = produtoFornecedorRepository
                                        .findLowestPriceByProdutoCodigo(comp.getMaterial().getCodigo())
                                        .orElseThrow(() -> new RuntimeException(
                                                        "Preço não encontrado para insumo: " + comp.getComponente()));
                        BigDecimal custo = quantidadeKg.multiply(precoKg);

                        MaterialConsumo consumo = materialConsumoFromProduto(comp.getMaterial().getCodigo(),
                                        quantidadeKg, custo);
                        listaDestino.add(consumo);

                        custoTotal = custoTotal.add(custo);
                        massaTotal = massaTotal.add(quantidadeKg);
                }
                return new BigDecimal[] { custoTotal, massaTotal };
        }

        // 8. Resina Epóxi
        private BigDecimal[] calcularEpoxi(Carretel carretel, TPCostRequestDto request,
                        List<MaterialConsumo> listaDestino) {
                // Massas da resina
                BigDecimal massaEpoxiCarretel = carretel.getEpoxiCarretelKg() != null ? carretel.getEpoxiCarretelKg()
                                : BigDecimal.ZERO;
                BigDecimal massaEpoxiCapa = carretel.getEpoxiCapaKg() != null ? carretel.getEpoxiCapaKg()
                                : BigDecimal.ZERO;
                BigDecimal massaEncapsulamento = request.getMassaEncapsulamentoMolde() != null
                                ? request.getMassaEncapsulamentoMolde()
                                : BigDecimal.ZERO;
                BigDecimal massaIsoladores = BigDecimal.valueOf(request.getNumeroIsoladores()).multiply(MASSA_ISOLADOR);
                BigDecimal massaTotal = massaEpoxiCarretel.add(massaEpoxiCapa).add(massaEncapsulamento)
                                .add(massaIsoladores);

                // Busca composição da tabela epoxi_composicao
                List<EpoxiComposicao> composicoes = epoxiComposicaoRepository.findAll();
                BigDecimal custoTotal = BigDecimal.ZERO;

                for (EpoxiComposicao comp : composicoes) {
                        BigDecimal massaComponente = massaTotal.multiply(comp.getFator());
                        BigDecimal precoComponente = produtoFornecedorRepository
                                        .findLowestPriceByProdutoCodigo(comp.getMaterial().getCodigo())
                                        .orElseThrow(() -> new RuntimeException(
                                                        "Preço não encontrado para insumo: " + comp.getComponente()));
                        BigDecimal custoComponente = massaComponente.multiply(precoComponente);

                        // Adiciona à lista de consumo
                        MaterialConsumo consumo = materialConsumoFromProduto(comp.getMaterial().getCodigo(),
                                        massaComponente, custoComponente);
                        listaDestino.add(consumo);

                        custoTotal = custoTotal.add(custoComponente);
                }

                return new BigDecimal[] { custoTotal, massaTotal };
        }

        // 9. Insumos Simples
        private List<MaterialConsumo> calcularCustoInsumos(List<InsumoRequestDto> insumos) {
                List<MaterialConsumo> consumos = new ArrayList<>();
                if (insumos == null || insumos.isEmpty()) {
                        return consumos;
                }
                for (InsumoRequestDto item : insumos) {
                        BigDecimal precoUnitario = produtoFornecedorRepository
                                        .findLowestPriceByProdutoCodigo(item.getIdInsumo())
                                        .orElseThrow(() -> new RuntimeException(
                                                        "Preço não encontrado para o insumo: " + item.getIdInsumo()));

                        BigDecimal custo = precoUnitario.multiply(item.getQuantidade());

                        MaterialConsumo consumo = materialConsumoFromProduto(item.getIdInsumo(), item.getQuantidade(),
                                        custo);
                        consumos.add(consumo);
                }
                return consumos;
        }
}
