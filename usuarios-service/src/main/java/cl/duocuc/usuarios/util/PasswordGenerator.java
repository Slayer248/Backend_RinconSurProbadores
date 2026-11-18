package cl.duocuc.usuarios.util;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class PasswordGenerator {

    private static final String MAYUSCULAS = "ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final String MINUSCULAS = "abcdefghijkmnopqrstuvwxyz";
    private static final String NUMEROS = "23456789";
    private static final String ESPECIALES = "@#$%&*!";
    private static final String TODOS = MAYUSCULAS + MINUSCULAS + NUMEROS + ESPECIALES;
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordGenerator() {}

    /**
     * Genera una contraseña segura de forma aleatoria que cumple con:
     * - Al menos una mayúscula
     * - Al menos una minúscula
     * - Al menos un dígito
     * - Al menos un carácter especial (@#$%&*!)
     * - Longitud mínima de 10 caracteres
     */
    public static String generar(int longitud) {
        int len = Math.max(longitud, 10);
        List<Character> caracteres = new ArrayList<>();

        caracteres.add(MAYUSCULAS.charAt(RANDOM.nextInt(MAYUSCULAS.length())));
        caracteres.add(MINUSCULAS.charAt(RANDOM.nextInt(MINUSCULAS.length())));
        caracteres.add(NUMEROS.charAt(RANDOM.nextInt(NUMEROS.length())));
        caracteres.add(ESPECIALES.charAt(RANDOM.nextInt(ESPECIALES.length())));

        for (int i = 4; i < len; i++) {
            caracteres.add(TODOS.charAt(RANDOM.nextInt(TODOS.length())));
        }

        Collections.shuffle(caracteres, RANDOM);

        StringBuilder sb = new StringBuilder();
        for (char c : caracteres) {
            sb.append(c);
        }
        return sb.toString();
    }
}
