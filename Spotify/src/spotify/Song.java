package spotify;

import java.io.Serializable;

public class Song implements Serializable {

    private static final long serialVersionUID = 1L;
    private String nombre;
    private String artista;
    private long duracion;
    private String imagenPath;
    private String genero;
    private String rutaArchivo;

    public Song(String nombre, String artista, long duracion, String imagenPath, String genero, String rutaArchivo) {
        this.nombre = nombre;
        this.artista = artista;
        this.duracion = duracion;
        this.imagenPath = imagenPath;
        this.genero = genero;
        this.rutaArchivo = rutaArchivo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getArtista() {
        return artista;
    }

    public long getDuracion() {
        return duracion;
    }

    public String getImagenPath() {
        return imagenPath;
    }

    public String getGenero() {
        return genero;
    }

    public String getRutaArchivo() {
        return rutaArchivo;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setArtista(String artista) {
        this.artista = artista;
    }

    public void setDuracion(long duracion) {
        this.duracion = duracion;
    }

    public void setImagenPath(String imagenPath) {
        this.imagenPath = imagenPath;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    // obtengo la formacion formateada pepepepepe
    public String getDuracionFormateada() {
        long segundosTotales = duracion / 1000;
        long minutos = segundosTotales / 60;
        long segundos = segundosTotales % 60;
        return String.format("%02d:%02d", minutos, segundos);
    }

    @Override
    public String toString() {
        return nombre + " - " + artista + " (" + getDuracionFormateada() + ")";
    }
}
