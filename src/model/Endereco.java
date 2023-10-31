package model;

import factory.anotations.Atribute;
import factory.anotations.EngineEntity;
import factory.anotations.Entity;
import factory.anotations.Identity;
import factory.enums.EngineType;

@Entity
@EngineEntity(engine = EngineType.CASCATE)
public class Endereco {

    @Identity
    private long id;

    @Atribute(nullable = false, tam = 150)
    private String rua;

    @Atribute(nullable = false, tam = 20)
    private String cep;

    public Endereco(){}

    public Endereco(String rua, String cep) {
        this.rua = rua;
        this.cep = cep;
    }

    @Override
    public String toString() {
        return "Endereco [id=" + id + ", rua=" + rua + ", cep=" + cep + "]";
    }



}
