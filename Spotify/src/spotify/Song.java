package spotify;

public class Song {

    private String nombre;
    private String artista;
    private long duracion;
    private String imagen;
    private String genero;
    private String rutaArchivo;

    public Song(String nombre, String artista, long duracion, String imagen, String genero, String rutaArchivo) {
        this.nombre = nombre;
        this.artista = artista;
        this.duracion = duracion;
        this.imagen = imagen;
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

    public String getImagen() {
        return imagen;
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

    public void setImagenPath(String imagen) {
        this.imagen = imagen;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    // duracion formateada pepepepe
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
