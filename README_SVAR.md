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


## Oppgave 2 - Infrastruktur med Terraform og ECS Express

### Terraform
Terraform brukes til å lage infrastrukturen i AWS. Terraform filene ligger i mappen `infra/` . Terraform oppretter: 
ECS Express -  kjører applikasjon , 
S3 bucket - lagrer bilder ,
IAM rolle - gir tilgang til S3, Rekognition, CloudWatch og Bedrock ,
En offentlig URL til applikasjon . Terraform lagrer informasjon om infrastruktur i egen S3 bucket. 

### ECS Express
Applikasjon kjører i en Docker container på AWS ECS Express. Applikasjon bruker S3_BUCKET for å vite hvilken S3 bucket bildene skal lagres i. Health check sjekker om applikasjonen fungerer ved å bruke /api/health .
 https://wi-80949c3cc7a34054bcf6656fa866b821.ecs.eu-west-1.on.aws

 ### Testing
 Jeg testet om applikasjonen fungerte. Health-endpoint ga HTTP 200 og status UP.

 /api/analyze med et bilde, da ble bildet lagret i S3, innholdet i bildet gjenkjent med Rekognition, gaveforslag laget med Bedrock.

 ### CI/CD med Terraform
 GitHub Actions jobber med build and push og terraform. Build and push bygger Docker image og sender det til Amazon ECR. Terraform sjekker og oppdaterer infrastrukturen i AWS. Terraform starter når Docker image er ferdig bygget og sendt til ECR. 




