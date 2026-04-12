-- ======================================================
-- V1__create_tables.sql
-- Apenas tabelas necessárias para o cálculo de custo TP
-- ======================================================

-- 1. TABELA: fornecedor
CREATE TABLE IF NOT EXISTS fornecedor (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    telefone VARCHAR(20),
    ativo BOOLEAN
);

-- 2. TABELA: produto (materiais/insumos)
CREATE TABLE IF NOT EXISTS produto (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(20) NOT NULL UNIQUE,
    nome VARCHAR(200) NOT NULL,
    tipo_material VARCHAR(50),
    unidade_medida VARCHAR(10),
    disponibilidade BOOLEAN,
    estoque DECIMAL(10,2),
    massa_por_unidade DECIMAL(12,6);
);

-- 3. TABELA: produto_fornecedor (relacionamento com preço)
CREATE TABLE IF NOT EXISTS produto_fornecedor (
    id BIGSERIAL PRIMARY KEY,
    produto_id VARCHAR(20) NOT NULL REFERENCES produto(codigo),
    fornecedor_id INTEGER NOT NULL REFERENCES fornecedor(id),
    valor DECIMAL(18,2) NOT NULL,
    ativo BOOLEAN DEFAULT TRUE,
    UNIQUE(produto_id, fornecedor_id)
);

-- 4. TABELA: produto_cobre (especificações de fios)
CREATE TABLE IF NOT EXISTS produto_cobre (
    id BIGSERIAL PRIMARY KEY,
    codigo_produto VARCHAR(20) NOT NULL UNIQUE REFERENCES produto(codigo),
    awg INTEGER,
    diametro DECIMAL(10,4),
    secao DECIMAL(10,4),
    espiras_por_cm DECIMAL(10,2),
    kg_por_km DECIMAL(10,2),
    resistencia DECIMAL(10,2),
    capacidade DECIMAL(10,2)
);

-- 5. TABELA: carretel (dados do carretel e dependentes: papel, fita de latao, capa e cano de secundario)
CREATE TABLE IF NOT EXISTS carretel (
    tamanho VARCHAR(10) PRIMARY KEY,
    circunferencia_mm DECIMAL(10,2),
    epoxi_carretel_kg DECIMAL(10,2),
    epoxi_capa_kg DECIMAL(10,2),
    primario_papel_largura_mm DECIMAL(10,2),
    primario_largura_espira_mm DECIMAL(10,2),
    fita_latao_massa_kg DECIMAL(10,3),
    cano_altura_mm DECIMAL(10,2),
    cano_circunferencia_mm DECIMAL(10,2),
    secundario_largura_espira_mm DECIMAL(10,2),
    cano_massa_kg DECIMAL(10,3)
);

-- 6. TABELA: nucleo (dados do nucleo e sua fita de fixação)
CREATE TABLE IF NOT EXISTS nucleo (
    tipo VARCHAR(20) PRIMARY KEY,
    massa_kg DECIMAL(10,2),
    cinta_massa_kg DECIMAL(10,3),
    cinta_metro DECIMAL(10,2)
);

-- 7. TABELA: epoxi_composicao (fatores para cálculo da resina epóxi)
CREATE TABLE IF NOT EXISTS epoxi_composicao (
    id BIGSERIAL PRIMARY KEY,
    componente VARCHAR(50) NOT NULL,
    fator DECIMAL(10,6) NOT NULL,
    material_codigo VARCHAR(20) REFERENCES produto(codigo)
);

-- 8. TABELA: poliuretano_composicao (acolchoamento do nucleo de silicio)
CREATE TABLE IF NOT EXISTS poliuretano_composicao (
    id BIGSERIAL PRIMARY KEY,
    componente VARCHAR(50) NOT NULL,
    quantidade_kg DECIMAL(10,3) NOT NULL,
    material_codigo VARCHAR(20) REFERENCES produto(codigo)
);
-- ======================================================
-- CARGA DE DADOS INICIAIS PARA TESTES (mantidos os CSVs anteriores)
-- ======================================================

-- Fornecedor
INSERT INTO fornecedor (nome, telefone, ativo) VALUES
('Shimtek', '(11) 4496-4099', 'true'),
('Nanopoxy', '(48) 3372-5783', 'true'),
('Redelease', '(11) 3932-1196', 'true'),
('ChemTrend', '(19) 3881-8200', 'true'),
('Neno Comércio', '(19) 3891-7030', 'true'),
('Fechadura CIA', '(19) 99875-6492', 'true'),
('Minérios Gerais', '(11) 4031-3448', 'true'),
('Tremax', '(16) 3266-1297', 'true'),
('Lopes LED', '(19) 3569-8278', 'true'),
('HM Eletrônica', '(19) 3861-0615', 'true'),
('MGP Elétrica', '(11) 4024-1871', 'true'),
('União Ferramentas', '(19) 3818-2338', 'true'),
('Guaçu Parafuso', '(19) 3891-6951', 'true'),
('Maxepoxi', '(11) 5645-1900', 'true'),
('Rodrigues Metal Laser', '(19) 2240-0298', 'true'),
('Miura Laser', '(19) 99629-8126', 'true'),
('Guarnieri Pallets e Embalagens', '(19) 97125-9561', 'true'),
('KS Equipamentos de Aço', '(19) 3831-3727', 'true'),
('Mogipar', '(19) 3022-1444', 'true');

-- Produto
INSERT INTO produto (id, codigo, nome, tipo_material, unidade_medida, disponibilidade, estoque, massa_por_unidade) VALUES
(1, 'INS-010', 'Fio de Cobre AWG 10', 'COBRE', 'KG', true, 20.000, 1),
(2, 'INS-011', 'Fio de Cobre AWG 11', 'COBRE', 'KG', false, 0.000, 1),
(3, 'INS-012', 'Fio de Cobre AWG 12', 'COBRE', 'KG', false, 0.000, 1),
(4, 'INS-013', 'Fio de Cobre AWG 13', 'COBRE', 'KG', false, 0.000, 1),
(5, 'INS-014', 'Fio de Cobre AWG 14', 'COBRE', 'KG', false, 0.000, 1),
(6, 'INS-015', 'Fio de Cobre AWG 15', 'COBRE', 'KG', true, 60.000, 1),
(7, 'INS-016', 'Fio de Cobre AWG 16', 'COBRE', 'KG', true, 40.000, 1),
(8, 'INS-017', 'Fio de Cobre AWG 17', 'COBRE', 'KG', true, 80.000, 1),
(9, 'INS-018', 'Fio de Cobre AWG 18', 'COBRE', 'KG', false, 0.000, 1),
(10, 'INS-019', 'Fio de Cobre AWG 19', 'COBRE', 'KG', false, 0.000, 1),
(11, 'INS-020', 'Fio de Cobre AWG 20', 'COBRE', 'KG', false, 0.000, 1),
(12, 'INS-021', 'Fio de Cobre AWG 21', 'COBRE', 'KG', false, 0.000, 1),
(13, 'INS-022', 'Fio de Cobre AWG 22', 'COBRE', 'KG', false, 0.000, 1),
(14, 'INS-023', 'Fio de Cobre AWG 23', 'COBRE', 'KG', false, 0.000, 1),
(15, 'INS-024', 'Fio de Cobre AWG 24', 'COBRE', 'KG', false, 0.000, 1),
(16, 'INS-025', 'Fio de Cobre AWG 25', 'COBRE', 'KG', false, 0.000, 1),
(17, 'INS-026', 'Fio de Cobre AWG 26', 'COBRE', 'KG', false, 0.000, 1),
(18, 'INS-027', 'Fio de Cobre AWG 27', 'COBRE', 'KG', false, 0.000, 1),
(19, 'INS-028', 'Fio de Cobre AWG 28', 'COBRE', 'KG', false, 0.000, 1),
(20, 'INS-029', 'Fio de Cobre AWG 29', 'COBRE', 'KG', false, 0.000, 1),
(21, 'INS-030', 'Fio de Cobre AWG 30', 'COBRE', 'KG', true, 20.000, 1),
(22, 'INS-031', 'Fio de Cobre AWG 31', 'COBRE', 'KG', true, 20.000, 1),
(23, 'INS-032', 'Fio de Cobre AWG 32', 'COBRE', 'KG', true, 40.000, 1),
(24, 'INS-033', 'Fio de Cobre AWG 33', 'COBRE', 'KG', true, 100.000, 1),
(25, 'INS-034', 'Fio de Cobre AWG 34', 'COBRE', 'KG', true, 300.000, 1),
(26, 'INS-035', 'Fio de Cobre AWG 35', 'COBRE', 'KG', false, 0.000, 1),
(27, 'INS-036', 'Fio de Cobre AWG 36', 'COBRE', 'KG', false, 0.000, 1),
(28, 'INS-037', 'Fio de Cobre AWG 37', 'COBRE', 'KG', false, 0.000, 1),
(29, 'INS-038', 'Fio de Cobre AWG 38', 'COBRE', 'KG', false, 0.000, 1),
(30, 'INS-039', 'Fio de Cobre AWG 39', 'COBRE', 'KG', false, 0.000, 1),
(31, 'INS-040', 'Fio de Cobre AWG 40', 'COBRE', 'KG', false, 0.000, 1),
(32, 'INS-041', 'Fio de Cobre AWG 41', 'COBRE', 'KG', false, 0.000, 1),
(33, 'INS-042', 'Fio de Cobre AWG 42', 'COBRE', 'KG', false, 0.000, 1),
(34, 'INS-043', 'Fio de Cobre AWG 43', 'COBRE', 'KG', false, 0.000, 1),
(35, 'INS-044', 'Fio de Cobre AWG 44', 'COBRE', 'KG', false, 0.000, 1),
(36, 'INS-045', 'QUARTZO', 'GERAL', 'KG', true, 2000.000, 1),
(37, 'INS-046', 'INSERTO M5', 'GERAL', 'PÇ', true, 300.000, 0.004),
(38, 'INS-047', 'INSERTO M10', 'GERAL', 'PÇ', true, 300.000, 0.018),
(39, 'INS-048', 'CHAPA DE LATÃO', 'GERAL', 'KG', true, 50.000, 1),
(40, 'INS-049', 'NÚCLEO SILÍCIO', 'GERAL', 'KG', true, 3000.000, 1),
(41, 'INS-050', 'VERNIZ', 'GERAL', 'KG', true, 15.000, 1),
(42, 'INS-051', 'COLA', 'GERAL', 'KG', true, 10.000, 1),
(43, 'INS-052', 'BORRACHA', 'GERAL', 'M', true, 30.000,),
(44, 'INS-053', 'CANO', 'GERAL', 'M', true, 30.000, 0.675),
(45, 'INS-054', 'TNT', 'GERAL', 'M', true, 30.000,),
(46, 'INS-055', 'CADARÇO', 'GERAL', 'M', true, 30.000,),
(47, 'INS-056', 'POLIESTER', 'GERAL', 'KG', true, 15.000, 1),
(48, 'INS-057', 'CINTA', 'GERAL', 'M', true, 50.000, 0.08),
(49, 'INS-058', 'PARAFUSO M10x16', 'GERAL', 'PÇ', true, 500.000, 0.022),
(50, 'INS-059', 'PARAFUSO M5x12', 'GERAL', 'PÇ', true, 400.000, 0.0035),
(51, 'INS-060', 'PAPEL', 'GERAL', 'KG', true, 15.000, 1),
(52, 'INS-061', 'INSERTO M12', 'GERAL', 'PÇ', true, 200.000, 0.018),
(53, 'INS-062', 'PARAFUSO M12x18', 'GERAL', 'PÇ', true, 800.000, 0.022),
(54, 'INS-063', 'FUSÍVEL DE PROTEÇÃO', 'GERAL', 'PÇ', true, 60.000,),
(55, 'INS-064', 'BORRACHA COLUMBIA', 'GERAL', 'M', true, 50.000,),
(56, 'INS-065', 'FITA DE ALUMINIO', 'GERAL', 'KG', true, 200.000, 1),
(57, 'INS-066', 'FITA DE COBRE', 'GERAL', 'KG', true, 200.000, 1),
(58, 'INS-067', 'RESISTOR', 'GERAL', 'PÇ', true, 300.000,),
(59, 'INS-068', 'RESINA POLIURETANO', 'GERAL', 'KG', true, 240.000, 1),
(60, 'INS-069', 'CATALIZADOR POLIURETANO', 'GERAL', 'KG', true, 30.000, 1),
(61, 'INS-070', 'MICROESFERA DE VIDRO', 'GERAL', 'KG', true, 75.000, 1),
(62, 'INS-071', 'RESINA NB121', 'GERAL', 'KG', true, 480.000, 1),
(63, 'INS-072', 'ENDURECEDOR N019', 'GERAL', 'KG', true, 240.000, 1);

-- Produto_fornecedor
INSERT INTO produto_fornecedor (id, produto_id, fornecedor_id, valor, ativo) VALUES
(1, 1, 1, 72.02, true),
(2, 2, 1, 72.57, true),
(3, 3, 1, 73.08, true),
(4, 4, 1, 73.68, true),
(5, 5, 1, 74.12, true),
(6, 6, 1, 74.59, true),
(7, 7, 1, 74.98, true),
(8, 8, 1, 75.28, true),
(9, 9, 1, 75.87, true),
(10, 10, 1, 76.31, true),
(11, 11, 1, 77.05, true),
(12, 12, 1, 77.55, true),
(13, 13, 1, 78.15, true),
(14, 14, 1, 78.97, true),
(15, 15, 1, 79.67, true),
(16, 16, 1, 80.16, true),
(17, 17, 1, 81.97, true),
(18, 18, 1, 83.99, true),
(19, 19, 1, 86.16, true),
(20, 20, 1, 89.76, true),
(21, 21, 1, 93.56, true),
(22, 22, 1, 97.89, true),
(23, 23, 1, 100.30, true),
(24, 24, 1, 103.73, true),
(25, 25, 1, 106.50, true),
(26, 26, 1, 110.11, true),
(27, 27, 1, 116.67, true),
(28, 28, 1, 120.71, true),
(29, 29, 1, 124.38, true),
(30, 30, 1, 128.08, true),
(31, 31, 1, 132.65, true),
(32, 32, 1, 137.10, true),
(33, 33, 1, 141.09, true),
(34, 34, 1, 145.76, true),
(35, 35, 1, 150.05, true),
(36, 63, 1, 29.02, true),
(37, 60, 1, 28.20, true),
(38, 57, 1, 74.50, true),
(39, 54, 1, 35.00, true),
(40, 51, 1, 55.17, true),
(41, 48, 1, 3.28, true),
(42, 45, 1, 3.20, true),
(43, 42, 1, 32.50, true),
(44, 39, 1, 123.00, true),
(45, 36, 1, 2.01, true),
(46, 61, 2, 7.80, true),
(47, 58, 2, 0.31, true),
(48, 55, 2, 58.00, true),
(49, 52, 2, 42.00, true),
(50, 49, 2, 0.60, true),
(51, 46, 2, 7.90, true),
(52, 43, 2, 2.89, true),
(53, 40, 2, 13.50, true),
(54, 37, 2, 1.40, true),
(55, 62, 4, 50.75, true),
(56, 59, 4, 20.60, true),
(57, 56, 4, 36.78, true),
(58, 53, 4, 0.70, true),
(59, 50, 4, 0.09, true),
(60, 47, 4, 38.50, true),
(61, 44, 4, 15.50, true),
(62, 41, 4, 48.00, true),
(63, 38, 4, 5.50, true);

-- Produto_cobre
INSERT INTO produto_cobre (codigo_produto, awg, diametro, secao, espiras_por_cm, kg_por_km, resistencia, capacidade) VALUES
('INS-010', 10, 2.588, 5.26, 3.6, 46.8, 3.23, 15),
('INS-011', 11, 2.305, 4.17, 4.0, 32.1, 4.07, 12),
('INS-012', 12, 2.053, 3.31, 4.4, 29.4, 5.13, 9.5),
('INS-013', 13, 1.828, 2.63, 5.0, 23.3, 6.49, 7.5),
('INS-014', 14, 1.628, 2.08, 5.6, 18.5, 8.17, 6.0),
('INS-015', 15, 1.450, 1.65, 6.4, 14.7, 10.3, 4.8),
('INS-016', 16, 1.291, 1.31, 7.2, 11.6, 12.9, 3.7),
('INS-017', 17, 1.150, 1.04, 8.4, 9.26, 16.34, 3.2),
('INS-018', 18, 1.024, 0.82, 9.2, 7.30, 20.73, 2.5),
('INS-019', 19, 0.9116, 0.65, 10.2, 5.79, 26.15, 2.0),
('INS-020', 20, 0.8118, 0.52, 11.6, 4.61, 32.69, 1.6),
('INS-021', 21, 0.7230, 0.41, 12.8, 3.64, 41.46, 1.2),
('INS-022', 22, 0.6438, 0.33, 14.4, 2.89, 51.50, 0.92),
('INS-023', 23, 0.5733, 0.26, 16.0, 2.29, 56.40, 0.73),
('INS-024', 24, 0.5106, 0.20, 18.0, 1.82, 85.00, 0.58),
('INS-025', 25, 0.4547, 0.16, 20.0, 1.44, 106.2, 0.46),
('INS-026', 26, 0.4049, 0.13, 22.8, 1.14, 130.7, 0.37),
('INS-027', 27, 0.3606, 0.10, 25.6, 0.91, 170.0, 0.29),
('INS-028', 28, 0.3211, 0.08, 28.4, 0.72, 212.5, 0.23),
('INS-029', 29, 0.2859, 0.064, 32.4, 0.57, 265.6, 0.18),
('INS-030', 30, 0.2546, 0.051, 35.6, 0.45, 333.3, 0.15),
('INS-031', 31, 0.2268, 0.04, 39.8, 0.36, 425.0, 0.11),
('INS-032', 32, 0.2019, 0.032, 44.5, 0.28, 531.2, 0.09),
('INS-033', 33, 0.1798, 0.0254, 56.0, 0.23, 669.3, 0.072),
('INS-034', 34, 0.1601, 0.0201, 56.0, 0.18, 845.8, 0.057),
('INS-035', 35, 0.1426, 0.0159, 62.3, 0.14, 1069, 0.045),
('INS-036', 36, 0.1270, 0.0127, 69.0, 0.10, 1338, 0.036),
('INS-037', 37, 0.1131, 0.0100, 78.0, 0.089, 1700, 0.028),
('INS-038', 38, 0.1007, 0.0079, 82.3, 0.070, 2152, 0.022),
('INS-039', 39, 0.0897, 0.0063, 97.5, 0.056, 2696, 0.017),
('INS-040', 40, 0.0799, 0.0050, 111.0, 0.044, 3400, 0.014),
('INS-041', 41, 0.0711, 0.0040, 126.8, 0.035, 4250, 0.011),
('INS-042', 42, 0.0633, 0.0032, 139.9, 0.028, 5312, 0.009),
('INS-043', 43, 0.0564, 0.0025, 156.4, 0.022, 6800, 0.007),
('INS-044', 44, 0.0503, 0.0020, 169.7, 0.018, 8500, 0.005);

-- Carretel
INSERT INTO carretel (tamanho, circunferencia_mm, epoxi_carretel_kg, epoxi_capa_kg, primario_papel_largura_mm, primario_largura_espira_mm, fita_latao_massa_kg, cano_altura_mm, cano_circunferencia_mm, secundario_largura_espira_mm, cano_massa_kg) VALUES
('P', 400, 2, 4, 48, 38, 0.03, 145, 240, 135, 0.09),
('M', 420, 3, 6, 55, 45, 0.045, 160, 250, 150, 0.105),
('G', 440, 5, 8, 65, 55, 0.065, 200, 260, 190, 0.135);

-- Nucleo
INSERT INTO nucleo (tipo, massa_kg, cinta_massa_kg, cinta_metro) VALUES
('15kV FF', 6.8, 0.08, 1),
('15kV FT', 5.8, 0.16, 2),
('24kV FF', 10.3, 0.088, 1.1),
('24kV FT', 11.1, 0.176, 2.2),
('36kV FF', 13.3, 0.1, 1.25),
('36kV FT', 13.1, 0.2, 2.5);

-- Composição da resina epoxi
INSERT INTO epoxi_composicao (componente, fator, material_codigo) VALUES
('RESINA NB121', 0.2222, 'INS-071'),
('ENDURECEDOR N019', 0.1111, 'INS-072'),
('QUARTZO', 0.6667, 'INS-045');

-- Composição do Acolchoamento de Poliuretano
INSERT INTO poliuretano_composicao (componente, quantidade_kg, material_codigo) VALUES
('RESINA POLIURETANO', 4.0, 'INS-068'),
('CATALIZADOR POLIURETANO', 0.5, 'INS-069'),
('QUARTZO', 0.75, 'INS-045');

-- Fim do script