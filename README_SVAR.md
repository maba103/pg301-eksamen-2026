# PG301 – Eksamensbesvarelse

## Oppgave 1 – Container og CI

### Docker

Jeg har laget en Docker-container for applikasjonen ved hjelp av en multi-stage Dockerfile. Jeg bruker Maven og Java 21 til å bygge applikasjonen. For å starte applikasjon bruker jeg et mindre Java image som gjør Docker-imaget mindre.

Hvor bygger imaget?
```bash
docker build -t wishifai:local .
```

Hvordan å starte applikasjon?
```bash
docker run --rm -p 9999:8080 wishifai:local
```

Applikasjonen kjører på port 8080 inne i containeren. Jeg kobler denne til port 9999 på maskinen min.

### GitHub Actions og ECR

Jeg bruker GitHub Actions til å bygge Docker image og laste det opp til Amazon ECR når jeg pusher kode til `main`. AWS keys ligger i GitHub Secrets, ikke direkte i koden. Commit-SHA brukes som image tag. Da kan jeg se hvilken kodeversjon som hører til imaget.

Lenke til workflow:
[GitHub Actions](https://github.com/maba103/pg301-eksamen-2026/actions)

## Oppgave 2 – Infrastruktur med Terraform og ECS Express

### Infrastruktur og Terraform

Terraform filene ligger i mappen `infra/`. Terraform brukes til å lage infrastrukturen jeg trenger i AWS.

Hva oppretter Terraform+
ECS Express, som kjører applikasjonen. ,
En S3-bucket, som lagrer bildene. ,
En IAM-rolle, som gir applikasjonen tilgang til S3, Rekognition, Bedrock og CloudWatch. ,
En offentlig URL, slik at man kan åpne applikasjonen på internett.

Jeg bruker variabler for AWS-region, kandidatnummer og image tag. Da er det enklere å endre disse verdiene. Jeg lager også en egen task role med kandidatnummeret i navnet. De andre ECS rollene som trengs, finnes allerede og blir brukt. Applikasjonen får navnet på S3-bucketen gjennom miljøvariabelen `S3_BUCKET`.
Health check bruker `/api/health` for å sjekke om applikasjonen fungerer.

Terraform lagrer også state i en S3-bucket:
- Bucket: `pgr301-terraform-state`
- Key: `wishifai/7/terraform.tfstate`

Dette gjør at Terraform kan lagre informasjon om infrastrukturen og bruke den neste gang jeg kjører Terraform.

### Testing

Jeg testet health endepunktet. Det svarte med HTTP 200 og status `UP` som betyr at applikasjonen kjører. Jeg testet også `/api/analyze` med et bilde. Bildet ble lagret i S3. Rekognition fant innhold i bildet, og Bedrock laget gaveforslag.

Applikasjonen ligger her:

[https://wi-80949c3cc7a34054bcf6656fa866b821.ecs.eu-west-1.on.aws](https://wi-80949c3cc7a34054bcf6656fa866b821.ecs.eu-west-1.on.aws)

### CI/CD med Terraform

GitHub Actions jobber med å bygge Docker image som sendes til ECR og deretter å kjøre Terraform når er ferdig laget. Terraform sjekker at filene har riktig format, at oppsettet er gyldig, og oppdaterer infrastrukturen i AWS. Dette lager en ny versjon av applikasjonen automatisk til `main` når koden blir "push".

## Oppgave 3 – Overvåking

### Egne målinger (Custom metrics)

Micrometer brukes til å sende målinger til CloudWatch -> `Wishifai`.

Vi har tre målinger -> wishifai.rekognition.duration, wishifai.bedrock.duration og wishifai.gift.suggestions. Wishifai.rekognition.duration måler hvor lang tid Rekognition bruker, wishifai.bedrock.duration måler hvor lang tid Bedrock bruker og wishifai.gift.suggestions gir forslag til gavene. Disse målingene gjør det lettere å følge med på hvor lang tid AI-tjenestene bruker, og hvor mange gaveforslag applikasjonen lager. Målingene ble testet og fikk blant annet registrert fem gaveforslag.

### Dashboard og alarm

Jeg har laget et CloudWatch-dashboard med Terraform som heter `Wishifai-Dashboard`.

Dashboardet viser -> Business hvor mange gaveforslag som blir laget, infrastructure som viser CPU, minnebruk og antall aktive ECS-tasks og application viser Java-minnebruk, responstid og hvor lang tid Rekognition og Bedrock bruker.

En alarm som ble laget -> `Wishifai-Bedrock-High-Latency`. Alarmen varsler hvis Bedrock bruker mer enn 30 sekunder i to måleperioder som gjør det lettere å oppdage hvis Bedrock er treg.

## Oppgave 4 – Generativ AI og DevOps

AI kan hjelpe oss med å lage applikasjoner. Når vi gir AI en oppgave, kan den gi forskjellige svar selv om vi spør om det samme. Derfor kan det være vanskelig å vite om svaret alltid er riktig.

I Wishifai brukes AWS Bedrock til å lage gaveforslag basert på bilder. Applikasjonen kan fungere teknisk, selv om gaveforslagene ikke passer til bildet. Derfor er det ikke nok å bare teste om applikasjonen gir HTTP 200 eller riktig JSON.

For å teste AI kan man bruke flere bilder og se om man får gode svar. Man bør sjekke at svaret har riktig format, inneholder nødvendig informasjon og at gaveforslagene passer til bildet. Automatiske tester kan sjekke formatet, mens mennesker kan vurdere om forslagene faktisk er gode.

Hvis man bytter AI-modell eller endrer prompten, bør man teste dette før man legger ut den nye versjonen. Det er viktig å vite hvilken modell og prompt som brukes. Hvis den nye versjonen fungerer dårligere, bør man kunne gå tilbake til den gamle. Man kan også teste den nye versjonen med noen få brukere først før alle får den.

Det er også viktig å følge med på applikasjonen etter at den er lagt ut. I Wishifai måler jeg hvor lang tid Rekognition og Bedrock bruker, og hvor mange gaveforslag som blir laget. Man bør også følge med på feil, kostnader, tokenbruk og kvaliteten på svarene.

CloudWatch-alarmen min varsler hvis Bedrock bruker for lang tid. Men alarmen kan ikke vite om gaveforslagene er dårlige eller ikke passer til bildet. Derfor må man også kontrollere kvaliteten på AI-svarene. Dette kan gjøres med tester og ved at mennesker ser på resultatene.

Hvor mye man tester og følger med på, bør også komme an på hvor viktig det er at AI gir riktige svar. Hvis AI brukes til noe som kan få store konsekvenser for mennesker, må man være ekstra nøye.

Generativ AI kan gjøre utviklingen raskere, men mennesker må fortsatt kontrollere at applikasjonen fungerer, er trygg og ikke bruker unødvendig mye penger.