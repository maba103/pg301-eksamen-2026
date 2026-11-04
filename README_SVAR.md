# PG301 - Eksamensbesvarelse

## Oppgave 1 - Container og CI

### Container
Applikasjonen er lagt i en Docker container med to deler - 1. Maven og Java brukes til å bygge applikasjonen
2. Java brukes til å kjøre applikasjonen
Dette gjør image mindre

For å bygge image: docker build -t wishifai:local .

For å starte applikasjonen: docker run --rm -p 9999:8080 wishifai:local 

### CI
Github Actions brukes til å bygge Docker image og sende det til Amazon ECR. Workflow kjører når koden er pushet til main.
AWS-nøklene er lagret i GitHub Secrets. commit-SHA brukes som tag som viser hvilken versjon av koden er brukt.

Lenke til workflow: https://github.com/maba103/pg301-eksamen-2026/actions/runs/41905954438
