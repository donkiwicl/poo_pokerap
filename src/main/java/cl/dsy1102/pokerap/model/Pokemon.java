package cl.dsy1102.pokerap.model;

/**
 * Un Pokémon de la Pokédex. Las reglas del negocio viven aquí: es imposible
 * crear un Pokémon inválido, venga del formulario, del JSON o de la base de datos.
 *
 * Es inmutable (salvo el id que asigna la base de datos): para "editar" se
 * crea un objeto nuevo con los datos corregidos.
 */
public class Pokemon {

    public static final int LARGO_NOMBRE = 40;
    public static final int ESTADISTICA_MAXIMA = 255;
    public static final double ALTURA_MAXIMA = 100.0;   // metros
    public static final double PESO_MAXIMO = 9999.9;    // kilos

    /** 0 mientras el Pokémon no se ha guardado en la base de datos. */
    private int id;
    private final int numero;
    private final String nombre;
    private final Tipo tipoPrincipal;
    private final Tipo tipoSecundario;
    private final double altura;
    private final double peso;
    private final int hp;
    private final int ataque;
    private final int defensa;
    private final int velocidad;

    public Pokemon(int numero, String nombre, Tipo tipoPrincipal, Tipo tipoSecundario,
                   double altura, double peso, int hp, int ataque, int defensa, int velocidad) {
        if (numero <= 0) {
            throw new IllegalArgumentException("El número de la Pokédex debe ser mayor que cero.");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }
        if (nombre.trim().length() > LARGO_NOMBRE) {
            throw new IllegalArgumentException("El nombre no puede superar los " + LARGO_NOMBRE + " caracteres.");
        }
        if (tipoPrincipal == null) {
            throw new IllegalArgumentException("El tipo principal es obligatorio.");
        }
        if (tipoPrincipal.equals(tipoSecundario)) {
            throw new IllegalArgumentException("El tipo secundario debe ser distinto del principal.");
        }
        this.numero = numero;
        this.nombre = nombre.trim();
        this.tipoPrincipal = tipoPrincipal;
        this.tipoSecundario = tipoSecundario;
        this.altura = medida("La altura", altura, ALTURA_MAXIMA);
        this.peso = medida("El peso", peso, PESO_MAXIMO);
        this.hp = estadistica("HP", hp);
        this.ataque = estadistica("Ataque", ataque);
        this.defensa = estadistica("Defensa", defensa);
        this.velocidad = estadistica("Velocidad", velocidad);
    }

    /** Las medidas se guardan con un decimal, igual que en la tabla. */
    private static double medida(String nombre, double valor, double maximo) {
        double redondeado = Math.round(valor * 10) / 10.0;
        if (!(redondeado > 0) || redondeado > maximo) {
            throw new IllegalArgumentException(nombre + " debe ser mayor que 0 y no superar " + maximo + ".");
        }
        return redondeado;
    }

    private static int estadistica(String nombre, int valor) {
        if (valor < 1 || valor > ESTADISTICA_MAXIMA) {
            throw new IllegalArgumentException(nombre + " debe estar entre 1 y " + ESTADISTICA_MAXIMA + ".");
        }
        return valor;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getNumero() {
        return numero;
    }

    public String getNombre() {
        return nombre;
    }

    public Tipo getTipoPrincipal() {
        return tipoPrincipal;
    }

    /** null si el Pokémon tiene un solo tipo. */
    public Tipo getTipoSecundario() {
        return tipoSecundario;
    }

    public double getAltura() {
        return altura;
    }

    public double getPeso() {
        return peso;
    }

    public int getHp() {
        return hp;
    }

    public int getAtaque() {
        return ataque;
    }

    public int getDefensa() {
        return defensa;
    }

    public int getVelocidad() {
        return velocidad;
    }

    /** Valor calculado: no se guarda en la base de datos. */
    public int getTotal() {
        return hp + ataque + defensa + velocidad;
    }

    public boolean esDeTipo(Tipo tipo) {
        return tipoPrincipal.equals(tipo) || (tipoSecundario != null && tipoSecundario.equals(tipo));
    }

    @Override
    public String toString() {
        return "#" + numero + " " + nombre;
    }
}
