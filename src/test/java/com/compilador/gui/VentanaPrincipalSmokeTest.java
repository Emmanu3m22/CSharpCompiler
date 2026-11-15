package com.compilador.gui;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de humo y comportamiento del marco de la ventana (barra de título propia
 * y pantalla de bienvenida con disposición lateral estilo IDE).
 * Se omiten en entornos sin pantalla.
 */
public class VentanaPrincipalSmokeTest {

    @Test
    @DisplayName("La ventana principal se construye sin decoración del sistema y con barra propia")
    void construyeVentanaPersonalizada() throws Exception {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(), "Sin pantalla: se omite");

        AtomicBoolean sinDecoracion = new AtomicBoolean();
        SwingUtilities.invokeAndWait(() -> {
            AplicacionPrincipal ventana = new AplicacionPrincipal();
            sinDecoracion.set(ventana.isUndecorated());
            ventana.setTitle("Prueba");
            ventana.dispose();
        });
        assertTrue(sinDecoracion.get(), "La ventana debe ser undecorated para usar la barra propia");
    }

    @Test
    @DisplayName("BarraTitulo actualiza el título y alterna maximizado/restaurado")
    void barraTituloAlternaMaximizado() throws Exception {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(), "Sin pantalla: se omite");

        SwingUtilities.invokeAndWait(() -> {
            JFrame f = new JFrame();
            f.setUndecorated(true);
            f.setSize(600, 400);
            BarraTitulo barra = new BarraTitulo(f, "Inicial");
            f.add(barra, BorderLayout.NORTH);
            BarraTitulo.instalarRedimension(f, barra);
            f.setVisible(true);

            barra.setTitulo("Nuevo título");
            assertFalse(barra.estaMaximizada());
            barra.alternarMaximizada();
            assertTrue(barra.estaMaximizada());
            assertTrue(f.getWidth() > 600);
            barra.alternarMaximizada();
            assertFalse(barra.estaMaximizada());
            assertEquals(600, f.getWidth());
            f.dispose();
        });
    }

    @Test
    @DisplayName("PantallaBienvenida usa disposición lateral izquierda y no centrado flotante")
    void bienvenidaUsaDisposicionLateralIzquierda() throws Exception {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(), "Sin pantalla: se omite");

        SwingUtilities.invokeAndWait(() -> {
            PantallaBienvenida p = new PantallaBienvenida(() -> {}, () -> {}, i -> {}, f -> {}, () -> {});
            assertInstanceOf(BorderLayout.class, p.getLayout(),
                "La bienvenida debe usar BorderLayout con barra lateral izquierda");

            JFrame contenedor = new JFrame();
            contenedor.setUndecorated(true);
            contenedor.setSize(1200, 760);
            contenedor.add(p, BorderLayout.CENTER);
            contenedor.doLayout();
            p.doLayout();

            Component lateral = buscarPorNombre(p, "panel-lateral-bienvenida");
            Component principal = buscarPorNombre(p, "panel-principal-bienvenida");
            Component gridEjemplos = buscarPorNombre(p, "grid-ejemplos");

            assertNotNull(lateral, "Debe existir el panel lateral izquierdo");
            assertNotNull(principal, "Debe existir el panel principal de contenido");
            assertNotNull(gridEjemplos, "Debe existir la rejilla de plantillas y ejemplos");

            assertEquals(0, lateral.getX(), "El panel lateral debe estar pegado al borde izquierdo (x=0)");
            assertEquals(300, lateral.getPreferredSize().width, "El panel lateral debe tener ancho fijo de IDE");
            assertTrue(principal.getX() >= lateral.getWidth(),
                "El panel principal debe ubicarse inmediatamente a la derecha de la barra lateral");
            assertEquals(Ejemplos.cantidad(), ((Container) gridEjemplos).getComponentCount(),
                "Deben mostrarse todas las tarjetas de ejemplo disponibles");

            contenedor.dispose();
        });
    }

    @Test
    @DisplayName("PantallaBienvenida dispara callbacks de acciones laterales, ejemplos y archivos recientes")
    void bienvenidaEjecutaAccionesYRecientes() throws Exception {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(), "Sin pantalla: se omite");

        File tempReciente = Files.createTempFile("prueba_bienvenida_", ".cs").toFile();
        tempReciente.deleteOnExit();
        PreferenciasApp.agregarReciente(tempReciente);

        AtomicBoolean nuevoLlamado = new AtomicBoolean(false);
        AtomicBoolean abrirLlamado = new AtomicBoolean(false);
        AtomicBoolean continuarLlamado = new AtomicBoolean(false);
        AtomicInteger ejemploElegido = new AtomicInteger(-1);
        AtomicReference<File> recienteAbierto = new AtomicReference<>();

        SwingUtilities.invokeAndWait(() -> {
            PantallaBienvenida p = new PantallaBienvenida(
                () -> nuevoLlamado.set(true),
                () -> abrirLlamado.set(true),
                ejemploElegido::set,
                recienteAbierto::set,
                () -> continuarLlamado.set(true)
            );
            p.actualizarRecientes();

            Component btnNuevo = buscarPorNombre(p, "accion-nuevo");
            Component btnAbrir = buscarPorNombre(p, "accion-abrir");
            Component btnContinuar = buscarPorNombre(p, "accion-continuar");
            Component tarjetaEjemplo = buscarPorNombre(p, "ejemplo-2");
            Component filaReciente = buscarPorNombre(p, "reciente-" + tempReciente.getName());

            assertNotNull(btnNuevo, "Debe existir el botón lateral 'Nuevo archivo'");
            assertNotNull(btnAbrir, "Debe existir el botón lateral 'Abrir archivo…'");
            assertNotNull(btnContinuar, "Debe existir el botón lateral 'Ir al editor'");
            assertNotNull(tarjetaEjemplo, "Debe existir la tarjeta del ejemplo 2");
            assertNotNull(filaReciente, "Debe mostrarse la fila del archivo reciente agregado");

            simularClicIzquierdo(btnNuevo);
            simularClicIzquierdo(btnAbrir);
            simularClicIzquierdo(btnContinuar);
            simularClicIzquierdo(tarjetaEjemplo);
            simularClicIzquierdo(filaReciente);

            assertTrue(nuevoLlamado.get(), "Clic en 'Nuevo archivo' debe invocar alNuevo");
            assertTrue(abrirLlamado.get(), "Clic en 'Abrir archivo…' debe invocar alAbrir");
            assertTrue(continuarLlamado.get(), "Clic en 'Ir al editor' debe invocar alContinuar");
            assertEquals(2, ejemploElegido.get(), "Clic en tarjeta de ejemplo debe pasar su índice");
            assertEquals(tempReciente, recienteAbierto.get(), "Clic en fila reciente debe abrir el archivo correspondiente");
        });
    }

    @Test
    @DisplayName("PantallaBienvenida resalta acciones y plantillas con múltiples colores de acento")
    void bienvenidaTieneAcentosMulticolores() throws Exception {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(), "Sin pantalla: se omite");

        SwingUtilities.invokeAndWait(() -> {
            PantallaBienvenida p = new PantallaBienvenida(() -> {}, () -> {}, i -> {}, f -> {}, () -> {});

            Component iconoNuevo = buscarPorNombre(p, "accion-nuevo-icono");
            Component iconoAbrir = buscarPorNombre(p, "accion-abrir-icono");
            Component iconoContinuar = buscarPorNombre(p, "accion-continuar-icono");

            assertNotNull(iconoNuevo);
            assertNotNull(iconoAbrir);
            assertNotNull(iconoContinuar);

            Set<Color> coloresAcciones = new HashSet<>();
            coloresAcciones.add(iconoNuevo.getForeground());
            coloresAcciones.add(iconoAbrir.getForeground());
            coloresAcciones.add(iconoContinuar.getForeground());
            assertEquals(3, coloresAcciones.size(),
                "Cada acción lateral debe usar un color de acento distinto");

            Set<Color> coloresEjemplos = new HashSet<>();
            for (int i = 0; i < Ejemplos.cantidad(); i++) {
                Component cat = buscarPorNombre(p, "ejemplo-" + i + "-categoria");
                assertNotNull(cat, "Debe existir la etiqueta de categoría del ejemplo " + i);
                coloresEjemplos.add(cat.getForeground());
            }
            assertTrue(coloresEjemplos.size() >= 5,
                "Las tarjetas de ejemplo deben diferenciarse con múltiples acentos de la paleta");
        });
    }

    @Test
    @DisplayName("El atajo global F5 cambia al IDE y ejecuta el análisis del código actual")
    void atajoF5EjecutaAnalisis() throws Exception {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(), "Sin pantalla: se omite");

        boolean bienvenidaPrevia = PreferenciasApp.mostrarBienvenida();
        try {
            PreferenciasApp.setMostrarBienvenida(true);
            SwingUtilities.invokeAndWait(() -> {
                AplicacionPrincipal ventana = new AplicacionPrincipal();
                try {
                    JRootPane root = ventana.getRootPane();
                    KeyStroke ksF5 = KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0);
                    InputMap mapa = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
                    Object claveAccion = mapa.get(ksF5);
                    assertNotNull(claveAccion, "La ventana debe registrar F5 en WHEN_IN_FOCUSED_WINDOW");

                    Action accionF5 = root.getActionMap().get(claveAccion);
                    assertNotNull(accionF5, "Debe existir la acción asociada al atajo F5");

                    PanelResumen resumen = buscarPorTipo(ventana.getContentPane(), PanelResumen.class);
                    ResumenColapsable barraResumen = buscarPorTipo(ventana.getContentPane(), ResumenColapsable.class);
                    assertNotNull(resumen);
                    assertNotNull(barraResumen);
                    assertEquals("Sin análisis", resumen.textoCorto());
                    assertFalse(barraResumen.isVisible(), "En la bienvenida el resumen debe estar oculto");

                    accionF5.actionPerformed(new ActionEvent(root, ActionEvent.ACTION_PERFORMED, "f5"));

                    assertTrue(barraResumen.isVisible(), "Al presionar F5 desde bienvenida debe mostrarse el IDE");
                    String textoResumen = resumen.textoCorto();
                    assertNotEquals("Sin análisis", textoResumen, "F5 debe ejecutar el análisis y actualizar el resumen");
                    assertTrue(textoResumen.contains("tokens"), "El resumen debe mostrar los tokens analizados tras F5");
                    assertFalse(textoResumen.startsWith("0 tokens"), "Debe haber analizado el ejemplo cargado");
                } finally {
                    ventana.dispose();
                }
            });
        } finally {
            PreferenciasApp.setMostrarBienvenida(bienvenidaPrevia);
        }
    }

    private static <T extends Component> T buscarPorTipo(Component raiz, Class<T> tipo) {
        if (tipo.isInstance(raiz)) {
            return tipo.cast(raiz);
        }
        if (raiz instanceof Container cont) {
            for (Component hijo : cont.getComponents()) {
                T hallado = buscarPorTipo(hijo, tipo);
                if (hallado != null) {
                    return hallado;
                }
            }
        }
        return null;
    }

    private static Component buscarPorNombre(Component raiz, String nombre) {
        if (nombre.equals(raiz.getName())) {
            return raiz;
        }
        if (raiz instanceof Container cont) {
            for (Component hijo : cont.getComponents()) {
                Component hallado = buscarPorNombre(hijo, nombre);
                if (hallado != null) {
                    return hallado;
                }
            }
        }
        return null;
    }

    private static void simularClicIzquierdo(Component c) {
        MouseEvent evento = new MouseEvent(
            c,
            MouseEvent.MOUSE_CLICKED,
            System.currentTimeMillis(),
            MouseEvent.BUTTON1_DOWN_MASK,
            5,
            5,
            1,
            false,
            MouseEvent.BUTTON1
        );
        c.dispatchEvent(evento);
    }
}
