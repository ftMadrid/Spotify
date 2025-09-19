package spotify;

public class Nodo {

    Song cancion;
    Nodo siguiente;

    public Nodo(Song cancion) {
        this.cancion = cancion;
        siguiente = null;
    }
}
