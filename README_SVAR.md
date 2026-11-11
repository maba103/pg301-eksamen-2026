# PG301 – Eksamensbesvarelse

## Oppgave 1 – Container og CI

### Docker

Jeg har pakket applikasjonen i en Docker-container med en multi-stage Dockerfile.

Først bygges applikasjonen med Maven og Java 21. Etterpå brukes et mindre Java-image til å kjøre applikasjonen. Dette gjør at Docker-imaget blir mindre.

For å bygge image:

```bash
docker build -t wishifai:local .
```

For å starte applikasjonen:

```bash
docker run --rm -p 9999:8080 wishifai:local
```

Applikasjonen bruker port 8080 inne i containeren. Denne er koblet til port 9999 på maskinen min.

### CI, GitHub Actions og ECR

Jeg bruker GitHub Actions til å bygge Docker-imaget og sende det til Amazon ECR når jeg pusher kode til `main`.

AWS-nøklene er lagret i GitHub Secrets. De ligger derfor ikke i kildekoden.

Jeg bruker commit-SHA som image-tag. Da kan jeg se hvilken versjon av koden imaget kommer fra.

Workflow: https://github.com/maba103/pg301-eksamen-2026/actions

## Oppgave 2 – Infrastruktur med Terraform og ECS Express

### Infrastruktur og Terraform

Terraform-filene ligger i mappen `infra/`. Jeg bruker Terraform til å opprette infrastrukturen i AWS.

Terraform oppretter:

- ECS Express, som kjører applikasjonen.
- En S3-bucket, som lagrer bilder.
- En IAM-rolle, som gir applikasjonen tilgang til S3, Rekognition, Bedrock og CloudWatch.
- En offentlig URL, slik at man kan åpne applikasjonen på internett.

Jeg bruker variabler for AWS-region, kandidatnummer og image-tag. Dette gjør det enklere å endre innstillingene.

Jeg oppretter én egen task role med kandidatnummeret i navnet. De andre nødvendige ECS-rollene finnes allerede og blir brukt.

Applikasjonen får automatisk navnet på S3-bucketen gjennom miljøvariabelen `S3_BUCKET`.

Health check bruker `/api/health` for å sjekke om applikasjonen fungerer.

Terraform lagrer også state i en S3-bucket:

- Bucket: `pgr301-terraform-state`
- Key: `wishifai/7/terraform.tfstate`

Dette gjør at Terraform kan lagre og bruke informasjon om infrastrukturen mellom kjøringene.

### Testing

Jeg testet health-endepunktet. Det svarte med HTTP 200 og status `UP`, som betyr at applikasjonen fungerer.

Jeg testet også `/api/analyze` med et bilde. Bildet ble lagret i S3. Rekognition fant innhold i bildet, og Bedrock laget gaveforslag.

Applikasjonen er tilgjengelig her:

https://wi-80949c3cc7a34054bcf6656fa866b821.ecs.eu-west-1.on.aws

### CI/CD med Terraform

GitHub Actions har to jobber.

Den første jobben bygger Docker-imaget og sender det til ECR.

Den andre jobben kjører Terraform etter at den første jobben er ferdig. Terraform sjekker at filene er riktig formatert, kontrollerer konfigurasjonen og oppdaterer infrastrukturen i AWS.

På denne måten blir den nye versjonen av applikasjonen automatisk lagt ut etter en push til `main`.

## Oppgave 3 – Overvåking

### Egne målinger (Custom metrics)

Jeg bruker Micrometer til å sende målinger til CloudWatch. Navnet på området (namespace) er `Wishifai`.

Jeg har laget tre egne målinger:

- `wishifai.rekognition.duration`: måler hvor lang tid Rekognition bruker.
- `wishifai.bedrock.duration`: måler hvor lang tid Bedrock bruker.
- `wishifai.gift.suggestions`: teller hvor mange gaveforslag som blir laget.

Disse målingene viser hvor raskt AI-tjenestene fungerer, og hvor mange gaveforslag applikasjonen lager.

CloudWatch registrerte blant annet fem gaveforslag i en måleperiode.

### Dashboard og alarm

Jeg har laget et CloudWatch-dashboard med Terraform. Det heter `Wishifai-Dashboard`.

Dashboardet viser:

- Business: hvor mange gaveforslag som blir laget.
- Infrastructure: CPU, minnebruk og antall aktive ECS-tasks.
- Application: minnebruk i Java, responstid og hvor lang tid Rekognition og Bedrock bruker.

Jeg har også laget en alarm som heter `Wishifai-Bedrock-High-Latency`.

Alarmen varsler hvis Bedrock bruker mer enn 30 sekunder i to måleperioder. Dette gjør det lettere å oppdage hvis Bedrock bruker for lang tid.

## Oppgave 4 – Generativ AI og DevOps

Generativ AI påvirker hvordan vi lager, tester og følger med på applikasjoner. En forskjell fra vanlige programmer er at AI kan gi forskjellige svar selv om den får samme input. Derfor er det vanskeligere å vite om svaret alltid er riktig.

I Wishifai bruker jeg AWS Bedrock til å lage gaveforslag basert på bilder. Applikasjonen kan fungere som den skal, selv om gaveforslagene ikke er gode. Det er derfor ikke nok å bare teste om applikasjonen gir HTTP 200 eller riktig JSON-format.

For å teste AI kan man bruke flere bilder med forventede resultater. Man kan teste om svaret har riktig format og inneholder nødvendig informasjon. Man bør også sjekke om gaveforslagene passer til bildet. Automatiske tester kan kontrollere formatet, mens mennesker kan vurdere om forslagene faktisk er gode.

Hvis man bytter AI-modell eller endrer prompten, bør man teste den nye versjonen før den tas i bruk. Det er viktig å vite hvilken modell og prompt som brukes. Hvis den nye versjonen fungerer dårligere, bør man kunne gå tilbake til den gamle. Man kan også slippe den nye versjonen til bare noen få brukere først. Da kan man teste om den fungerer før alle får den.

Det er også viktig å følge med på applikasjonen etter at den er lagt ut. I Wishifai måler jeg hvor lang tid Rekognition og Bedrock bruker, og hvor mange gaveforslag som blir laget. Man bør også følge med på feil, kostnader, tokenbruk og kvaliteten på svarene.

CloudWatch-alarmen min varsler hvis Bedrock bruker for lang tid. Men den kan ikke vite om gaveforslagene er dårlige eller ikke passer til bildet. Derfor må man også teste kvaliteten på AI-svarene og la mennesker kontrollere resultatene.

Generativ AI kan gjøre utviklingen raskere, men mennesker må fortsatt passe på at applikasjonen fungerer, er trygg og ikke bruker unødvendig mye penger.