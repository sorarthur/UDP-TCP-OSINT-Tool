# Atlas de Vulnerabilidades OSINT - Sistema Distribuído

> **Disciplina:** Redes de Computadores I - PUC Minas
> **Tecnologia:** Java (Sockets + Swing)
> **Protocolos:** TCP & UDP

## 📖 Sobre o Projeto

Este projeto implementa uma simulação de **coleta de inteligência (OSINT)** em uma arquitetura de rede distribuída. O sistema é composto por uma **Sonda (Cliente)** que coleta dados e os envia para uma **Central de Comando (Servidor)** através de uma infraestrutura de rede segmentada.

O objetivo é demonstrar a comunicação de dados confiável (TCP) e em tempo real (UDP) atravessando múltiplas camadas de NAT (Network Address Translation).

---

## 📋 Índice

1. [Requisitos do Sistema](#-requisitos-do-sistema)
2. [Início Rápido](#-início-rápido-teste-em-3-minutos)
3. [Funcionalidades](#-funcionalidades-principais)
4. [Arquivos do Projeto](#-arquivos-do-projeto)
5. [Como Funciona](#-como-funciona-conceitos-técnicos)
6. [Teste em Rede](#-teste-em-rede-com-roteadores)
7. [Configuração Cisco Packet Tracer](#-configuração-cisco-packet-tracer)
8. [Captura no Wireshark](#-captura-no-wireshark)
9. [Solução de Problemas](#-solução-de-problemas)
10. [Para o Relatório](#-para-o-relatório)

---

## 💻 Requisitos do Sistema

Para executar este projeto, você precisará de:

- **Java JDK 8+** (Para compilar e executar o código)
- **Cisco Packet Tracer** (Para simulação da topologia de rede)
- **Wireshark** (Para análise e captura de pacotes - opcional)

---

## 🚀 Início Rápido (Teste em 3 minutos)

### Passo 1: Compilar (se necessário)

```bash
cd "Trabalho Redes"
javac ServidorOSINT.java
javac ClienteOSINT.java
```

### Passo 2: Iniciar o Servidor

```bash
# Opção A: Duplo clique em IniciarServidor.bat
# Opção B: Linha de comando
java ServidorOSINT
```

→ Clique em **"▶ INICIAR SERVIDOR"**
→ Aguarde ver: `[TCP] Servidor TCP iniciado na porta 6789` e `[UDP] Servidor UDP iniciado na porta 9876`

### Passo 3: Iniciar o Cliente

```bash
# Opção A: Duplo clique em IniciarCliente.bat
# Opção B: Linha de comando
java ClienteOSINT
```

→ Deixe IP como `localhost` (para teste local)
→ Teste as funcionalidades:
- **"📡 ENVIAR PING (UDP)"** - Envia heartbeat
- **"📄 ENVIAR RELATÓRIO (TCP)"** - Envia dados OSINT
- **"📁 ENVIAR ARQUIVO (TCP)"** - Envia arquivo ao servidor
- **"▶ INICIAR HEARTBEAT"** - Heartbeat automático

### Passo 4: Verificar Funcionamento

**No Servidor, você verá:**
```
[14:30:20] [UDP] 📡 PING de: 127.0.0.1 | HEARTBEAT - Sonda Ativa
[14:30:25] [TCP] RELATÓRIO RECEBIDO DE: 127.0.0.1
[14:30:25] [TCP] === RELATÓRIO OSINT === | Timestamp: ...
```

**No Cliente, você verá:**
```
[14:30:20] [UDP] ✓ PING enviado para 127.0.0.1:9876
[14:30:25] [TCP] Resposta: ✅ Relatório recebido com sucesso!
```

✅ **Funcionou? Pronto! A aplicação está operacional.**

---

## ⚙️ Funcionalidades Principais

### Servidor (Central de Comando - PC2)

- **Interface gráfica moderna** com monitoramento em tempo real
- **Multithreading**: 3 threads simultâneas (GUI + TCP + UDP)
- **Porta TCP 6789**: Recebe relatórios de vulnerabilidades e arquivos
- **Porta UDP 9876**: Recebe heartbeat/ping (sinal de vida)
- **Estatísticas em tempo real**:
  - Total de PINGs recebidos
  - Total de Relatórios recebidos
  - Total de Arquivos recebidos
  - Clientes Ativos conectados
- **Tabela de clientes conectados**:
  - IP do cliente
  - Última atividade (timestamp)
  - Status: 🟢 ATIVO / 🟡 INATIVO / 🔴 OFFLINE
- **Recepção de arquivos**: Salva arquivos enviados em `arquivos_recebidos/`
- **Exportação de logs**: Salva histórico completo em `logs/`

### Cliente (Sonda Remota - PC1)

- **Interface gráfica moderna** para operações
- **Configuração flexível**: IP e portas do servidor
- **Heartbeat Automático (UDP)**:
  - Intervalo configurável (1-30 segundos via slider)
  - Botões para iniciar/parar
  - Mantém sonda visível como "ativa" no servidor
- **Envio de Relatório (TCP)**: Dados OSINT coletados do sistema
  - Timestamp atual
  - Hostname do PC
  - Endereço IP local
  - Usuário do sistema
  - Sistema Operacional + versão
  - Versão do Java
  - **Dados extras opcionais** (via checkbox):
    - Memória usada/total
    - Número de CPUs
    - Diretório home
    - Arquitetura (x86, x64, arm64)
- **Envio de Arquivo (TCP)**: Envia arquivos (.txt, .pdf, .jpg, .png, .doc, .docx)
- **Log de operações** em tempo real

**Importante:** Os dados OSINT são **REAIS** (coletados do sistema), não simulados.

---

## 📁 Arquivos do Projeto

```
Trabalho Redes/
├── ServidorOSINT.java          # Código do servidor (central)
├── ClienteOSINT.java           # Código do cliente (sonda)
├── ServidorOSINT.class         # Compilado
├── ClienteOSINT.class          # Compilado
├── IniciarServidor.bat         # Script para iniciar servidor
├── IniciarCliente.bat          # Script para iniciar cliente
├── TESTAR.bat                  # Script para testar ambos
├── arquivos_recebidos/         # Pasta para arquivos recebidos
├── logs/                       # Pasta para logs exportados
├── tcp/                        # Exemplos simples de TCP
│   ├── Cliente.java
│   └── Servidor.java
├── udp/                        # Exemplos simples de UDP
│   ├── Cliente.java
│   └── Servidor.java
├── README.md                   # Este arquivo
├── NOVAS-FUNCIONALIDADES.md    # Detalhes das funcionalidades
└── CORRECOES-APLICADAS.md      # Log de correções visuais
```

---

## 🔧 Como Funciona (Conceitos Técnicos)

### TCP (Transmission Control Protocol)

- **Usado para**: Envio de relatórios de vulnerabilidades e arquivos
- **Por quê**: Dados críticos que não podem ser perdidos
- **Características**:
  - Three-Way Handshake (SYN, SYN-ACK, ACK)
  - Garantia de entrega
  - Confirmação de recebimento
  - Controle de fluxo e congestionamento
  - Retransmissão em caso de perda

**No projeto:**
- Cliente estabelece conexão TCP com servidor
- Envia identificador ("RELATORIO" ou "ARQUIVO")
- Transmite dados
- Aguarda confirmação do servidor

### UDP (User Datagram Protocol)

- **Usado para**: Envio de heartbeat/ping (telemetria)
- **Por quê**: Velocidade, baixo overhead, não precisa de confirmação
- **Características**:
  - Sem conexão (connectionless)
  - Rápido e leve
  - Sem garantia de entrega
  - Ideal para dados não-críticos que podem ser perdidos

**No projeto:**
- Cliente envia datagramas UDP periodicamente
- Servidor recebe e registra atividade do cliente
- Se heartbeat para de chegar, cliente é marcado como inativo/offline

### Multithreading

**Servidor:**
- Thread 1: Interface gráfica (Event Dispatch Thread)
- Thread 2: Escuta TCP na porta 6789
- Thread 3: Escuta UDP na porta 9876
- Thread 4: Monitor de clientes (verifica status a cada 5s)

**Cliente:**
- Thread principal: Interface gráfica
- Threads adicionais criadas para cada operação de rede (não bloqueia GUI)
- Thread de heartbeat (quando ativo): envia PINGs em intervalo configurado

**Benefício**: Interface nunca trava durante operações de rede.

### Sockets Java

**ServerSocket (TCP):**
```java
ServerSocket serverSocket = new ServerSocket(6789);
Socket cliente = serverSocket.accept(); // Bloqueia até conexão
```

**DatagramSocket (UDP):**
```java
DatagramSocket socket = new DatagramSocket(9876);
DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
socket.receive(packet); // Recebe datagrama
```

---

## 🌐 Teste em Rede (com Roteadores)

### Topologia

```
PC1 (Cliente/Sonda) → R1 → R2 → R3 → PC2 (Servidor/Central)
  192.168.0.0/16      172.16.0.0/12    10.0.0.0/8
```

### Configuração no PC2 (Servidor)

1. Descubra o IP local do PC2:
   ```bash
   ipconfig  # Windows
   ifconfig  # Linux/Mac
   ```
   Exemplo: `10.0.0.100`

2. Inicie o servidor normalmente
3. Clique em "▶ INICIAR SERVIDOR"
4. Anote o IP para configurar os roteadores

### Configuração no PC1 (Cliente)

1. Abra o cliente
2. No campo "IP do Servidor", configure: IP da interface WAN do R1 (ex: `192.168.0.1`)
3. Mantenha portas: TCP `6789` e UDP `9876`
4. Envie PING e RELATÓRIO

**Caminho dos pacotes:** PC1 → R1 → R2 → R3 → PC2

### Verificação de Conectividade Básica

```bash
# Do PC1, testar conectividade com R1
ping 192.168.0.1

# Do PC2, testar conectividade com R3
ping 10.0.0.1
```

---

## 🔧 Configuração Cisco Packet Tracer

### Redes Configuradas

- **R1 (Origem)**: `192.168.0.0/16`
- **R2 (Trânsito)**: `172.16.0.0/12`
- **R3 (Destino)**: `10.0.0.0/8`

### Port Forwarding (NAT Estático)

Configure em **cada roteador** para redirecionar as portas 6789 (TCP) e 9876 (UDP).

#### R3 (Último roteador - próximo ao servidor)

```cisco
enable
configure terminal

# Assumindo PC2 tem IP 10.0.0.100
ip nat inside source static tcp 10.0.0.100 6789 interface Serial0/0/0 6789
ip nat inside source static udp 10.0.0.100 9876 interface Serial0/0/0 9876

interface FastEthernet0/0
 ip nat inside
exit

interface Serial0/0/0
 ip nat outside
exit

write memory
```

#### R2 (Roteador intermediário)

```cisco
enable
configure terminal

# Substitua [IP_LAN_R2] pelo IP da LAN do R2 que conecta ao R3
ip nat inside source static tcp [IP_LAN_R2] 6789 interface Serial0/0/1 6789
ip nat inside source static udp [IP_LAN_R2] 9876 interface Serial0/0/1 9876

interface FastEthernet0/0
 ip nat inside
exit

interface Serial0/0/1
 ip nat outside
exit

write memory
```

#### R1 (Primeiro roteador - próximo ao cliente)

```cisco
enable
configure terminal

# Substitua [IP_LAN_R1] pelo IP da LAN do R1 que conecta ao R2
ip nat inside source static tcp [IP_LAN_R1] 6789 interface Serial0/0/0 6789
ip nat inside source static udp [IP_LAN_R1] 9876 interface Serial0/0/0 9876

interface FastEthernet0/0
 ip nat inside
exit

interface Serial0/0/0
 ip nat outside
exit

write memory
```

### Verificar Configuração

```cisco
show ip route                 # Ver tabela de rotas
show ip interface brief       # Ver interfaces e IPs
show ip nat translations      # Ver traduções NAT ativas
```

---

## 📊 Captura no Wireshark

### Configuração

1. Abra o Wireshark
2. Selecione a interface de rede (ex: Ethernet, Wi-Fi)
3. Inicie a captura **ANTES** de executar o cliente/servidor

### Filtros Úteis

```
tcp.port == 6789        # Ver apenas tráfego TCP da aplicação
udp.port == 9876        # Ver apenas tráfego UDP da aplicação
ip.addr == 127.0.0.1    # Ver tráfego localhost (teste local)
tcp.flags.syn == 1      # Ver pacotes SYN (início de conexão TCP)
```

### O que Capturar para o Relatório

#### 1. TCP Three-Way Handshake
- **Pacote 1**: SYN (Cliente → Servidor)
- **Pacote 2**: SYN-ACK (Servidor → Cliente)
- **Pacote 3**: ACK (Cliente → Servidor)

#### 2. Dados TCP (Relatório/Arquivo)
- Pacote com flag PSH contendo dados do relatório
- Confirmação ACK do servidor

#### 3. Pacotes UDP (Ping/Heartbeat)
- Datagrama UDP com mensagem "HEARTBEAT - Sonda Ativa"
- Observe: sem confirmação (característica do UDP)

### Prints Recomendados

1. Lista de pacotes mostrando handshake TCP completo
2. Detalhe de um pacote TCP com dados do relatório (aba "Data")
3. Detalhe de um pacote UDP mostrando o payload
4. Estatísticas: `Statistics → Protocol Hierarchy`
5. Gráfico de IO: `Statistics → IO Graph`

---

## 🆘 Solução de Problemas

### Erro: "Connection refused"

**Causa:** Servidor não está rodando ou não foi iniciado
**Solução:**
1. Inicie o servidor ANTES do cliente
2. Certifique-se de clicar em "▶ INICIAR SERVIDOR"
3. Verifique se apareceu "Servidor TCP/UDP iniciado" no log

### Erro: "Address already in use"

**Causa:** Já existe um processo usando as portas 6789 ou 9876
**Solução (Windows):**
```bash
# Ver processos Java
jps

# Matar processo específico
taskkill /F /PID [número_do_processo]

# Verificar portas em uso
netstat -an | findstr "6789"
netstat -an | findstr "9876"
```

**Solução (Linux/Mac):**
```bash
# Ver processos usando as portas
lsof -i :6789
lsof -i :9876

# Matar processo
kill -9 [PID]
```

### Erro: "Could not find or load main class"

**Causa:** Não está na pasta correta ou classes não compiladas
**Solução:**
```bash
cd "Trabalho Redes"
dir *.class         # Verificar se arquivos .class existem
javac ServidorOSINT.java  # Recompilar se necessário
javac ClienteOSINT.java
```

### Servidor não recebe nada (teste em rede)

**Checklist:**
- ✅ Port forwarding configurado em TODOS os 3 roteadores?
- ✅ Rotas corretas? (`show ip route` em cada roteador)
- ✅ Interfaces UP? (`show ip interface brief`)
- ✅ Firewall liberou portas 6789 e 9876?
- ✅ IP do servidor no cliente está correto?
- ✅ Testou em localhost primeiro?

### Cliente não aparece como "ATIVO" no servidor

**Causa:** Heartbeat não está sendo enviado
**Solução:**
1. No cliente, clique em "▶ INICIAR HEARTBEAT"
2. Verifique se intervalo está configurado (slider)
3. Verifique logs do cliente e servidor

---

## 📝 Para o Relatório

### Estrutura Sugerida

#### 1. Introdução
- Objetivo do trabalho
- Conceitos de TCP/UDP
- Topologia de rede utilizada (desenho/diagrama)

#### 2. Desenvolvimento

**Aplicação Java:**
- Arquitetura cliente-servidor
- Implementação de Sockets (ServerSocket, DatagramSocket)
- Multithreading para operações concorrentes
- Interface gráfica (Swing) para interação

**Comunicação TCP:**
- Three-Way Handshake (SYN, SYN-ACK, ACK)
- Envio de relatórios e arquivos
- Garantias: entrega ordenada, integridade, controle de fluxo
- Demonstração via Wireshark (prints de pacotes)

**Comunicação UDP:**
- Envio de heartbeat/telemetria
- Características: velocidade, sem conexão, sem garantias
- Comparação com TCP (quando usar cada protocolo)
- Demonstração via Wireshark (prints de datagramas)

**Roteamento e NAT:**
- Configuração dos 3 roteadores
- Port forwarding em cadeia (R1 → R2 → R3)
- NAT estático (traduções de endereço)
- Tabelas de rotas

#### 3. Testes e Resultados

**Prints do Packet Tracer:**
- Topologia completa (3 roteadores + 2 PCs)
- Configuração de cada roteador (CLI)
- Tabelas de rotas (`show ip route`)
- Traduções NAT (`show ip nat translations`)

**Prints do Wireshark:**
- TCP handshake completo (3 pacotes)
- Pacotes de dados TCP (relatório)
- Datagramas UDP (heartbeat)
- Análise de flags TCP
- Comparação de tamanhos (overhead TCP vs UDP)

**Prints da Aplicação:**
- Interface do servidor mostrando estatísticas
- Tabela de clientes conectados
- Interface do cliente com configurações
- Logs de ambos mostrando comunicação bem-sucedida

#### 4. Análise

**TCP vs UDP na prática:**
| Característica | TCP (Porta 6789) | UDP (Porta 9876) |
|----------------|------------------|------------------|
| Conexão | Orientado à conexão | Sem conexão |
| Confiabilidade | Confiável (ACKs) | Não confiável |
| Ordenação | Pacotes ordenados | Sem ordenação |
| Overhead | Maior (headers) | Menor |
| Velocidade | Mais lento | Mais rápido |
| Uso neste projeto | Relatórios/Arquivos | Heartbeat/Telemetria |

**Análise do NAT:**
- Explicar como funcionou o redirecionamento em cadeia
- Tabelas de tradução de endereços (antes/depois)
- Importância do NAT em redes reais

#### 5. Conclusão
- Objetivos alcançados
- Aprendizado sobre protocolos TCP/UDP
- Compreensão de roteamento e NAT
- Aplicação prática dos conceitos teóricos

### Evidências Obrigatórias

1. ✅ Print do servidor recebendo mensagens
2. ✅ Print do cliente enviando dados
3. ✅ Captura Wireshark do TCP handshake
4. ✅ Captura Wireshark dos datagramas UDP
5. ✅ Arquivo .pkt do Cisco Packet Tracer salvo
6. ✅ Prints da configuração dos roteadores
7. ✅ Tabelas de rotas de cada roteador

---

## 📚 Referências Técnicas

- RFC 793 - Transmission Control Protocol (TCP)
- RFC 768 - User Datagram Protocol (UDP)
- RFC 1631 - Network Address Translation (NAT)
- Java Network Programming (Oracle Documentation)
- Cisco Packet Tracer User Guide

---

## ✅ Checklist de Entrega

- [ ] Código fonte (ServidorOSINT.java + ClienteOSINT.java)
- [ ] Arquivos compilados (.class)
- [ ] Simulação Cisco Packet Tracer (arquivo .pkt)
- [ ] Capturas Wireshark (imagens .png ou .pcap)
- [ ] Relatório técnico (PDF)
- [ ] Prints da aplicação funcionando
- [ ] Prints das configurações dos roteadores
- [ ] Apresentação (se aplicável)

---

**Aplicação desenvolvida para demonstrar comunicação TCP/UDP com multithreading, NAT e roteamento multi-hop.**


