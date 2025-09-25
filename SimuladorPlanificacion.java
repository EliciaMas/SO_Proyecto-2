import javax.swing.*; 
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.List;

public class SimuladorPlanificacion extends JFrame {
    private JComboBox<String> algoritmoCombo;
    private JButton btnAgregarProceso, btnIniciarSimulacion, btnLimpiarHistorial;
    private JTextField txtNombre, txtTiempoCPU, txtLlegada, txtQuantum;
    private JTable colaProcesosTable, historialTable;
    private DefaultTableModel modeloCola, modeloHistorial;
    private List<Proceso> procesos;
    private int nextPID = 1;

    // Constante para definir la duración de cada unidad de tiempo (5 segundos)
    private static final int TIEMPO_UNIDAD_MS = 5000;

    public SimuladorPlanificacion() {
        setTitle("Simulador de Planificacion de Procesos");
        setSize(900, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        procesos = new ArrayList<>();

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(245, 245, 245));

        // Panel superior para agregar procesos
        JPanel panelEntrada = new JPanel(new GridLayout(2,6,5,5));
        panelEntrada.setBorder(BorderFactory.createTitledBorder("Agregar Proceso"));
        panelEntrada.setBackground(new Color(220, 235, 245));

        txtNombre = new JTextField();
        txtTiempoCPU = new JTextField();
        txtLlegada = new JTextField();
        txtQuantum = new JTextField();
        txtQuantum.setEnabled(false);

        panelEntrada.add(new JLabel("Nombre"));
        panelEntrada.add(new JLabel("Tiempo CPU (unids)"));
        panelEntrada.add(new JLabel("Instante Llegada (unids)"));
        panelEntrada.add(new JLabel("Quantum (unids)"));
        panelEntrada.add(new JLabel("Algoritmo"));
        panelEntrada.add(new JLabel(""));

        panelEntrada.add(txtNombre);
        panelEntrada.add(txtTiempoCPU);
        panelEntrada.add(txtLlegada);
        panelEntrada.add(txtQuantum);

        algoritmoCombo = new JComboBox<>(new String[]{"FCFS", "SJF", "SRTF", "Round Robin"});
        panelEntrada.add(algoritmoCombo);

        btnAgregarProceso = new JButton("Agregar Proceso");
        btnAgregarProceso.setBackground(new Color(100, 149, 237));
        btnAgregarProceso.setForeground(Color.WHITE);
        panelEntrada.add(btnAgregarProceso);

        panel.add(panelEntrada, BorderLayout.NORTH);

        // Tablas
        modeloCola = new DefaultTableModel(new String[]{"PID", "Nombre", "Tiempo CPU", "Llegada", "Quantum Restante"}, 0);
        colaProcesosTable = new JTable(modeloCola);
        modeloHistorial = new DefaultTableModel(new String[]{"PID", "Nombre", "Tiempo Ejecutado"}, 0);
        historialTable = new JTable(modeloHistorial);

        JPanel panelCentro = new JPanel(new GridLayout(1,2));
        panelCentro.add(new JScrollPane(colaProcesosTable));
        panelCentro.add(new JScrollPane(historialTable));
        panel.add(panelCentro, BorderLayout.CENTER);

        // Panel inferior con botones
        JPanel panelBotones = new JPanel();
        panelBotones.setBackground(new Color(245, 245, 245));

        btnIniciarSimulacion = new JButton("Iniciar Simulacion");
        btnIniciarSimulacion.setBackground(new Color(60, 179, 113));
        btnIniciarSimulacion.setForeground(Color.WHITE);

        btnLimpiarHistorial = new JButton("Limpiar Historial y Cola");
        btnLimpiarHistorial.setBackground(new Color(220, 20, 60));
        btnLimpiarHistorial.setForeground(Color.WHITE);

        panelBotones.add(btnIniciarSimulacion);
        panelBotones.add(btnLimpiarHistorial);
        panel.add(panelBotones, BorderLayout.SOUTH);

        add(panel);

        // Eventos
        algoritmoCombo.addActionListener(e -> {
            String algoritmo = (String) algoritmoCombo.getSelectedItem();
            txtQuantum.setEnabled("Round Robin".equals(algoritmo));
        });

        btnAgregarProceso.addActionListener(e -> agregarProceso());

        btnIniciarSimulacion.addActionListener(e -> iniciarSimulacion());

        btnLimpiarHistorial.addActionListener(e -> {
            procesos.clear();  // Limpia la lista de procesos
            modeloCola.setRowCount(0); // Limpia la tabla de cola
            modeloHistorial.setRowCount(0); // Limpia la tabla de historial
            nextPID = 1; // Reiniciar contador de PID
            JOptionPane.showMessageDialog(this, "Historial y cola de procesos limpiados.");
        });
    }

    private void agregarProceso() {
        try {
            String nombre = txtNombre.getText().trim();
            int tiempoCPU = Integer.parseInt(txtTiempoCPU.getText().trim());
            int instanteLlegada = Integer.parseInt(txtLlegada.getText().trim());
            int quantum = 0;
            if(txtQuantum.isEnabled()) {
                quantum = Integer.parseInt(txtQuantum.getText().trim());
            }
            if(nombre.isEmpty() || tiempoCPU<=0 || instanteLlegada<0 || (txtQuantum.isEnabled() && quantum<=0)) {
                JOptionPane.showMessageDialog(this, "Ingrese valores válidos");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this, "Desea agregar el proceso?", "Confirmacin", JOptionPane.YES_NO_OPTION);
            if(confirm == JOptionPane.NO_OPTION) return;

            Proceso p = new Proceso(nextPID++, nombre, tiempoCPU, instanteLlegada, quantum);
            procesos.add(p);
            JOptionPane.showMessageDialog(this, "Proceso agregado: "+nombre);
            limpiarCampos();
            actualizarTablaCola();
        } catch(NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Ingrese solo números en campos correspondientes");
        }
    }

    private void limpiarCampos() {
        txtNombre.setText("");
        txtTiempoCPU.setText("");
        txtLlegada.setText("");
        txtQuantum.setText("");
    }

    private void actualizarTablaCola() {
        modeloCola.setRowCount(0);
        for(Proceso p: procesos) {
            modeloCola.addRow(new Object[]{p.pid, p.nombre, p.tiempoCPU, p.instanteLlegada, p.quantumRestante});
        }
    }

    private void iniciarSimulacion() {
        String algoritmo = (String) algoritmoCombo.getSelectedItem();
        if(procesos.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Agregue al menos un proceso");
            return;
        }
        procesos.sort(Comparator.comparingInt(p -> p.instanteLlegada));
        Simulador simulador = new Simulador(procesos, algoritmo);
        simulador.start();
    }

    private class Simulador extends Thread {
        private List<Proceso> listaProcesos;
        private String algoritmo;
        private int tiempoSistema = 0;
        private List<Proceso> historial = new ArrayList<>();

        public Simulador(List<Proceso> procesos, String algoritmo) {
            listaProcesos = new ArrayList<>();
            for(Proceso p: procesos) listaProcesos.add(new Proceso(p));
            this.algoritmo = algoritmo;
        }

        @Override
        public void run() {
            List<Proceso> colaListos = new ArrayList<>();
            int quantumRR = 0;

            while(!colaListos.isEmpty() || !listaProcesos.isEmpty()) {
                Iterator<Proceso> it = listaProcesos.iterator();
                while(it.hasNext()) {
                    Proceso p = it.next();
                    if(p.instanteLlegada <= tiempoSistema) {
                        colaListos.add(p);
                        it.remove();
                    }
                }

                if(colaListos.isEmpty()) {
                    tiempoSistema++;
                    try { Thread.sleep(TIEMPO_UNIDAD_MS);} catch(Exception ex) {}
                    continue;
                }
                Proceso actual = null;

                switch(algoritmo) {
                    case "FCFS":
                        actual = colaListos.get(0);
                        runProceso(actual, colaListos, historial, actual.tiempoCPU);
                        tiempoSistema += actual.tiempoCPU;
                        break;
                    case "SJF":
                        colaListos.sort(Comparator.comparingInt(p -> p.tiempoCPU));
                        actual = colaListos.get(0);
                        runProceso(actual, colaListos, historial, actual.tiempoCPU);
                        tiempoSistema += actual.tiempoCPU;
                        break;
                    case "SRTF":
                        colaListos.sort(Comparator.comparingInt(p -> p.tiempoCPU - p.tiempoEjecutado));
                        actual = colaListos.get(0);
                        runProceso(actual, colaListos, historial, 1);
                        tiempoSistema++;
                        break;
                    case "Round Robin":
                        actual = colaListos.get(0);
                        quantumRR = actual.quantumRestante > 0 ? actual.quantumRestante : actual.quantum;
                        int step = Math.min(quantumRR, actual.tiempoCPU - actual.tiempoEjecutado);
                        runProceso(actual, colaListos, historial, step);
                        tiempoSistema += step;
                        break;
                }
                actualizarTablaColaSim(colaListos);
                actualizarTablaHistorial(historial);
            }

            // Construir mensaje final
            StringBuilder resultado = new StringBuilder("Simulacion finalizada en " + tiempoSistema + " unidades de tiempo.\n\n");
            resultado.append("Tiempos de finalizacion por proceso:\n");

            for(Proceso p : historial) {
                resultado.append("PID ").append(p.pid)
                         .append(" (").append(p.nombre).append(") : ")
                         .append(p.tiempoFinalizacion).append(" unidades\n");
            }

            JOptionPane.showMessageDialog(null, resultado.toString());
        }

        private void runProceso(Proceso p, List<Proceso> cola, List<Proceso> hist, int tiempoEjecutar) {
            for(int i=0;i<tiempoEjecutar;i++) {
                p.tiempoEjecutado++;
                if(algoritmo.equals("Round Robin")) {
                    p.quantumRestante--;
                }
                try { Thread.sleep(TIEMPO_UNIDAD_MS);} catch(Exception ex) {}
            }
            if(p.tiempoEjecutado >= p.tiempoCPU) {
                cola.remove(p);
                hist.add(p);
                p.quantumRestante = p.quantum;
                p.tiempoFinalizacion = tiempoSistema + tiempoEjecutar; // Guardar finalización
            } else if(algoritmo.equals("Round Robin")) {
                cola.remove(p);
                cola.add(p);
                p.quantumRestante = p.quantum;
            }
        }

        private void actualizarTablaColaSim(List<Proceso> cola) {
            SwingUtilities.invokeLater(() -> {
                modeloCola.setRowCount(0);
                for(Proceso p: cola) {
                    modeloCola.addRow(new Object[]{p.pid, p.nombre, p.tiempoCPU, p.instanteLlegada, p.quantumRestante});
                }
            });
        }

        private void actualizarTablaHistorial(List<Proceso> hist) {
            SwingUtilities.invokeLater(() -> {
                modeloHistorial.setRowCount(0);
                for(Proceso p: hist) {
                    modeloHistorial.addRow(new Object[]{p.pid, p.nombre, p.tiempoEjecutado});
                }
            });
        }
    }

    private class Proceso {
        int pid;
        String nombre;
        int tiempoCPU;
        int instanteLlegada;
        int quantum;
        int quantumRestante;
        int tiempoEjecutado;
        int tiempoFinalizacion = -1; // Nuevo atributo

        public Proceso(int pid, String nombre, int tiempoCPU, int instanteLlegada, int quantum) {
            this.pid = pid;
            this.nombre = nombre;
            this.tiempoCPU = tiempoCPU;
            this.instanteLlegada = instanteLlegada;
            this.quantum = quantum;
            this.quantumRestante = quantum;
            this.tiempoEjecutado = 0;
        }

        public Proceso(Proceso p) {
            this.pid = p.pid;
            this.nombre = p.nombre;
            this.tiempoCPU = p.tiempoCPU;
            this.instanteLlegada = p.instanteLlegada;
            this.quantum = p.quantum;
            this.quantumRestante = p.quantumRestante;
            this.tiempoEjecutado = p.tiempoEjecutado;
            this.tiempoFinalizacion = p.tiempoFinalizacion;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new SimuladorPlanificacion().setVisible(true);
        });
    }
}
