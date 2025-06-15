package com.exemplo.services;

/**
 * Gerencia a sessão do usuário logado usando o padrão Singleton.
 * Isso garante que haja apenas uma instância desta classe em toda a aplicação,
 * funcionando como uma memória central para os dados do usuário.
 */
public final class SessionManager {

    // A única instância da classe é criada aqui e nunca mais.
    private static final SessionManager instance = new SessionManager();

    // Variáveis para guardar os dados do usuário.
    private String userId;
    private String userEmail;

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
     * Inicia a sessão, guardando os dados do usuário.
     * @param userId O UUID do usuário vindo do Supabase.
     * @param userEmail O e-mail do usuário.
     */
    public void startSession(String userId, String userEmail) {
        this.userId = userId;
        this.userEmail = userEmail;
    }

    /**
     * Encerra a sessão, limpando os dados do usuário (útil para logout).
     */
    public void endSession() {
        this.userId = null;
        this.userEmail = null;
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
     * Verifica se há uma sessão ativa.
     * @return true se um usuário estiver logado, false caso contrário.
     */
    public boolean isLoggedIn() {
        return userId != null;
    }
}
