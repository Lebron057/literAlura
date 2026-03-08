package br.com.alura.literAlura.principal;

import br.com.alura.literAlura.model.*;
import br.com.alura.literAlura.repository.AutorRepository;
import br.com.alura.literAlura.repository.LivroRepository;
import br.com.alura.literAlura.service.ConsumoApi;
import br.com.alura.literAlura.service.ConverteDados;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Scanner;

public class Principal {
    private ConsumoApi consumo = new ConsumoApi();
    private ConverteDados conversor = new ConverteDados();
    private Scanner leitura = new Scanner(System.in);
    private final String ENDERECO = "https://gutendex.com/books/";
    private AutorRepository autorRepository;
    private LivroRepository livroRepository;

    // --- ADICIONE ESTE CONSTRUTOR ---
    public Principal(LivroRepository livroRepository, AutorRepository autorRepository) {
        this.livroRepository = livroRepository;
        this.autorRepository = autorRepository;
    }

    public void exibeMenu() {

        String menu = """
                
                ------------------------------------------
                Escolha o numero de sua opção:
                1- Buscar livro pelo titulo
                2- listar livros registrados
                3- listar autores registrados
                4- listar autores vivos em determinado ano
                5- listar livros de determinado idioma
                0- sair
                """;

        int opcao = -1;
        while (opcao != 0) {
            System.out.println(menu);
            System.out.println("Escolha a opcao");
            opcao = leitura.nextInt();
            leitura.nextLine();
            switch (opcao) {
                case 1:
                    buscaLivroPorTitulo();
                    break;
                case 2:
                    listarLivrosRegistrados();
                    break;
                case 3:
                    listarAutoresRegistrados();
                    break;
                case 4:
                    listarAutoresVivos();
                    break;
                case 5:
                    listarLivrosPorIdioma();
                    break;
                case 0:
                    System.out.println("Saindo.....");
                    break;
                default:
                    System.out.println("Opção inválida");
            }
        }
    }

    private void buscaLivroPorTitulo() {
        System.out.println("Digite o nome do livro:");
        String tituloLivro = leitura.nextLine();

        var json = consumo.obterDados(ENDERECO + "?search=" + tituloLivro.replace(" ", "+"));

        DadosResultados dados = conversor.obterDados(json, DadosResultados.class);

        if (dados.livros() != null && !dados.livros().isEmpty()) {
            DadosLivro dadosLivro = dados.livros().getFirst();

            Livro livro = new Livro(dadosLivro);

            // Verificação do autor
            Autor autor = null;
            if (!dadosLivro.autores().isEmpty()){
                DadosAutor dadosAutor = dadosLivro.autores().getFirst();

                autor = autorRepository.findByNome(dadosAutor.nome());

                if (autor == null) {
                    System.out.println("Autor novo detectado. Cadastrando...");
                    autor = new Autor(dadosAutor);
                    autorRepository.save(autor);
                } else {
                    // Se não for null, o autor já existe. Usamos o que veio do banco.
                    System.out.println("Autor já existente no banco. Vinculando...");
                }
            }

            livro.setAutor(autor);

            try {
                livroRepository.save(livro);
                System.out.println("Livro salvo com sucesso");
                System.out.println(livro);
            } catch (DataIntegrityViolationException e) {
                System.out.println("Não foi possível salvar: O livro '" + livro.getTitulo() + "' já está cadastrado no banco!");
            }

        } else {
            System.out.println("Livro nao encontrado");
        }
    }

    private void listarLivrosRegistrados() {
        List<Livro> livros = livroRepository.findAll();
        if (!livros.isEmpty()) {
            livros.forEach(System.out::println);
        } else {
            System.out.println("Nenhum livro encontrado no banco de dados.");
        }
    }

    private void listarAutoresRegistrados() {
        List<Autor> autores = autorRepository.findAll();
        if (!autores.isEmpty()) {
            autores.forEach(System.out::println);
        } else {
            System.out.println("Nenhum autor encontrado no banco de dados.");
        }
    }

    private void listarAutoresVivos() {
        System.out.println("Digite o ano:");
        int ano = leitura.nextInt();
        leitura.nextLine();
        List<Autor> autoresVivos = autorRepository.obterAutoresVivosPorAno(ano);

        if (!autoresVivos.isEmpty()) {
            System.out.println("\n--- Autores Vivos em " + ano + " ---");
            autoresVivos.forEach(System.out::println);
        } else {
            System.out.println("Nenhum autor vivo encontrado neste ano no banco de dados.");
        }
    }

    private void listarLivrosPorIdioma() {
        System.out.println("""
            Escolha o idioma para busca:
            es - Espanhol
            en - Inglês
            fr - Francês
            pt - Português
            """);
        String idioma = leitura.nextLine();

        List<Livro> livros = livroRepository.findByIdioma(idioma);

        if (!livros.isEmpty()) {
            livros.forEach(System.out::println);
        } else {
            System.out.println("Nenhum livro encontrado nesse idioma.");
        }
    }
}
