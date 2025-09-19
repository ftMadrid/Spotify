package spotify;

import java.io.Serializable;

public class Nodo implements Serializable {

    private static final long serialVersionUID = 1L;
    Song cancion;
    Nodo siguiente;

    public Nodo(Song cancion) {
        this.cancion = cancion;
        this.siguiente = null;
    }
}
