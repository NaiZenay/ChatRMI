package UI;
import javax.swing.*;
import java.awt.*;

public class MensajePanel extends JPanel {
    private JLabel lblEmisor;
    private JTextPane txtMensaje;
    private static final int MAX_WIDTH = 400;
    private static final int PADDING = 12;

    public MensajePanel(String emisor, String mensaje) {
        setLayout(new BorderLayout());
        setOpaque(false);

        // Panel interno que contendrá todo
        JPanel contenido = new JPanel();
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));
        contenido.setOpaque(false);

        // Configurar el label del emisor
        lblEmisor = new JLabel(emisor);
        lblEmisor.setFont(new Font("Arial", Font.BOLD, 11));
        lblEmisor.setForeground(Color.LIGHT_GRAY);
        lblEmisor.setBorder(BorderFactory.createEmptyBorder(8, PADDING, 2, PADDING));
        lblEmisor.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Usar JTextPane en lugar de JTextArea para mejor manejo de tamaño
        txtMensaje = new JTextPane();
        txtMensaje.setText(mensaje);
        txtMensaje.setFont(new Font("Arial", Font.PLAIN, 14));
        txtMensaje.setForeground(Color.WHITE);
        txtMensaje.setBackground(new Color(60, 60, 60));
        txtMensaje.setEditable(false);
        txtMensaje.setBorder(BorderFactory.createEmptyBorder(5, PADDING, 10, PADDING));
        txtMensaje.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Agregar componentes
        contenido.add(lblEmisor);
        contenido.add(txtMensaje);
        add(contenido, BorderLayout.CENTER);

        // Calcular y establecer tamaño
        ajustarTamano(mensaje);
    }

    private void ajustarTamano(String mensaje) {
        // Obtener métricas de fuente
        FontMetrics fm = getFontMetrics(new Font("Arial", Font.PLAIN, 14));

        // Calcular ancho disponible para el texto
        int anchoDisponible = MAX_WIDTH - (PADDING * 2);

        // Dividir el mensaje en palabras para calcular líneas
        String[] palabras = mensaje.split("\\s+");
        int lineaActual = 0;
        int anchoLinea = 0;
        int numLineas = 1;

        for (String palabra : palabras) {
            int anchoPalabra = fm.stringWidth(palabra + " ");

            if (anchoLinea + anchoPalabra > anchoDisponible) {
                numLineas++;
                anchoLinea = anchoPalabra;
            } else {
                anchoLinea += anchoPalabra;
            }
        }

        // Calcular altura total
        int alturaLinea = fm.getHeight();
        int alturaTexto = (numLineas * alturaLinea) + 15; // +15 para padding interno
        int alturaEmisor = 25; // Altura del label del emisor
        int alturaTotal = alturaEmisor + alturaTexto + 10; // +10 margen adicional

        // Establecer tamaños
        Dimension size = new Dimension(MAX_WIDTH, alturaTotal);
        setPreferredSize(size);
        setMinimumSize(new Dimension(MAX_WIDTH, 60));
        setMaximumSize(new Dimension(MAX_WIDTH, Integer.MAX_VALUE));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();

        // Activar antialiasing para bordes suaves
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Dibujar fondo redondeado
        g2d.setColor(new Color(60, 60, 60));
        g2d.fillRoundRect(0, 20, getWidth() - 1, getHeight() - 21, 15, 15);

        g2d.dispose();
    }

    // Métodos para actualizar el contenido si es necesario
    public void setEmisor(String emisor) {
        lblEmisor.setText(emisor);
    }

    public void setMensaje(String mensaje) {
        txtMensaje.setText(mensaje);
        ajustarTamano(mensaje);
        revalidate();
        repaint();
    }
}