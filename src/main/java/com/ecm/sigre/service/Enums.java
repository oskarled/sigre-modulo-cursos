package com.ecm.sigre.service;

import com.ecm.sigre.exception.DatosInvalidosException;
import java.util.Arrays;

/** Utilidad para convertir texto en un valor de enum con un mensaje de error claro. */
final class Enums {

    private Enums() {
    }

    /**
     * Convierte el texto en el valor del enum, sin importar mayusculas.
     *
     * @throws DatosInvalidosException si el texto no corresponde a ningun valor
     */
    static <E extends Enum<E>> E convertir(Class<E> tipo, String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new DatosInvalidosException("El campo " + campo + " es obligatorio");
        }
        try {
            return Enum.valueOf(tipo, valor.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new DatosInvalidosException("Valor invalido para " + campo + ": '" + valor
                    + "'. Valores permitidos: " + Arrays.toString(tipo.getEnumConstants()));
        }
    }
}
