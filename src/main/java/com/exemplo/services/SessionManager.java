package com.exemplo.services;

/**
 * Gerencia a sessão do usuário logado usando o padrão Singleton.
 * Isso garante que haja apenas uma instância desta classe em toda a aplicação,
 * funcionando como uma memória central para os dados do usuário, incluindo seu cargo.
 */
public final class SessionManager {

    // A única instância da classe é criada aqui e nunca mais.
    private static final SessionManager instance = new SessionManager();

    // Variáveis para guardar os dados do usuário.
    private String userId;
    private String userEmail;
    private String role; // NOVO: Campo para armazenar o cargo do usuário.

    // Construtor privado impede que outras classes criem novas instâncias.
    private SessionManager() {}

    /**
     * O único ponto de acesso para obter a instância do SessionManager.
     * @return A instância única do SessionManager.
     */
    public static SessionManager getInstance() {
        return instance;
    }

    /**
     * MÉTODO ATUALIZADO: Inicia a sessão, guardando todos os dados do usuário.
     * @param userId O UUID do usuário vindo do Supabase.
     * @param userEmail O e-mail do usuário.
     * @param role O cargo do usuário (ex: "SUPERVISOR").
     */
    public void startSession(String userId, String userEmail, String role) {
        this.userId = userId;
        this.userEmail = userEmail;
        this.role = role;
    }

    /**
     * Encerra a sessão, limpando todos os dados do usuário.
     */
    public void endSession() {
        this.userId = null;
        this.userEmail = null;
        this.role = null; // Limpa o cargo ao sair.
    }

    /**
     * Retorna o ID do usuário logado.
     * @return O UUID do usuário, ou null se ninguém estiver logado.
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Retorna o e-mail do usuário logado.
     * @return O e-mail do usuário, ou null se ninguém estiver logado.
     */
    public String getUserEmail() {
        return userEmail;
    }

    /**
     * NOVO MÉTODO: Retorna o cargo do usuário logado.
     * @return A string do cargo (ex: "ESTAGIARIO"), ou null se ninguém estiver logado.
     */
    public String getRole() {
        return role;
    }

    /**
     * Verifica se há uma sessão ativa.
     * @return true se um usuário estiver logado, false caso contrário.
     */
    public boolean isLoggedIn() {
        return userId != null;
    }
}
