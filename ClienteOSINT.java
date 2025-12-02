import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ClienteOSINT extends JFrame {
    // Configurações padrão
    private static final int PORTA_TCP_PADRAO = 6789;
    private static final int PORTA_UDP_PADRAO = 9876;

    // Paleta de cores Material Design
    private static final Color COR_FUNDO = new Color(18, 18, 18);
    private static final Color COR_CARD = new Color(30, 30, 30);
    private static final Color COR_AZUL = new Color(33, 150, 243);
    private static final Color COR_VERDE = new Color(76, 175, 80);
    private static final Color COR_ROXO = new Color(156, 39, 176);
    private static final Color COR_LARANJA = new Color(255, 152, 0);
    private static final Color COR_VERMELHO = new Color(244, 67, 54);

    // Componentes da GUI
    private JTextArea logArea;
    private JButton btnEnviarPing;
    private JButton btnEnviarRelatorio;
    private JButton btnEnviarArquivo;
    private JButton btnIniciarHeartbeat;
    private JButton btnPararHeartbeat;
    private JLabel statusLabel;
    private JSlider sliderIntervalo;
    private JLabel lblIntervalo;
    private JCheckBox chkDadosExtras;

    // Controle de heartbeat
    private Thread threadHeartbeat;
    private volatile boolean heartbeatAtivo = false;

    public ClienteOSINT() {
        configurarGUI();
    }

    private void configurarGUI() {
        setTitle("CLIENTE OSINT - Sonda Remota");
        setSize(1100, 900);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(COR_FUNDO);
        setMinimumSize(new Dimension(1000, 700));

        // Painel superior com GRADIENTE (laranja/vermelho - diferente do servidor)
        JPanel painelTopo = new PainelGradiente(
            new Color(230, 74, 25),  // Laranja escuro
            new Color(179, 55, 113)  // Magenta
        );
        painelTopo.setLayout(new GridLayout(3, 1, 0, 5));
        painelTopo.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JLabel titulo = new JLabel("SONDA OSINT - CLIENTE", SwingConstants.CENTER);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titulo.setForeground(Color.WHITE);

        JLabel subtitulo = new JLabel("Sistema de Coleta e Transmissao de Dados", SwingConstants.CENTER);
        subtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitulo.setForeground(new Color(255, 200, 200));

        JPanel panelInfo = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        panelInfo.setOpaque(false);

        statusLabel = new JLabel("PRONTO", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        statusLabel.setForeground(COR_VERDE);

        panelInfo.add(statusLabel);

        painelTopo.add(titulo);
        painelTopo.add(subtitulo);
        painelTopo.add(panelInfo);

        // Painel de configuração arredondado
        PainelArredondado containerConfig = new PainelArredondado(20);
        containerConfig.setBackground(COR_CARD);
        containerConfig.setLayout(new BorderLayout());
        containerConfig.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel tituloConfig = new JLabel("Configuracao do Servidor");
        tituloConfig.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tituloConfig.setForeground(Color.WHITE);
        tituloConfig.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        // Painel de configuração
        JPanel painelConfig = new JPanel(new GridBagLayout());
        painelConfig.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // IP do Servidor
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel lblIp = new JLabel("IP do Servidor:");
        lblIp.setForeground(new Color(240, 240, 240));
        lblIp.setFont(new Font("Segoe UI", Font.BOLD, 13));
        painelConfig.add(lblIp, gbc);
        gbc.gridx = 1;
        JLabel lblIpValor = new JLabel("localhost");
        lblIpValor.setForeground(new Color(100, 200, 255));
        lblIpValor.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        painelConfig.add(lblIpValor, gbc);

        // Porta TCP
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel lblTcp = new JLabel("Porta TCP:");
        lblTcp.setForeground(new Color(240, 240, 240));
        lblTcp.setFont(new Font("Segoe UI", Font.BOLD, 13));
        painelConfig.add(lblTcp, gbc);
        gbc.gridx = 1;
        JLabel lblTcpValor = new JLabel(String.valueOf(PORTA_TCP_PADRAO));
        lblTcpValor.setForeground(new Color(100, 200, 255));
        lblTcpValor.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        painelConfig.add(lblTcpValor, gbc);

        // Porta UDP
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel lblUdp = new JLabel("Porta UDP:");
        lblUdp.setForeground(new Color(240, 240, 240));
        lblUdp.setFont(new Font("Segoe UI", Font.BOLD, 13));
        painelConfig.add(lblUdp, gbc);
        gbc.gridx = 1;
        JLabel lblUdpValor = new JLabel(String.valueOf(PORTA_UDP_PADRAO));
        lblUdpValor.setForeground(new Color(100, 200, 255));
        lblUdpValor.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        painelConfig.add(lblUdpValor, gbc);

        containerConfig.add(tituloConfig, BorderLayout.NORTH);
        containerConfig.add(painelConfig, BorderLayout.CENTER);

        // Painel de Heartbeat Automático arredondado
        PainelArredondado containerHeartbeat = new PainelArredondado(20);
        containerHeartbeat.setBackground(COR_CARD);
        containerHeartbeat.setLayout(new BorderLayout());
        containerHeartbeat.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel tituloHeartbeat = new JLabel("Heartbeat Automatico (UDP)");
        tituloHeartbeat.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tituloHeartbeat.setForeground(Color.WHITE);
        tituloHeartbeat.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        // Painel de Heartbeat Automático
        JPanel painelHeartbeat = new JPanel(new GridBagLayout());
        painelHeartbeat.setOpaque(false);
        GridBagConstraints gbcHb = new GridBagConstraints();
        gbcHb.insets = new Insets(5, 5, 5, 5);
        gbcHb.fill = GridBagConstraints.HORIZONTAL;

        // Slider de intervalo
        gbcHb.gridx = 0; gbcHb.gridy = 0;
        JLabel lblSlider = new JLabel("Intervalo (segundos):");
        lblSlider.setForeground(Color.WHITE);
        lblSlider.setFont(new Font("Segoe UI", Font.BOLD, 12));
        painelHeartbeat.add(lblSlider, gbcHb);

        gbcHb.gridx = 1; gbcHb.weightx = 1.0;
        sliderIntervalo = new JSlider(1, 30, 5);
        sliderIntervalo.setMajorTickSpacing(5);
        sliderIntervalo.setMinorTickSpacing(1);
        sliderIntervalo.setPaintTicks(true);
        sliderIntervalo.setPaintLabels(true);
        sliderIntervalo.setBackground(new Color(45, 45, 45));
        sliderIntervalo.setForeground(Color.WHITE);
        sliderIntervalo.addChangeListener(e -> {
            lblIntervalo.setText("Intervalo: " + sliderIntervalo.getValue() + "s");
        });
        painelHeartbeat.add(sliderIntervalo, gbcHb);

        gbcHb.gridx = 2; gbcHb.weightx = 0;
        lblIntervalo = new JLabel("Intervalo: 5s");
        lblIntervalo.setForeground(new Color(100, 200, 255));
        lblIntervalo.setFont(new Font("Segoe UI", Font.BOLD, 13));
        painelHeartbeat.add(lblIntervalo, gbcHb);

        // Botões de heartbeat
        gbcHb.gridx = 0; gbcHb.gridy = 1; gbcHb.gridwidth = 3;
        JPanel painelBotoesHb = new JPanel(new FlowLayout());
        painelBotoesHb.setOpaque(false);

        btnIniciarHeartbeat = new JButton("INICIAR HEARTBEAT") {
            @Override
            public void setEnabled(boolean enabled) {
                super.setEnabled(enabled);
                setForeground(Color.WHITE);
                if (!enabled) {
                    setBackground(new Color(46, 204, 113).darker());
                } else {
                    setBackground(new Color(46, 204, 113));
                }
            }
        };
        btnIniciarHeartbeat.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnIniciarHeartbeat.setBackground(new Color(46, 204, 113));
        btnIniciarHeartbeat.setForeground(Color.WHITE);
        btnIniciarHeartbeat.setFocusPainted(false);
        btnIniciarHeartbeat.setBorderPainted(false);
        btnIniciarHeartbeat.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnIniciarHeartbeat.setPreferredSize(new Dimension(200, 40));
        btnIniciarHeartbeat.addActionListener(e -> iniciarHeartbeat());

        btnPararHeartbeat = new JButton("PARAR HEARTBEAT") {
            @Override
            public void setEnabled(boolean enabled) {
                super.setEnabled(enabled);
                setForeground(Color.WHITE);
                if (!enabled) {
                    setBackground(new Color(231, 76, 60).darker());
                } else {
                    setBackground(new Color(231, 76, 60));
                }
            }
        };
        btnPararHeartbeat.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnPararHeartbeat.setBackground(new Color(231, 76, 60));
        btnPararHeartbeat.setForeground(Color.WHITE);
        btnPararHeartbeat.setFocusPainted(false);
        btnPararHeartbeat.setBorderPainted(false);
        btnPararHeartbeat.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnPararHeartbeat.setPreferredSize(new Dimension(200, 40));
        btnPararHeartbeat.setEnabled(false);
        btnPararHeartbeat.addActionListener(e -> pararHeartbeat());

        painelBotoesHb.add(btnIniciarHeartbeat);
        painelBotoesHb.add(btnPararHeartbeat);
        painelHeartbeat.add(painelBotoesHb, gbcHb);

        containerHeartbeat.add(tituloHeartbeat, BorderLayout.NORTH);
        containerHeartbeat.add(painelHeartbeat, BorderLayout.CENTER);

        // Painel de Opções de Coleta arredondado
        PainelArredondado containerOpcoes = new PainelArredondado(20);
        containerOpcoes.setBackground(COR_CARD);
        containerOpcoes.setLayout(new BorderLayout());
        containerOpcoes.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel tituloOpcoes = new JLabel("Opcoes de Coleta OSINT");
        tituloOpcoes.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tituloOpcoes.setForeground(Color.WHITE);
        tituloOpcoes.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        JPanel painelOpcoes = new JPanel(new FlowLayout(FlowLayout.LEFT));
        painelOpcoes.setOpaque(false);

        chkDadosExtras = new JCheckBox("Incluir dados extras (memória, CPUs, arquitetura)");
        chkDadosExtras.setSelected(true);
        chkDadosExtras.setForeground(Color.WHITE);
        chkDadosExtras.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkDadosExtras.setOpaque(false);
        painelOpcoes.add(chkDadosExtras);

        containerOpcoes.add(tituloOpcoes, BorderLayout.NORTH);
        containerOpcoes.add(painelOpcoes, BorderLayout.CENTER);

        // Painel de ações
        JPanel painelAcoes = new JPanel(new GridLayout(2, 2, 10, 10));
        painelAcoes.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        painelAcoes.setBackground(new Color(30, 30, 30));

        btnEnviarPing = new JButton("ENVIAR PING (UDP)");
        btnEnviarPing.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnEnviarPing.setBackground(new Color(52, 152, 219));
        btnEnviarPing.setForeground(Color.WHITE);
        btnEnviarPing.setFocusPainted(false);
        btnEnviarPing.setBorderPainted(false);
        btnEnviarPing.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnEnviarPing.addActionListener(e -> enviarPingUDP());

        btnEnviarRelatorio = new JButton("ENVIAR RELATORIO (TCP)");
        btnEnviarRelatorio.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnEnviarRelatorio.setBackground(new Color(46, 204, 113));
        btnEnviarRelatorio.setForeground(Color.WHITE);
        btnEnviarRelatorio.setFocusPainted(false);
        btnEnviarRelatorio.setBorderPainted(false);
        btnEnviarRelatorio.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnEnviarRelatorio.addActionListener(e -> enviarRelatorioTCP());

        btnEnviarArquivo = new JButton("ENVIAR ARQUIVO (TCP)");
        btnEnviarArquivo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnEnviarArquivo.setBackground(new Color(155, 89, 182));
        btnEnviarArquivo.setForeground(Color.WHITE);
        btnEnviarArquivo.setFocusPainted(false);
        btnEnviarArquivo.setBorderPainted(false);
        btnEnviarArquivo.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnEnviarArquivo.addActionListener(e -> enviarArquivo());

        JButton btnLimparLog = new JButton("LIMPAR LOG");
        btnLimparLog.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLimparLog.setBackground(new Color(149, 165, 166));
        btnLimparLog.setForeground(Color.WHITE);
        btnLimparLog.setFocusPainted(false);
        btnLimparLog.setBorderPainted(false);
        btnLimparLog.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLimparLog.addActionListener(e -> logArea.setText(""));

        painelAcoes.add(btnEnviarPing);
        painelAcoes.add(btnEnviarRelatorio);
        painelAcoes.add(btnEnviarArquivo);
        painelAcoes.add(btnLimparLog);

        // Container arredondado para ações
        PainelArredondado containerAcoes = new PainelArredondado(20);
        containerAcoes.setBackground(COR_CARD);
        containerAcoes.setLayout(new BorderLayout());
        containerAcoes.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel tituloAcoes = new JLabel("Acoes");
        tituloAcoes.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tituloAcoes.setForeground(Color.WHITE);
        tituloAcoes.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        containerAcoes.add(tituloAcoes, BorderLayout.NORTH);
        containerAcoes.add(painelAcoes, BorderLayout.CENTER);

        // Área de log
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("JetBrains Mono", Font.PLAIN, 13));
        logArea.setBackground(new Color(15, 15, 15));
        logArea.setForeground(new Color(0, 255, 255)); // Ciano
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        logArea.setMargin(new Insets(15, 15, 15, 15));

        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        // Container arredondado para log
        PainelArredondado containerLog = new PainelArredondado(20);
        containerLog.setBackground(COR_CARD);
        containerLog.setLayout(new BorderLayout());
        containerLog.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel tituloLog = new JLabel("Log de Operacoes");
        tituloLog.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tituloLog.setForeground(Color.WHITE);
        tituloLog.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        containerLog.add(tituloLog, BorderLayout.NORTH);
        containerLog.add(scrollPane, BorderLayout.CENTER);

        // Painel de configurações (coluna esquerda)
        JPanel painelConfigs = new JPanel(new GridLayout(4, 1, 0, 15));
        painelConfigs.setBackground(COR_FUNDO);
        painelConfigs.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 10));
        painelConfigs.add(containerConfig);
        painelConfigs.add(containerHeartbeat);
        painelConfigs.add(containerOpcoes);
        painelConfigs.add(containerAcoes);

        // Painel de log (coluna direita)
        JPanel painelLog = new JPanel(new BorderLayout());
        painelLog.setBackground(COR_FUNDO);
        painelLog.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 20));
        painelLog.add(containerLog, BorderLayout.CENTER);

        // Painel principal com layout de 2 colunas
        JPanel painelPrincipal = new JPanel(new GridLayout(1, 2, 15, 0));
        painelPrincipal.setBackground(COR_FUNDO);
        painelPrincipal.add(painelConfigs);
        painelPrincipal.add(painelLog);

        // Adicionar componentes
        add(painelTopo, BorderLayout.NORTH);
        add(painelPrincipal, BorderLayout.CENTER);

        setLocationRelativeTo(null);

        // Log inicial
        log("=== CLIENTE OSINT INICIADO ===");
        log("Aguardando comandos...");
    }

    private void log(String mensagem) {
        SwingUtilities.invokeLater(() -> {
            String timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
            logArea.append("[" + timestamp + "] " + mensagem + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    private void atualizarStatus(String texto, Color cor) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText("STATUS: " + texto);
            statusLabel.setForeground(cor);
        });
    }

    private void iniciarHeartbeat() {
        heartbeatAtivo = true;
        btnIniciarHeartbeat.setEnabled(false);
        btnPararHeartbeat.setEnabled(true);
        sliderIntervalo.setEnabled(false);

        log("[HEARTBEAT] Iniciado (intervalo: " + sliderIntervalo.getValue() + "s)");

        threadHeartbeat = new Thread(() -> {
            while (heartbeatAtivo) {
                enviarPingUDP();
                try {
                    Thread.sleep(sliderIntervalo.getValue() * 1000L);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        threadHeartbeat.setDaemon(true);
        threadHeartbeat.start();
    }

    private void pararHeartbeat() {
        heartbeatAtivo = false;
        btnIniciarHeartbeat.setEnabled(true);
        btnPararHeartbeat.setEnabled(false);
        sliderIntervalo.setEnabled(true);

        log("[HEARTBEAT] Parado");
    }

    private void enviarPingUDP() {
        new Thread(() -> {
            try {
                String ip = "localhost";
                int porta = PORTA_UDP_PADRAO;

                if (!heartbeatAtivo) {
                    atualizarStatus("Enviando PING...", Color.ORANGE);
                }

                DatagramSocket socket = new DatagramSocket();
                InetAddress enderecoServidor = InetAddress.getByName(ip);

                String mensagem = "HEARTBEAT - Sonda Ativa";
                byte[] dados = mensagem.getBytes();

                DatagramPacket pacote = new DatagramPacket(dados, dados.length, enderecoServidor, porta);
                socket.send(pacote);
                socket.close();

                if (!heartbeatAtivo) {
                    log("[UDP] OK - PING enviado para " + ip + ":" + porta);
                    atualizarStatus("PING enviado!", new Color(39, 174, 96));
                }

            } catch (Exception e) {
                log("[UDP] ERRO: " + e.getMessage());
                atualizarStatus("Erro ao enviar PING", Color.RED);
                pararHeartbeat(); // Parar heartbeat em caso de erro
            }
        }).start();
    }

    private void enviarRelatorioTCP() {
        new Thread(() -> {
            try {
                String ip = "localhost";
                int porta = PORTA_TCP_PADRAO;

                atualizarStatus("Enviando RELATÓRIO...", Color.ORANGE);
                log("[TCP] Conectando ao servidor...");

                Socket socket = new Socket(ip, porta);
                DataOutputStream saida = new DataOutputStream(socket.getOutputStream());

                // Identificar tipo de mensagem
                saida.writeUTF("RELATORIO");

                // Gerar e enviar relatório
                String relatorio = gerarRelatorioCompleto();
                saida.writeUTF(relatorio);

                log("[TCP] ========================================");
                log("[TCP] RELATÓRIO ENVIADO");
                log("[TCP] ========================================");

                // Receber confirmação
                DataInputStream entrada = new DataInputStream(socket.getInputStream());
                String resposta = entrada.readUTF();
                log("[TCP] Resposta: " + resposta);

                socket.close();
                atualizarStatus("Relatório enviado!", new Color(39, 174, 96));

            } catch (Exception e) {
                log("[TCP] ✗ ERRO: " + e.getMessage());
                atualizarStatus("Erro ao enviar relatório", Color.RED);
            }
        }).start();
    }

    private void enviarArquivo() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Selecione um arquivo para enviar");
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
            "Arquivos (txt, pdf, jpg, png, doc)", "txt", "pdf", "jpg", "png", "doc", "docx");
        fileChooser.setFileFilter(filter);

        int resultado = fileChooser.showOpenDialog(this);
        if (resultado == JFileChooser.APPROVE_OPTION) {
            File arquivo = fileChooser.getSelectedFile();

            new Thread(() -> {
                try {
                    String ip = "localhost";
                    int porta = PORTA_TCP_PADRAO;

                    atualizarStatus("Enviando arquivo...", Color.ORANGE);
                    log("[TCP] Enviando arquivo: " + arquivo.getName() + " (" + formatarTamanho(arquivo.length()) + ")");

                    Socket socket = new Socket(ip, porta);
                    DataOutputStream saida = new DataOutputStream(socket.getOutputStream());

                    // Identificar tipo de mensagem
                    saida.writeUTF("ARQUIVO");
                    saida.writeUTF(arquivo.getName());
                    saida.writeLong(arquivo.length());

                    // Enviar arquivo
                    FileInputStream fis = new FileInputStream(arquivo);
                    byte[] buffer = new byte[4096];
                    int bytesLidos;

                    while ((bytesLidos = fis.read(buffer)) != -1) {
                        saida.write(buffer, 0, bytesLidos);
                    }
                    fis.close();

                    log("[TCP] OK - Arquivo enviado com sucesso!");

                    // Receber confirmação
                    DataInputStream entrada = new DataInputStream(socket.getInputStream());
                    String resposta = entrada.readUTF();
                    log("[TCP] Resposta: " + resposta);

                    socket.close();
                    atualizarStatus("Arquivo enviado!", new Color(39, 174, 96));

                } catch (Exception e) {
                    log("[TCP] ERRO ao enviar arquivo: " + e.getMessage());
                    atualizarStatus("Erro ao enviar arquivo", Color.RED);
                }
            }).start();
        }
    }

    private String formatarTamanho(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
    }

    private String gerarRelatorioCompleto() {
        StringBuilder relatorio = new StringBuilder();
        relatorio.append("=== RELATÓRIO OSINT ===");

        try {
            // Dados básicos
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            String hostname = InetAddress.getLocalHost().getHostName();
            String ipLocal = InetAddress.getLocalHost().getHostAddress();
            String os = System.getProperty("os.name");
            String osVersion = System.getProperty("os.version");
            String javaVersion = System.getProperty("java.version");
            String user = System.getProperty("user.name");

            relatorio.append(" | Timestamp: ").append(timestamp);
            relatorio.append(" | Hostname: ").append(hostname);
            relatorio.append(" | IP: ").append(ipLocal);
            relatorio.append(" | Usuario: ").append(user);
            relatorio.append(" | SO: ").append(os).append(" ").append(osVersion);
            relatorio.append(" | Java: ").append(javaVersion);

            // Dados extras se ativado
            if (chkDadosExtras.isSelected()) {
                // Memória
                Runtime runtime = Runtime.getRuntime();
                long memoriaTotal = runtime.totalMemory() / (1024 * 1024);
                long memoriaLivre = runtime.freeMemory() / (1024 * 1024);
                long memoriaUsada = memoriaTotal - memoriaLivre;

                relatorio.append(" | Memoria: ").append(memoriaUsada).append("MB/").append(memoriaTotal).append("MB");

                // Processadores
                int processadores = runtime.availableProcessors();
                relatorio.append(" | CPUs: ").append(processadores);

                // Diretório home
                String userHome = System.getProperty("user.home");
                relatorio.append(" | Home: ").append(userHome);

                // Arquitetura
                String arch = System.getProperty("os.arch");
                relatorio.append(" | Arquitetura: ").append(arch);
            }

        } catch (Exception e) {
            relatorio.append(" | ERRO ao coletar dados: ").append(e.getMessage());
        }

        return relatorio.toString();
    }

    // PAINEL COM GRADIENTE
    class PainelGradiente extends JPanel {
        private Color cor1, cor2;

        PainelGradiente(Color cor1, Color cor2) {
            this.cor1 = cor1;
            this.cor2 = cor2;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            GradientPaint gp = new GradientPaint(0, 0, cor1, 0, getHeight(), cor2);
            g2d.setPaint(gp);
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    // PAINEL ARREDONDADO COM SOMBRA
    class PainelArredondado extends JPanel {
        private int radius;

        PainelArredondado(int radius) {
            this.radius = radius;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Sombra
            g2.setColor(new Color(0, 0, 0, 30));
            g2.fill(new RoundRectangle2D.Double(2, 2, getWidth() - 4, getHeight() - 4, radius, radius));

            // Fundo
            g2.setColor(getBackground());
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 4, getHeight() - 4, radius, radius));

            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}

        SwingUtilities.invokeLater(() -> {
            ClienteOSINT cliente = new ClienteOSINT();
            cliente.setVisible(true);
        });
    }
}
