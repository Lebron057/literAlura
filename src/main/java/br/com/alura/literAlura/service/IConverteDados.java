package br.com.alura.literAlura.service;

public interface IConverteDados {
    // Retorno generico
    // Recebe uma string em formato json e retorna a classe desejada
    <T> T obterDados(String json, Class<T> classe);
}
