package model;



import factory.anotations.Atribute;
import factory.anotations.EngineEntity;
import factory.anotations.Entity;
import factory.anotations.Identity;
import factory.anotations.Join;
import factory.enums.EngineType;

@Entity
@EngineEntity(engine = EngineType.CASCATE)
public class Pessoa {

    @Identity
    private long id;

    @Atribute(nullable = false, tam = 80)
    private String nome;

    @Join
    private Endereco endereco;

    public Pessoa(){}

    public Pessoa(String nome, Endereco endereco) {
        this.nome = nome;
        this.endereco = endereco;
    }

    @Override
    public String toString() {
        return "Pessoa [id=" + id + ", nome=" + nome + ", endereco=" + endereco + "]";
    }



}

