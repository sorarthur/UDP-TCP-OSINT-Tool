import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ServidorOSINT extends JFrame {
    // Configurações
    private static final int PORTA_TCP = 6700;
    private static final int PORTA_UDP = 9111;
    private static final String PASTA_ARQUIVOS = "arquivos_recebidos";
    private static final String PASTA_LOGS = "logs";

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
    private JLabel statusLabel;
    private JButton btnIniciar;
    private JButton btnParar;
    private JButton btnExportarLog;

    // Estatísticas
    private JLabel lblTotalPings;
    private JLabel lblTotalRelatorios;
    private JLabel lblTotalArquivos;
    private JLabel lblClientesAtivos;
    private JTable tabelaClientes;
    private DefaultTableModel modeloTabela;

    // Controle de threads
    private Thread threadTCP;
    private Thread threadUDP;
    private volatile boolean rodando = false;

    // Estatísticas
    private AtomicInteger contadorPings = new AtomicInteger(0);
    private AtomicInteger contadorRelatorios = new AtomicInteger(0);
    private AtomicInteger contadorArquivos = new AtomicInteger(0);
    private Map<String, Long> clientesAtivos = new ConcurrentHashMap<>();

    // Buffer de log
    private StringBuilder logBuffer = new StringBuilder();

    public ServidorOSINT() {
        configurarGUI();
        criarPastas();
    }

    private void criarPastas() {
        new File(PASTA_ARQUIVOS).mkdirs();
        new File(PASTA_LOGS).mkdirs();
    }

    private void configurarGUI() {
        setTitle("SERVIDOR OSINT");
        setSize(1000, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(COR_FUNDO);
        setMinimumSize(new Dimension(900, 650));

        // Painel superior com GRADIENTE
        JPanel painelTopo = new PainelGradiente(
            new Color(26, 35, 126),
            new Color(13, 71, 161)
        );
        painelTopo.setLayout(new GridLayout(3, 1, 0, 3));
        painelTopo.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        JLabel titulo = new JLabel("SERVIDOR OSINT", SwingConstants.CENTER);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titulo.setForeground(Color.WHITE);

        JLabel subtitulo = new JLabel("Central de Monitoramento e Controle", SwingConstants.CENTER);
        subtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitulo.setForeground(new Color(200, 200, 255));

        JPanel panelInfo = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        panelInfo.setOpaque(false);

        statusLabel = new JLabel("PARADO", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        statusLabel.setForeground(new Color(255, 235, 59));

        JLabel infoPortas = new JLabel("TCP:" + PORTA_TCP + " | UDP:" + PORTA_UDP);
        infoPortas.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        infoPortas.setForeground(new Color(180, 180, 220));

        panelInfo.add(statusLabel);
        panelInfo.add(new JLabel(" | ") {{ setForeground(new Color(150, 150, 200)); }});
        panelInfo.add(infoPortas);

        painelTopo.add(titulo);
        painelTopo.add(subtitulo);
        painelTopo.add(panelInfo);

        // Painel de estatísticas com CARDS ELEVADOS
        JPanel painelStats = new JPanel(new GridLayout(1, 4, 15, 0));
        painelStats.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        painelStats.setBackground(COR_FUNDO);

        lblTotalPings = criarLabelStat("0");
        lblTotalRelatorios = criarLabelStat("0");
        lblTotalArquivos = criarLabelStat("0");
        lblClientesAtivos = criarLabelStat("0");

        painelStats.add(criarCardStat("PING", "PINGs", lblTotalPings, COR_AZUL));
        painelStats.add(criarCardStat("RELAT", "Relatorios", lblTotalRelatorios, COR_VERDE));
        painelStats.add(criarCardStat("ARQS", "Arquivos", lblTotalArquivos, COR_ROXO));
        painelStats.add(criarCardStat("USERS", "Ativos", lblClientesAtivos, COR_LARANJA));

        // Tabela de clientes
        String[] colunas = {"IP Cliente", "Última Atividade", "Status"};
        modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabelaClientes = new JTable(modeloTabela);
        tabelaClientes.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabelaClientes.setRowHeight(35);
        tabelaClientes.setBackground(COR_CARD);
        tabelaClientes.setForeground(Color.WHITE);
        tabelaClientes.setGridColor(new Color(50, 50, 50));
        tabelaClientes.setSelectionBackground(new Color(50, 50, 70));
        tabelaClientes.setSelectionForeground(Color.WHITE);
        tabelaClientes.getTableHeader().setBackground(new Color(25, 25, 25));
        tabelaClientes.getTableHeader().setForeground(Color.WHITE);
        tabelaClientes.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));

        // Renderer customizado para garantir texto branco legível
        tabelaClientes.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (isSelected) {
                    c.setBackground(new Color(50, 50, 70));
                    c.setForeground(Color.WHITE);
                } else {
                    c.setBackground(COR_CARD);
                    c.setForeground(new Color(230, 230, 230));
                }
                c.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                if (c instanceof JComponent) {
                    ((JComponent)c).setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                }
                return c;
            }
        });

        JScrollPane scrollTabela = new JScrollPane(tabelaClientes);
        scrollTabela.setBorder(BorderFactory.createEmptyBorder());
        scrollTabela.setBackground(COR_CARD);
        scrollTabela.setPreferredSize(new Dimension(0, 130));

        PainelArredondado containerTabela = new PainelArredondado(20);
        containerTabela.setBackground(COR_CARD);
        containerTabela.setLayout(new BorderLayout());
        containerTabela.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel tituloTabela = new JLabel("Clientes Conectados");
        tituloTabela.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tituloTabela.setForeground(Color.WHITE);
        tituloTabela.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        containerTabela.add(tituloTabela, BorderLayout.NORTH);
        containerTabela.add(scrollTabela, BorderLayout.CENTER);

        // Área de log
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("JetBrains Mono", Font.PLAIN, 12));
        logArea.setBackground(new Color(15, 15, 15));
        logArea.setForeground(new Color(0, 255, 0));
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        logArea.setMargin(new Insets(12, 12, 12, 12));

        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        PainelArredondado containerLog = new PainelArredondado(20);
        containerLog.setBackground(COR_CARD);
        containerLog.setLayout(new BorderLayout());
        containerLog.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel tituloLog = new JLabel("Log de Eventos");
        tituloLog.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tituloLog.setForeground(Color.WHITE);
        tituloLog.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        containerLog.add(tituloLog, BorderLayout.NORTH);
        containerLog.add(scrollPane, BorderLayout.CENTER);

        // Painel central
        JPanel painelCentral = new JPanel(new BorderLayout(0, 12));
        painelCentral.setBackground(COR_FUNDO);
        painelCentral.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));
        painelCentral.add(containerTabela, BorderLayout.NORTH);
        painelCentral.add(containerLog, BorderLayout.CENTER);

        // Painel de controle com BOTÕES MODERNOS
        JPanel painelControle = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        painelControle.setBackground(COR_FUNDO);

        btnIniciar = criarBotaoModerno("INICIAR", COR_VERDE);
        btnIniciar.addActionListener(e -> iniciarServidor());

        btnParar = criarBotaoModerno("PARAR", COR_VERMELHO);
        btnParar.setEnabled(false);
        btnParar.addActionListener(e -> pararServidor());

        btnExportarLog = criarBotaoModerno("EXPORTAR", COR_AZUL);
        btnExportarLog.addActionListener(e -> exportarLog());

        painelControle.add(btnIniciar);
        painelControle.add(btnParar);
        painelControle.add(btnExportarLog);

        // Montar janela
        JPanel painelPrincipal = new JPanel(new BorderLayout(0, 0));
        painelPrincipal.setBackground(COR_FUNDO);
        painelPrincipal.add(painelStats, BorderLayout.NORTH);
        painelPrincipal.add(painelCentral, BorderLayout.CENTER);

        add(painelTopo, BorderLayout.NORTH);
        add(painelPrincipal, BorderLayout.CENTER);
        add(painelControle, BorderLayout.SOUTH);

        setLocationRelativeTo(null);
        iniciarMonitorClientes();
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

    private JButton criarBotaoModerno(String texto, Color cor) {
        JButton btn = new JButton(texto) {
            @Override
            public void setEnabled(boolean enabled) {
                super.setEnabled(enabled);
                // Manter texto branco mesmo quando desabilitado
                setForeground(Color.WHITE);
                if (!enabled) {
                    setBackground(cor.darker());
                } else {
                    setBackground(cor);
                }
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(cor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setPreferredSize(new Dimension(160, 45));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // EFEITO HOVER (apenas quando habilitado)
        btn.addMouseListener(new MouseAdapter() {
            Color corOriginal = cor;
            @Override
            public void mouseEntered(MouseEvent e) {
                if (btn.isEnabled()) {
                    btn.setBackground(corOriginal.brighter());
                }
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if (btn.isEnabled()) {
                    btn.setBackground(corOriginal);
                } else {
                    btn.setBackground(corOriginal.darker());
                }
            }
        });

        return btn;
    }

    private JLabel criarLabelStat(String valor) {
        JLabel label = new JLabel(valor);
        label.setFont(new Font("Segoe UI", Font.BOLD, 42));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setForeground(Color.WHITE);
        return label;
    }

    private JPanel criarCardStat(String icone, String titulo, JLabel valorLabel, Color corDestaque) {
        PainelArredondado card = new PainelArredondado(20);
        card.setBackground(COR_CARD);
        card.setLayout(new BorderLayout(10, 10));
        card.setBorder(BorderFactory.createEmptyBorder(20, 15, 15, 15));
        card.setPreferredSize(new Dimension(220, 130));

        JLabel lblIcone = new JLabel(icone);
        lblIcone.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblIcone.setHorizontalAlignment(SwingConstants.CENTER);
        lblIcone.setVerticalAlignment(SwingConstants.CENTER);
        lblIcone.setForeground(corDestaque);
        lblIcone.setBorder(BorderFactory.createEmptyBorder(5, 0, 3, 0));

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitulo.setForeground(new Color(220, 220, 220));
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel painelTopo = new JPanel(new GridLayout(2, 1, 0, 5));
        painelTopo.setOpaque(false);
        painelTopo.add(lblIcone);
        painelTopo.add(lblTitulo);

        card.add(painelTopo, BorderLayout.NORTH);
        card.add(valorLabel, BorderLayout.CENTER);

        return card;
    }

    private void atualizarEstatistica(JLabel label, int valor) {
        SwingUtilities.invokeLater(() -> label.setText(String.valueOf(valor)));
    }

    private void iniciarMonitorClientes() {
        Thread monitor = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(5000);
                    atualizarTabelaClientes();
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        monitor.setDaemon(true);
        monitor.start();
    }

    private void atualizarTabelaClientes() {
        SwingUtilities.invokeLater(() -> {
            modeloTabela.setRowCount(0);
            long agora = System.currentTimeMillis();
            int ativos = 0;

            for (Map.Entry<String, Long> entry : clientesAtivos.entrySet()) {
                String ip = entry.getKey();
                long ultimaAtividade = entry.getValue();
                long diferenca = (agora - ultimaAtividade) / 1000;

                String status;
                if (diferenca < 30) {
                    status = "ATIVO";
                    ativos++;
                } else if (diferenca < 60) {
                    status = "INATIVO";
                } else {
                    status = "OFFLINE";
                }

                String tempo = new SimpleDateFormat("HH:mm:ss").format(new Date(ultimaAtividade));
                modeloTabela.addRow(new Object[]{ip, tempo + " (" + diferenca + "s)", status});
            }

            atualizarEstatistica(lblClientesAtivos, ativos);
        });
    }

    private void registrarAtividade(String ip) {
        clientesAtivos.put(ip, System.currentTimeMillis());
        atualizarTabelaClientes();
    }

    private void log(String mensagem) {
        SwingUtilities.invokeLater(() -> {
            String timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
            String linha = "[" + timestamp + "] " + mensagem + "\n";
            logArea.append(linha);
            logArea.setCaretPosition(logArea.getDocument().getLength());
            logBuffer.append(linha);
        });
    }

    private void exportarLog() {
        try {
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String nomeArquivo = PASTA_LOGS + "/log_servidor_" + timestamp + ".txt";

            FileWriter writer = new FileWriter(nomeArquivo);
            writer.write("=== LOG DO SERVIDOR OSINT ===\n");
            writer.write("Data/Hora: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "\n");
            writer.write("=====================================\n\n");
            writer.write(logBuffer.toString());
            writer.close();

            log("Log exportado: " + nomeArquivo);
            JOptionPane.showMessageDialog(this, "Log exportado com sucesso!\n" + nomeArquivo,
                "Exportacao Concluida", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            log("ERRO ao exportar log: " + e.getMessage());
        }
    }

    private void iniciarServidor() {
        rodando = true;
        btnIniciar.setEnabled(false);
        btnParar.setEnabled(true);
        statusLabel.setText("RODANDO");
        statusLabel.setForeground(COR_VERDE);

        log("=== SERVIDOR INICIADO ===");
        log("Aguardando conexões...");

        threadTCP = new Thread(this::executarServidorTCP);
        threadTCP.setDaemon(true);
        threadTCP.start();

        threadUDP = new Thread(this::executarServidorUDP);
        threadUDP.setDaemon(true);
        threadUDP.start();
    }

    private void pararServidor() {
        rodando = false;
        btnIniciar.setEnabled(true);
        btnParar.setEnabled(false);
        statusLabel.setText("PARADO");
        statusLabel.setForeground(new Color(255, 235, 59));

        log("=== SERVIDOR PARADO ===");
    }

    private void executarServidorTCP() {
        try (ServerSocket serverSocket = new ServerSocket(PORTA_TCP)) {
            log("[TCP] Servidor TCP iniciado na porta " + PORTA_TCP);

            while (rodando) {
                try {
                    serverSocket.setSoTimeout(1000);
                    Socket cliente = serverSocket.accept();
                    new Thread(() -> processarClienteTCP(cliente)).start();
                } catch (SocketTimeoutException e) {
                    // Timeout normal
                }
            }
        } catch (IOException e) {
            log("[TCP] ERRO: " + e.getMessage());
        }
    }

    private void processarClienteTCP(Socket cliente) {
        try {
            String ipCliente = cliente.getInetAddress().getHostAddress();
            registrarAtividade(ipCliente);

            DataInputStream entrada = new DataInputStream(cliente.getInputStream());
            String tipoMensagem = entrada.readUTF();

            if (tipoMensagem.equals("RELATORIO")) {
                String relatorio = entrada.readUTF();

                log("[TCP] ========================================");
                log("[TCP] RELATÓRIO RECEBIDO DE: " + ipCliente);
                log("[TCP] " + relatorio);
                log("[TCP] ========================================");

                contadorRelatorios.incrementAndGet();
                atualizarEstatistica(lblTotalRelatorios, contadorRelatorios.get());

                DataOutputStream saida = new DataOutputStream(cliente.getOutputStream());
                saida.writeUTF("Relatorio recebido com sucesso!");

            } else if (tipoMensagem.equals("ARQUIVO")) {
                String nomeArquivo = entrada.readUTF();
                long tamanhoArquivo = entrada.readLong();

                log("[TCP] Recebendo arquivo: " + nomeArquivo + " (" + formatarTamanho(tamanhoArquivo) + ")");

                File arquivo = new File(PASTA_ARQUIVOS + "/" + nomeArquivo);
                FileOutputStream fos = new FileOutputStream(arquivo);

                byte[] buffer = new byte[4096];
                int bytesLidos;
                long totalLido = 0;

                while (totalLido < tamanhoArquivo && (bytesLidos = entrada.read(buffer, 0,
                        (int)Math.min(buffer.length, tamanhoArquivo - totalLido))) != -1) {
                    fos.write(buffer, 0, bytesLidos);
                    totalLido += bytesLidos;
                }

                fos.close();

                log("[TCP] OK - Arquivo recebido: " + nomeArquivo);

                contadorArquivos.incrementAndGet();
                atualizarEstatistica(lblTotalArquivos, contadorArquivos.get());

                DataOutputStream saida = new DataOutputStream(cliente.getOutputStream());
                saida.writeUTF("Arquivo recebido!");
            }

            cliente.close();

        } catch (IOException e) {
            log("[TCP] Erro: " + e.getMessage());
        }
    }

    private String formatarTamanho(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
    }

    private void executarServidorUDP() {
        try (DatagramSocket socket = new DatagramSocket(PORTA_UDP)) {
            log("[UDP] Servidor UDP iniciado na porta " + PORTA_UDP);
            socket.setSoTimeout(1000);

            byte[] buffer = new byte[1024];

            while (rodando) {
                try {
                    DatagramPacket pacote = new DatagramPacket(buffer, buffer.length);
                    socket.receive(pacote);

                    String mensagem = new String(pacote.getData(), 0, pacote.getLength());
                    String ipCliente = pacote.getAddress().getHostAddress();

                    registrarAtividade(ipCliente);

                    log("[UDP] PING de: " + ipCliente + " | " + mensagem);

                    contadorPings.incrementAndGet();
                    atualizarEstatistica(lblTotalPings, contadorPings.get());

                } catch (SocketTimeoutException e) {
                    // Timeout normal
                }
            }

        } catch (IOException e) {
            log("[UDP] ERRO: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}

        SwingUtilities.invokeLater(() -> {
            ServidorOSINT servidor = new ServidorOSINT();
            servidor.setVisible(true);
        });
    }
}
