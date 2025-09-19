package spotify;

public class Song {

    private String nombre;
    private String artista;
    private long duracion;
    private String imagen;
    private String genero;

    public Song(String nombre, String artista, long duracion, String imagen, String genero) {
        this.nombre = nombre;
        this.artista = artista;
        this.duracion = duracion;
        this.imagen = imagen;
        this.genero = genero;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getArtista() {
        return artista;
    }

    public void setArtista(String artista) {
        this.artista = artista;
    }

    public long getDuracion() {
        return duracion;
    }

    public void setDuracion(long duracion) {
        this.duracion = duracion;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

}
