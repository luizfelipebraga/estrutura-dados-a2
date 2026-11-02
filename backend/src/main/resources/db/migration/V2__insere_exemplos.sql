-- Teste 1: Caneta
INSERT INTO produto (id, nome) VALUES (1, 'Caneta');
INSERT INTO item_formula (id, produto_id, pai_id, nome, quantidade, valor) VALUES
 (1, 1, NULL, 'Caneta',   1, 2.50),
 (2, 1, 1,    'Tinta',    1, 0.80),
 (3, 1, 2,    'Pigmento', 1, 0.30),
 (4, 1, 2,    'Solvente', 1, 0.50),
 (5, 1, 1,    'Corpo',    1, 1.00),
 (6, 1, 5,    'Plástico', 1, 0.70),
 (7, 1, 5,    'Clip',     1, 0.30),
 (8, 1, 1,    'Tampa',    1, 0.70),
 (9, 1, 8,    'Plástico', 1, 0.50),
 (10, 1, 8,   'Vedação',  1, 0.20);

-- Teste 2: Computador
INSERT INTO produto (id, nome) VALUES (2, 'Computador');
INSERT INTO item_formula (id, produto_id, pai_id, nome, quantidade, valor) VALUES
 (11, 2, NULL, 'Computador',           1, 2500.00),
 (12, 2, 11,   'Gabinete',             1,  800.00),
 (13, 2, 12,   'Placa-Mãe',            1,  500.00),
 (14, 2, 13,   'CPU',                  1, 1000.00),
 (15, 2, 13,   'Memória RAM',          2,  200.00),
 (16, 2, 13,   'Chipset',              1,  150.00),
 (17, 2, 12,   'Fonte de Alimentação', 1,  300.00),
 (18, 2, 12,   'Armazenamento',        1,    0.00),
 (19, 2, 18,   'HDD',                  1,  250.00),
 (20, 2, 18,   'SSD',                  1,  400.00),
 (21, 2, 11,   'Monitor',              1,  900.00),
 (22, 2, 21,   'Tela LCD',             1,  600.00),
 (23, 2, 21,   'Carcaça',              1,  100.00),
 (24, 2, 11,   'Periféricos',          1,    0.00),
 (25, 2, 24,   'Teclado',              1,  100.00),
 (26, 2, 24,   'Mouse',                1,   80.00);

-- alinha as sequences para os próximos cadastros não colidirem com os ids fixos
SELECT setval(pg_get_serial_sequence('produto', 'id'),       (SELECT MAX(id) FROM produto));
SELECT setval(pg_get_serial_sequence('item_formula', 'id'),  (SELECT MAX(id) FROM item_formula));
