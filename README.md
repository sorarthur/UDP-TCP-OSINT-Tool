# Atlas de Vulnerabilidades OSINT - Sistema Distribuído

> **Disciplina:** Redes de Computadores I
> **Tecnologia:** Java (Sockets + Swing)
> **Protocolos:** TCP & UDP

## Sobre o Projeto
Este projeto implementa uma simulação de **coleta de inteligência (OSINT)** em uma arquitetura de rede distribuída. O sistema é composto por uma **Sonda (Cliente)** que coleta dados e os envia para uma **Central de Comando (Servidor)** através de uma infraestrutura de rede segmentada.

O objetivo é demonstrar a comunicação de dados confiável (TCP) e em tempo real (UDP) atravessando múltiplas camadas de NAT (Network Address Translation).

---

## Requisitos do Sistema

Para executar este experimento científico, você precisará de:

* **Java JDK 8+** (Para compilar e rodar o código).
* **Cisco Packet Tracer** (Para a simulação da topologia de rede).
* **Wireshark** (Para análise e captura de pacotes).

---

## Como Executar (Instalação e Compilação)

### 1. Compilando o Código
Abra o terminal na pasta do projeto e execute os comandos abaixo para compilar as classes Java:

    javac OsintServer.java
    javac OsintClient.java

### 2. Iniciando a Central (Servidor)
A Central deve ser executada na máquina designada como **PC2** (na topologia).

    java OsintServer

* **O que acontece:** O servidor abre a **Porta 5000** e inicia duas Threads: uma esperando conexões TCP (Relatórios) e outra escutando datagramas UDP (Heartbeats).

### 3. Iniciando a Sonda (Cliente)
A Sonda deve ser executada na máquina designada como **PC1**.

    java OsintClient

* **O que acontece:** Uma interface gráfica abre permitindo configurar o IP de destino e disparar os pacotes.

---

## Configuração da Rede (Topologia)

Para que a comunicação funcione, a rede deve ser configurada conforme as especificações do Trabalho Prático:

### Endereçamento IP
* **Rede R1 (Onde está o PC1):** 192.168.0.0/16
* **Rede R2 (Trânsito):** 172.16.0.0/12
* **Rede R3 (Onde está o PC2):** 10.0.0.0/8

### Regras de Redirecionamento (Port Forwarding)
O tráfego deve fluir da seguinte maneira através da porta **5000**:

1. **R3 (Onde está o Servidor):** Redireciona pacotes da porta 5000 -> para o IP local do PC2 (Servidor).
2. **R2:** Redireciona pacotes da porta 5000 -> para o IP da WAN de R3.
3. **R1:** Redireciona pacotes da porta 5000 -> para o IP da WAN de R2.

> **Nota Científica:** Certifique-se de configurar o **Gateway Padrão** nos PCs corretamente, ou os pacotes não saberão como sair da rede local!

---

## Funcionalidades da Aplicação

### 1. Envio de Relatório (Protocolo TCP)
* **Ação:** Clique no botão "Enviar Relatório (TCP)".
* **Porquê (Científico):** Usamos TCP porque relatórios de vulnerabilidade são dados críticos. O TCP garante a entrega e a integridade dos dados através do *Three-Way Handshake*.
* **Comportamento:** O servidor exibe "[RELATÓRIO] TARGET_ID..."

### 2. Ping de Status (Protocolo UDP)
* **Ação:** Clique no botão "Ping de Status (UDP)".
* **Porquê (Científico):** Usamos UDP para telemetria rápida ("Heartbeat"). Não importa se um pacote se perder, precisamos de velocidade e baixo *overhead*.
* **Comportamento:** O servidor exibe "[UDP ALERT] Sonda Operante..."

---

## Evidências para Entrega

Não esqueça de gerar as evidências durante os testes:
1. Print do Servidor recebendo as mensagens.
2. Print do Wireshark filtrando por "tcp.port == 5000" e "udp.port == 5000".
3. Arquivo .pkt do Cisco Packet Tracer salvo.

---

**Autor:** [Nome do Senhor Arthur e Grupo]
**Laboratório:** Ciência da Computação - PUC Minas
