package com.agendaclientes.api.usuario.infrastructure.web;

/**
 * Não há envio de email configurado nesta API ainda: o token de redefinição
 * (quando o email existe) é devolvido diretamente na resposta para o
 * frontend exibir em modo de desenvolvimento, em vez de ser enviado por
 * email de verdade.
 */
public record EsqueciSenhaResponse(String mensagem, String tokenDev) {
}
