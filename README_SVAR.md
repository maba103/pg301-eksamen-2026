# PG301 - Eksamensbesvarelse

## Oppgave 1 - Container og CI

### Docker
Applikasjonen er lagt i en Docker container med en multistage Dockerfile. Først bygges applikasjonen med Maven og Java 21. Deretter brukes Java til å kjøre applikasjonen, som gjør Docker imaget mindre.

For å bygge image: docker build -t wishifai:local .

For å starte applikasjonen: docker run --rm -p 9999:8080 wishifai:local 

### CI, GitHub Actions og ECR
Github Actions brukes til å bygge Docker image og sende det til Amazon ECR når koden pusher til main. AWS keys er lagret i GitHub Secrets, ikke i kildekoden. 

Commit-SHA brukes som image-tag, slik at man kan se hvilken kodeversjon imaget kommer fra.

Lenke til workflow: https://github.com/maba103/pg301-eksamen-2026/actions/runs/41905954438


## Oppgave 2 - Infrastruktur med Terraform og ECS Express

### Infrastruktur og Terraform

Terraform filene ligger i mappen `infra/` . Terraform brukes til å opprette infrastrukturen i AWS :

ECS Express -  kjører applikasjon , 
S3 bucket - lagrer bilder som lastes opp ,
IAM rolle - gir tilgang til S3, Rekognition, CloudWatch og Bedrock ,
En offentlig URL - applikasjon blir tilgjengelig på internett . Terraform lagrer informasjon om infrastruktur i egen S3 bucket. 

Applikasjon kjører i en Docker container på AWS ECS Express. Den bruker S3_BUCKET for å vite hvilken S3 bucket bildene skal lagres i. Health check bruker /api/health for å sjekke om applikasjonen fungerer .

Lenke til applikasjon:
 https://wi-80949c3cc7a34054bcf6656fa866b821.ecs.eu-west-1.on.aws

 ### Testing
 Health endpoint ble testet som ga HTTP 200 og status UP.

 Testet /api/analyze med et bilde. Bildet ble lagret i S3, innholdet i bildet gjenkjent med Rekognition, gaveforslag laget med Bedrock.

 ### CI/CD med Terraform
 GitHub Actions jobber med "build and push" og "terraform". "Build and push" bygger Docker image og sender det til Amazon ECR. "Terraform" sjekker og oppdaterer infrastrukturen i AWS. Terraform starter når Docker image er ferdig bygget og sendt til ECR. 

## Oppgave 3

### Custom metrics
Micrometer brukes til å sende målinger til CloudWatch. Namespace er Wishfai.

Vi har 3 målinger:
wishfai.rekognition.duration: måler hvor lang Rekognition bruker.
wishifai.bedrock.duration: måler hvir lang tid Bedrock bruker.
wishfai.gift.suggestions: teller antall gaveforslag

Disse målingene viser hvor raskt AI-tjenestene fungerer og hvor mange gaveforslag som blir laget. Målingene blir sendt til Cloudwatch der kunne vi se blant fem gaveforslag i måleperiode.

## Dashboard og alarm
Jeg har laget et CloudWatch dashboard, som heter Wishifai dashboard, med Terraform. Det viser antall gaveforslag, CPU og minnebruk, antall aktive tasks, reponstid og JVM-minne og hvor lang tid Rekognition og Bedrock bruker.

Jeg har også laget alarmen Wishifai-Bedrock High Latency, som varsler hvis bedrock bruker med enn 30 sekunder i to måleperioder. Dette gjør det lettere å oppdage problemer med applikasjonen.

## Oppgave 4
### Generativ AI og DevOps

Generativ AI påvirker hvordan applikasjoner blir utviklet, testet og overvåket. AI kan hjelpe med å gi forskjellige svar selv om den får samme input. Derfor er det vanskeligere å teste AI enn vanlige programmer. 

I Wishfai brukte jeg Bedrock til å lage gaveforslag basert på bilder. Applikasjon kan fungere som den skal, selv om gaveforslagene ikke alltid er gode. 

For å teste AI kan man bruke bilder med eksempler på gode svar. Man kan også teste at svarene har riktig format og inneholder nødvendig informasjon. Mennesker bør også kontrollere om forslagene er gode. 

Når man endrer AI-modell, bør man teste endringen før den tas i bruk. Hvis den nye versjonen fungerer dårligere, bør man kunne gå tilbake til den gamle. 

Det er også viktig å følge med på responstid, feil, kostnader og kvalitet. I Wishfai måler jeg blant annet hvor lang tid Rekognition og Bedrock bruker, og hvor mage gaveforslag som blir lagret.

CloudWatch varsler hvis Bedrock bruker for lang tid. Men det kan ikke vite om gaveforslagene er dårlige. Derfor må man også teste kvaliteten på AI svarene. AI kan gjøre utviklingen raskere, men mennesker må fortsatt passe på at applikasjonen er sikker og fungerer som den skal.


