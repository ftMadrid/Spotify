package spotify;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;

public class ReproductorCancion extends JFrame {

    private ListaReproduccion playlist;
    private DefaultListModel<Song> listModel;
    private JList<Song> songList;
    private JLabel imagenLabel;
    private JLabel infoCancionLabel;
    private JButton playButton, pauseButton, stopButton;
    private JButton addButton, removeButton, saveButton;

    // variables para reproducción de audio (me perdi noooo)
    private AdvancedPlayer mp3Player;
    private boolean isPlaying = false;
    private boolean isPaused = false;
    private Song cancionActual = null;
    private Thread playbackThread;

    private FileInputStream fileInputStream;
    private long totalSongLengthBytes;
    private long pausedPositionBytes = 0;

    private JProgressBar progressBar;
    private JLabel tiempoActualLabel;
    private JLabel tiempoTotalLabel;
    private Timer progressTimer;

    private static final String PLAYLIST_FOLDER = "Data";
    private static final String PLAYLIST_FILE = "mi-perfil.playlist";

    public ReproductorCancion() {
        playlist = new ListaReproduccion();
        initComponents();
        setupLayout();
        setupEventListeners();
        cargarPlaylistAutomaticamente();

        // solo resetea timer de barra para evitar posibles crashsss pepepe
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                progressTimer.stop();
            }
        });

    }

    private void initComponents() {
        setTitle("Spotitec");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        listModel = new DefaultListModel<>();
        songList = new JList<>(listModel);
        songList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        songList.setFont(new Font("Verdana", Font.PLAIN, 12));

        imagenLabel = new JLabel();
        imagenLabel.setPreferredSize(new Dimension(200, 200));
        imagenLabel.setBorder(BorderFactory.createLoweredBevelBorder());
        imagenLabel.setHorizontalAlignment(JLabel.CENTER);
        imagenLabel.setText("La imagen no esta disponible");

        infoCancionLabel = new JLabel("<html><center>No hay cancion seleccionada</center></html>");
        infoCancionLabel.setHorizontalAlignment(JLabel.CENTER);
        infoCancionLabel.setFont(new Font("Verdana", Font.BOLD, 14));

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(false);
        progressBar.setPreferredSize(new Dimension(300, 20));

        tiempoActualLabel = new JLabel("00:00");
        tiempoTotalLabel = new JLabel("00:00");

        Font tiempoFont = new Font("Verdana", Font.PLAIN, 12);
        tiempoActualLabel.setFont(tiempoFont);
        tiempoTotalLabel.setFont(tiempoFont);

        // pa actualizar la barra
        progressTimer = new Timer(1000, e -> actualizarProgreso());

        playButton = new JButton("▶ Play");
        pauseButton = new JButton("⏸ Pause");
        stopButton = new JButton("⏹ Stop");
        addButton = new JButton("➕ Agregar");
        removeButton = new JButton("🗑 Eliminar");
        saveButton = new JButton("💾 Guardar");

        playButton.setEnabled(false);
        pauseButton.setEnabled(false);
        stopButton.setEnabled(false);
        removeButton.setEnabled(false);
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        JPanel leftPanel = new JPanel(new BorderLayout());
        TitledBorder border = BorderFactory.createTitledBorder("Mi PlayList");
        border.setTitleFont(new Font("Verdana", Font.BOLD, 16));
        leftPanel.setBorder(border);
        leftPanel.add(new JScrollPane(songList), BorderLayout.CENTER);

        JPanel managementPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        managementPanel.add(addButton);
        managementPanel.add(removeButton);
        managementPanel.add(saveButton);
        leftPanel.add(managementPanel, BorderLayout.SOUTH);

        JPanel rightPanel = new JPanel(new BorderLayout());

        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.add(imagenLabel, BorderLayout.CENTER);
        infoPanel.add(infoCancionLabel, BorderLayout.SOUTH);

        JPanel progressPanel = new JPanel(new BorderLayout(5, 5));
        progressPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel timePanel = new JPanel(new BorderLayout());
        timePanel.add(tiempoActualLabel, BorderLayout.WEST);
        timePanel.add(tiempoTotalLabel, BorderLayout.EAST);

        progressPanel.add(progressBar, BorderLayout.CENTER);
        progressPanel.add(timePanel, BorderLayout.SOUTH);

        JPanel controlPanel = new JPanel(new FlowLayout());
        controlPanel.add(playButton);
        controlPanel.add(pauseButton);
        controlPanel.add(stopButton);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(progressPanel, BorderLayout.CENTER);
        bottomPanel.add(controlPanel, BorderLayout.SOUTH);

        rightPanel.add(infoPanel, BorderLayout.CENTER);
        rightPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.CENTER);

        leftPanel.setPreferredSize(new Dimension(350, 600));
    }

    private void setupEventListeners() {
        songList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Song cancionSeleccionada = songList.getSelectedValue();
                if (cancionSeleccionada != null) {
                    seleccionarCancion(cancionSeleccionada);
                }
            }
        });

        addButton.addActionListener(e -> agregarCancion());
        removeButton.addActionListener(e -> eliminarCancion());
        saveButton.addActionListener(e -> guardarPlaylistAutomaticamente());
        playButton.addActionListener(e -> reproducir());
        pauseButton.addActionListener(e -> pausar());
        stopButton.addActionListener(e -> detener());
    }

    private void agregarCancion() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "Archivos de Audio", "wav", "mp3", "au", "aiff");
        fileChooser.setFileFilter(filter);

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File archivoSeleccionado = fileChooser.getSelectedFile();

            long duracionMs = 0;
            String duracionFormateada = "00:00";

            try {
                AudioFile audioFile = AudioFileIO.read(archivoSeleccionado);
                AudioHeader audioHeader = audioFile.getAudioHeader();
                duracionMs = audioHeader.getTrackLength() * 1000L;

                // Formatear duración a mm:ss
                int segundosTotales = (int) (duracionMs / 1000);
                int minutos = segundosTotales / 60;
                int segundos = segundosTotales % 60;
                duracionFormateada = String.format("%02d:%02d", minutos, segundos);

            } catch (Exception e) {
                System.out.println("No se pudo obtener la duracion automaticamente: " + e.getMessage());
            }

            // Diálogo para ingresar información de la canción
            JPanel panel = new JPanel(new GridLayout(5, 2));
            JTextField nombreField = new JTextField(archivoSeleccionado.getName());
            JTextField artistaField = new JTextField("Artista desconocido");
            JTextField duracionField = new JTextField(duracionFormateada);
            JTextField generoField = new JTextField("Sin genero");
            JButton imagenButton = new JButton("Seleccionar imagen");
            JLabel imagenPathLabel = new JLabel("Sin imagen");

            final String[] imagenPath = {""};

            imagenButton.addActionListener(e -> {
                JFileChooser imgChooser = new JFileChooser();
                FileNameExtensionFilter imgFilter = new FileNameExtensionFilter(
                        "Imágenes", "jpg", "jpeg", "png", "gif");
                imgChooser.setFileFilter(imgFilter);

                if (imgChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                    imagenPath[0] = imgChooser.getSelectedFile().getAbsolutePath();
                    imagenPathLabel.setText(imgChooser.getSelectedFile().getName());
                }
            });

            panel.add(new JLabel("Nombre:"));
            panel.add(nombreField);
            panel.add(new JLabel("Artista:"));
            panel.add(artistaField);
            panel.add(new JLabel("Duracion (mm:ss):"));
            panel.add(duracionField);
            panel.add(new JLabel("Genero:"));
            panel.add(generoField);
            panel.add(imagenButton);
            panel.add(imagenPathLabel);

            int option = JOptionPane.showConfirmDialog(this, panel,
                    "Informacion de la cancion", JOptionPane.OK_CANCEL_OPTION);

            if (option == JOptionPane.OK_OPTION) {
                if (!duracionField.getText().equals(duracionFormateada)) {
                    duracionMs = parsearDuracion(duracionField.getText());
                }

                Song nuevaCancion = new Song(
                        nombreField.getText(),
                        artistaField.getText(),
                        duracionMs,
                        imagenPath[0],
                        generoField.getText(),
                        archivoSeleccionado.getAbsolutePath()
                );

                playlist.agregar(nuevaCancion);
                listModel.addElement(nuevaCancion);

                JOptionPane.showMessageDialog(this, "Cancion agregada exitosamente!");
            }
        }
    }

    private long parsearDuracion(String duracionStr) {
        try {
            if (duracionStr.contains(":")) {
                String[] partes = duracionStr.split(":");
                if (partes.length == 2) {
                    int minutos = Integer.parseInt(partes[0]);
                    int segundos = Integer.parseInt(partes[1]);
                    return (minutos * 60L + segundos) * 1000L;
                }
            }
            else {
                int segundos = Integer.parseInt(duracionStr);
                return segundos * 1000L;
            }
        } catch (NumberFormatException e) {
            System.out.println("Error al castear duracion: " + e.getMessage());
        }
        return 0L;
    }

    private void eliminarCancion() {
        int indiceSeleccionado = songList.getSelectedIndex();
        if (indiceSeleccionado != -1) {
            Song cancionAEliminar = songList.getSelectedValue();

            if (cancionActual == cancionAEliminar) {
                detener();
                cancionActual = null;
                actualizarInterfaz(null);
            }

            playlist.eliminar(indiceSeleccionado);
            listModel.removeElementAt(indiceSeleccionado);

            JOptionPane.showMessageDialog(this, "Cancion eliminada!");
        } else {
            JOptionPane.showMessageDialog(this, "Selecciona una cancion para eliminar.");
        }
    }

    private void seleccionarCancion(Song cancion) {
        cancionActual = cancion;
        actualizarInterfaz(cancion);

        resetearProgreso();

        playButton.setEnabled(true);
        removeButton.setEnabled(true);

        if (isPlaying) {
            detener();
        }
    }

    private void actualizarInterfaz(Song cancion) {
        if (cancion == null) {
            imagenLabel.setIcon(null);
            imagenLabel.setText("Sin imagen");
            infoCancionLabel.setText("<html><center>No hay cancion seleccionada</center></html>");
            resetearProgreso();
            return;
        }

        // Actualizar información
        String info = "<html><center><b>" + cancion.getNombre() + "</b><br>"
                + "Artista: " + cancion.getArtista() + "<br>"
                + "Duracion: " + cancion.getDuracionFormateada() + "<br>"
                + "Genero: " + cancion.getGenero() + "</center></html>";
        infoCancionLabel.setText(info);

        tiempoTotalLabel.setText(cancion.getDuracionFormateada());

        if (!cancion.getImagenPath().isEmpty()) {
            try {
                BufferedImage img = ImageIO.read(new File(cancion.getImagenPath()));
                ImageIcon icon = new ImageIcon(img.getScaledInstance(180, 180, Image.SCALE_SMOOTH));
                imagenLabel.setIcon(icon);
                imagenLabel.setText("");
            } catch (IOException e) {
                imagenLabel.setIcon(null);
                imagenLabel.setText("Error cargando imagen");
            }
        } else {
            imagenLabel.setIcon(null);
            imagenLabel.setText("Sin imagen");
        }
    }

    private void reproducir() {
        if (cancionActual == null) {
            JOptionPane.showMessageDialog(this, "Selecciona una cancion primero.");
            return;
        }

        try {
            if (isPlaying) {
                return;
            }

            reproducirMP3DesdePosicion(pausedPositionBytes);

            playButton.setEnabled(false);
            pauseButton.setEnabled(true);
            stopButton.setEnabled(true);

            progressTimer.start();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al reproducir el archivo MP3: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void reproducirMP3DesdePosicion(long startPositionBytes) {
        if (playbackThread != null && playbackThread.isAlive()) {
            playbackThread.interrupt();
        }

        playbackThread = new Thread(() -> {
            try {
                File audioFile = new File(cancionActual.getRutaArchivo());
                totalSongLengthBytes = audioFile.length();
                fileInputStream = new FileInputStream(audioFile);

                if (startPositionBytes > 0) {
                    fileInputStream.skip(startPositionBytes);
                }

                BufferedInputStream bis = new BufferedInputStream(fileInputStream);
                mp3Player = new AdvancedPlayer(bis);

                mp3Player.setPlayBackListener(new PlaybackListener() {
                    @Override
                    public void playbackFinished(PlaybackEvent evt) {
                        if (isPlaying) {
                            detener();
                        }
                    }
                });

                isPlaying = true;
                isPaused = false;
                mp3Player.play();

            } catch (JavaLayerException | IOException e) {
                if (!(e instanceof JavaLayerException && e.getCause() instanceof InterruptedException)) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(ReproductorCancion.this,
                                "Error reproduciendo MP3: " + e.getMessage());
                    });
                }
            }
        });

        playbackThread.start();
    }

    private void pausar() {
        if (isPlaying && mp3Player != null) {
            try {
                pausedPositionBytes = totalSongLengthBytes - fileInputStream.available();
                isPaused = true;
                isPlaying = false;

                progressTimer.stop();

                mp3Player.close();

                playButton.setEnabled(true);
                pauseButton.setEnabled(false);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void detener() {
        isPlaying = false;
        isPaused = false;
        pausedPositionBytes = 0;

        progressTimer.stop();
        resetearProgreso();

        if (mp3Player != null) {
            mp3Player.close();
        }
        if (playbackThread != null) {
            playbackThread.interrupt();
        }

        SwingUtilities.invokeLater(() -> {
            playButton.setEnabled(cancionActual != null);
            pauseButton.setEnabled(false);
            stopButton.setEnabled(false);
        });
    }

    private void guardarPlaylistAutomaticamente() {
        if (playlist.estaVacia()) {
            JOptionPane.showMessageDialog(this, "La playlist esta vacia! No hay nada que guardar.");
            return;
        }

        File playlistDir = new File(PLAYLIST_FOLDER);
        if (!playlistDir.exists()) {
            playlistDir.mkdirs();
        }

        File archivo = new File(playlistDir, PLAYLIST_FILE);

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(archivo))) {
            oos.writeObject(playlist);

            JOptionPane.showMessageDialog(this,
                    "Playlist guardada exitosamente!");

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Error al guardar la playlist: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarPlaylistAutomaticamente() {
        File playlistDir = new File(PLAYLIST_FOLDER);
        File playlistFile = new File(playlistDir, PLAYLIST_FILE);

        if (!playlistDir.exists()) {
            playlistDir.mkdirs();
            JOptionPane.showMessageDialog(this,
                    "Carpeta 'PlayList' creada!");
            return;
        }

        if (playlistFile.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(playlistFile))) {
                if (isPlaying) {
                    detener();
                }

                ListaReproduccion playlistCargada = (ListaReproduccion) ois.readObject();

                listModel.clear();
                playlist = playlistCargada;

                actualizarListaEnInterfaz();
                actualizarInterfaz(null);
                cancionActual = null;

                verificarArchivosAudio();

                JOptionPane.showMessageDialog(this,
                        "Playlist cargada automaticamente!\n"
                        + "Se cargaron " + playlist.getTamano() + " canciones.");

            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                        "Error al leer la playlist: " + e.getMessage(),
                        "Error de E/O", JOptionPane.ERROR_MESSAGE);
            } catch (ClassNotFoundException e) {
                JOptionPane.showMessageDialog(this,
                        "Error: El archivo no contiene una playlist valida.",
                        "Error de formato", JOptionPane.ERROR_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Error inesperado al cargar la playlist: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "No se encontro playlist automatica!");
        }
    }

    private void actualizarListaEnInterfaz() {
        Song[] canciones = playlist.obtenerTodas();
        for (Song cancion : canciones) {
            listModel.addElement(cancion);
        }
    }

    private void verificarArchivosAudio() {
        Song[] canciones = playlist.obtenerTodas();
        int archivosNoEncontrados = 0;
        StringBuilder mensaje = new StringBuilder();

        for (int i = 0; i < canciones.length; i++) {
            File archivo = new File(canciones[i].getRutaArchivo());
            if (!archivo.exists()) {
                archivosNoEncontrados++;
                if (archivosNoEncontrados <= 5) {
                    mensaje.append("- ").append(canciones[i].getNombre()).append("\n");
                }
            }

            String nombreArchivo = archivo.getName().toLowerCase();
            if (!nombreArchivo.endsWith(".mp3")) {
                mensaje.append("- ").append(canciones[i].getNombre())
                        .append(" (formato no MP3: ").append(nombreArchivo).append(")\n");
            }
        }

        if (archivosNoEncontrados > 0) {
            String textoCompleto = "Advertencia: " + archivosNoEncontrados
                    + " archivo(s) de audio no se encontraron:\n\n" + mensaje.toString();

            if (archivosNoEncontrados > 5) {
                textoCompleto += "\n... y " + (archivosNoEncontrados - 5) + " más.";
            }

            textoCompleto += "\n\nLas canciones permanecen en la lista, pero no se podran reproducir.";

            JOptionPane.showMessageDialog(this, textoCompleto,
                    "Archivos no encontrados", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void actualizarProgreso() {
        if (isPlaying && cancionActual != null) {
            try {
                if (fileInputStream != null) {
                    long bytesLeidos = totalSongLengthBytes - fileInputStream.available();
                    double progreso = (double) bytesLeidos / totalSongLengthBytes;

                    int porcentaje = (int) (progreso * 100);
                    progressBar.setValue(porcentaje);

                    long tiempoActualMs = (long) (cancionActual.getDuracion() * progreso);
                    tiempoActualLabel.setText(formatearTiempo(tiempoActualMs));

                    tiempoTotalLabel.setText(cancionActual.getDuracionFormateada());
                }
            } catch (IOException e) {
                System.out.println("Error al actualizar progreso: " + e.getMessage());
            }
        }
    }

    private String formatearTiempo(long milisegundos) {
        int segundosTotales = (int) (milisegundos / 1000);
        int minutos = segundosTotales / 60;
        int segundos = segundosTotales % 60;
        return String.format("%02d:%02d", minutos, segundos);
    }

    private void resetearProgreso() {
        progressBar.setValue(0);
        tiempoActualLabel.setText("00:00");
        if (cancionActual != null) {
            tiempoTotalLabel.setText(cancionActual.getDuracionFormateada());
        } else {
            tiempoTotalLabel.setText("00:00");
        }
    }

    public static void main(String[] args) {
        new ReproductorCancion().setVisible(true);
    }
}
