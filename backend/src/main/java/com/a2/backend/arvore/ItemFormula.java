package com.a2.backend.arvore;

public class ItemFormula {

    private String nome;
    private String pai;
    private int quantidade;
    private double valor;

    public ItemFormula() {
    }

    public ItemFormula(String nome, String pai, int quantidade, double valor) {
        this.nome = nome;
        this.pai = pai;
        this.quantidade = quantidade;
        this.valor = valor;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getPai() {
        return pai;
    }

    public void setPai(String pai) {
        this.pai = pai;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }
}
