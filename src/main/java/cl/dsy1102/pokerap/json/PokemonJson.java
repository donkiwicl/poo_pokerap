package cl.dsy1102.pokerap.json;

import java.util.List;

/**
 * Un Pokémon tal como se guarda en datos/pokedex-inicial.json. Gson llena los
 * campos por su nombre, por eso coinciden con las claves del archivo:
 *
 *   { "numero": 1, "nombre": "Bulbasaur", "tipos": ["Planta", "Veneno"],
 *     "altura": 0.7, "peso": 6.9, "hp": 45, "ataque": 49, "defensa": 49, "velocidad": 45 }
 *
 * No es el modelo: aquí los tipos son solo nombres. El repositorio lo
 * convierte en un Pokemon (con sus objetos Tipo) antes de guardarlo.
 */
public class PokemonJson {

    private int numero;
    private String nombre;
    private List<String> tipos;
    private double altura;
    private double peso;
    private int hp;
    private int ataque;
    private int defensa;
    private int velocidad;

    public PokemonJson(int numero, String nombre, List<String> tipos, double altura, double peso,
                       int hp, int ataque, int defensa, int velocidad) {
        this.numero = numero;
        this.nombre = nombre;
        this.tipos = tipos;
        this.altura = altura;
        this.peso = peso;
        this.hp = hp;
        this.ataque = ataque;
        this.defensa = defensa;
        this.velocidad = velocidad;
    }

    public int getNumero() {
        return numero;
    }

    public String getNombre() {
        return nombre;
    }

    /** Uno o dos nombres de tipo. Nunca null. */
    public List<String> getTipos() {
        return tipos == null ? List.of() : tipos;
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
}
