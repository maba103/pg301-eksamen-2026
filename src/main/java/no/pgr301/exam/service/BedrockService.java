
package no.pgr301.exam.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import no.pgr301.exam.model.GiftAnalysis;
import no.pgr301.exam.model.GiftSuggestion;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.ContentBlock;
import software.amazon.awssdk.services.bedrockruntime.model.ConversationRole;
import software.amazon.awssdk.services.bedrockruntime.model.ConverseRequest;
import software.amazon.awssdk.services.bedrockruntime.model.ConverseResponse;
import software.amazon.awssdk.services.bedrockruntime.model.Message;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class BedrockService {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Pattern JSON_OBJECT = Pattern.compile("\\{.*}", Pattern.DOTALL);

    private final BedrockRuntimeClient client;
    private final String modelId;
    private final MeterRegistry meterRegistry;

    public BedrockService(BedrockRuntimeClient client,
                          @Value("${app.bedrock.model-id}") String modelId,
                          MeterRegistry meterRegistry) {
        this.client = client;
        this.modelId = modelId;
        this.meterRegistry = meterRegistry;
    }

    public GiftAnalysis suggestGifts(List<String> labels) {
        Timer.Sample sample = Timer.start(meterRegistry);

        try {
            String prompt = """
                    Du er en gaveassistent. Du får en liste med objekter som Rekognition
                    oppdaget i et bilde av et rom eller en gjenstand. Oppgaven din er å
                    foreslå gaver som passer for personen som eier rommet.

                    Objekter: %s

                    Tenk gjennom disse stegene før du svarer:

                    1. ROM-ANALYSE: Hva slags rom eller miljø er dette sannsynligvis?
                    2. MOTTAKER-PROFIL: Hvem bruker rommet?
                    3. GAVER: Foreslå inntil 5 konkrete gaver som passer.

                    Svar KUN med gyldig JSON på dette formatet:
                    {
                      "roomAnalysis": "kort beskrivelse",
                      "recipientProfile": "antagelser om personen",
                      "suggestions": [
                        {"gift": "konkret gaveforslag", "reasoning": "hvorfor denne gaven passer"}
                      ]
                    }
                    """.formatted(String.join(", ", labels));

            ConverseResponse resp = client.converse(ConverseRequest.builder()
                    .modelId(modelId)
                    .messages(Message.builder()
                            .role(ConversationRole.USER)
                            .content(ContentBlock.fromText(prompt))
                            .build())
                    .build());

            String text = resp.output().message().content().get(0).text();
            return parseAnalysis(text);

        } finally {
            sample.stop(Timer.builder("wishifai.bedrock.duration")
                    .description("Time taken by AWS Bedrock")
                    .register(meterRegistry));
        }
    }

    private GiftAnalysis parseAnalysis(String text) {
        try {
            Matcher m = JSON_OBJECT.matcher(text);
            String json = m.find() ? m.group() : text;
            return MAPPER.readValue(json, GiftAnalysis.class);
        } catch (Exception e) {
            return new GiftAnalysis(
                    "(ukjent)",
                    "(kunne ikke parse svar)",
                    List.of(new GiftSuggestion("(ukjent format)", text))
            );
        }
    }
}
