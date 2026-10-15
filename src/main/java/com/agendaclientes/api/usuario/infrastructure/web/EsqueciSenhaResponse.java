package com.agendaclientes.api.usuario.infrastructure.web;

/**
 * Não devolve o token de redefinição: ele só pode chegar ao dono da conta
 * por um canal próprio (email), senão qualquer um que saiba o email de um
 * usuário consegue trocar a senha dele.
 */
public record EsqueciSenhaResponse(String mensagem) {
}
