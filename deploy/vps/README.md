# Deploy na VPS

- **URL:** https://agenda-api.aamora.com.br (health: `/actuator/health`)
- **Porta:** `127.0.0.1:5101` → container `web` (8080). Rede `172.30.1.0/24`.
- **Servidor:** `/opt/agenda-api/` com `docker-compose.yml`, `deploy.conf`, `.env` (senha do
  Postgres, só existe lá), `secrets.env` (escrito pelo deploy), `.version` e `backups/`.
  Nginx em `/etc/nginx/conf.d/agenda-api.conf`.
- **Deploy:** automático. Todo merge na `main` com `feat:`/`fix:` gera a release `vX.Y.Z` no
  `release.yml`, e o job `deploy-vps` sobe essa versão. Se o `/actuator/health` não responder, o
  `app-deploy` volta sozinho para a versão anterior.
- **Configuração no GitHub:** secrets `VPS_HOST`, `VPS_SSH_KEY`, `VPS_KNOWN_HOSTS`,
  `APP_JWT_SECRET` e a variável `APP_CORS_ALLOWED_ORIGINS` (URLs do front, separadas por vírgula).
- **Backup:** diário (banco), 7 dias de retenção, em `/opt/agenda-api/backups/`.

## Operação

```bash
ssh vps 'app-deploy agenda-api status'      # versão no ar, imagens guardadas, containers
ssh vps 'app-deploy agenda-api up 0.3.0'    # rollback para uma das 3 últimas imagens
ssh vps 'app-backup agenda-api'             # backup na hora
```

Os arquivos desta pasta não vão sozinhos para o servidor: mudou `docker-compose.yml`,
`deploy.conf` ou `nginx.conf`, rode de novo o `preparar_app.sh agenda-api` da skill deploy-vps.
