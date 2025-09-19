package spotify;

import java.io.Serializable;

public class ListaReproduccion implements Serializable {

    private static final long serialVersionUID = 1L;
    private Nodo cabeza;
    private int tamano;

    public ListaReproduccion() {
        this.cabeza = null;
        this.tamano = 0;
    }

    // agregar cancion (al final de la lista)
    public void agregar(Song cancion) {
        Nodo nuevoNodo = new Nodo(cancion);
        if (cabeza == null) {
            cabeza = nuevoNodo;
        } else {
            Nodo actual = cabeza;
            while (actual.siguiente != null) {
                actual = actual.siguiente;
            }
            actual.siguiente = nuevoNodo;
        }
        tamano++;
    }

    // obtengo cancion pepepe
    public Song obtener(int indice) {
        if (indice < 0 || indice >= tamano) {
            return null;
        }

        Nodo actual = cabeza;
        for (int i = 0; i < indice; i++) {
            actual = actual.siguiente;
        }
        return actual.cancion;
    }

    // eliminar cancion tutut
    public boolean eliminar(int indice) {
        if (indice < 0 || indice >= tamano) {
            return false;
        }

        if (indice == 0) {
            cabeza = cabeza.siguiente;
            tamano--;
            return true;
        }

        Nodo actual = cabeza;
        for (int i = 0; i < indice - 1; i++) {
            actual = actual.siguiente;
        }
        actual.siguiente = actual.siguiente.siguiente;
        tamano--;
        return true;
    }

    // obtener canciones max verstappen
    public Song[] obtenerTodas() {
        Song[] canciones = new Song[tamano];
        Nodo actual = cabeza;
        for (int i = 0; i < tamano; i++) {
            canciones[i] = actual.cancion;
            actual = actual.siguiente;
        }
        return canciones;
    }

    public int getTamano() {
        return tamano;
    }

    public boolean estaVacia() {
        return cabeza == null;
    }
}
