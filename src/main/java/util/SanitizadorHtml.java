package util;

import java.util.regex.Pattern;

/**
 * Prevención de XSS (Cross-Site Scripting) según guía OWASP / Semana 14:
 * <ul>
 *   <li>Escapa caracteres especiales HTML ({@code &}, {@code <}, {@code >}, {@code "}, {@code '}) antes de mostrarlos.</li>
 *   <li>Detecta patrones de inyección de scripts ({@code <script>}, {@code javascript:}, manejadores {@code on*=}).</li>
 * </ul>
 */
public final class SanitizadorHtml {

    private static final Pattern PATRON_SCRIPT = Pattern.compile(
        "(?i)<\\s*/?\\s*script\\b|javascript\\s*:|vbscript\\s*:|data\\s*:\\s*text/html|\\bon[a-z]{2,20}\\s*=|<\\s*(iframe|object|embed|svg|img|body|link|meta)\\b"
    );

    private SanitizadorHtml() {}

    /**
     * Escapa los caracteres especiales HTML (&amp;, &lt;, &gt;, &quot;, &#39;)
     * para neutralizar cualquier intento de inyección XSS al renderizar texto en el navegador.
     */
    public static String escapar(String entrada) {
        if (entrada == null || entrada.isEmpty()) return "";
        StringBuilder sb = new StringBuilder(entrada.length() + 16);
        for (int i = 0; i < entrada.length(); i++) {
            char c = entrada.charAt(i);
            switch (c) {
                case '&' -> sb.append("&amp;");
                case '<' -> sb.append("&lt;");
                case '>' -> sb.append("&gt;");
                case '"' -> sb.append("&quot;");
                case '\'' -> sb.append("&#39;");
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Devuelve {@code true} si la cadena contiene etiquetas {@code <script>}, esquemas {@code javascript:}
     * o manejadores de eventos HTML maliciosos.
     */
    public static boolean contieneScript(String entrada) {
        return entrada != null && PATRON_SCRIPT.matcher(entrada).find();
    }
}
